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

package kubatech;

import static kubatech.loaders.BlockLoader.registerBlocks;
import static kubatech.loaders.ItemLoader.registerItems;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.*;
import kubatech.api.LoaderReference;
import kubatech.commands.CommandConfig;
import kubatech.commands.CommandHandler;
import kubatech.commands.CommandHelp;
import kubatech.commands.CommandTea;
import kubatech.config.Config;
import kubatech.loaders.RecipeLoader;
import kubatech.loaders.TCLoader;
import kubatech.savedata.PlayerDataManager;
import net.minecraftforge.common.MinecraftForge;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        kubatech.info("Initializing ! Version: " + Tags.VERSION);

        Config.init(event.getModConfigurationDirectory());
        Config.synchronizeConfiguration();
        FMLCommonHandler.instance().bus().register(new FMLEventHandler());
        MinecraftForge.EVENT_BUS.register(new PlayerDataManager());
        registerItems();
        registerBlocks();
        if (LoaderReference.Thaumcraft) TCLoader.load();
    }

    public void init(FMLInitializationEvent event) {}

    public void postInit(FMLPostInitializationEvent event) {
        RecipeLoader.addRecipes();
    }

    public void serverAboutToStart(FMLServerAboutToStartEvent event) {}

    public void serverStarting(FMLServerStartingEvent event) {
        RecipeLoader.addRecipesLate();
        if (LoaderReference.Thaumcraft) TCLoader.lateLoad();
        CommandHandler cmd = new CommandHandler();
        cmd.addCommand(new CommandHelp());
        cmd.addCommand(new CommandConfig());
        // cmd.addCommand(new CommandBees());
        cmd.addCommand(new CommandTea());
        event.registerServerCommand(cmd);
    }

    public void serverStarted(FMLServerStartedEvent event) {}

    public void serverStopping(FMLServerStoppingEvent event) {}

    public void serverStopped(FMLServerStoppedEvent event) {}
}
