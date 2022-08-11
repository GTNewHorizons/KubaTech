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

package kubatech.nei;

import static org.lwjgl.opengl.GL11.GL_MODELVIEW_STACK_DEPTH;

import codechicken.lib.gui.GuiDraw;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.*;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.event.FMLInterModComms;
import crazypants.enderio.EnderIO;
import crazypants.enderio.machine.spawner.BlockPoweredSpawner;
import java.awt.*;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;
import kubatech.api.utils.FastRandom;
import kubatech.api.utils.InfernalHelper;
import kubatech.api.utils.ModUtils;
import kubatech.kubatech;
import kubatech.loaders.MobRecipeLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class Mob_Handler extends TemplateRecipeHandler {
    private static final Mob_Handler instance = new Mob_Handler();
    private static final List<MobCachedRecipe> cachedRecipes = new ArrayList<>();
    public static int cycleTicksStatic = Math.abs((int) System.currentTimeMillis());
    private static final int itemsPerRow = 8, itemXShift = 18, itemYShift = 18, nextRowYShift = 35;

    public static void addRecipe(EntityLiving e, List<MobRecipeLoader.MobDrop> drop) {
        List<MobPositionedStack> positionedStacks = new ArrayList<>();
        int xorigin = 7, xoffset = xorigin, yoffset = 95, normaldrops = 0, raredrops = 0, additionaldrops = 0;
        MobRecipeLoader.MobDrop.DropType i = null;
        for (MobRecipeLoader.MobDrop d : drop) {
            if (i == d.type) {
                xoffset += itemXShift;
                if (xoffset >= xorigin + (itemXShift * itemsPerRow)) {
                    xoffset = xorigin;
                    yoffset += itemYShift;
                }
            }
            if (i != null && i != d.type) {
                xoffset = xorigin;
                yoffset += nextRowYShift;
            }
            i = d.type;
            if (d.type == MobRecipeLoader.MobDrop.DropType.Normal) normaldrops++;
            else if (d.type == MobRecipeLoader.MobDrop.DropType.Rare) raredrops++;
            else if (d.type == MobRecipeLoader.MobDrop.DropType.Additional) additionaldrops++;
            positionedStacks.add(new MobPositionedStack(
                    d.stack.copy(),
                    xoffset,
                    yoffset,
                    d.type,
                    d.chance,
                    d.enchantable,
                    d.damages != null ? new ArrayList<>(d.damages.keySet()) : null));
        }
        instance.addRecipeInt(e, positionedStacks, normaldrops, raredrops, additionaldrops);
    }

    private void addRecipeInt(
            EntityLiving e,
            List<Mob_Handler.MobPositionedStack> l,
            int normaldrops,
            int raredrops,
            int additionaldrops) {
        cachedRecipes.add(new MobCachedRecipe(e, l, normaldrops, raredrops, additionaldrops));
    }

    public Mob_Handler() {
        this.transferRects.add(new RecipeTransferRect(new Rectangle(7, 62, 16, 16), getOverlayIdentifier()));
        if (!NEI_Config.isAdded) {
            FMLInterModComms.sendRuntimeMessage(
                    kubatech.instance,
                    "NEIPlugins",
                    "register-crafting-handler",
                    "kubatech@" + getRecipeName() + "@" + getOverlayIdentifier());
            GuiCraftingRecipe.craftinghandlers.add(this);
            GuiUsageRecipe.usagehandlers.add(this);
        }
    }

    @Override
    public TemplateRecipeHandler newInstance() {
        return new Mob_Handler();
    }

    @Override
    public String getOverlayIdentifier() {
        return "kubatech.mobhandler";
    }

    @Override
    public String getGuiTexture() {
        return "kubatech:textures/gui/MobHandler.png";
    }

    @Override
    public void drawBackground(int recipe) {
        GL11.glColor4f(1f, 1f, 1f, 1f);
        GuiDraw.changeTexture(getGuiTexture());
        GuiDraw.drawTexturedModalRect(0, 0, 0, 0, 168, 192);

        MobCachedRecipe currentrecipe = ((MobCachedRecipe) arecipes.get(recipe));

        {
            int x = 6, y = 94, yshift = nextRowYShift;
            if (currentrecipe.normalOutputsCount > 0) {
                for (int i = 0; i < ((currentrecipe.normalOutputsCount - 1) / itemsPerRow) + 1; i++) {
                    GuiDraw.drawTexturedModalRect(x, y + (18 * i), 0, 192, 144, 18);
                    if (i > 0) GuiDraw.drawTexturedModalRect(x, y + ((18 * i) - 1), 0, 193, 144, 2);
                }
                y += yshift + ((currentrecipe.normalOutputsCount - 1) / itemsPerRow) * 18;
            }
            if (currentrecipe.rareOutputsCount > 0) {
                for (int i = 0; i < ((currentrecipe.rareOutputsCount - 1) / itemsPerRow) + 1; i++) {
                    GuiDraw.drawTexturedModalRect(x, y + (18 * i), 0, 192, 144, 18);
                    if (i > 0) GuiDraw.drawTexturedModalRect(x, y + ((18 * i) - 1), 0, 193, 144, 2);
                }
                y += yshift + ((currentrecipe.rareOutputsCount - 1) / itemsPerRow) * 18;
            }
            if (currentrecipe.additionalOutputsCount > 0) {
                for (int i = 0; i < ((currentrecipe.additionalOutputsCount - 1) / itemsPerRow) + 1; i++) {
                    GuiDraw.drawTexturedModalRect(x, y + (18 * i), 0, 192, 144, 18);
                    if (i > 0) GuiDraw.drawTexturedModalRect(x, y + ((18 * i) - 1), 0, 193, 144, 2);
                }
            }
        }

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1f, 1f, 1f, 1f);

        Minecraft mc = Minecraft.getMinecraft();

        ScaledResolution scale = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        int width = scale.getScaledWidth();
        int height = scale.getScaledHeight();
        int mouseX = Mouse.getX() * width / mc.displayWidth;
        int mouseZ = height - Mouse.getY() * height / mc.displayHeight - 1;

        // Get current x,y from matrix
        FloatBuffer buf = BufferUtils.createFloatBuffer(16);
        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, buf);
        float x = buf.get(12);
        float y = buf.get(13);

        int stackdepth = GL11.glGetInteger(GL_MODELVIEW_STACK_DEPTH);

        GL11.glPushMatrix();

        ItemStack s = getIngredientStacks(recipe).get(0).item;
        try {
            EntityLiving e = currentrecipe.mob;
            float ehight = e.height;
            int desiredhight = 27;

            int scaled = (int) (desiredhight / ehight);
            int mobx = 30, moby = 50;
            e.setPosition(mc.thePlayer.posX + 5, mc.thePlayer.posY, mc.thePlayer.posZ);
            // ARGS: x, y, scale, rot, rot, entity
            GuiInventory.func_147046_a(
                    mobx, moby, scaled, (float) (x + mobx) - mouseX, (float) (y + moby - ehight * scaled) - mouseZ, e);
        } catch (Throwable ignored) {

        }

        stackdepth -= GL11.glGetInteger(GL_MODELVIEW_STACK_DEPTH);
        if (stackdepth < 0) for (; stackdepth < 0; stackdepth++) GL11.glPopMatrix();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    @Override
    public void drawForeground(int recipe) {
        MobCachedRecipe currentrecipe = ((MobCachedRecipe) arecipes.get(recipe));
        int y = 7, yshift = 10, x = 57;
        GuiDraw.drawString(currentrecipe.localizedName, x, y, 0xFF555555, false);
        GuiDraw.drawString("Mod: " + currentrecipe.mod, x, y += yshift, 0xFF555555, false);
        GuiDraw.drawString("Max health: " + currentrecipe.maxHealth, x, y += yshift, 0xFF555555, false);
        switch (currentrecipe.infernaltype) {
            case -1:
                break;
            case 0:
                GuiDraw.drawString("Cannot spawn infernal", x, y += yshift, 0xFF555555, false);
                break;
            case 1:
                GuiDraw.drawString("Can spawn infernal", x, y += yshift, 0xFFFF0000, false);
                break;
            case 2:
                GuiDraw.drawString("Always spawns infernal", x, y += yshift, 0xFFFF0000, false);
                break;
        }
        x = 6;
        y = 83;
        yshift = nextRowYShift;
        if (currentrecipe.normalOutputsCount > 0) {
            GuiDraw.drawString("Normal Drops", x, y, 0xFF555555, false);
            y += yshift + ((currentrecipe.normalOutputsCount - 1) / itemsPerRow) * 18;
        }
        if (currentrecipe.rareOutputsCount > 0) {
            GuiDraw.drawString("Rare Drops", x, y, 0xFF555555, false);
            y += yshift + ((currentrecipe.rareOutputsCount - 1) / itemsPerRow) * 18;
        }
        if (currentrecipe.additionalOutputsCount > 0) {
            GuiDraw.drawString("Additional Drops", x, y, 0xFF555555, false);
        }
    }

    @Override
    public String getRecipeName() {
        return "Mob drops";
    }

    @Override
    public IUsageHandler getUsageAndCatalystHandler(String inputId, Object... ingredients) {
        if (inputId.equals("item")) {
            TemplateRecipeHandler handler = newInstance();
            ItemStack candidate = (ItemStack) ingredients[0];
            if (RecipeCatalysts.containsCatalyst(handler, candidate)) {
                handler.loadCraftingRecipes(getOverlayIdentifier(), (Object) null);
                return handler;
            }
        }
        return this.getUsageHandler(inputId, ingredients);
    }

    @Override
    public void loadCraftingRecipes(String outputId, Object... results) {
        if (outputId.equals(getOverlayIdentifier())) {
            arecipes.addAll(cachedRecipes);
            return;
        }
        super.loadCraftingRecipes(outputId, results);
    }

    @Override
    public void loadCraftingRecipes(ItemStack result) {
        for (MobCachedRecipe r : cachedRecipes) if (r.contains(r.mOutputs, result)) arecipes.add(r);
    }

    @Override
    public void loadUsageRecipes(ItemStack ingredient) {
        if (Loader.isModLoaded("EnderIO")
                && ingredient.getItem() == Item.getItemFromBlock(EnderIO.blockPoweredSpawner)) {
            if (!ingredient.hasTagCompound() || !ingredient.getTagCompound().hasKey("mobType")) {
                loadCraftingRecipes(getOverlayIdentifier(), (Object) null);
                return;
            }
            for (MobCachedRecipe r : cachedRecipes)
                if (r.mInput.stream()
                        .anyMatch(s -> s.getItem() == ingredient.getItem()
                                && Objects.equals(
                                        s.getTagCompound().getString("mobType"),
                                        ingredient.getTagCompound().getString("mobType")))) arecipes.add(r);
        } else
            for (MobCachedRecipe r : cachedRecipes)
                if (r.mInput.stream().anyMatch(ingredient::isItemEqual)) arecipes.add(r);
    }

    @Override
    public void onUpdate() {
        cycleTicksStatic++;
    }

    public static class MobPositionedStack extends PositionedStack {

        public final MobRecipeLoader.MobDrop.DropType type;
        public final int chance;
        public final boolean enchantable;
        public final boolean randomdamage;
        public final List<Integer> damages;
        public final int enchantmentLevel;
        private final Random rand;

        public MobPositionedStack(
                Object object,
                int x,
                int y,
                MobRecipeLoader.MobDrop.DropType type,
                int chance,
                Integer enchantable,
                List<Integer> damages) {
            super(object, x, y, false);
            rand = new FastRandom();
            this.type = type;
            this.chance = chance;
            this.enchantable = enchantable != null;
            if (this.enchantable) enchantmentLevel = enchantable;
            else enchantmentLevel = 0;
            this.randomdamage = damages != null;
            if (this.randomdamage) this.damages = damages;
            else this.damages = null;
            NBTTagList extratooltip = new NBTTagList();

            if (chance != 10000)
                extratooltip.appendTag(new NBTTagString(
                        EnumChatFormatting.RESET + "Chance: " + (chance / 100) + "." + (chance % 100) + "%"));
            extratooltip.appendTag(new NBTTagString(EnumChatFormatting.RESET + "" + EnumChatFormatting.GRAY + ""
                    + EnumChatFormatting.ITALIC + "Please remember that these are average drops."));

            NBTTagCompound itemtag = this.items[0].getTagCompound();
            if (itemtag == null) itemtag = new NBTTagCompound();
            NBTTagCompound display = new NBTTagCompound();
            if (itemtag.hasKey("display")) {
                display = itemtag.getCompoundTag("display");
                if (display.hasKey("Lore")) {
                    NBTTagList lore = display.getTagList("Lore", 8);
                    for (int i = 0; i < extratooltip.tagCount(); i++)
                        lore.appendTag(new NBTTagString(extratooltip.getStringTagAt(i)));
                    display.setTag("Lore", lore);
                } else display.setTag("Lore", extratooltip);
            } else display.setTag("Lore", extratooltip);
            itemtag.setTag("display", display);
            this.items[0].setTagCompound(itemtag);
            this.item.setTagCompound(itemtag);
            setPermutationToRender(0);
        }

        @Override
        public void setPermutationToRender(int index) {
            if (this.item == null) this.item = this.items[0].copy();
            if (enchantable) {
                this.item.getTagCompound().removeTag("ench");
                EnchantmentHelper.addRandomEnchantment(rand, this.item, enchantmentLevel);
            }
            if (randomdamage) this.item.setItemDamage(damages.get(rand.nextInt(damages.size())));
        }
    }

    private class MobCachedRecipe extends TemplateRecipeHandler.CachedRecipe {

        public final EntityLiving mob;
        public final List<PositionedStack> mOutputs;
        public final List<ItemStack> mInput;
        public final String mobname;
        public final int infernaltype;
        public final PositionedStack ingredient;
        public final String localizedName;
        public final String mod;
        public final float maxHealth;
        public final int normalOutputsCount;
        public final int rareOutputsCount;
        public final int additionalOutputsCount;

        public MobCachedRecipe(
                EntityLiving mob,
                List<MobPositionedStack> mOutputs,
                int normalOutputsCount,
                int rareOutputsCount,
                int additionalOutputsCount) {
            super();
            String classname = mob.getClass().getName();
            this.mod = ModUtils.getModNameFromClassName(classname);
            this.mob = mob;
            this.maxHealth = mob.getMaxHealth();
            this.mOutputs = new ArrayList<>(mOutputs.size());
            this.mOutputs.addAll(mOutputs);
            this.normalOutputsCount = normalOutputsCount;
            this.rareOutputsCount = rareOutputsCount;
            this.additionalOutputsCount = additionalOutputsCount;
            this.mInput = new ArrayList<>();
            int id = EntityList.getEntityID(mob);
            mobname = EntityList.getEntityString(mob);
            localizedName = StatCollector.translateToLocal("entity." + mobname + ".name");
            if (id != 0) {
                this.mInput.add(new ItemStack(Items.spawn_egg, 1, id));
                this.mInput.add(new ItemStack(Blocks.mob_spawner, 1, id));
            }
            if (Loader.isModLoaded("EnderIO")) {
                ItemStack s = new ItemStack(EnderIO.blockPoweredSpawner, 1);
                NBTTagCompound nbt = new NBTTagCompound();
                BlockPoweredSpawner.writeMobTypeToNBT(nbt, mobname);
                s.setTagCompound(nbt);
                this.mInput.add(0, s);
            } else if (id == 0) this.mInput.add(new ItemStack(Items.spawn_egg, 1, 0)); // ???
            ingredient = new PositionedStack(this.mInput.get(0), 38, 44, false);

            if (!Loader.isModLoaded("InfernalMobs")) infernaltype = -1; // not supported
            else {
                if (!InfernalHelper.isClassAllowed(mob)) infernaltype = 0; // not allowed
                else if (InfernalHelper.checkEntityClassForced(mob)) infernaltype = 2; // forced
                else infernaltype = 1; // normal
            }
        }

        @Override
        public PositionedStack getIngredient() {
            return ingredient;
        }

        @Override
        public PositionedStack getResult() {
            return null;
        }

        @Override
        public List<PositionedStack> getOtherStacks() {
            if (cycleTicksStatic % 10 == 0) mOutputs.forEach(p -> p.setPermutationToRender(0));
            return mOutputs;
        }
    }
}
