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
