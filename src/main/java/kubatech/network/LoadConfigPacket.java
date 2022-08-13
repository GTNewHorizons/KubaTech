package kubatech.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import kubatech.Config;

public class LoadConfigPacket implements IMessage {

    public static LoadConfigPacket instance = new LoadConfigPacket();

    public HashSet<String> mobsToLoad = new HashSet<>();

    @Override
    public void fromBytes(ByteBuf buf) {
        if (!buf.readBoolean()) mobsToLoad.clear();
        else {
            mobsToLoad.clear();
            int mobssize = buf.readInt();
            for (int i = 0; i < mobssize; i++) {
                byte[] sbytes = new byte[buf.readInt()];
                buf.readBytes(sbytes);
                mobsToLoad.add(new String(sbytes, StandardCharsets.UTF_8));
            }
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (!Config.mobHandlerEnabled) buf.writeBoolean(false);
        else {
            buf.writeBoolean(true);
            buf.writeInt(mobsToLoad.size());
            mobsToLoad.forEach(s -> {
                byte[] sbytes = s.getBytes(StandardCharsets.UTF_8);
                buf.writeInt(sbytes.length);
                buf.writeBytes(sbytes);
            });
        }
    }
}
