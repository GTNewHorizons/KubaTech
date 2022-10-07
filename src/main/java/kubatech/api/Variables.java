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

package kubatech.api;

import net.minecraft.util.EnumChatFormatting;

public class Variables {
    public static final String Author = "Author: " + EnumChatFormatting.GOLD + "kuba6000";
    public static String buildAuthorList(String... authors){
        if(authors.length == 0)
            return "Author: Unknown";
        StringBuilder b = new StringBuilder("Author: ").append(EnumChatFormatting.GOLD).append(authors[0]);
        for (int i = 1; i < authors.length; i++) {
            String author = authors[i];
            b.append(EnumChatFormatting.RESET).append(" & ").append(EnumChatFormatting.GOLD).append(author);
        }
        return b.toString();
    }
    public static final String StructureHologram =
            "To see the structure, use a " + EnumChatFormatting.BLUE + "Tec" + EnumChatFormatting.DARK_BLUE + "Tech"
                    + EnumChatFormatting.RESET + "" + EnumChatFormatting.GRAY + " Blueprint on the Controller!";
}
