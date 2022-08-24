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

package kubatech.loaders.item.items;

import java.util.List;
import kubatech.loaders.item.ItemProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

public class Tea extends ItemProxy {
    public Tea(String unlocalizedName) {
        super("teacollection." + unlocalizedName, "teacollection/" + unlocalizedName);
    }

    @Override
    public void addInformation(EntityPlayer p_77624_2_, List p_77624_3_, boolean p_77624_4_) {
        p_77624_3_.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("item.fromcollection"));
        p_77624_3_.add(EnumChatFormatting.GRAY + "" + EnumChatFormatting.BOLD + "" + EnumChatFormatting.ITALIC + ""
                + EnumChatFormatting.UNDERLINE + StatCollector.translateToLocal("item.teacollection"));
    }

    @Override
    public EnumAction getItemUseAction() {
        return EnumAction.drink;
    }

    @Override
    public ItemStack onItemRightClick(ItemStack p_77659_1_, World p_77659_2_, EntityPlayer p_77659_3_) {
        p_77659_3_.setItemInUse(p_77659_1_, 32);
        return p_77659_1_;
    }

    @Override
    public ItemStack onEaten(ItemStack p_77654_1_, World p_77654_2_, EntityPlayer p_77654_3_) {
        if (p_77654_2_.isRemote) return p_77654_1_;
        p_77654_3_.addChatComponentMessage(new ChatComponentText(
                EnumChatFormatting.GREEN + StatCollector.translateToLocal("item.teacollection.mmm")));
        return p_77654_1_;
    }

    @Override
    public int getMaxItemUseDuration() {
        return 32;
    }
}
