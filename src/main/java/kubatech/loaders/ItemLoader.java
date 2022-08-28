/*
 * KubaTech - Gregtech Addon
 * Copyright (C) 2022  kuba6000
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package kubatech.loaders;

import static kubatech.api.enums.ItemList.*;

import cpw.mods.fml.common.registry.GameRegistry;
import gregtech.api.enums.GT_Values;
import gregtech.api.util.GT_Utility;
import gtPlusPlus.core.lib.CORE;
import kubatech.api.LoaderReference;
import kubatech.loaders.item.KubaItems;
import kubatech.loaders.item.items.Tea;
import kubatech.loaders.item.items.TeaCollection;
import kubatech.loaders.item.items.TeaIngredient;
import kubatech.loaders.item.items.TeaUltimate;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public class ItemLoader {
    public static final KubaItems kubaitems = new KubaItems();

    public static void RegisterItems() {
        GameRegistry.registerItem(kubaitems, "kubaitems");

        // DON'T EVER CHANGE ORDER IN HERE, ADD NEW ITEMS ON BOTTOM

        LegendaryBlackTea.set(kubaitems.registerProxyItem(new TeaCollection("black_tea")));
        LegendaryButterflyTea.set(kubaitems.registerProxyItem(new TeaCollection("butterfly_tea")));
        LegendaryEarlGrayTea.set(kubaitems.registerProxyItem(new TeaCollection("earl_gray_tea")));
        LegendaryGreenTea.set(kubaitems.registerProxyItem(new TeaCollection("green_tea")));
        LegendaryLemonTea.set(kubaitems.registerProxyItem(new TeaCollection("lemon_tea")));
        LegendaryMilkTea.set(kubaitems.registerProxyItem(new TeaCollection("milk_tea")));
        LegendaryOolongTea.set(kubaitems.registerProxyItem(new TeaCollection("oolong_tea")));
        LegendaryPeppermintTea.set(kubaitems.registerProxyItem(new TeaCollection("peppermint_tea")));
        LegendaryPuerhTea.set(kubaitems.registerProxyItem(new TeaCollection("pu-erh_tea")));
        LegendaryRedTea.set(kubaitems.registerProxyItem(new TeaCollection("red_tea")));
        LegendaryWhiteTea.set(kubaitems.registerProxyItem(new TeaCollection("white_tea")));
        LegendaryYellowTea.set(kubaitems.registerProxyItem(new TeaCollection("yellow_tea")));
        LegendaryUltimateTea.set(kubaitems.registerProxyItem(new TeaUltimate()));

        BlackTea.set(kubaitems.registerProxyItem(new Tea("black_tea", 4, 0.3f)));
        EarlGrayTea.set(kubaitems.registerProxyItem(new Tea("earl_gray_tea", 4, 0.3f)));
        GreenTea.set(kubaitems.registerProxyItem(new Tea("green_tea", 4, 0.3f)));
        LemonTea.set(kubaitems.registerProxyItem(new Tea("lemon_tea", 4, 0.3f)));
        MilkTea.set(kubaitems.registerProxyItem(new Tea("milk_tea", 4, 0.3f)));
        OolongTea.set(kubaitems.registerProxyItem(new Tea("oolong_tea", 4, 0.3f)));
        PeppermintTea.set(kubaitems.registerProxyItem(new Tea("peppermint_tea", 4, 0.3f)));
        PuerhTea.set(kubaitems.registerProxyItem(new Tea("pu-erh_tea", 4, 0.3f)));
        WhiteTea.set(kubaitems.registerProxyItem(new Tea("white_tea", 4, 0.3f)));
        YellowTea.set(kubaitems.registerProxyItem(new Tea("yellow_tea", 4, 0.3f)));

        BlackTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("black_tea_leaf")));
        GreenTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("green_tea_leaf")));
        OolongTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("oolong_tea_leaf")));
        PuerhTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("pu-erh_tea_leaf")));
        WhiteTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("white_tea_leaf")));
        YellowTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("yellow_tea_leaf")));

        TeaLeafDehydrated.set(kubaitems.registerProxyItem(new TeaIngredient("tea_leaf_dehydrated")));
        SteamedTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("steamed_tea_leaf")));
        RolledTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("rolled_tea_leaf")));
        OxidizedTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("oxidized_tea_leaf")));
        FermentedTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("fermented_tea_leaf")));
        BruisedTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("bruised_tea_leaf")));
        PartiallyOxidizedTeaLeaf.set(kubaitems.registerProxyItem(new TeaIngredient("partially_oxidized_tea_leaf")));

        //    TEA LINE    //
        if (LoaderReference.GTPlusPlus && LoaderReference.HarvestCraft) {
            CORE.RA.addDehydratorRecipe(
                    new ItemStack[] {GameRegistry.findItemStack("harvestcraft", "tealeafItem", 1)},
                    null,
                    null,
                    new ItemStack[] {TeaLeafDehydrated.get(1)},
                    null,
                    100,
                    32);
            CORE.RA.addDehydratorRecipe(
                    new ItemStack[] {TeaLeafDehydrated.get(1)},
                    null,
                    null,
                    new ItemStack[] {WhiteTeaLeaf.get(1)},
                    null,
                    100,
                    32);
            GT_Values.RA.addMixerRecipe(
                    new ItemStack[] {TeaLeafDehydrated.get(1)},
                    new FluidStack[] {FluidRegistry.getFluidStack("water", 50)},
                    new ItemStack[] {SteamedTeaLeaf.get(1)},
                    null,
                    100,
                    32);
            CORE.RA.addDehydratorRecipe(
                    new ItemStack[] {SteamedTeaLeaf.get(1)},
                    null,
                    null,
                    new ItemStack[] {YellowTeaLeaf.get(1)},
                    null,
                    100,
                    32);
            GT_Values.RA.addBenderRecipe(TeaLeafDehydrated.get(1), RolledTeaLeaf.get(1), 100, 32);
            CORE.RA.addDehydratorRecipe(
                    new ItemStack[] {RolledTeaLeaf.get(1)},
                    null,
                    null,
                    new ItemStack[] {GreenTeaLeaf.get(1)},
                    null,
                    100,
                    32);
            GT_Values.RA.addChemicalRecipe(
                    RolledTeaLeaf.get(1), GT_Utility.getIntegratedCircuit(1), OxidizedTeaLeaf.get(1), 100, 32);
            CORE.RA.addDehydratorRecipe(
                    new ItemStack[] {OxidizedTeaLeaf.get(1)},
                    null,
                    null,
                    new ItemStack[] {BlackTeaLeaf.get(1)},
                    null,
                    100,
                    32);
            GT_Values.RA.addChemicalRecipe(
                    RolledTeaLeaf.get(1), GT_Utility.getIntegratedCircuit(2), FermentedTeaLeaf.get(1), 200, 32);
            CORE.RA.addDehydratorRecipe(
                    new ItemStack[] {FermentedTeaLeaf.get(1)},
                    null,
                    null,
                    new ItemStack[] {PuerhTeaLeaf.get(1)},
                    null,
                    100,
                    32);
            GT_Values.RA.addCutterRecipe(
                    new ItemStack[] {TeaLeafDehydrated.get(1)},
                    new ItemStack[] {BruisedTeaLeaf.get(1)},
                    100,
                    32,
                    false);
            GT_Values.RA.addChemicalRecipe(
                    BruisedTeaLeaf.get(1), GT_Utility.getIntegratedCircuit(1), PartiallyOxidizedTeaLeaf.get(1), 50, 32);
            CORE.RA.addDehydratorRecipe(
                    new ItemStack[] {PartiallyOxidizedTeaLeaf.get(1)},
                    null,
                    null,
                    new ItemStack[] {OolongTeaLeaf.get(1)},
                    null,
                    100,
                    32);

            // Tea Assembly
            GameRegistry.addSmelting(BlackTeaLeaf.get(1), BlackTea.get(1), 10);
            GT_Values.RA.addMixerRecipe(
                    new ItemStack[] {BlackTea.get(1), GameRegistry.findItemStack("harvestcraft", "limejuiceItem", 1)},
                    null,
                    new ItemStack[] {EarlGrayTea.get(1)},
                    null,
                    100,
                    32);
            GameRegistry.addSmelting(GreenTeaLeaf.get(1), GreenTea.get(1), 10);
            GT_Values.RA.addMixerRecipe(
                    new ItemStack[] {BlackTea.get(1)},
                    new FluidStack[] {FluidRegistry.getFluidStack("potion.lemonjuice", 1000)},
                    new ItemStack[] {LemonTea.get(1)},
                    null,
                    100,
                    32);
            GT_Values.RA.addMixerRecipe(
                    new ItemStack[] {BlackTea.get(1)},
                    new FluidStack[] {FluidRegistry.getFluidStack("milk", 1000)},
                    new ItemStack[] {MilkTea.get(1)},
                    null,
                    100,
                    32);
            GameRegistry.addSmelting(OolongTeaLeaf.get(1), OolongTea.get(1), 10);
            GT_Values.RA.addMixerRecipe(
                    new ItemStack[] {GameRegistry.findItemStack("harvestcraft", "peppermintItem", 1)},
                    new FluidStack[] {FluidRegistry.getFluidStack("water", 1000)},
                    new ItemStack[] {PeppermintTea.get(1)},
                    null,
                    100,
                    32);
            GameRegistry.addSmelting(PuerhTeaLeaf.get(1), PuerhTea.get(1), 10);
            GameRegistry.addSmelting(WhiteTeaLeaf.get(1), WhiteTea.get(1), 10);
            GameRegistry.addSmelting(YellowTeaLeaf.get(1), YellowTea.get(1), 10);
        }
    }
}
