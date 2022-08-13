/*
 * KubaTech - Gregtech Addon
 * Copyright (C) 2022  kuba6000
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package kubatech.common.tileentity.gregtech.multiblock;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.util.GT_StructureUtility.ofHatchAdder;
import static kubatech.api.Variables.Author;

import WayofTime.alchemicalWizardry.api.alchemy.energy.ReagentRegistry;
import WayofTime.alchemicalWizardry.api.event.RitualRunEvent;
import WayofTime.alchemicalWizardry.api.rituals.Rituals;
import WayofTime.alchemicalWizardry.api.soulNetwork.SoulNetworkHandler;
import WayofTime.alchemicalWizardry.api.tile.IBloodAltar;
import WayofTime.alchemicalWizardry.common.rituals.RitualEffectWellOfSuffering;
import WayofTime.alchemicalWizardry.common.tileEntity.TEMasterStone;
import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import crazypants.enderio.EnderIO;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_EnhancedMultiBlockBase;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Utility;
import java.util.HashMap;
import java.util.Random;
import kubatech.Tags;
import kubatech.api.LoaderReference;
import kubatech.api.utils.FastRandom;
import kubatech.api.utils.ReflectionHelper;
import kubatech.loaders.MobRecipeLoader;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;

public class GT_MetaTileEntity_ExtremeExterminationChamber
        extends GT_MetaTileEntity_EnhancedMultiBlockBase<GT_MetaTileEntity_ExtremeExterminationChamber> {

    public static final HashMap<String, MobRecipeLoader.MobRecipe> MobNameToRecipeMap = new HashMap<>();
    public final Random rand = new FastRandom();

    public GT_MetaTileEntity_ExtremeExterminationChamber(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_ExtremeExterminationChamber(String aName) {
        super(aName);
        if (LoaderReference.BloodMagic) MinecraftForge.EVENT_BUS.register(this);
    }

    @Override
    public void onRemoval() {
        if (LoaderReference.BloodMagic) MinecraftForge.EVENT_BUS.unregister(this);
    }

    private static final String WellOfSufferingRitualName = "AW013Suffering";

    private static final Item poweredSpawnerItem = Item.getItemFromBlock(EnderIO.blockPoweredSpawner);
    private static final int CASING_INDEX = 16;
    private static final String STRUCTURE_PIECE_MAIN = "main";
    private static final IStructureDefinition<GT_MetaTileEntity_ExtremeExterminationChamber> STRUCTURE_DEFINITION =
            StructureDefinition.<GT_MetaTileEntity_ExtremeExterminationChamber>builder()
                    .addShape(STRUCTURE_PIECE_MAIN, transpose(new String[][] {
                        {"ccccc", "ccccc", "ccccc", "ccccc", "ccccc"},
                        {"ccccc", "c---c", "c---c", "c---c", "ccccc"},
                        {"ccccc", "c---c", "c---c", "c---c", "ccccc"},
                        {"ccccc", "c---c", "c---c", "c---c", "ccccc"},
                        {"ccccc", "c---c", "c---c", "c---c", "ccccc"},
                        {"ccccc", "csssc", "csssc", "csssc", "ccccc"},
                        {"CC~CC", "CCCCC", "CCCCC", "CCCCC", "CCCCC"},
                    }))
                    .addElement('c', ofBlock(GregTech_API.sBlockCasings2, 0))
                    .addElement(
                            'C',
                            ofChain(
                                    ofBlock(GregTech_API.sBlockCasings2, 0),
                                    ofHatchAdder(
                                            GT_MetaTileEntity_ExtremeExterminationChamber::addOutputToMachineList,
                                            CASING_INDEX,
                                            1),
                                    ofHatchAdder(
                                            GT_MetaTileEntity_ExtremeExterminationChamber::addEnergyInputToMachineList,
                                            CASING_INDEX,
                                            1),
                                    ofHatchAdder(
                                            GT_MetaTileEntity_ExtremeExterminationChamber::addMaintenanceToMachineList,
                                            CASING_INDEX,
                                            1)))
                    .addElement(
                            's',
                            Loader.isModLoaded("ExtraUtilities")
                                    ? ofBlock(Block.getBlockFromName("ExtraUtilities:spike_base_diamond"), 0)
                                    : isAir())
                    .build();

    private TileEntity masterStoneRitual = null;
    private TileEntity tileAltar = null;
    private boolean isInRitualMode = false;

    @Override
    public void saveNBTData(NBTTagCompound aNBT) {
        super.saveNBTData(aNBT);
        aNBT.setBoolean("isInRitualMode", isInRitualMode);
    }

    @Override
    public void loadNBTData(NBTTagCompound aNBT) {
        super.loadNBTData(aNBT);
        isInRitualMode = aNBT.getBoolean("isInRitualMode");
    }

    @Override
    public IStructureDefinition<GT_MetaTileEntity_ExtremeExterminationChamber> getStructureDefinition() {
        return STRUCTURE_DEFINITION;
    }

    @Override
    protected GT_Multiblock_Tooltip_Builder createTooltip() {
        GT_Multiblock_Tooltip_Builder tt = new GT_Multiblock_Tooltip_Builder();
        tt.addMachineType("Powered Spawner")
                .addInfo("Controller block for Extreme Extermination Chamber")
                .addInfo("Spawns and Exterminates monsters for you")
                .addInfo("Base energy usage: 2,000 EU/t")
                .addInfo("Recipe time is based on mob health")
                .addInfo("If the mob spawns infernal")
                .addInfo("it will drain 8 times more power")
                .addInfo("You can enable ritual mode with a screwdriver")
                .addInfo("When in ritual mode and Well Of Suffering ritual is built directly on the machine in center")
                .addInfo("The mobs will start to buffer and die very slowly by a ritual")
                .addInfo(Author)
                .addSeparator()
                .beginStructureBlock(5, 7, 5, true)
                .addController("Front Bottom Center")
                .addCasingInfo("Solid Steel Machine Casing", 10)
                .addOutputBus("Any casing", 1)
                .addEnergyHatch("Any casing", 1)
                .addMaintenanceHatch("Any casing", 1)
                .toolTipFinisher(Tags.MODNAME);
        return tt;
    }

    @Override
    public void construct(ItemStack itemStack, boolean b) {
        buildPiece(STRUCTURE_PIECE_MAIN, itemStack, b, 2, 6, 0);
    }

    @Override
    public IMetaTileEntity newMetaEntity(IGregTechTileEntity aTileEntity) {
        return new GT_MetaTileEntity_ExtremeExterminationChamber(this.mName);
    }

    @Override
    public ITexture[] getTexture(
            IGregTechTileEntity aBaseMetaTileEntity,
            byte aSide,
            byte aFacing,
            byte aColorIndex,
            boolean aActive,
            boolean aRedstone) {
        if (aSide == aFacing) {
            if (aActive)
                return new ITexture[] {
                    Textures.BlockIcons.getCasingTextureForId(CASING_INDEX),
                    TextureFactory.builder()
                            .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE)
                            .extFacing()
                            .build(),
                    TextureFactory.builder()
                            .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_ACTIVE_GLOW)
                            .extFacing()
                            .glow()
                            .build()
                };
            return new ITexture[] {
                Textures.BlockIcons.getCasingTextureForId(CASING_INDEX),
                TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER)
                        .extFacing()
                        .build(),
                TextureFactory.builder()
                        .addIcon(OVERLAY_FRONT_DISTILLATION_TOWER_GLOW)
                        .extFacing()
                        .glow()
                        .build()
            };
        }
        return new ITexture[] {Textures.BlockIcons.getCasingTextureForId(CASING_INDEX)};
    }

    @Override
    public boolean isCorrectMachinePart(ItemStack aStack) {
        return true;
    }

    @Override
    public void onFirstTick(IGregTechTileEntity aBaseMetaTileEntity) {
        super.onFirstTick(aBaseMetaTileEntity);
    }

    @Override
    public void onScrewdriverRightClick(byte aSide, EntityPlayer aPlayer, float aX, float aY, float aZ) {
        if (!LoaderReference.BloodMagic) return;
        if (this.mMaxProgresstime > 0) {
            GT_Utility.sendChatToPlayer(aPlayer, "Can't change mode when running !");
            return;
        }
        isInRitualMode = !isInRitualMode;
        if (!isInRitualMode) {
            GT_Utility.sendChatToPlayer(aPlayer, "Ritual mode disabled");
        } else {
            GT_Utility.sendChatToPlayer(aPlayer, "Ritual mode enabled");
            if (connectToRitual()) GT_Utility.sendChatToPlayer(aPlayer, "Successfully connected to the ritual");
            else GT_Utility.sendChatToPlayer(aPlayer, "Can't connect to the ritual");
        }
    }

    @SuppressWarnings("unused")
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onRitualPerform(RitualRunEvent event) {
        if (!isInRitualMode) return;
        if (masterStoneRitual == null) return;
        if (this.mMaxProgresstime == 0) return;
        if (event.mrs.equals(masterStoneRitual) && event.ritualKey.equals(WellOfSufferingRitualName)) {
            Rituals ritual = Rituals.ritualMap.get(WellOfSufferingRitualName);
            if (ritual != null && ritual.effect instanceof RitualEffectWellOfSuffering) {
                RitualEffectWellOfSuffering effect = (RitualEffectWellOfSuffering) ritual.effect;
                event.setCanceled(true); // we will handle that
                String owner = event.mrs.getOwner();
                int currentEssence = SoulNetworkHandler.getCurrentEssence(owner);
                World world = event.mrs.getWorld();
                int x = event.mrs.getXCoord();
                int y = event.mrs.getYCoord();
                int z = event.mrs.getZCoord();

                if (world.getWorldTime() % RitualEffectWellOfSuffering.timeDelay != 0) return;

                if (tileAltar == null || ((TileEntity) tileAltar).isInvalid()) {
                    tileAltar = null;
                    for (int i = -5; i <= 5; i++)
                        for (int j = -5; j <= 5; j++)
                            for (int k = -10; k <= 10; k++)
                                if (world.getTileEntity(x + i, y + k, z + j) instanceof IBloodAltar)
                                    tileAltar = world.getTileEntity(x + i, y + k, z + j);
                }
                if (tileAltar == null) return;

                if (currentEssence < effect.getCostPerRefresh() * 100) SoulNetworkHandler.causeNauseaToPlayer(owner);

                ((IBloodAltar) tileAltar)
                        .sacrificialDaggerCall(
                                100
                                        * RitualEffectWellOfSuffering.amount
                                        * (effect.canDrainReagent(
                                                        event.mrs,
                                                        ReagentRegistry.offensaReagent,
                                                        ReflectionHelper.getField(effect, "offensaDrain", true, 3),
                                                        true)
                                                ? 2
                                                : 1)
                                        * (effect.canDrainReagent(
                                                        event.mrs,
                                                        ReagentRegistry.tenebraeReagent,
                                                        ReflectionHelper.getField(effect, "tennebraeDrain", true, 5),
                                                        true)
                                                ? 2
                                                : 1),
                                true);

                SoulNetworkHandler.syphonFromNetwork(owner, effect.getCostPerRefresh() * 100);
            }
        }
    }

    @Override
    public boolean checkRecipe(ItemStack aStack) {
        if (this.getBaseMetaTileEntity().getWorld().difficultySetting == EnumDifficulty.PEACEFUL) return false;

        if (aStack == null) return false;

        if (aStack.getItem() != poweredSpawnerItem) return false;

        if (aStack.getTagCompound() == null) return false;
        String mobType = aStack.getTagCompound().getString("mobType");
        if (mobType.isEmpty()) return false;

        MobRecipeLoader.MobRecipe recipe = MobNameToRecipeMap.get(mobType);

        if (recipe == null) return false;

        this.mOutputItems = recipe.generateOutputs(rand, this);

        if (isInRitualMode) {
            if (isRitualValid()) {
                this.mMaxProgresstime = 400;
                this.mEUt /= 4;
            }
        } else calculateOverclockedNessMulti(this.mEUt, this.mMaxProgresstime, 2, getMaxInputVoltage());
        if (this.mEUt > 0) this.mEUt = -this.mEUt;
        this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
        this.mEfficiencyIncrease = 10000;

        return true;
    }

    private boolean isRitualValid() {
        if (!isInRitualMode) return false;
        if (masterStoneRitual == null) return false;
        if (masterStoneRitual.isInvalid()
                || !(((TEMasterStone) masterStoneRitual).getCurrentRitual().equals(WellOfSufferingRitualName))) {
            masterStoneRitual = null;
            return false;
        }
        return true;
    }

    private boolean connectToRitual() {
        if (!Loader.isModLoaded("AWWayofTime")) return false;
        ChunkCoordinates coords = this.getBaseMetaTileEntity().getCoords();
        int[] abc = new int[] {0, -8, 2};
        int[] xyz = new int[] {0, 0, 0};
        this.getExtendedFacing().getWorldOffset(abc, xyz);
        xyz[0] += coords.posX;
        xyz[1] += coords.posY;
        xyz[2] += coords.posZ;
        TileEntity te = this.getBaseMetaTileEntity().getTileEntity(xyz[0], xyz[1], xyz[2]);
        if (te instanceof TEMasterStone) {
            if (((TEMasterStone) te).getCurrentRitual().equals(WellOfSufferingRitualName)) {
                masterStoneRitual = te;
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        if (!checkPiece(STRUCTURE_PIECE_MAIN, 2, 6, 0)) return false;
        if (mMaintenanceHatches.size() != 1 || mEnergyHatches.size() == 0) return false;
        if (isInRitualMode) connectToRitual();
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
}
