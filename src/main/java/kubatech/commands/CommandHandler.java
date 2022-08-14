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

import static kubatech.commands.CommandHandler.Translations.*;

import java.util.*;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

public class CommandHandler extends CommandBase {
    enum Translations {
        INVALID,
        CANT_FIND,
        GENERIC_HELP,
        USAGE,
        ;
        final String key;

        Translations() {
            key = "commandhandler." + this.name().toLowerCase();
        }

        public String get() {
            return StatCollector.translateToLocal(key);
        }

        public String get(Object... args) {
            return StatCollector.translateToLocalFormatted(key, args);
        }

        public String getKey() {
            return key;
        }

        @Override
        public String toString() {
            return get();
        }
    }

    private static final ArrayList<String> aliases = new ArrayList<>(Collections.singleton("kt"));
    public static final HashMap<String, ICommand> commands = new HashMap<>();

    @Override
    public String getCommandName() {
        return "kubatech";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "kubatech " + USAGE.get();
    }

    @Override
    public List getCommandAliases() {
        return aliases;
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        if (p_71515_1_.getEntityWorld().isRemote) return;
        if (p_71515_2_.length == 0) {
            p_71515_1_.addChatMessage(new ChatComponentText(INVALID.get(getCommandUsage(p_71515_1_))));
            p_71515_1_.addChatMessage(new ChatComponentText(GENERIC_HELP.get()));
            return;
        }
        if (!commands.containsKey(p_71515_2_[0])) {
            p_71515_1_.addChatMessage(new ChatComponentText(CANT_FIND.get(p_71515_2_[0])));
            p_71515_1_.addChatMessage(new ChatComponentText(GENERIC_HELP.get()));
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
