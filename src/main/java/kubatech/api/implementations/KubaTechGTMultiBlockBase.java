package kubatech.api.implementations;

import static kubatech.api.Variables.ln2;
import static kubatech.api.Variables.ln4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.fluids.FluidStack;

import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.screen.ITileWithModularUI;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.api.widget.Widget;
import com.gtnewhorizons.modularui.common.builder.UIBuilder;
import com.gtnewhorizons.modularui.common.builder.UIInfo;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;

import gregtech.api.enums.GT_Values;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.metatileentity.BaseMetaTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_ExtendedPowerMultiBlockBase;

public abstract class KubaTechGTMultiBlockBase<T extends GT_MetaTileEntity_ExtendedPowerMultiBlockBase<T>>
        extends GT_MetaTileEntity_ExtendedPowerMultiBlockBase<T> {

    @Deprecated
    public final int mEUt = 0;

    @SuppressWarnings("unchecked")
    protected static <K extends KubaTechGTMultiBlockBase<?>> UIInfo<?, ?> createKTMetaTileEntityUI(
            KTContainerConstructor<K> containerConstructor) {
        return UIBuilder.of().container((player, world, x, y, z) -> {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof BaseMetaTileEntity) {
                IMetaTileEntity mte = ((BaseMetaTileEntity) te).getMetaTileEntity();
                if (!(mte instanceof KubaTechGTMultiBlockBase)) return null;
                final UIBuildContext buildContext = new UIBuildContext(player);
                final ModularWindow window = ((ITileWithModularUI) te).createWindow(buildContext);
                return containerConstructor.of(new ModularUIContext(buildContext, te::markDirty), window, (K) mte);
            }
            return null;
        }).gui(((player, world, x, y, z) -> {
            if (!world.isRemote) return null;
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof BaseMetaTileEntity) {
                IMetaTileEntity mte = ((BaseMetaTileEntity) te).getMetaTileEntity();
                if (!(mte instanceof KubaTechGTMultiBlockBase)) return null;
                final UIBuildContext buildContext = new UIBuildContext(player);
                final ModularWindow window = ((ITileWithModularUI) te).createWindow(buildContext);
                return new ModularGui(
                        containerConstructor.of(new ModularUIContext(buildContext, null), window, (K) mte));
            }
            return null;
        })).build();
    }

    @FunctionalInterface
    protected interface KTContainerConstructor<T extends KubaTechGTMultiBlockBase<?>> {

        ModularUIContainer of(ModularUIContext context, ModularWindow mainWindow, T multiBlock);
    }

    protected KubaTechGTMultiBlockBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected KubaTechGTMultiBlockBase(String aName) {
        super(aName);
    }

    /**
     * Enables infinite overclocking (will give more outputs with more energy past 1 tick) Currently doesn't support
     * recipe inputs
     *
     * @return If this supports infinite overclock
     */
    protected boolean isOverclockingInfinite() {
        return false;
    }

    /**
     * @return The minimum amount of ticks this multiblock can overclock to
     */
    protected int getOverclockTimeLimit() {
        return 1;
    }

    @Override
    protected void calculateOverclockedNessMultiInternal(long aEUt, int aDuration, int mAmperage, long maxInputVoltage,
            boolean perfectOC) {
        calculateOverclock(aEUt, aDuration, getMaxInputEu(), perfectOC);
    }

    /**
     * @param aEUt       Recipe EU/t
     * @param aDuration  Recipe duration (in ticks)
     * @param maxInputEU The amount of energy we want to overclock to
     * @param isPerfect  Is this overclock perfect ?
     * @return The amount of overclocks
     */
    protected int calculateOverclock(long aEUt, int aDuration, final long maxInputEU, final boolean isPerfect) {
        final int minDuration = getOverclockTimeLimit();
        int tiers = (int) (Math.log((double) maxInputEU / (double) aEUt) / ln4);
        if (tiers <= 0) {
            this.lEUt = aEUt;
            this.mMaxProgresstime = aDuration;
            return 0;
        }
        int durationTiers = (int) Math
                .ceil(Math.log((double) aDuration / (double) minDuration) / (isPerfect ? ln4 : ln2));
        if (durationTiers < 0) durationTiers = 0; // We do not support downclocks (yet)
        if (durationTiers > tiers) durationTiers = tiers;
        if (!isOverclockingInfinite()) {
            tiers = durationTiers;
            if (tiers == 0) {
                this.lEUt = aEUt;
                this.mMaxProgresstime = aDuration;
                return 0;
            }
            this.lEUt = aEUt << (tiers << 1);
            aDuration >>= isPerfect ? (tiers << 1) : tiers;
            if (aDuration < minDuration) aDuration = minDuration;
            this.mMaxProgresstime = aDuration;
            return tiers;
        }
        this.lEUt = aEUt << (tiers << 1);
        aDuration >>= isPerfect ? (durationTiers << 1) : durationTiers;
        int dMulti = tiers - durationTiers;
        if (dMulti > 0) {
            dMulti = 1 << (isPerfect ? (dMulti << 1) : dMulti);
            // TODO: Use more inputs???
            final ArrayList<ItemStack> stacks = new ArrayList<>(Arrays.asList(this.mOutputItems));
            for (ItemStack mOutputItem : this.mOutputItems) {
                mOutputItem.stackSize *= dMulti;
                int maxSize = mOutputItem.getMaxStackSize();
                while (mOutputItem.stackSize > maxSize)
                    stacks.add(mOutputItem.splitStack(Math.min(mOutputItem.stackSize - maxSize, maxSize)));
            }
            if (stacks.size() != this.mOutputItems.length) this.mOutputItems = stacks.toArray(new ItemStack[0]);
            for (FluidStack mOutputFluid : this.mOutputFluids) mOutputFluid.amount *= dMulti;
        }
        if (aDuration < minDuration) aDuration = minDuration;
        this.mMaxProgresstime = aDuration;
        return tiers;
    }

    protected int calculateOverclock(long aEUt, int aDuration, boolean isPerfect) {
        return calculateOverclock(aEUt, aDuration, getMaxInputEu(), isPerfect);
    }

    protected int calculateOverclock(long aEUt, int aDuration) {
        return calculateOverclock(aEUt, aDuration, false);
    }

    protected int calculatePerfectOverclock(long aEUt, int aDuration) {
        return calculateOverclock(aEUt, aDuration, true);
    }

    public int getVoltageTier() {
        return (int) getVoltageTierExact();
    }

    public double getVoltageTierExact() {
        return Math.log((double) getMaxInputEu() / 8d) / ln4 + 1e-8d;
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public int getMaxEfficiency(ItemStack aStack) {
        return 10000;
    }

    @Override
    public int getDamageToComponent(ItemStack aStack) {
        return 0;
    }

    @Override
    public boolean explodesOnComponentBreak(ItemStack aStack) {
        return false;
    }

    // UI stuff

    protected static String voltageTooltipFormatted(int tier) {
        return GT_Values.TIER_COLORS[tier] + GT_Values.VN[tier] + EnumChatFormatting.GRAY;
    }

    protected final Function<Widget, Boolean> isFixed = widget -> getIdealStatus() == getRepairStatus() && mMachine;
    protected static final Function<Integer, IDrawable[]> toggleButtonBackgroundGetter = val -> {
        if (val == 0) return new IDrawable[] { GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_BUTTON_CROSS };
        else return new IDrawable[] { GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_BUTTON_CHECKMARK };
    };
}
