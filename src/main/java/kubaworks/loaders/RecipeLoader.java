/*
 * kubaworks - Gregtech Addon
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

package kubaworks.loaders;

import cpw.mods.fml.common.Loader;
import kubaworks.api.enums.ItemList;
import kubaworks.common.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeExterminationChamber;

public class RecipeLoader {

    public static void addRecipes() {
        if (Loader.isModLoaded("EnderIO")) {
            ItemList.ExtremeExterminationChamber.set(new GT_MetaTileEntity_ExtremeExterminationChamber(
                            1006, "multimachine.exterminationchamber", "Extreme Extermination Chamber")
                    .getStackForm(1L));
            // TODO: RECIPE
        }
    }

    private static boolean lateRecipesInitialized = false;

    public static void addRecipesLate() {
        // Runs on server start
        if (lateRecipesInitialized) return;
        lateRecipesInitialized = true;

        // GT_MetaTileEntity_ExtremeExterminationChamber.initializeRecipeMap();
        MobRecipeLoader.generateMobRecipeMap();
    }
}
