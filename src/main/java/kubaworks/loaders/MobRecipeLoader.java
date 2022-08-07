/*
 * kubaworks - Gregtech Addon
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

package kubaworks.loaders;

import static kubaworks.api.utils.ModUtils.isDeobfuscatedEnvironment;
import static kubaworks.common.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeExterminationChamber.EECRecipeMap;
import static kubaworks.common.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeExterminationChamber.MobNameToRecipeMap;
import static kubaworks.kubaworks.info;
import static kubaworks.kubaworks.warn;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import crazypants.enderio.EnderIO;
import crazypants.enderio.machine.spawner.BlockPoweredSpawner;
import gregtech.api.util.GT_Utility;
import gregtech.common.GT_DummyWorld;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Consumer;
import kubaworks.api.utils.ModUtils;
import kubaworks.nei.Mob_Handler;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fluids.FluidStack;

public class MobRecipeLoader {

    public static final MobRecipeLoader instance = new MobRecipeLoader();

    private static final HashSet<String> MobBlacklist = new HashSet<>(Arrays.asList(new String[] {"chisel.snowman"}));

    @SubscribeEvent
    public void onOpenGui(GuiOpenEvent event) {
        MobRecipeLoader.generateMobRecipeMap();
        MinecraftForge.EVENT_BUS.unregister(instance);
    }

    private static final String dropFewItemsName = isDeobfuscatedEnvironment ? "dropFewItems" : "func_70628_a";
    private static final String dropRareDropName = isDeobfuscatedEnvironment ? "dropRareDrop" : "func_70600_l";
    private static final String setSlimeSizeName = isDeobfuscatedEnvironment ? "setSlimeSize" : "func_70799_a";
    private static final String randName = isDeobfuscatedEnvironment ? "rand" : "field_70146_Z";
    private static final String addRandomEnchantmentName =
            isDeobfuscatedEnvironment ? "addRandomEnchantment" : "func_77504_a";

    private static boolean alreadyGenerated = false;

    public static class MobDrop {
        public enum DropType {
            Normal,
            Rare,
            Additional
        }

        public ItemStack stack;
        public DropType type;
        public int chance;
        public Integer enchantable;

        public MobDrop(ItemStack stack, DropType type, int chance, Integer enchantable) {
            this.stack = stack;
            this.type = type;
            this.chance = chance;
            this.enchantable = enchantable;
        }
    }

    @SideOnly(Side.CLIENT)
    public static void addNEIMobRecipe(EntityLiving e, List<MobDrop> drop) {
        Mob_Handler.addRecipe(e, drop);
    }

    public static class fakeRand extends Random {
        public boolean doescallnextbool = false;
        public boolean nextbool = false;
        public ArrayList<ItemStack> randomenchantmentdetected = new ArrayList<>();
        public ArrayList<Integer> enchantabilityLevel = new ArrayList<>();
        public int maxbound = 1;
        public int overridenext = 0;

        @Override
        public int nextInt(int bound) {
            if (maxbound < bound) maxbound = bound;
            return overridenext % bound;
        }

        @Override
        public boolean nextBoolean() {
            doescallnextbool = true;
            return nextbool;
        }
    }

    @SuppressWarnings("unchecked")
    public static void generateMobRecipeMap() {

        if (alreadyGenerated) return;
        alreadyGenerated = true;

        World f = new GT_DummyWorld() {
            @Override
            public boolean blockExists(int p_72899_1_, int p_72899_2_, int p_72899_3_) {
                return false;
            }

            @Override
            public List getEntitiesWithinAABB(Class p_72872_1_, AxisAlignedBB p_72872_2_) {
                return new ArrayList();
            }
        };
        f.isRemote = true; // quick hack to get around achievements

        fakeRand frand = new fakeRand();
        f.rand = frand;

        info("[Mob Handler]Generating Recipe Map for Mob Handler and EEC");
        // Stupid MC code, I need to cast myself
        ((Map<String, Class<? extends Entity>>) EntityList.stringToClassMapping).forEach((k, v) -> {
            if (v == null) return;

            info("[Mob Handler]Generating entry for mob: " + k);

            if (Modifier.isAbstract(v.getModifiers())) {
                info("[Mob Handler]Entity " + k + " is abstract, skipping");
                return;
            }

            if (MobBlacklist.contains(k)) {
                info("[Mob Handler]Entity " + k + " is blacklisted, skipping");
                return;
            }

            EntityLiving e;
            try {
                e = (EntityLiving) v.getConstructor(new Class[] {World.class}).newInstance(new Object[] {f});
            } catch (ClassCastException ex) {
                // not a EntityLiving
                info("[Mob Handler]Entity " + k + " is not a LivingEntity, skipping");
                return;
            } catch (NoSuchMethodException ex) {
                // No constructor ?
                info("[Mob Handler]Entity " + k + " doesn't have constructor, skipping");
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            if (StatCollector.translateToLocal("entity." + k + ".name").equals("entity." + k + ".name")) {
                info("[Mob Handler]Entity " + k + " does't have localized name, skipping");
                return;
            }

            e.captureDrops = true;

            // POWERFULL GENERATION

            Class<?> s = e.getClass();
            while (!s.equals(EntityLivingBase.class)) {
                if (s.equals(EntitySlime.class)) {
                    try {
                        Method setSlimeSize = s.getDeclaredMethod(setSlimeSizeName, int.class);
                        setSlimeSize.setAccessible(true);
                        setSlimeSize.invoke(e, 1);
                    } catch (Exception ignored) {
                    }
                }

                s = s.getSuperclass();
            }
            Method dropFewItems;
            Method dropRareDrop;
            try {
                dropFewItems = s.getDeclaredMethod(dropFewItemsName, boolean.class, int.class);
                dropFewItems.setAccessible(true);
                dropRareDrop = s.getDeclaredMethod(dropRareDropName, int.class);
                dropRareDrop.setAccessible(true);
                Field rand = s.getSuperclass().getDeclaredField(randName);
                rand.setAccessible(true);
                rand.set(e, frand);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            HashMap<GT_Utility.ItemId, ItemStack> drops = new HashMap<>();
            HashMap<GT_Utility.ItemId, Integer> dropcount = new HashMap<>();
            HashMap<GT_Utility.ItemId, Integer> dropsrandomenchanted = new HashMap<>();
            HashMap<GT_Utility.ItemId, ItemStack> raredrops = new HashMap<>();
            HashMap<GT_Utility.ItemId, Integer> raredropcount = new HashMap<>();
            HashMap<GT_Utility.ItemId, Integer> raredropsrandomenchanted = new HashMap<>();
            Consumer<EntityItem> addDrop = (entityItem) -> {
                ItemStack ostack = entityItem.getEntityItem();
                if (ostack == null) return;
                ItemStack stack = ostack.copy();
                GT_Utility.ItemId itemId = GT_Utility.ItemId.createNoCopy(stack);
                drops.putIfAbsent(itemId, stack);
                dropcount.merge(itemId, stack.stackSize, Integer::sum);
                int i;
                if ((i = frand.randomenchantmentdetected.indexOf(ostack)) != -1) {
                    frand.randomenchantmentdetected.remove(i);
                    dropsrandomenchanted.put(itemId, frand.enchantabilityLevel.get(i));
                    frand.enchantabilityLevel.remove(i);
                }
            };
            Consumer<EntityItem> addDropRare = (entityItem) -> {
                ItemStack ostack = entityItem.getEntityItem();
                if (ostack == null) return;
                ItemStack stack = ostack.copy();
                GT_Utility.ItemId itemId = GT_Utility.ItemId.createNoCopy(stack);
                raredrops.putIfAbsent(itemId, stack);
                raredropcount.merge(itemId, stack.stackSize, Integer::sum);
                int i;
                if ((i = frand.randomenchantmentdetected.indexOf(ostack)) != -1) {
                    frand.randomenchantmentdetected.remove(i);
                    raredropsrandomenchanted.put(itemId, frand.enchantabilityLevel.get(i));
                    frand.enchantabilityLevel.remove(i);
                }
            };

            info("[Mob Handler]Generating normal drops");

            frand.maxbound = 1;
            frand.doescallnextbool = false;
            frand.nextbool = false;
            frand.overridenext = 0;

            try {
                dropFewItems.invoke(e, true, 0);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            e.capturedDrops.forEach(addDrop);
            if (frand.maxbound > 1 || frand.doescallnextbool) {
                e.capturedDrops.clear();
                for (int nb = 0; nb < (frand.doescallnextbool ? 2 : 1); nb++)
                    for (int i = 0; i < frand.maxbound; i++) {
                        if (nb == 0 && i == 0) continue; // already called
                        if (nb == 1 && i == 0) frand.maxbound = 1;
                        frand.nextbool = nb == 1;
                        frand.overridenext = i;
                        try {
                            dropFewItems.invoke(e, true, 0);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return;
                        }
                        e.capturedDrops.forEach(addDrop);
                        e.capturedDrops.clear();
                    }
            }

            double maxnormalchance = frand.maxbound * (frand.doescallnextbool ? 2 : 1);

            info("[Mob Handler]Generating rare drops");

            frand.maxbound = 1;
            frand.doescallnextbool = false;
            frand.nextbool = false;
            frand.overridenext = 0;

            try {
                dropRareDrop.invoke(e, 0);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            e.capturedDrops.forEach(addDropRare);
            if (!frand.randomenchantmentdetected.isEmpty()) {
                warn("[Mob Handler]Random enchantment detected but not emptied !!!");
                frand.randomenchantmentdetected.clear();
                frand.enchantabilityLevel.clear();
            }
            if (frand.maxbound > 1 || frand.doescallnextbool) {
                e.capturedDrops.clear();
                for (int nb = 0; nb < (frand.doescallnextbool ? 2 : 1); nb++)
                    for (int i = 0; i < frand.maxbound; i++) {
                        if (nb == 0 && i == 0) continue; // already called
                        if (nb == 1 && i == 0) frand.maxbound = 1;
                        frand.nextbool = nb == 1;
                        frand.overridenext = i;
                        try {
                            dropRareDrop.invoke(e, 0);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            return;
                        }
                        e.capturedDrops.forEach(addDropRare);
                        e.capturedDrops.clear();
                    }
            }

            double maxrarechance = frand.maxbound * (frand.doescallnextbool ? 2 : 1);

            if (drops.isEmpty() && raredrops.isEmpty()) {
                if (ModUtils.isClientSided) addNEIMobRecipe(e, new ArrayList<>());
                info("[Mob handler]Entity " + k + " doesn't drop any items, skipping EEC Recipe map");
                return;
            }

            List<MobDrop> moboutputs = new ArrayList<>();

            ItemStack[] outputs = new ItemStack[drops.size() + raredrops.size()];
            int[] outputchances = new int[drops.size() + raredrops.size()];
            int i = 0;
            for (Map.Entry<GT_Utility.ItemId, ItemStack> entry : drops.entrySet()) {
                GT_Utility.ItemId kk = entry.getKey();
                ItemStack vv = entry.getValue();
                outputs[i] = vv;
                outputchances[i] = (int) ((dropcount.get(kk).doubleValue() / maxnormalchance) * 10000);
                while (outputchances[i] > 10000) {
                    outputs[i].stackSize *= 2;
                    outputchances[i] /= 2;
                }

                moboutputs.add(new MobDrop(
                        outputs[i], MobDrop.DropType.Normal, outputchances[i], dropsrandomenchanted.get(kk)));
                i++;
            }
            for (Map.Entry<GT_Utility.ItemId, ItemStack> entry : raredrops.entrySet()) {
                GT_Utility.ItemId kk = entry.getKey();
                ItemStack vv = entry.getValue();
                outputs[i] = vv;
                outputchances[i] = (int) ((raredropcount.get(kk).doubleValue() / maxrarechance) * 250);
                while (outputchances[i] > 10000) {
                    outputs[i].stackSize *= 2;
                    outputchances[i] /= 2;
                }
                moboutputs.add(new MobDrop(
                        outputs[i], MobDrop.DropType.Rare, outputchances[i], raredropsrandomenchanted.get(kk)));
                i++;
            }

            if (ModUtils.isClientSided) addNEIMobRecipe(e, moboutputs);
            if (Loader.isModLoaded("EnderIO")) {
                ItemStack sSpawner = new ItemStack(EnderIO.blockPoweredSpawner, 0);
                NBTTagCompound nbt = new NBTTagCompound();
                BlockPoweredSpawner.writeMobTypeToNBT(nbt, k);
                sSpawner.setTagCompound(nbt);
                MobNameToRecipeMap.put(
                        k,
                        EECRecipeMap.addFakeRecipe(
                                true,
                                new ItemStack[] {sSpawner},
                                outputs,
                                null,
                                outputchances,
                                new FluidStack[0],
                                new FluidStack[0],
                                (int) e.getMaxHealth() * 2,
                                8000,
                                0));
            }
            info("[Mob Handler]Mapped " + k);
        });
    }
}
