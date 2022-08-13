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

package kubatech.loaders;

import kubatech.Tags;
import kubatech.api.LoaderReference;
import kubatech.api.enums.ItemList;
import kubatech.common.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeExterminationChamber;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecipeLoader {

    private static final Logger LOG = LogManager.getLogger(Tags.MODID + "[Recipe Loader]");

    private static int MTEID = 14201;
    private static final int MTEIDMax = 14300;

    public static void addRecipes() {
        if (LoaderReference.EnderIO) {
            ItemList.ExtremeExterminationChamber.set(new GT_MetaTileEntity_ExtremeExterminationChamber(
                            MTEID++, "multimachine.exterminationchamber", "Extreme Extermination Chamber")
                    .getStackForm(1L));
            // TODO: RECIPE
        }
        if (MTEID > MTEIDMax + 1) throw new RuntimeException("MTE ID's");
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
