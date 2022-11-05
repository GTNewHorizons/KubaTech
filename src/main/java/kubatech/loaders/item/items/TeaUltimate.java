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

import com.gtnewhorizons.modularui.api.ModularUITextures;
import com.gtnewhorizons.modularui.api.drawable.Text;
import com.gtnewhorizons.modularui.api.math.Color;
import com.gtnewhorizons.modularui.api.screen.ModularUIContext;
import com.gtnewhorizons.modularui.api.screen.ModularWindow;
import com.gtnewhorizons.modularui.api.screen.UIBuildContext;
import com.gtnewhorizons.modularui.common.builder.UIBuilder;
import com.gtnewhorizons.modularui.common.builder.UIInfo;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularGui;
import com.gtnewhorizons.modularui.common.internal.wrapper.ModularUIContainer;
import com.gtnewhorizons.modularui.common.widget.DynamicTextWidget;
import kubatech.api.utils.ModUtils;
import kubatech.api.utils.StringUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

public class TeaUltimate extends TeaCollection {
    public TeaUltimate() {
        super("ultimate_tea");
    }

    private static String name = "";
    private static long timeCounter = 0;
    private static int colorCounter = 0;

    @Override
    public String getDisplayName(ItemStack stack) {
        if (!ModUtils.isClientSided) return super.getDisplayName(stack);
        if (stack.stackTagCompound == null
                || (!stack.stackTagCompound.hasKey("TeaOwner")
                        || stack.stackTagCompound
                                .getString("TeaOwner")
                                .equals(Minecraft.getMinecraft().thePlayer.getCommandSenderName()))) {
            long current = System.currentTimeMillis();
            if (current - timeCounter > 100) {
                timeCounter = current;
                name = StringUtils.applyRainbow(
                        "ULTIMATE", colorCounter++, EnumChatFormatting.BOLD.toString() + EnumChatFormatting.OBFUSCATED);
            }
            return String.format(super.getDisplayName(stack), name + EnumChatFormatting.RESET);
        }
        return EnumChatFormatting.GOLD + "" + EnumChatFormatting.BOLD + "" + EnumChatFormatting.ITALIC + "???????";
    }

    private long teaAmount = 0L;

    private ModularWindow createWindow() {
        ModularWindow.Builder builder = ModularWindow.builder(200, 150);
        builder.setBackground(ModularUITextures.VANILLA_BACKGROUND);
        DynamicTextWidget text = new DynamicTextWidget(() -> new Text("Tea: " + teaAmount).color(Color.GREEN.normal));
        builder.widget(text.setPos(20, 20));
        return builder.build();
    }

    // private static final

    @Override
    public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer entity) {
        if (world.isRemote) return stack;
        UIInfo<?, ?> TeaUI = UIBuilder.of()
                .container((player, w, x, y, z) -> {
                    UIBuildContext context = new UIBuildContext(player);
                    ModularWindow window = createWindow();
                    return new ModularUIContainer(
                            new ModularUIContext(context, () -> player.inventoryContainer.detectAndSendChanges()),
                            window);
                })
                .gui((player, w, x, y, z) -> {
                    UIBuildContext context = new UIBuildContext(player);
                    ModularWindow window = createWindow();
                    return new ModularGui(new ModularUIContainer(
                            new ModularUIContext(context, () -> player.inventory.markDirty()), window));
                })
                .build();
        TeaUI.open(entity);
        return stack;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isCurrentItem) {
        teaAmount++;
    }
}
