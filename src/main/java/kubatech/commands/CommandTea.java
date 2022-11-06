package kubatech.commands;

import kubatech.savedata.PlayerData;
import kubatech.savedata.PlayerDataManager;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class CommandTea extends CommandBase {
    @Override
    public String getCommandName() {
        return "tea";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "tea <username> get/set/add <amount>";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 4;
    }

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
        if (p_71515_2_.length != 3) return;
        PlayerData playerData = PlayerDataManager.getPlayer(p_71515_2_[0]);
        if (playerData == null) return;
        switch (p_71515_2_[1].toLowerCase()) {
            case "get":
                p_71515_1_.addChatMessage(new ChatComponentText(p_71515_2_[0] + " has " + playerData.teaAmount));
                break;
            case "set":
                playerData.teaAmount = Long.parseLong(p_71515_2_[2]);
                playerData.markDirty();
                p_71515_1_.addChatMessage(new ChatComponentText(p_71515_2_[0] + " now has " + playerData.teaAmount));
                break;
            case "add":
                playerData.teaAmount += Long.parseLong(p_71515_2_[2]);
                playerData.markDirty();
                p_71515_1_.addChatMessage(new ChatComponentText(p_71515_2_[0] + " now has " + playerData.teaAmount));
                break;
            default:
                break;
        }
    }
}
