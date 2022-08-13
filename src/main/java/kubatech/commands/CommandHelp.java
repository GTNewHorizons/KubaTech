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

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandHelp extends CommandBase {
    @Override
    public String getCommandName() {
        return "help";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "help - Shows all commands";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
        return true;
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        p_71515_1_.addChatMessage(new ChatComponentText("Possible commands: "));
        CommandHandler.commands.values().forEach(c -> {
            p_71515_1_.addChatMessage(new ChatComponentText("/kubatech " + c.getCommandUsage(p_71515_1_)));
        });
    }
}
