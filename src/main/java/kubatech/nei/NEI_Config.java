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

package KubaTech.nei;

import KubaTech.Tags;
import codechicken.nei.api.IConfigureNEI;

public class NEI_Config implements IConfigureNEI {
    public static boolean isAdded = true;

    @Override
    public void loadConfig() {
        isAdded = false;
        // if (Loader.isModLoaded("EnderIO")) new
        // EEC_Handler(GT_MetaTileEntity_ExtremeExterminationChamber.EECRecipeMap);
        new Mob_Handler();
        isAdded = true;
    }

    @Override
    public String getName() {
        return Tags.MODNAME + " NEI Plugin";
    }

    @Override
    public String getVersion() {
        return Tags.VERSION;
    }
}
