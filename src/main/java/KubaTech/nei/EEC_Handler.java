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

package KubaTech.nei;

import KubaTech.KubaTech;
import codechicken.nei.guihook.GuiContainerManager;
import codechicken.nei.recipe.GuiCraftingRecipe;
import codechicken.nei.recipe.GuiUsageRecipe;
import codechicken.nei.recipe.TemplateRecipeHandler;
import cpw.mods.fml.common.event.FMLInterModComms;
import gregtech.api.util.GT_Recipe;
import gregtech.nei.GT_NEI_DefaultHandler;
import java.nio.FloatBuffer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class EEC_Handler extends GT_NEI_DefaultHandler {
    public EEC_Handler(GT_Recipe.GT_Recipe_Map aRecipeMap) {
        super(aRecipeMap);
        if (!NEI_Config.isAdded) {
            FMLInterModComms.sendRuntimeMessage(
                    KubaTech.instance,
                    "NEIPlugins",
                    "register-crafting-handler",
                    "KubaTech@" + getRecipeName() + "@" + getOverlayIdentifier());
            GuiCraftingRecipe.craftinghandlers.add(this);
            GuiUsageRecipe.usagehandlers.add(this);
        }
    }

    @Override
    public TemplateRecipeHandler newInstance() {
        return new EEC_Handler(this.mRecipeMap);
    }

    @Override
    public void drawForeground(int recipe) {
        super.drawForeground(recipe);

        GuiContainerManager.enable3DRender();
        GL11.glColor4f(1f, 1f, 1f, 1f);

        Minecraft mc = Minecraft.getMinecraft();

        ScaledResolution scale = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int factor = scale.getScaleFactor();

        int width = scale.getScaledWidth();
        int height = scale.getScaledHeight();
        int mouseX = Mouse.getX() * width / mc.displayWidth;
        int mouseZ = height - Mouse.getY() * height / mc.displayHeight - 1;

        // Get current x,y from matrix
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf);
        float x = buf.get(12);
        float y = buf.get(13);

        ItemStack s = getIngredientStacks(recipe).get(0).item;
        try {
            EntityLivingBase e = (EntityLivingBase)
                    EntityList.createEntityByID(s.getItemDamage(), Minecraft.getMinecraft().theWorld);
            if (e instanceof EntitySlime) {
                NBTTagCompound nbt = new NBTTagCompound();
                e.writeEntityToNBT(nbt);
                nbt.setInteger("Size", 0);
                e.readEntityFromNBT(nbt);
            }

            float ehight = e.height;
            int desiredhight = 27;

            int scaled = (int) (desiredhight / ehight);
            // ARGS: x, y, scale, rot, rot, entity
            GuiInventory.func_147046_a(
                    25, 37, scaled, (float) (x + 25) - mouseX, (float) (y + 37 - ehight * scaled) - mouseZ, e);
        } catch (Throwable ignored) {

        }

        GuiContainerManager.enable2DRender();
    }
}
