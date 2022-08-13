package kubatech.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import kubatech.kubatech;
import kubatech.loaders.MobRecipeLoader;

public class LoadConfigHandler implements IMessageHandler<LoadConfigPacket, IMessage> {

    @Override
    public IMessage onMessage(LoadConfigPacket message, MessageContext ctx) {
        kubatech.info("Received Mob Handler config, parsing");
        MobRecipeLoader.processMobRecipeMap(message.mobsToLoad);
        return null;
    }
}
