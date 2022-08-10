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

package kubatech.api.utils;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import net.minecraft.entity.EntityLivingBase;

public class InfernalHelper {
    private static Method isClassAllowed = null;

    public static boolean isClassAllowed(EntityLivingBase e) {
        try {
            if (isClassAllowed == null) {
                isClassAllowed = InfernalMobsCore.class.getDeclaredMethod("isClassAllowed", EntityLivingBase.class);
                isClassAllowed.setAccessible(true);
            }
            return (boolean) isClassAllowed.invoke(InfernalMobsCore.instance(), e);
        } catch (Throwable exception) {
            exception.printStackTrace();
        }
        return false;
    }

    private static Method checkEntityClassForced = null;

    public static boolean checkEntityClassForced(EntityLivingBase e) {
        try {
            if (checkEntityClassForced == null) {
                checkEntityClassForced =
                        InfernalMobsCore.class.getDeclaredMethod("checkEntityClassForced", EntityLivingBase.class);
                checkEntityClassForced.setAccessible(true);
            }
            return (boolean) checkEntityClassForced.invoke(InfernalMobsCore.instance(), e);
        } catch (Throwable exception) {
            exception.printStackTrace();
        }
        return false;
    }

    private static Field eliteRarity;

    public static int getEliteRarity() {
        try {
            if (eliteRarity == null) {
                eliteRarity = InfernalMobsCore.class.getDeclaredField("eliteRarity");
                eliteRarity.setAccessible(true);
            }
            return eliteRarity.getInt(InfernalMobsCore.instance());
        } catch (Throwable exception) {
            exception.printStackTrace();
        }
        return 15;
    }

    private static Field ultraRarity;

    public static int getUltraRarity() {
        try {
            if (ultraRarity == null) {
                ultraRarity = InfernalMobsCore.class.getDeclaredField("ultraRarity");
                ultraRarity.setAccessible(true);
            }
            return ultraRarity.getInt(InfernalMobsCore.instance());
        } catch (Throwable exception) {
            exception.printStackTrace();
        }
        return 15;
    }

    private static Field infernoRarity;

    public static int getInfernoRarity() {
        try {
            if (infernoRarity == null) {
                infernoRarity = InfernalMobsCore.class.getDeclaredField("infernoRarity");
                infernoRarity.setAccessible(true);
            }
            return infernoRarity.getInt(InfernalMobsCore.instance());
        } catch (Throwable exception) {
            exception.printStackTrace();
        }
        return 15;
    }

    private static Field dimensionBlackList;

    public static ArrayList<Integer> getDimensionBlackList() {
        try {
            if (dimensionBlackList == null) {
                dimensionBlackList = InfernalMobsCore.class.getDeclaredField("dimensionBlackList");
                dimensionBlackList.setAccessible(true);
            }
            return (ArrayList<Integer>) dimensionBlackList.get(InfernalMobsCore.instance());
        } catch (Throwable exception) {
            exception.printStackTrace();
        }
        return new ArrayList<>();
    }
}
