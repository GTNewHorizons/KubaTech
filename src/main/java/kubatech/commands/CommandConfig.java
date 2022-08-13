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

package kubatech.commands;

import kubatech.Config;
import kubatech.kubatech;
import kubatech.loaders.MobRecipeLoader;
import kubatech.network.LoadConfigPacket;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;

public class CommandConfig extends CommandBase {

    @Override
    public String getCommandName() {
        return "config";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "config <option>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        if (p_71515_2_.length == 0 || !p_71515_2_[0].equals("reload")) {
            p_71515_1_.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.RED + "Invalid option ! Possible options: reload"));
            return;
        }
        Config.synchronizeConfiguration();
        MobRecipeLoader.processMobRecipeMap();
        MinecraftServer.getServer().getConfigurationManager().playerEntityList.forEach(p -> {
            if (!(p instanceof EntityPlayerMP)) return;
            kubatech.info("Sending config to " + ((EntityPlayerMP) p).getDisplayName());
            kubatech.NETWORK.sendTo(LoadConfigPacket.instance, (EntityPlayerMP) p);
        });
        p_71515_1_.addChatMessage(new ChatComponentText(EnumChatFormatting.GREEN + "Config reloaded successfully !"));
    }
}
