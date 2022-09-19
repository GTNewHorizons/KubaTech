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

package kubatech.config;

import java.io.File;
import kubatech.Tags;
import net.minecraftforge.common.config.Configuration;

public class Config {

    private enum Category {
        MOB_HANDLER("MobHandler"),
        DEBUG("Debug");
        String categoryName = "";

        Category(String s) {
            categoryName = s;
        }

        public String get() {
            return categoryName;
        }

        @Override
        public String toString() {
            return get();
        }
    }

    public static class MobHandler {

        public static boolean mobHandlerEnabled = true;
        public static _CacheRegenerationTrigger regenerationTrigger =
                _CacheRegenerationTrigger.ModAdditionRemovalChange;
        public static boolean includeEmptyMobs = true;
        public static String[] mobBlacklist;

        public enum _CacheRegenerationTrigger {
            Never,
            ModAdditionRemoval,
            ModAdditionRemovalChange,
            Always;

            public static _CacheRegenerationTrigger get(int oridinal) {
                return values()[oridinal];
            }
        }
    }

    public static class Debug {
        public static boolean showRenderErrors = false;
    }

    public static File configFile;
    public static File configDirectory;

    public static void init(File configFile) {
        configDirectory = new File(configFile, Tags.MODID);
        Config.configFile = new File(configDirectory, Tags.MODID + ".cfg");
    }

    public static File getConfigFile(String file) {
        return new File(configDirectory, file);
    }

    public static void synchronizeConfiguration() {
        Configuration configuration = new Configuration(configFile);
        configuration.load();

        loadMobHandlerConfig(configuration);
        loadDebugConfig(configuration);

        if (configuration.hasChanged()) {
            configuration.save();
        }
    }

    private static void loadMobHandlerConfig(Configuration configuration) {
        Category category = Category.MOB_HANDLER;
        MobHandler.mobHandlerEnabled = configuration
                .get(category.get(), "Enabled", true, "Enable \"Mob Drops\" NEI page and Extreme Extermination Chamber")
                .getBoolean();
        StringBuilder c = new StringBuilder("When will cache regeneration trigger? ");
        for (MobHandler._CacheRegenerationTrigger value : MobHandler._CacheRegenerationTrigger.values())
            c.append(value.ordinal()).append(" - ").append(value.name()).append(", ");
        MobHandler.regenerationTrigger = MobHandler._CacheRegenerationTrigger.get(configuration
                .get(
                        category.get(),
                        "CacheRegenerationTrigger",
                        MobHandler._CacheRegenerationTrigger.ModAdditionRemovalChange.ordinal(),
                        c.toString())
                .getInt());
        MobHandler.includeEmptyMobs = configuration
                .get(category.get(), "IncludeEmptyMobs", true, "Include mobs that have no drops in NEI")
                .getBoolean();
        MobHandler.mobBlacklist = configuration
                .get(
                        category.get(),
                        "MobBlacklist",
                        new String[] {
                            "Giant",
                            "Thaumcraft.TravelingTrunk",
                            "chisel.snowman",
                            "OpenBlocks.Luggage",
                            "OpenBlocks.MiniMe",
                            "SpecialMobs.SpecialCreeper",
                            "SpecialMobs.SpecialZombie",
                            "SpecialMobs.SpecialPigZombie",
                            "SpecialMobs.SpecialSlime",
                            "SpecialMobs.SpecialSkeleton",
                            "SpecialMobs.SpecialEnderman",
                            "SpecialMobs.SpecialCaveSpider",
                            "SpecialMobs.SpecialGhast",
                            "SpecialMobs.SpecialWitch",
                            "SpecialMobs.SpecialSpider",
                            "TwilightForest.HydraHead",
                            "TwilightForest.RovingCube",
                            "TwilightForest.Harbinger Cube",
                            "TwilightForest.Adherent",
                            "SpecialMobs.SpecialSilverfish",
                        },
                        "These mobs will be skipped when generating recipe map")
                .getStringList();
    }

    private static void loadDebugConfig(Configuration configuration) {
        Category category = Category.DEBUG;
        Debug.showRenderErrors =
                configuration.get(category.get(), "ShowRenderErrors", false).getBoolean();
    }
}
