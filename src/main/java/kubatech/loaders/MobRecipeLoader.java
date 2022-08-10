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
import java.util.function.BiConsumer;
import kubatech.Tags;
import kubatech.api.utils.ModUtils;
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

public class MobRecipeLoader {

    private static final Logger LOG = LogManager.getLogger(Tags.MODID + "[Mob Handler]");

    public static final MobRecipeLoader instance = new MobRecipeLoader();

    private static final HashSet<String> MobBlacklist = new HashSet<>(Arrays.asList(new String[] {"chisel.snowman"}));

    @SuppressWarnings("unused")
    @SubscribeEvent
    public void onOpenGui(GuiOpenEvent event) {
        MobRecipeLoader.generateMobRecipeMap();
        MinecraftForge.EVENT_BUS.unregister(instance);
    }

    private static final String dropFewItemsName = isDeobfuscatedEnvironment ? "dropFewItems" : "func_70628_a";
    private static final String dropRareDropName = isDeobfuscatedEnvironment ? "dropRareDrop" : "func_70600_l";
    private static final String setSlimeSizeName = isDeobfuscatedEnvironment ? "setSlimeSize" : "func_70799_a";
    private static final String randName = isDeobfuscatedEnvironment ? "rand" : "field_70146_Z";

    private static boolean alreadyGenerated = false;
    public static boolean isInGenerationProcess = false;

    public static class MobRecipe {
        public final ArrayList<MobDrop> mOutputs;
        public final int mEUt = 8000;
        public final int mDuration = 100;
        public final int mMaxDamageChance;

        public MobRecipe(ArrayList<MobDrop> outputs) {
            mOutputs = outputs;
            int maxdamagechance = 0;
            for (MobDrop o : mOutputs) if (o.damages != null) for (int v : o.damages.values()) maxdamagechance += v;
            mMaxDamageChance = maxdamagechance;
        }

        public ItemStack[] generateOutputs(Random rnd) {
            ArrayList<ItemStack> stacks = new ArrayList<>(mOutputs.size());
            for (MobDrop o : mOutputs)
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
            return stacks.toArray(new ItemStack[0]);
        }
    }

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

            private boolean next() {
                next++;
                return next >= bound;
            }
        }

        private final ArrayList<nexter> nexts = new ArrayList<>();
        private int walkCounter = 0;

        @Override
        public int nextInt(int bound) {
            if (nexts.size() <= walkCounter) { // new call
                nexts.add(new nexter(0, bound));
                walkCounter++;
                return 0;
            }
            return nexts.get(walkCounter++).getInt();
        }

        @Override
        public boolean nextBoolean() {
            if (nexts.size() <= walkCounter) { // new call
                nexts.add(new nexter(1, 2));
                walkCounter++;
                return false;
            }
            return nexts.get(walkCounter++).getBoolean();
        }

        public void newRound() {
            walkCounter = 0;
            nexts.clear();
        }

        public boolean nextRound() {
            walkCounter = 0;
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
        private int dropcount = 1;
        private final droplist owner;

        public dropinstance(ItemStack s, droplist owner) {
            this.owner = owner;
            stack = s;
            itemId = GT_Utility.ItemId.createNoCopy(stack);
        }

        public int getchance(int maxchance, int chancemodifier) {
            maxchance -= owner.rollsskipped;
            return (int) (((double) dropcount / (double) maxchance) * chancemodifier);
        }

        @Override
        public int hashCode() {
            return itemId.hashCode();
        }
    }

    public static class droplist {
        private final ArrayList<dropinstance> drops = new ArrayList<>();
        private final HashMap<GT_Utility.ItemId, Integer> dropschecker = new HashMap<>();
        public int rollsskipped = 0;

        public dropinstance add(dropinstance i) {
            if (contains(i)) {
                i = get(dropschecker.get(i.itemId));
                i.dropcount++;
                return i;
            }
            drops.add(i);
            dropschecker.put(i.itemId, drops.size() - 1);
            return i;
        }

        public dropinstance addIfAbsent(dropinstance i) {
            if (!contains(i)) return add(i);
            return get(i);
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

        isInGenerationProcess = true;

        LOG.info("Generating Recipe Map for Mob Handler and EEC");

        long time = System.currentTimeMillis();

        // Stupid MC code, I need to cast myself
        ((Map<String, Class<? extends Entity>>) EntityList.stringToClassMapping).forEach((k, v) -> {
            if (v == null) return;

            LOG.info("Generating entry for mob: " + k);

            if (Modifier.isAbstract(v.getModifiers())) {
                LOG.info("Entity " + k + " is abstract, skipping");
                return;
            }

            if (MobBlacklist.contains(k)) {
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

            droplist drops = new droplist();
            droplist raredrops = new droplist();

            BiConsumer<droplist, List<EntityItem>> addDrop = (fdrops, listToParse) -> {
                HashMap<GT_Utility.ItemId, Integer> damagableChecker = new HashMap<>();
                boolean haveDetectedDamageRandomizationInAny = false;
                for (EntityItem entityItem : listToParse) {
                    ItemStack ostack = entityItem.getEntityItem();
                    if (ostack == null) continue;
                    dropinstance drop;
                    boolean randomchomenchantdetected =
                            ostack.hasTagCompound() && ostack.stackTagCompound.hasKey("RandomEnchantmentDetected");
                    int randomenchantmentlevel = 0;
                    if (randomchomenchantdetected) {
                        randomenchantmentlevel = ostack.stackTagCompound.getInteger("RandomEnchantmentDetected");
                        ostack.stackTagCompound.removeTag("ench");
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
                            haveDetectedDamageRandomizationInAny = true;
                        } else ostack.setItemDamage(odamage);
                    }
                    drop = fdrops.add(new dropinstance(ostack.copy(), fdrops));
                    if (!drop.isEnchatmentRandomized && randomchomenchantdetected) {
                        drop.isEnchatmentRandomized = true;
                        drop.enchantmentLevel = randomenchantmentlevel;
                    }
                    if (drop.isDamageRandomized && !randomdamagedetected) {
                        drop.damagesPossible.merge(drop.stack.getItemDamage(), 1, Integer::sum);
                        drop.dropcount = 1;
                        fdrops.rollsskipped++;
                    }
                    if (randomdamagedetected || (haveDetectedDamageRandomizationInAny && drop.dropcount == 100)) {
                        if (!drop.isDamageRandomized) {
                            drop.isDamageRandomized = true;
                            drop.damagesPossible.merge(drop.stack.getItemDamage(), drop.dropcount - 1, Integer::sum);
                            fdrops.rollsskipped += drop.dropcount - 2;
                            for (int i = 0; i < fdrops.indexOf(drop); i++) {
                                dropinstance idrop = fdrops.get(i);
                                if (!idrop.isDamageRandomized
                                        && idrop.dropcount > 100) // I have to assume that it is damagable too
                                {
                                    idrop.isDamageRandomized = true;
                                    idrop.damagesPossible.merge(idrop.stack.getItemDamage(), 1, Integer::sum);
                                    fdrops.rollsskipped += idrop.dropcount - 1;
                                    idrop.dropcount = 1;
                                }
                            }
                        }
                        if (newdamage == -1) newdamage = drop.stack.getItemDamage();
                        drop.damagesPossible.merge(newdamage, 1, Integer::sum);
                        drop.dropcount = 1;
                        fdrops.rollsskipped++;
                    }
                }

                listToParse.clear();
            };

            LOG.info("Generating normal drops");

            frand.newRound();

            int timesrolled = 0;
            do {
                try {
                    dropFewItems.invoke(e, true, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                timesrolled++;
            } while (frand.nextRound());

            addDrop.accept(drops, e.capturedDrops);

            int maxnormalchance = timesrolled;

            LOG.info("Generating rare drops");

            frand.newRound();

            timesrolled = 0;

            do {
                try {
                    dropRareDrop.invoke(e, 0);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    return;
                }
                timesrolled++;
            } while (frand.nextRound());
            addDrop.accept(raredrops, e.capturedDrops);

            int maxrarechance = timesrolled;

            if (drops.isEmpty() && raredrops.isEmpty()) {
                if (ModUtils.isClientSided) addNEIMobRecipe(e, new ArrayList<>());
                LOG.info("Entity " + k + " doesn't drop any items, skipping EEC Recipe map");
                return;
            }

            ArrayList<MobDrop> moboutputs = new ArrayList<>();

            ItemStack[] outputs = new ItemStack[drops.size() + raredrops.size()];
            int[] outputchances = new int[drops.size() + raredrops.size()];
            int i = 0;
            for (dropinstance drop : drops.drops) {
                outputs[i] = drop.stack;
                outputchances[i] = drop.getchance(maxnormalchance, 10000);
                while (outputchances[i] > 10000) {
                    outputs[i].stackSize *= 2;
                    outputchances[i] /= 2;
                }
                moboutputs.add(new MobDrop(
                        outputs[i],
                        MobDrop.DropType.Normal,
                        outputchances[i],
                        drop.isEnchatmentRandomized ? drop.enchantmentLevel : null,
                        drop.isDamageRandomized ? drop.damagesPossible : null));
                i++;
            }
            for (dropinstance drop : raredrops.drops) {
                outputs[i] = drop.stack;
                outputchances[i] = drop.getchance(maxrarechance, 250);
                while (outputchances[i] > 10000) {
                    outputs[i].stackSize *= 2;
                    outputchances[i] /= 2;
                }
                moboutputs.add(new MobDrop(
                        outputs[i],
                        MobDrop.DropType.Rare,
                        outputchances[i],
                        drop.isEnchatmentRandomized ? drop.enchantmentLevel : null,
                        drop.isDamageRandomized ? drop.damagesPossible : null));
                i++;
            }

            if (ModUtils.isClientSided) addNEIMobRecipe(e, moboutputs);
            if (Loader.isModLoaded("EnderIO")) {
                ItemStack sSpawner = new ItemStack(EnderIO.blockPoweredSpawner, 0);
                NBTTagCompound nbt = new NBTTagCompound();
                BlockPoweredSpawner.writeMobTypeToNBT(nbt, k);
                sSpawner.setTagCompound(nbt);
                MobNameToRecipeMap.put(k, new MobRecipe(moboutputs));
            }
            LOG.info("Mapped " + k);
        });

        time -= System.currentTimeMillis();
        time = -time;
        LOG.info("Recipe map generated ! It took " + time + "ms");

        isInGenerationProcess = false;
    }
}
