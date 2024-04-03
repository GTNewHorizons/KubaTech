package kubatech.api.implementations;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import gregtech.api.GregTech_API;
import gregtech.api.enums.ItemList;
import gregtech.common.blocks.GT_Block_Ores_Abstract;
import gregtech.common.blocks.GT_Item_Ores;
import gregtech.common.blocks.GT_TileEntity_Ores;
import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.core.Ic2Items;
import ic2.core.crop.IC2Crops;
import ic2.core.crop.TileEntityCrop;
import kubatech.api.eig.EIGBucket;
import kubatech.api.eig.EIGDropTable;
import kubatech.api.eig.IEIGBucketFactory;
import kubatech.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeIndustrialGreenhouse;

public class EIGIC2Bucket extends EIGBucket {

    public final static IEIGBucketFactory factory = new EIGIC2Bucket.Factory();
    private static final String NBT_IDENTIFIER = "IC2";
    private static final int REVISION_NUMBER = 0;

    // region crop simulation variables

    private final static int NUMBER_OF_DROPS_TO_SIMULATE = 100;
    // nutrient factors
    /**
     * Set to true if you want to assume the crop is on wet farmland for a +2 bonus to nutrients
     */
    private static final boolean IS_ON_WET_FARMLAND = true;
    /**
     * The amount of water stored in the crop stick when hydration is turned on.
     */
    private static final int WATER_STORAGE_VALUE = Math.max(0, Math.max(200, 200));
    // nutrient factors
    /**
     * The number of blocks of dirt we assume are under. Subtract 1 if we have a block under our crop.
     */
    private static final int NUMBER_OF_DIRT_BLOCKS_UNDER = Math.max(0, Math.max(3, 0));
    /**
     * The amount of fertilizer stored in the crop stick
     */
    private static final int FERTILIZER_STORAGE_VALUE = Math.max(0, Math.max(200, 0));
    // air quality factors
    /**
     * How many blocks in a 3x3 area centered on the crop do not contain solid blocks or other crops.
     * Max value is 8 because the crop always counts itself.
     */
    private static final int CROP_OBSTRUCTION_VALUE = Math.max(0, Math.max(8, 9 - 4));
    /**
     * Being able to see the sky gives a +2 bonus to the air quality
     */
    private static final boolean CROP_CAN_SEE_SKY = false;

    // endregion crop simulation variables

    public static class Factory implements IEIGBucketFactory {

        @Override
        public String getNBTIdentifier() {
            return NBT_IDENTIFIER;
        }

        @Override
        public EIGBucket tryCreateBucket(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse, ItemStack input,
                                         int maxConsume) {
            // Check if input is a seed.
            if (!ItemList.IC2_Crop_Seeds.isStackEqual(input, true, true)) return null;
            if (!input.hasTagCompound()) return null;
            // Validate that stat nbt data exists.
            NBTTagCompound nbt = input.getTagCompound();
            if (!(nbt.hasKey("growth") && nbt.hasKey("gain") && nbt.hasKey("resistance"))) return null;

            CropCard cc = IC2Crops.instance.getCropCard(input);
            if (cc == null) return null;

            ItemStack singleSeed = input.copy();
            singleSeed.stackSize = 1;
            EIGIC2Bucket bucket = new EIGIC2Bucket(greenhouse, input);
            if (!bucket.isValid()) return null;
            // consume the seed
            input.stackSize--;
            // try adding the rest of the stack ot the bucket up to the max it can add.
            bucket.tryAddSeed(greenhouse, input, maxConsume - 1);
            return bucket;
        }

        @Override
        public EIGBucket restore(NBTTagCompound nbt) {
            return new EIGIC2Bucket(nbt);
        }
    }

    private EIGDropTable drops;
    private int growthTime;
    private boolean isValid = false;

    private EIGIC2Bucket(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse, ItemStack seed) {
        super(seed, 1, null);
        this.recalculateDrops(greenhouse);
    }

    private EIGIC2Bucket(NBTTagCompound nbt) {
        super(nbt);
        this.drops = new EIGDropTable(nbt, "drops");
        this.growthTime = nbt.getInteger("growthTime");
        this.isValid = nbt.getInteger("version") == REVISION_NUMBER;
    }

    @Override
    public NBTTagCompound save() {
        NBTTagCompound nbt = super.save();
        nbt.setTag("drops", this.drops.save());
        nbt.setInteger("growthTime", this.growthTime);
        nbt.setInteger("version", REVISION_NUMBER);
        return nbt;
    }

    @Override
    protected String getNBTIdentifier() {
        return NBT_IDENTIFIER;
    }

    @Override
    public void addProgress(double timeDelta, EIGDropTable tracker) {
        // abort early if slot is invalid
        if (!this.isValid) return;
        // else apply drops to tracker
        double growthPercent = timeDelta / (double) this.growthTime;
        if (this.drops != null) {
            this.drops.addTo(tracker, this.seedCount * growthPercent);
        }
    }

    @Override
    public boolean revalidate(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse) {
        this.recalculateDrops(greenhouse);
        return this.isValid();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && this.isValid;
    }

    /**
     * (Re-)calculates the pre-generated drop table for this bucket.
     *
     * @param greenhouse The {@link GT_MetaTileEntity_ExtremeIndustrialGreenhouse} that contains this bucket.
     */
    public void recalculateDrops(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse) {
        this.isValid = false;
        World world = greenhouse.getBaseMetaTileEntity()
            .getWorld();
        CropCard cc = Crops.instance.getCropCard(this.seed);
        NBTTagCompound nbt = seed.getTagCompound();
        byte gr = nbt.getByte("growth");
        byte ga = nbt.getByte("gain");
        byte re = nbt.getByte("resistance");
        int[] abc = new int[] { 0, -2, 3 };
        int[] xyz = new int[] { 0, 0, 0 };
        greenhouse.getExtendedFacing()
            .getWorldOffset(abc, xyz);
        xyz[0] += greenhouse.getBaseMetaTileEntity()
            .getXCoord();
        xyz[1] += greenhouse.getBaseMetaTileEntity()
            .getYCoord();
        xyz[2] += greenhouse.getBaseMetaTileEntity()
            .getZCoord();
        boolean cheating = false;
        try {
            if (world.getBlock(xyz[0], xyz[1] - 2, xyz[2]) != GregTech_API.sBlockCasings4
                || world.getBlockMetadata(xyz[0], xyz[1] - 2, xyz[2]) != 1) {
                // no
                cheating = true;
                return;
            }

            // TODO: get rid of the need for the placing the crop block, rework already on the way, just not for now.
            // We temporarily add a crop stick into the world to see if it grows.
            world.setBlock(xyz[0], xyz[1], xyz[2], Block.getBlockFromItem(Ic2Items.crop.getItem()), 0, 0);
            TileEntity wte = world.getTileEntity(xyz[0], xyz[1], xyz[2]);
            if (!(wte instanceof TileEntityCrop)) {
                // should not be even possible
                return;
            }

            TileEntityCrop crop = (TileEntityCrop) wte;
            crop.ticker = 1; // don't even think about ticking once
            crop.setCrop(cc);

            crop.setGrowth(gr);
            crop.setGain(ga);
            crop.setResistance(re);

            // Calculate the environmental stats using info obtained though decompiling IC2.
            crop.waterStorage = greenhouse.isInNoHumidityMode() ? 0 : WATER_STORAGE_VALUE;
            crop.humidity = getHumidity(greenhouse);
            crop.nutrientStorage = FERTILIZER_STORAGE_VALUE;
            crop.nutrients = getNutrients(greenhouse);
            crop.airQuality = getAirQuality(greenhouse);

            // Check if we can put the current block under the soil.
            if (this.supportItems != null && this.supportItems.length == 1
                && this.supportItems[0] != null
                && !setBlock(this.supportItems[0], xyz[0], xyz[1] - 2, xyz[2], world)) return;
            else crop.nutrients = getUpdateNutrientsForBlockUnder(crop, cc);

            // Check if we can grow
            crop.setSize((byte) (cc.maxSize() - 1));
            // Check if the crop has a chance to die in the current environment
            if (calcAvgGrowthRate(crop, cc, 0) < 0) return;
            // Check if the crop has a chance to grow in the current environment.
            if (calcAvgGrowthRate(crop, cc, 6) <= 0) return;

            ItemStack blockInputStackToConsume = null;
            if (!cc.canGrow(crop)) {
                // If the block we have in storage no longer functions, we are no longer valid, the seed and block
                // should be ejected if possible.
                if (this.supportItems != null) return;
                // assume we need a block under the farmland/fertilized dirt and update nutrients accordingly
                crop.nutrients = getUpdateNutrientsForBlockUnder(crop, cc);
                // Try to find the needed block in the inputs
                boolean canGrow = false;
                ArrayList<ItemStack> inputs = greenhouse.getStoredInputs();
                for (ItemStack potentialBlock : inputs) {
                    // if the input can't be placed in the world skip to the next input
                    if (potentialBlock == null || potentialBlock.stackSize <= 0) continue;
                    if (!setBlock(potentialBlock, xyz[0], xyz[1] - 2, xyz[2], world)) continue;
                    // check if the crop can grow with the block under it.
                    if (!cc.canGrow(crop)) continue;
                    // If we don't have enough blocks to consume, abort.
                    if (this.seedCount > potentialBlock.stackSize) return;
                    canGrow = true;
                    blockInputStackToConsume = potentialBlock;
                    // Don't consume the block just yet, we do that once everything is valid.
                    ItemStack newSupport = potentialBlock.copy();
                    newSupport.stackSize = 1;
                    this.supportItems = new ItemStack[] { newSupport };
                    break;
                }

                if (!canGrow) return;
            }

            crop.setSize((byte) (cc.maxSize()));
            if (!cc.canBeHarvested(crop)) return;

            // PRE GENERATE DROP CHANCES
            // TODO: Create better loot table handling for crops like red wheat, stonelilies, mana beans, magic metal
            // berries, etc.
            this.drops = new EIGDropTable();
            int sizeAfterHarvestTotal = 0;
            // Multiply drop sizes by the average number drop rounds per harvest.
            double avgDropRounds = getRealAverageDropRounds(crop, cc);
            double avgStackIncrease = getRealAverageDropIncrease(crop, cc);
            for (int i = 0; i < NUMBER_OF_DROPS_TO_SIMULATE; i++) {
                // The size after harvest is either random or constant, there is no way to know, so we have average
                crop.setSize((byte) cc.maxSize()); // this is JIC, it can likely be removed
                sizeAfterHarvestTotal += cc.getSizeAfterHarvest(crop);
                // try generating some loot drop
                ItemStack drop = cc.getGain(crop);
                if (drop == null || drop.stackSize <= 0) continue;

                // Merge the new drop with the current loot table.
                double avgAmount = (drop.stackSize + avgStackIncrease) * avgDropRounds;
                this.drops.addDrop(drop, avgAmount / NUMBER_OF_DROPS_TO_SIMULATE);
            }
            if (this.drops.isEmpty()) return;

            // CALC GROWTH RATE
            // TODO: Double terra wart and nether wart growth speed if using the correct block since their tick override
            // is what's responsible for accelerated growth
            // TODO: Make growth size after average an a floating point value since it's random for some crops (Eg:
            // stickreed)
            double avgGrowthRate = calcRealAvgGrowthRate(crop, cc);
            if (avgGrowthRate <= 0) return;
            this.growthTime = 0;
            for (int i = sizeAfterHarvestTotal / NUMBER_OF_DROPS_TO_SIMULATE; i < cc.maxSize(); i++) {
                crop.setSize((byte) i);
                int growthPointsForStage = cc.growthDuration(crop);
                // Growth progress is not allowed to spill over to a new stage
                this.growthTime += (int) Math.ceil(growthPointsForStage / avgGrowthRate);
            }
            // Multiply growth ticks by the tick time of the crop sticks
            this.growthTime = Math.max(1, TileEntityCrop.tickRate * this.growthTime);

            // Consume new under block if necessary
            if (blockInputStackToConsume != null) blockInputStackToConsume.stackSize -= this.seedCount;
            // We are good return success
            this.isValid = true;
        } catch (Exception e) {
            e.printStackTrace(System.err);
        } finally {
            // always reset the world to it's original state
            if (!cheating) world.setBlock(xyz[0], xyz[1] - 2, xyz[2], GregTech_API.sBlockCasings4, 1, 0);
            world.setBlockToAir(xyz[0], xyz[1], xyz[2]);
        }
    }

    // region crop simulation utils

    /**
     * Attempts to place a block in the world, used for testing crop viability and drops.
     *
     * @param stack The {@link ItemStack} to place.
     * @param x     The x coordinate at which to place the block.
     * @param y     The y coordinate at which to place the block.
     * @param z     The z coordinate at which to place the block.
     * @param world The world in which to place the block.
     * @return true of a block was placed.
     */
    private static boolean setBlock(ItemStack stack, int x, int y, int z, World world) {
        Item item = stack.getItem();
        Block b = Block.getBlockFromItem(item);
        if (b == Blocks.air || !(item instanceof ItemBlock)) return false;
        short tDamage = (short) item.getDamage(stack);
        if (item instanceof GT_Item_Ores && tDamage > 0) {
            if (!world.setBlock(
                x,
                y,
                z,
                b,
                GT_TileEntity_Ores.getHarvestData(
                    tDamage,
                    ((GT_Block_Ores_Abstract) b).getBaseBlockHarvestLevel(tDamage % 16000 / 1000)),
                0)) {
                return false;
            }
            GT_TileEntity_Ores tTileEntity = (GT_TileEntity_Ores) world.getTileEntity(x, y, z);
            tTileEntity.mMetaData = tDamage;
            tTileEntity.mNatural = false;
        } else world.setBlock(x, y, z, b, tDamage, 0);
        return true;
    }

    /**
     * Computes the number of nutrients that should be given to the crop if we need a block under based on the number of
     * dirt blocks we are simulating under the farmland/fertilized dirt.
     *
     * @param crop The {@link TileEntityCrop} with the seed growing on it.
     * @param cc   The {@link CropCard} for the seed growing on the crop stick.
     * @return The updated nutrient value.
     */
    private static byte getUpdateNutrientsForBlockUnder(TileEntityCrop crop, CropCard cc) {
        // -1 because the farm land is included in the root check.
        if ((cc.getrootslength(crop) - 1 - NUMBER_OF_DIRT_BLOCKS_UNDER) > 0) {
            return crop.nutrients;
        }
        return (byte) (crop.nutrients - 1);
    }

    /**
     * Calculates the humidity at the location of the controller using information obtained by decompiling IC2.
     * Returns 0 if the greenhouse is in no humidity mode.
     *
     * @see EIGIC2Bucket#IS_ON_WET_FARMLAND
     * @see EIGIC2Bucket#WATER_STORAGE_VALUE
     * @see TileEntityCrop#updateHumidity()
     * @param greenhouse The {@link GT_MetaTileEntity_ExtremeIndustrialGreenhouse} that holds the seed.
     * @return The humidity environmental value at the controller's location.
     */
    private byte getHumidity(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse) {
        // TODO: Check if we may want to have the slot remember whether humidity is turned on or not.
        if (greenhouse.isInNoHumidityMode()) return 0;
        int value = Crops.instance.getHumidityBiomeBonus(
            greenhouse.getBaseMetaTileEntity()
                .getBiome());
        if (IS_ON_WET_FARMLAND) value += 2;
        // we add 2 if we have more than 5 water in storage
        if (WATER_STORAGE_VALUE >= 5) value += 2;
        // add 1 for every 25 water stored (max of 200
        value += (WATER_STORAGE_VALUE + 24) / 25;
        return (byte) value;
    }

    /**
     * Calculates the nutrient value at the location of the controller using information obtained by decompiling IC2
     *
     * @see EIGIC2Bucket#NUMBER_OF_DIRT_BLOCKS_UNDER
     * @see EIGIC2Bucket#FERTILIZER_STORAGE_VALUE
     * @see TileEntityCrop#updateNutrients()
     * @param greenhouse The {@link GT_MetaTileEntity_ExtremeIndustrialGreenhouse} that holds the seed.
     * @return The nutrient environmental value at the controller's location.
     */
    private byte getNutrients(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse) {
        int value = Crops.instance.getNutrientBiomeBonus(
            greenhouse.getBaseMetaTileEntity()
                .getBiome());
        value += NUMBER_OF_DIRT_BLOCKS_UNDER;
        value += (FERTILIZER_STORAGE_VALUE + 19) / 20;
        return (byte) value;
    }

    /**
     * Calculates the air quality at the location of the controller bucket using information obtained by decompiling IC2
     *
     * @see EIGIC2Bucket#CROP_OBSTRUCTION_VALUE
     * @see EIGIC2Bucket#CROP_CAN_SEE_SKY
     * @see TileEntityCrop#updateAirQuality()
     * @param greenhouse The {@link GT_MetaTileEntity_ExtremeIndustrialGreenhouse} that holds the seed.
     * @return The air quality environmental value at the controller's location.
     */
    private byte getAirQuality(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse) {
        // clamp height bonus to 0-4, use the height of the crop itself
        // TODO: check if we want to add the extra +2 for the actual height of the crop stick in the EIG.
        int value = Math.max(
            0,
            Math.min(
                4,
                (greenhouse.getBaseMetaTileEntity()
                    .getYCoord() - 64) / 15));
        // min value of fresh is technically 8 since the crop itself will count as an obstruction at xOff = 0, zOff = 0
        value += CROP_OBSTRUCTION_VALUE / 2;
        // you get a +2 bonus for being able to see the sky
        if (CROP_CAN_SEE_SKY) value += 2;
        return (byte) value;
    }

    /**
     * Calculates the average number of separate item drops to be rolled per harvest using information obtained by
     * decompiling IC2.
     *
     * @see TileEntityCrop#harvest_automated(boolean)
     * @param te The {@link TileEntityCrop} holding the crop
     * @param cc The {@link CropCard} of the seed
     * @return The average number of drops to computer per harvest
     */
    private static double getRealAverageDropRounds(TileEntityCrop te, CropCard cc) {
        return cc.dropGainChance() * Math.pow(1.03, te.getGain());
    }

    /**
     * Calculates the average drop of the stack size caused by seed's gain using information obtained by
     * decompiling IC2.
     *
     * @see TileEntityCrop#harvest_automated(boolean)
     * @param te The {@link TileEntityCrop} holding the crop
     * @param cc The {@link CropCard} of the seed
     * @return The average number of drops to computer per harvest
     */
    private static double getRealAverageDropIncrease(TileEntityCrop te, CropCard cc) {
        // yes gain has the amazing ability to sometimes add 1 to your stack size!
        return te.getGain() / 100.0d;
    }

    /**
     * Calculates an average growth speed for crops which may roll a zero on low rng rolls.
     *
     * @see EIGIC2Bucket#calcAvgGrowthRate(TileEntityCrop, CropCard, int)
     * @param te The {@link TileEntityCrop} holding the crop
     * @param cc The {@link CropCard} of the seed
     * @return The average growth rate as a floating point number
     */
    private static double calcRealAvgGrowthRate(TileEntityCrop te, CropCard cc) {
        int total = 0;
        for (int rngRoll = 0; rngRoll <= 6; rngRoll++) {
            total += calcAvgGrowthRate(te, cc, rngRoll);
        }
        return total / 7.0d;
    }

    /**
     * Calculates the average growth rate of an ic2 crop using information obtained though decompiling IC2.
     * Calls to random functions have been either replaced with customisable values or boundary tests.
     *
     * @see TileEntityCrop#calcGrowthRate()
     * @param te      The {@link TileEntityCrop} holding the crop
     * @param cc      The {@link CropCard} of the seed
     * @param rngRoll The role for the base rng
     * @return The amounts of growth point added to the growth progress in average every growth tick
     */
    private static int calcAvgGrowthRate(TileEntityCrop te, CropCard cc, int rngRoll) {
        // the original logic uses IC2.random.nextInt(7)
        int base = rngRoll + te.getGrowth();
        int need = Math.max(0, (cc.tier() - 1) * 4 + te.getGrowth() + te.getGain() + te.getResistance());
        int have = cc.weightInfluences(te, te.getHumidity(), te.getNutrients(), te.getAirQuality()) * 5;

        if (have >= need) {
            // The crop has a good enough environment to grow normally
            return base * (100 + (have - need)) / 100;
        } else {
            // this only happens if we don't have enough
            // resources to grow properly.
            int neg = (need - have) * 4;

            if (neg > 100) {
                // a crop with a resistance 31 will never die since the original
                // checks for `IC2.random.nextInt(32) > this.statResistance`
                // so assume that the crop will eventually die if it doesn't
                // have maxed out resistance stats. 0 means no growth this tick
                // -1 means the crop dies.
                return te.getResistance() >= 31 ? 0 : -1;
            }
            // else apply neg to base
            return Math.max(0, base * (100 - neg) / 100);
        }
    }

    // endregion crop simulation utils
}
