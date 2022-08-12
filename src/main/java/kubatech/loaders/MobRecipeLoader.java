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

package kubatech.loaders;

import static kubatech.api.utils.ModUtils.isDeobfuscatedEnvironment;
import static kubatech.common.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeExterminationChamber.MobNameToRecipeMap;

import atomicstryker.infernalmobs.common.InfernalMobsCore;
import atomicstryker.infernalmobs.common.MobModifier;
import atomicstryker.infernalmobs.common.mods.api.ModifierLoader;
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
import kubatech.Config;
import kubatech.Tags;
import kubatech.api.utils.InfernalHelper;
import kubatech.api.utils.ModUtils;
import kubatech.common.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeExterminationChamber;
import kubatech.nei.Mob_Handler;
import net.minecraft.enchantment.EnchantmentHelper;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import thaumcraft.common.items.wands.ItemWandCasting;

public class MobRecipeLoader {

    private static final Logger LOG = LogManager.getLogger(Tags.MODID + "[Mob Handler]");

    public static final MobRecipeLoader instance = new MobRecipeLoader();

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onOpenGui(GuiOpenEvent event) {
        MobRecipeLoader.generateMobRecipeMap();
        MinecraftForge.EVENT_BUS.unregister(instance);
    }

    private static final String dropFewItemsName = isDeobfuscatedEnvironment ? "dropFewItems" : "func_70628_a";
    private static final String dropRareDropName = isDeobfuscatedEnvironment ? "dropRareDrop" : "func_70600_l";
    private static final String setSlimeSizeName = isDeobfuscatedEnvironment ? "setSlimeSize" : "func_70799_a";
    private static final String addRandomArmorName = isDeobfuscatedEnvironment ? "addRandomArmor" : "func_82164_bB";
    private static final String enchantEquipmentName = isDeobfuscatedEnvironment ? "enchantEquipment" : "func_82162_bC";
    private static final String randName = isDeobfuscatedEnvironment ? "rand" : "field_70146_Z";

    private static boolean alreadyGenerated = false;
    public static boolean isInGenerationProcess = false;
    public static final String randomEnchantmentDetectedString = "RandomEnchantmentDetected";

    public static class MobRecipe {
        public final ArrayList<MobDrop> mOutputs;
        public final int mEUt = 2000;
        public final int mDuration;
        public final int mMaxDamageChance;
        public final boolean infernalityAllowed;
        public final boolean alwaysinfernal;
        public static droplist infernaldrops;

        public MobRecipe(EntityLiving e, ArrayList<MobDrop> outputs) {
            if (infernaldrops == null && Loader.isModLoaded("InfernalMobs")) {
                infernaldrops = new droplist();
                LOG.info("Generating Infernal drops");
                ArrayList<ModifierLoader<?>> modifierLoaders = (ArrayList<ModifierLoader<?>>)
                        InfernalHelper.getModifierLoaders().clone();
                int i = 0;
                for (ModifierLoader<?> modifierLoader : modifierLoaders) {
                    MobModifier nextMod = modifierLoader.make(null);
                    if (nextMod.getBlackListMobClasses() != null)
                        for (Class<?> cl : nextMod.getBlackListMobClasses())
                            if (e.getClass().isAssignableFrom(cl)) break;
                    i++;
                }
                if (i > 0) {
                    double chance =
                            InfernalHelper.checkEntityClassForced(e) ? 1d : (1d / InfernalHelper.getEliteRarity());
                    ArrayList<ItemStack> elitelist = InfernalHelper.getDropIdListElite();
                    for (ItemStack stack : elitelist) {
                        dropinstance instance = infernaldrops.add(
                                new dropinstance(stack.copy(), infernaldrops), chance / elitelist.size());
                        instance.isEnchatmentRandomized = true;
                        instance.enchantmentLevel = stack.getItem().getItemEnchantability();
                    }
                    ArrayList<ItemStack> ultralist = InfernalHelper.getDropIdListUltra();
                    chance *= 1d / InfernalHelper.getUltraRarity();
                    for (ItemStack stack : ultralist) {
                        dropinstance instance = infernaldrops.add(
                                new dropinstance(stack.copy(), infernaldrops), chance / ultralist.size());
                        instance.isEnchatmentRandomized = true;
                        instance.enchantmentLevel = stack.getItem().getItemEnchantability();
                    }
                    ArrayList<ItemStack> infernallist = InfernalHelper.getDropIdListInfernal();
                    chance *= 1d / InfernalHelper.getInfernoRarity();
                    for (ItemStack stack : infernallist) {
                        dropinstance instance = infernaldrops.add(
                                new dropinstance(stack.copy(), infernaldrops), chance / infernallist.size());
                        instance.isEnchatmentRandomized = true;
                        instance.enchantmentLevel = stack.getItem().getItemEnchantability();
                    }
                }
            } else if (infernaldrops == null) infernaldrops = new droplist();

            infernalityAllowed = InfernalHelper.isClassAllowed(e);
            alwaysinfernal = InfernalHelper.checkEntityClassForced(e);

            mOutputs = outputs;
            int maxdamagechance = 0;
            for (MobDrop o : mOutputs) {
                if (o.damages != null) for (int v : o.damages.values()) maxdamagechance += v;
            }
            mMaxDamageChance = maxdamagechance;
            // Powered spawner with octadic capacitor spawns ~22/min ~= 0.366/sec ~= 2.72s/spawn ~= 54.54t/spawn
            mDuration = 55 + 10 + (((int) e.getMaxHealth() / 5) * 10);
        }

        public ItemStack[] generateOutputs(Random rnd, GT_MetaTileEntity_ExtremeExterminationChamber MTE) {
            MTE.mEUt = mEUt;
            MTE.mMaxProgresstime = mDuration;
            ArrayList<ItemStack> stacks = new ArrayList<>(mOutputs.size());
            for (MobDrop o : mOutputs) {
                if (o.chance == 10000 || rnd.nextInt(10000) < o.chance) {
                    ItemStack s = o.stack.copy();
                    if (o.enchantable != null) EnchantmentHelper.addRandomEnchantment(rnd, s, o.enchantable);
                    if (o.damages != null) {
                        int rChance = rnd.nextInt(mMaxDamageChance);
                        int cChance = 0;
                        for (Map.Entry<Integer, Integer> damage : o.damages.entrySet()) {
                            cChance += damage.getValue();
                            if (rChance <= cChance) {
                                s.setItemDamage(damage.getKey());
                                break;
                            }
                        }
                    }
                    stacks.add(s);
                }
            }

            if (infernalityAllowed
                    && mEUt * 8 < MTE.getMaxInputVoltage()
                    && !InfernalHelper.getDimensionBlackList()
                            .contains(MTE.getBaseMetaTileEntity().getWorld().provider.dimensionId)) {
                int p = 0;
                int mods = 0;
                if (alwaysinfernal || rnd.nextInt(InfernalHelper.getEliteRarity()) == 0) {
                    p = 1;
                    if (rnd.nextInt(InfernalHelper.getUltraRarity()) == 0) {
                        p = 2;
                        if (rnd.nextInt(InfernalHelper.getInfernoRarity()) == 0) p = 3;
                    }
                }
                ArrayList<ItemStack> infernalstacks = null;
                if (p > 0)
                    if (p == 1) {
                        infernalstacks = InfernalHelper.getDropIdListElite();
                        mods = InfernalHelper.getMinEliteModifiers();
                    } else if (p == 2) {
                        infernalstacks = InfernalHelper.getDropIdListUltra();
                        mods = InfernalHelper.getMinUltraModifiers();
                    } else if (p == 3) {
                        infernalstacks = InfernalHelper.getDropIdListInfernal();
                        mods = InfernalHelper.getMinInfernoModifiers();
                    }
                if (infernalstacks != null) {
                    ItemStack infernalstack = infernalstacks
                            .get(rnd.nextInt(infernalstacks.size()))
                            .copy();
                    EnchantmentHelper.addRandomEnchantment(
                            rnd, infernalstack, infernalstack.getItem().getItemEnchantability());
                    stacks.add(infernalstack);
                    MTE.mEUt *= 8;
                    MTE.mMaxProgresstime *= mods * InfernalMobsCore.instance().getMobModHealthFactor();
                }
            }

            return stacks.toArray(new ItemStack[0]);
        }
    }

    public static class MobDrop {
        public enum DropType {
            Normal,
            Rare,
            Additional,
            Infernal
        }

        public ItemStack stack;
        public DropType type;
        public int chance;
        public Integer enchantable;
        public HashMap<Integer, Integer> damages;

        public MobDrop(
                ItemStack stack, DropType type, int chance, Integer enchantable, HashMap<Integer, Integer> damages) {
            this.stack = stack;
            this.type = type;
            this.chance = chance;
            this.enchantable = enchantable;
            this.damages = damages;
        }
    }

    @SideOnly(Side.CLIENT)
    public static void addNEIMobRecipe(EntityLiving e, List<MobDrop> drop) {
        Mob_Handler.addRecipe(e, drop);
    }

    public static class fakeRand extends Random {
        private static class nexter {
            private final int type;
            private final int bound;
            private int next;

            public nexter(int type, int bound) {
                this.next = 0;
                this.bound = bound;
                this.type = type;
            }

            private int getType() {
                return type;
            }

            private boolean getBoolean() {
                return next == 1;
            }

            private int getInt() {
                return next;
            }

            private float getFloat() {
                return next * 0.1f;
            }

            private boolean next() {
                next++;
                return next >= bound;
            }
        }

        private final ArrayList<nexter> nexts = new ArrayList<>();
        private int walkCounter = 0;
        private double chance;

        @Override
        public int nextInt(int bound) {
            if (nexts.size() <= walkCounter) { // new call
                nexts.add(new nexter(0, bound));
                walkCounter++;
                chance /= bound;
                return 0;
            }
            chance /= bound;
            return nexts.get(walkCounter++).getInt();
        }

        @Override
        public float nextFloat() {
            if (nexts.size() <= walkCounter) { // new call
                nexts.add(new nexter(2, 10));
                walkCounter++;
                chance /= 10;
                return 0f;
            }
            chance /= 10;
            return nexts.get(walkCounter++).getFloat();
        }

        @Override
        public boolean nextBoolean() {
            if (nexts.size() <= walkCounter) { // new call
                nexts.add(new nexter(1, 2));
                walkCounter++;
                chance /= 2;
                return false;
            }
            chance /= 2;
            return nexts.get(walkCounter++).getBoolean();
        }

        public void newRound() {
            walkCounter = 0;
            nexts.clear();
            chance = 1d;
        }

        public boolean nextRound() {
            walkCounter = 0;
            chance = 1d;
            while (nexts.size() > 0 && nexts.get(nexts.size() - 1).next()) nexts.remove(nexts.size() - 1);
            return nexts.size() > 0;
        }
    }

    private static class dropinstance {
        public boolean isDamageRandomized = false;
        public HashMap<Integer, Integer> damagesPossible = new HashMap<>();
        public boolean isEnchatmentRandomized = false;
        public int enchantmentLevel = 0;
        public final ItemStack stack;
        public final GT_Utility.ItemId itemId;
        private double dropchance = 0d;
        private int dropcount = 1;
        private final droplist owner;

        public dropinstance(ItemStack s, droplist owner) {
            this.owner = owner;
            stack = s;
            itemId = GT_Utility.ItemId.createNoCopy(stack);
        }

        public int getchance(int chancemodifier) {
            dropchance = (double) Math.round(dropchance * 100000) / 100000d;
            return (int) (dropchance * chancemodifier);
        }

        @Override
        public int hashCode() {
            return itemId.hashCode();
        }
    }

    public static class droplist {
        private final ArrayList<dropinstance> drops = new ArrayList<>();
        private final HashMap<GT_Utility.ItemId, Integer> dropschecker = new HashMap<>();

        public dropinstance add(dropinstance i, double chance) {
            if (contains(i)) {
                int ssize = i.stack.stackSize;
                i = get(dropschecker.get(i.itemId));
                i.dropchance += chance * ssize;
                i.dropcount += ssize;
                return i;
            }
            drops.add(i);
            i.dropchance += chance * i.stack.stackSize;
            i.dropcount += i.stack.stackSize - 1;
            i.stack.stackSize = 1;
            dropschecker.put(i.itemId, drops.size() - 1);
            return i;
        }

        public dropinstance get(int index) {
            return drops.get(index);
        }

        public dropinstance get(dropinstance i) {
            if (!contains(i)) return null;
            return get(dropschecker.get(i.itemId));
        }

        public boolean contains(dropinstance i) {
            return dropschecker.containsKey(i.itemId);
        }

        public boolean contains(ItemStack stack) {
            return dropschecker.containsKey(GT_Utility.ItemId.createNoCopy(stack));
        }

        public boolean isEmpty() {
            return drops.isEmpty();
        }

        public int size() {
            return drops.size();
        }

        public int indexOf(dropinstance i) {
            if (!contains(i)) return -1;
            return dropschecker.get(i.itemId);
        }
    }

    private static class dropCollector {
        HashMap<GT_Utility.ItemId, Integer> damagableChecker = new HashMap<>();

        public void addDrop(droplist fdrops, ArrayList<EntityItem> listToParse, double chance) {
            for (EntityItem entityItem : listToParse) {
                ItemStack ostack = entityItem.getEntityItem();
                if (ostack == null) continue;
                dropinstance drop;
                boolean randomchomenchantdetected =
                        ostack.hasTagCompound() && ostack.stackTagCompound.hasKey(randomEnchantmentDetectedString);
                int randomenchantmentlevel = 0;
                if (randomchomenchantdetected) {
                    randomenchantmentlevel = ostack.stackTagCompound.getInteger(randomEnchantmentDetectedString);
                    ostack.stackTagCompound.removeTag("ench");
                    ostack.stackTagCompound.removeTag(randomEnchantmentDetectedString);
                }
                boolean randomdamagedetected = false;
                int newdamage = -1;
                if (ostack.isItemStackDamageable()) {
                    int odamage = ostack.getItemDamage();
                    ostack.setItemDamage(1);
                    GT_Utility.ItemId id = GT_Utility.ItemId.createNoCopy(ostack);
                    damagableChecker.putIfAbsent(id, odamage);
                    int check = damagableChecker.get(id);
                    if (check != odamage) {
                        randomdamagedetected = true;
                        newdamage = odamage;
                        ostack.setItemDamage(check);
                    } else ostack.setItemDamage(odamage);
                }
                drop = fdrops.add(new dropinstance(ostack.copy(), fdrops), chance);
                if (!drop.isEnchatmentRandomized && randomchomenchantdetected) {
                    drop.isEnchatmentRandomized = true;
                    drop.enchantmentLevel = randomenchantmentlevel;
                }
                if (drop.isDamageRandomized && !randomdamagedetected) {
                    drop.damagesPossible.merge(drop.stack.getItemDamage(), 1, Integer::sum);
                }
                if (randomdamagedetected) {
                    if (!drop.isDamageRandomized) {
                        drop.isDamageRandomized = true;
                        drop.damagesPossible.merge(drop.stack.getItemDamage(), drop.dropcount - 1, Integer::sum);
                    }
                    if (newdamage == -1) newdamage = drop.stack.getItemDamage();
                    drop.damagesPossible.merge(newdamage, 1, Integer::sum);
                }
            }

            listToParse.clear();
        }

        public void newRound() {
            damagableChecker.clear();
        }
    }

    @SuppressWarnings("unchecked")
    public static void generateMobRecipeMap() {

        if (alreadyGenerated) return;
        alreadyGenerated = true;
        if (!Config.mobHandlerEnabled) return;

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

        isInGenerationProcess = true;

        LOG.info("Generating Recipe Map for Mob Handler and EEC");

        long time = System.currentTimeMillis();

        Method setSlimeSize;
        Method dropFewItems;
        Method dropRareDrop;
        Method addRandomArmor;
        Method enchantEquipment;
        Field rand;

        try {
            setSlimeSize = EntitySlime.class.getDeclaredMethod(setSlimeSizeName, int.class);
            setSlimeSize.setAccessible(true);
            dropFewItems = EntityLivingBase.class.getDeclaredMethod(dropFewItemsName, boolean.class, int.class);
            dropFewItems.setAccessible(true);
            dropRareDrop = EntityLivingBase.class.getDeclaredMethod(dropRareDropName, int.class);
            dropRareDrop.setAccessible(true);
            addRandomArmor = EntityLiving.class.getDeclaredMethod(addRandomArmorName);
            addRandomArmor.setAccessible(true);
            enchantEquipment = EntityLiving.class.getDeclaredMethod(enchantEquipmentName);
            enchantEquipment.setAccessible(true);
            rand = Entity.class.getDeclaredField(randName);
            rand.setAccessible(true);
        } catch (Exception ex) {
            LOG.error("Failed to obtain methods");
            isInGenerationProcess = false;
            return;
        }

        dropCollector collector = new dropCollector();

        // Stupid MC code, I need to cast myself
        ((Map<String, Class<? extends Entity>>) EntityList.stringToClassMapping).forEach((k, v) -> {
            if (v == null) return;

            LOG.info("Generating entry for mob: " + k);

            if (Modifier.isAbstract(v.getModifiers())) {
                LOG.info("Entity " + k + " is abstract, skipping");
                return;
            }

            if (Arrays.asList(Config.mobBlacklist).contains(k)) {
                LOG.info("Entity " + k + " is blacklisted, skipping");
                return;
            }

            EntityLiving e;
            try {
                e = (EntityLiving) v.getConstructor(new Class[] {World.class}).newInstance(new Object[] {f});
            } catch (ClassCastException ex) {
                // not a EntityLiving
                LOG.info("Entity " + k + " is not a LivingEntity, skipping");
                return;
            } catch (NoSuchMethodException ex) {
                // No constructor ?
                LOG.info("Entity " + k + " doesn't have constructor, skipping");
                return;
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            if (StatCollector.translateToLocal("entity." + k + ".name").equals("entity." + k + ".name")) {
                LOG.info("Entity " + k + " does't have localized name, skipping");
                return;
            }

            e.captureDrops = true;

            // POWERFULL GENERATION

            if (e instanceof EntitySlime)
                try {
                    setSlimeSize.invoke(e, 1);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }

            try {
                rand.set(e, frand);
            } catch (Exception ex) {
                ex.printStackTrace();
                return;
            }

            droplist drops = new droplist();
            droplist raredrops = new droplist();
            droplist additionaldrops = new droplist();

            LOG.info("Generating normal drops");

            frand.newRound();
            collector.newRound();

            do {
                try {
                    dropFewItems.invoke(e, true, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                collector.addDrop(drops, e.capturedDrops, frand.chance);

            } while (frand.nextRound());

            LOG.info("Generating rare drops");

            frand.newRound();
            collector.newRound();

            do {
                try {
                    dropRareDrop.invoke(e, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                collector.addDrop(raredrops, e.capturedDrops, frand.chance);
            } while (frand.nextRound());

            LOG.info("Generating additional drops");

            frand.newRound();
            collector.newRound();

            try {
                Class<?> cl = e.getClass();
                boolean detectedException = false;
                do {
                    detectedException = false;
                    try {
                        cl.getDeclaredMethod(addRandomArmorName);
                    } catch (Exception ex) {
                        detectedException = true;
                        cl = cl.getSuperclass();
                    }
                } while (detectedException && !cl.equals(Entity.class));
                if (cl.equals(EntityLiving.class) || cl.equals(Entity.class)) throw new Exception();
                do {
                    addRandomArmor.invoke(e);
                    enchantEquipment.invoke(e);
                    for (ItemStack stack : e.getLastActiveItems()) {
                        if (stack != null) {
                            if (stack.getItem() instanceof ItemWandCasting)
                                continue; // crashes the game when rendering in GUI

                            int randomenchant = -1;
                            if (stack.hasTagCompound()
                                    && stack.stackTagCompound.hasKey(randomEnchantmentDetectedString)) {
                                randomenchant = stack.stackTagCompound.getInteger(randomEnchantmentDetectedString);
                                stack.stackTagCompound.removeTag("ench");
                                stack.stackTagCompound.removeTag(randomEnchantmentDetectedString);
                            }
                            dropinstance i =
                                    additionaldrops.add(new dropinstance(stack, additionaldrops), frand.chance);
                            if (!i.isDamageRandomized && i.stack.isItemStackDamageable()) {
                                i.isDamageRandomized = true;
                                int maxdamage = i.stack.getMaxDamage();
                                int max = Math.max(maxdamage - 25, 1);
                                for (int d = Math.min(max, 25); d <= max; d++) i.damagesPossible.put(d, 1);
                            }
                            if (!i.isEnchatmentRandomized && randomenchant != -1) {
                                i.isEnchatmentRandomized = true;
                                i.enchantmentLevel = randomenchant;
                            }
                        }
                    }
                    Arrays.fill(e.getLastActiveItems(), null);
                } while (frand.nextRound());
            } catch (Exception ignored) {
            }

            frand.newRound();
            collector.newRound();

            if (drops.isEmpty() && raredrops.isEmpty() && additionaldrops.isEmpty()) {
                if (ModUtils.isClientSided && Config.includeEmptyMobs) addNEIMobRecipe(e, new ArrayList<>());
                LOG.info("Entity " + k + " doesn't drop any items, skipping EEC Recipe map");
                return;
            }

            ArrayList<MobDrop> moboutputs = new ArrayList<>(drops.size() + raredrops.size() + additionaldrops.size());

            for (dropinstance drop : drops.drops) {
                ItemStack stack = drop.stack;
                int chance = drop.getchance(10000);
                while (chance > 10000) {
                    stack.stackSize *= 2;
                    chance /= 2;
                }
                moboutputs.add(new MobDrop(
                        stack,
                        MobDrop.DropType.Normal,
                        chance,
                        drop.isEnchatmentRandomized ? drop.enchantmentLevel : null,
                        drop.isDamageRandomized ? drop.damagesPossible : null));
            }
            for (dropinstance drop : raredrops.drops) {
                ItemStack stack = drop.stack;
                int chance = drop.getchance(250);
                while (chance > 10000) {
                    stack.stackSize *= 2;
                    chance /= 2;
                }
                moboutputs.add(new MobDrop(
                        stack,
                        MobDrop.DropType.Rare,
                        chance,
                        drop.isEnchatmentRandomized ? drop.enchantmentLevel : null,
                        drop.isDamageRandomized ? drop.damagesPossible : null));
            }
            for (dropinstance drop : additionaldrops.drops) {
                ItemStack stack = drop.stack;
                int chance = drop.getchance(850);
                while (chance > 10000) {
                    stack.stackSize *= 2;
                    chance /= 2;
                }
                moboutputs.add(new MobDrop(
                        stack,
                        MobDrop.DropType.Additional,
                        chance,
                        drop.isEnchatmentRandomized ? drop.enchantmentLevel : null,
                        drop.isDamageRandomized ? drop.damagesPossible : null));
            }

            if (ModUtils.isClientSided) addNEIMobRecipe(e, moboutputs);
            if (Loader.isModLoaded("EnderIO")) {
                ItemStack sSpawner = new ItemStack(EnderIO.blockPoweredSpawner, 0);
                NBTTagCompound nbt = new NBTTagCompound();
                BlockPoweredSpawner.writeMobTypeToNBT(nbt, k);
                sSpawner.setTagCompound(nbt);
                MobNameToRecipeMap.put(k, new MobRecipe(e, moboutputs));
            }
            LOG.info("Mapped " + k);
        });

        time -= System.currentTimeMillis();
        time = -time;
        LOG.info("Recipe map generated ! It took " + time + "ms");

        isInGenerationProcess = false;
    }
}
