package kubatech.commands;

import static kubatech.commands.CommandTea.Translations.*;

import kubatech.savedata.PlayerData;
import kubatech.savedata.PlayerDataManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.StatCollector;

public class CommandTea extends CommandBase {
    enum Translations {
        INVALID_OPTION,
        PLAYER_NOT_FOUND,
        SUCCESS_GET,
        SUCCESS_SET,
        SUCCESS_ADD,
        USAGE,
        ;
        final String key;

        Translations() {
            key = "command.tea." + this.name().toLowerCase();
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

    @Override
    public String getCommandName() {
        return "tea";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "tea " + USAGE.get();
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        if (p_71515_2_.length < 2) {
            p_71515_1_.addChatMessage(new ChatComponentText(INVALID_OPTION.get()));
            return;
        }
        PlayerData playerData = PlayerDataManager.getPlayer(p_71515_2_[0]);
        if (playerData == null) {
            p_71515_1_.addChatMessage(new ChatComponentText(PLAYER_NOT_FOUND.get()));
            return;
        }
        if (!p_71515_2_[1].equalsIgnoreCase("get") && p_71515_2_.length < 3) {
            p_71515_1_.addChatMessage(new ChatComponentText(INVALID_OPTION.get()));
            return;
        }
        switch (p_71515_2_[1].toLowerCase()) {
            case "get":
                p_71515_1_.addChatMessage(new ChatComponentText(SUCCESS_GET.get(p_71515_2_[0], playerData.teaAmount)));
                break;
            case "set": {
                long tea;
                try {
                    tea = Long.parseLong(p_71515_2_[2]);
                } catch (NumberFormatException ex) {
                    p_71515_1_.addChatMessage(new ChatComponentText(INVALID_OPTION.get()));
                    return;
                }
                playerData.teaAmount = tea;
                playerData.markDirty();
                p_71515_1_.addChatMessage(new ChatComponentText(SUCCESS_SET.get(p_71515_2_[0], playerData.teaAmount)));
                break;
            }
            case "add": {
                long tea;
                try {
                    tea = Long.parseLong(p_71515_2_[2]);
                } catch (NumberFormatException ex) {
                    p_71515_1_.addChatMessage(new ChatComponentText(INVALID_OPTION.get()));
                    return;
                }
                playerData.teaAmount += tea;
                playerData.markDirty();
                p_71515_1_.addChatMessage(new ChatComponentText(SUCCESS_ADD.get(p_71515_2_[0], playerData.teaAmount)));
                break;
            }
            default:
                break;
        }
    }
}
