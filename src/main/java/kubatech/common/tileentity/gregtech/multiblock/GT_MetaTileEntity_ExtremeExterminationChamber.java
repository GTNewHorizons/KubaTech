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

package KubaTech.common.tileentity.gregtech.multiblock;

import static com.gtnewhorizon.structurelib.structure.StructureUtility.*;
import static gregtech.api.enums.GT_Values.E;
import static gregtech.api.enums.GT_Values.RES_PATH_GUI;
import static gregtech.api.enums.Textures.BlockIcons.*;
import static gregtech.api.util.GT_StructureUtility.ofHatchAdder;

import com.gtnewhorizon.structurelib.structure.IStructureDefinition;
import com.gtnewhorizon.structurelib.structure.StructureDefinition;
import cpw.mods.fml.common.Loader;
import crazypants.enderio.EnderIO;
import gregtech.api.GregTech_API;
import gregtech.api.enums.Textures;
import gregtech.api.interfaces.ITexture;
import gregtech.api.interfaces.metatileentity.IMetaTileEntity;
import gregtech.api.interfaces.tileentity.IGregTechTileEntity;
import gregtech.api.metatileentity.implementations.GT_MetaTileEntity_EnhancedMultiBlockBase;
import gregtech.api.render.TextureFactory;
import gregtech.api.util.GT_Multiblock_Tooltip_Builder;
import gregtech.api.util.GT_Recipe;
import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;

public class GT_MetaTileEntity_ExtremeExterminationChamber
        extends GT_MetaTileEntity_EnhancedMultiBlockBase<GT_MetaTileEntity_ExtremeExterminationChamber> {

    public static final GT_Recipe.GT_Recipe_Map EECRecipeMap = new GT_Recipe.GT_Recipe_Map(
            new HashSet<>(4),
            "KubaTech.recipe.eec",
            "Extreme Extermination Chamber",
            null,
            RES_PATH_GUI + "basicmachines/Default",
            1,
            6,
            1,
            0,
            1,
            E,
            0,
            E,
            false,
            false);
    public static final HashMap<String, GT_Recipe> MobNameToRecipeMap = new HashMap<>();

    public GT_MetaTileEntity_ExtremeExterminationChamber(int aID, String aName, String aNameRegional) {
        super(aID, aName, aNameRegional);
    }

    public GT_MetaTileEntity_ExtremeExterminationChamber(String aName) {
        super(aName);
    }

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
                .addInfo("Author: " + EnumChatFormatting.GOLD + "kuba6000")
                .addSeparator()
                .beginStructureBlock(5, 7, 5, true)
                .addController("Front Bottom Center")
                .addCasingInfo("Solid Steel Machine Casing", 10)
                .addOutputBus("Any casing", 1)
                .addEnergyHatch("Any casing", 1)
                .addMaintenanceHatch("Any casing", 1)
                .toolTipFinisher("Gregtech");
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
    public boolean checkRecipe(ItemStack aStack) {
        if (aStack == null) return false;

        if (aStack.getItem() != poweredSpawnerItem) return false;

        if (aStack.getTagCompound() == null) return false;
        String mobType = aStack.getTagCompound().getString("mobType");
        if (mobType.isEmpty()) return false;

        GT_Recipe recipe = MobNameToRecipeMap.get(mobType);

        if (recipe == null) return false;

        ArrayList<ItemStack> outputs = new ArrayList<>(recipe.mOutputs.length);
        for (int i = 0; i < recipe.mOutputs.length; i++)
            if (getBaseMetaTileEntity().getRandomNumber(10000) < recipe.getOutputChance(i))
                outputs.add(recipe.getOutput(i));

        this.mOutputItems = outputs.toArray(new ItemStack[0]);
        calculateOverclockedNessMulti(recipe.mEUt, recipe.mDuration, 2, getMaxInputVoltage());
        if (this.mEUt > 0) this.mEUt = -this.mEUt;
        this.mEfficiency = (10000 - (getIdealStatus() - getRepairStatus()) * 1000);
        this.mEfficiencyIncrease = 10000;

        return true;
    }

    @Override
    public boolean checkMachine(IGregTechTileEntity aBaseMetaTileEntity, ItemStack aStack) {
        if (!checkPiece(STRUCTURE_PIECE_MAIN, 2, 6, 0)) return false;
        return mMaintenanceHatches.size() == 1 && mEnergyHatches.size() > 0;
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
