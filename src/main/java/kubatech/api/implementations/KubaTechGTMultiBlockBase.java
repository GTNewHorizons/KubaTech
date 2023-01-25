package kubatech.api.implementations;

import static kubatech.api.Variables.ln2;
import static kubatech.api.Variables.ln4;

import com.gtnewhorizons.modularui.api.drawable.IDrawable;
import com.gtnewhorizons.modularui.api.widget.Widget;
import gregtech.api.gui.modularui.GT_UITextures;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_ExtendedPowerMultiBlockBase;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_Hatch;
import gregtech.api.util.GT_ExoticEnergyInputHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Function;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public abstract class KubaTechGTMultiBlockBase<T extends GT_MetaTileEntity_ExtendedPowerMultiBlockBase<T>>
        extends GT_MetaTileEntity_ExtendedPowerMultiBlockBase<T> {

    protected KubaTechGTMultiBlockBase(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    protected KubaTechGTMultiBlockBase(String aName) {
        super(aName);
    }

    /**
     * Enables infinite overclocking (will give more outputs with more energy past 1 tick)
     * Currently doesn't support recipe inputs
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
    protected void calculateOverclockedNessMultiInternal(
            long aEUt, int aDuration, int mAmperage, long maxInputVoltage, boolean perfectOC) {
        calculateOverclock(aEUt, aDuration, maxInputVoltage, perfectOC);
    }

    /**
     * @param aEUt Recipe EU/t
     * @param aDuration Recipe duration (in ticks)
     * @param maxInputVoltage The amount of energy we want to overclock to
     * @param isPerfect Is this overclock perfect ?
     * @return The amount of overclocks
     */
    protected int calculateOverclock(long aEUt, int aDuration, final long maxInputVoltage, boolean isPerfect) {
        int tiers = (int) (Math.log((double) maxInputVoltage / (double) aEUt) / ln4);
        if (tiers <= 0) {
            this.lEUt = aEUt;
            this.mMaxProgresstime = aDuration;
            return 0;
        }
        int durationTiers =
                (int) (Math.log((double) aDuration / (double) getOverclockTimeLimit()) / (isPerfect ? ln4 : ln2));
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
            if (aDuration <= 0) aDuration = 1;
            this.mMaxProgresstime = aDuration;
            return tiers;
        }
        this.lEUt = aEUt << (tiers << 1);
        aDuration >>= isPerfect ? (durationTiers << 1) : durationTiers;
        int dMulti = tiers - durationTiers;
        if (dMulti > 0) {
            dMulti++;
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
        if (aDuration <= 0) aDuration = 1;
        this.mMaxProgresstime = aDuration;
        return tiers;
    }

    protected int calculateOverclock(long aEUt, int aDuration, boolean isPerfect) {
        return calculateOverclock(aEUt, aDuration, getMaxInputEnergy(), isPerfect);
    }

    protected int calculateOverclock(long aEUt, int aDuration) {
        return calculateOverclock(aEUt, aDuration, false);
    }

    protected int calculatePerfectOverclock(long aEUt, int aDuration) {
        return calculateOverclock(aEUt, aDuration, true);
    }

    @Override
    public long getMaxInputVoltage() {
        return GT_ExoticEnergyInputHelper.getMaxInputVoltageMulti(getExoticAndNormalEnergyHatchList());
    }

    public long getMaxInputAmps() {
        return GT_ExoticEnergyInputHelper.getMaxInputAmpsMulti(getExoticAndNormalEnergyHatchList());
    }

    public long getMaxInputEnergy() {
        long energy = 0L;
        for (GT_MetaTileEntity_Hatch hatch : getExoticAndNormalEnergyHatchList())
            if (isValidMetaTileEntity(hatch))
                energy += hatch.getBaseMetaTileEntity().getInputVoltage()
                        * hatch.getBaseMetaTileEntity().getInputAmperage();
        return energy;
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

    // ModularUI stuff

    protected final Function<Widget, Boolean> isFixed = widget -> getIdealStatus() == getRepairStatus() && mMachine;
    protected static final Function<Integer, IDrawable[]> toggleButtonBackgroundGetter = val -> {
        if (val == 0) return new IDrawable[] {GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_BUTTON_CROSS};
        else return new IDrawable[] {GT_UITextures.BUTTON_STANDARD, GT_UITextures.OVERLAY_BUTTON_CHECKMARK};
    };
}
