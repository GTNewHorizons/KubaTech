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

import java.util.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;

public class CommandHandler extends CommandBase {
    private static final ArrayList<String> aliases = new ArrayList<>(Collections.singleton("kt"));
    public static final HashMap<String, ICommand> commands = new HashMap<>();

    @Override
    public String getCommandName() {
        return "kubatech";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "kubatech <option>";
    }

    @Override
    public List getCommandAliases() {
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        if (p_71515_1_.getEntityWorld().isRemote) return;
        if (p_71515_2_.length == 0) {
            p_71515_1_.addChatMessage(new ChatComponentText(EnumChatFormatting.RED
                    + "Invalid use ! The proper use of this command is: /" + getCommandUsage(p_71515_1_)));
            p_71515_1_.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "You can also use \"/kubatech help\" to get possible commands"));
            return;
        }
        if (!commands.containsKey(p_71515_2_[0])) {
            p_71515_1_.addChatMessage(
                    new ChatComponentText(EnumChatFormatting.RED + "Can't find command option " + p_71515_2_[0]));
            p_71515_1_.addChatMessage(new ChatComponentText(
                    EnumChatFormatting.RED + "You can also use \"/kubatech help\" to get possible commands"));
            return;
        }
        ICommand cmd = commands.get(p_71515_2_[0]);
        if (!cmd.canCommandSenderUseCommand(p_71515_1_)) {
            ChatComponentTranslation chatcomponenttranslation2 =
                    new ChatComponentTranslation("commands.generic.permission");
            chatcomponenttranslation2.getChatStyle().setColor(EnumChatFormatting.RED);
            p_71515_1_.addChatMessage(chatcomponenttranslation2);
        } else
            cmd.processCommand(
                    p_71515_1_,
                    p_71515_2_.length > 1 ? Arrays.copyOfRange(p_71515_2_, 1, p_71515_2_.length) : new String[0]);
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
        return true;
    }

    public void addCommand(ICommand command) {
        commands.put(command.getCommandName(), command);
    }
}
