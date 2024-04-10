package kubatech.tileentity.gregtech.multiblock.eigbuckets;

import java.util.*;

import ic2.core.crop.CropStickreed;
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
    private static final int WATER_STORAGE_VALUE = Math.max(0, Math.min(200, 200));
    // nutrient factors
    /**
     * The number of blocks of dirt we assume are under. Subtract 1 if we have a block under our crop.
     */
    private static final int NUMBER_OF_DIRT_BLOCKS_UNDER = Math.max(0, Math.min(3, 0));
    /**
     * The amount of fertilizer stored in the crop stick
     */
    private static final int FERTILIZER_STORAGE_VALUE = Math.max(0, Math.min(200, 0));
    // air quality factors
    /**
     * How many blocks in a 3x3 area centered on the crop do not contain solid blocks or other crops.
     * Max value is 8 because the crop always counts itself.
     */
    private static final int CROP_OBSTRUCTION_VALUE = Math.max(0, Math.min(8, 9 - 4));
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
        public EIGBucket tryCreateBucket(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse, ItemStack input) {
            // Check if input is a seed.
            if (!ItemList.IC2_Crop_Seeds.isStackEqual(input, true, true)) return null;
            if (!input.hasTagCompound()) return null;
            // Validate that stat nbt data exists.
            NBTTagCompound nbt = input.getTagCompound();
            if (!(nbt.hasKey("growth") && nbt.hasKey("gain") && nbt.hasKey("resistance"))) return null;

            CropCard cc = IC2Crops.instance.getCropCard(input);
            if (cc == null) return null;
            return new EIGIC2Bucket(greenhouse, input);
        }

        @Override
        public EIGBucket restore(NBTTagCompound nbt) {
            return new EIGIC2Bucket(nbt);
        }
    }

    public final boolean useNoHumidity;
    private EIGDropTable drops = new EIGDropTable();
    private double growthTime;
    private boolean isValid = false;

    /**
     * Used to migrate old EIG greenhouse slots to the new bucket system, needs custom handling as to not void the
     * support blocks.
     *
     * @implNote DOES NOT VALIDATE THE CONTENTS OF THE BUCKET, YOU'LL HAVE TO REVALIDATE WHEN THE WORLD IS LOADED.
     *
     * @param seed          The item stack for the item that served as the seed before
     * @param count         The number of seed in the bucket
     * @param supportBlock  The block that goes under the bucket
     * @param useNoHumidity Whether to use no humidity in growth speed calculations.
     */
    public EIGIC2Bucket(ItemStack seed, int count, ItemStack supportBlock, boolean useNoHumidity) {
        super(seed, count, supportBlock == null ? null : new ItemStack[] { supportBlock });
        this.useNoHumidity = useNoHumidity;
        // revalidate me
        this.isValid = false;
    }

    private EIGIC2Bucket(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse, ItemStack seed) {
        super(seed, 1, null);
        this.useNoHumidity = greenhouse.isInNoHumidityMode();
        this.recalculateDrops(greenhouse);
    }

    private EIGIC2Bucket(NBTTagCompound nbt) {
        super(nbt);
        this.drops = new EIGDropTable(nbt, "drops");
        this.growthTime = nbt.getDouble("growthTime");
        this.useNoHumidity = nbt.getBoolean("useNoHumidity");
        this.isValid = nbt.getInteger("version") == REVISION_NUMBER;
    }

    @Override
    public NBTTagCompound save() {
        NBTTagCompound nbt = super.save();
        nbt.setTag("drops", this.drops.save());
        nbt.setDouble("growthTime", this.growthTime);
        nbt.setBoolean("useNoHumidity", this.useNoHumidity);
        nbt.setInteger("version", REVISION_NUMBER);
        return nbt;
    }

    @Override
    protected String getNBTIdentifier() {
        return NBT_IDENTIFIER;
    }

    @Override
    public void addProgress(double multiplier, EIGDropTable tracker) {
        // abort early if slot is invalid
        if (!this.isValid) return;
        // else apply drops to tracker
        double growthPercent = multiplier / this.growthTime;
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
            crop.waterStorage = this.useNoHumidity ? 0 : WATER_STORAGE_VALUE;
            crop.humidity = getHumidity(greenhouse, this.useNoHumidity);
            crop.nutrientStorage = FERTILIZER_STORAGE_VALUE;
            crop.nutrients = getNutrients(greenhouse);
            crop.airQuality = getAirQuality(greenhouse);

            // Check if we can put the current block under the soil.
            if (this.supportItems != null && this.supportItems.length == 1 && this.supportItems[0] != null) {
                if (!setBlock(this.supportItems[0], xyz[0], xyz[1] - 2, xyz[2], world)) {
                    return;
                }
                // update nutrients if we need a block under.
                crop.nutrients = getUpdateNutrientsForBlockUnder(crop, cc);
            }

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

            crop.setSize((byte) cc.maxSize());
            if (!cc.canBeHarvested(crop)) return;

            // PRE GENERATE DROP CHANCES
            // TODO: Create better loot table handling for crops like red wheat, stonelilies, mana beans, magic metal
            // berries, etc.
            this.drops = new EIGDropTable();
            // Multiply drop sizes by the average number drop rounds per harvest.
            double avgDropRounds = getRealAverageDropRounds(crop, cc);
            double avgStackIncrease = getRealAverageDropIncrease(crop, cc);
            HashMap<Integer, Integer> sizeAfterHarvestFrequencies = new HashMap<>();
            for (int i = 0; i < NUMBER_OF_DROPS_TO_SIMULATE; i++) {
                // try generating some loot drop
                ItemStack drop = cc.getGain(crop);
                if (drop == null || drop.stackSize <= 0) continue;
                sizeAfterHarvestFrequencies.merge((int) cc.getSizeAfterHarvest(crop), 1, Integer::sum);

                // Merge the new drop with the current loot table.
                double avgAmount = (drop.stackSize + avgStackIncrease) * avgDropRounds;
                this.drops.addDrop(drop, avgAmount / NUMBER_OF_DROPS_TO_SIMULATE);
            }
            if (this.drops.isEmpty()) return;

            // Just doing average(ceil(stageGrowth/growthPerm)) isn't good enough it's off by as much as 20%
            double avgGrowthCyclesToHarvest = calcRealAvgGrowthRate(crop, cc, sizeAfterHarvestFrequencies);
            if (avgGrowthCyclesToHarvest <= 0) { return; }
            this.growthTime = TileEntityCrop.tickRate * avgGrowthCyclesToHarvest;

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

    // region deterministic environmental calculations

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
    public static byte getHumidity(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse, boolean useNoHumidity) {
        // TODO: Check if we may want to have the slot remember whether humidity is turned on or not.
        if (useNoHumidity) return 0;
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
    public static byte getNutrients(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse) {
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
    public static byte getAirQuality(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse) {
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

    // endregion deterministic environmental calculations

    // region drop rate calculations

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
        // this should be ~99.99995%
        double total = 0;
        double multTotal = 0;
        double chance = (double) cc.dropGainChance() * Math.pow(1.03, te.getGain());
        // this range should cover ~99.8% of random values from the gaussian curve
        // idk why having the stop at 3.2825
        // also no idk why using a normal distribution instead of an actual gaussian formula results in higher accuracy
        for (int y = -300; y <= 328; y += 1) {
            double x = ((double) y / 100.0d);
            double mult = stdNormDistr(x);
            total += Math.max(0L, Math.round(x * chance * 0.6827D + chance)) * mult;
            multTotal += mult;
        }
        return total / multTotal;
    }

    private static final double STD_NORM_DISTR_P1 = 1 / Math.sqrt(2.0d * Math.PI);

    private static double stdNormDistr(double x) {
        return STD_NORM_DISTR_P1 * Math.exp(-0.5 * (x * x));
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
        return (te.getGain() + 1) / 100.0d;
    }

    // endregiondrop rate calculations

    // region growth time approximation

    /**
     * Calculates the average number growth cycles needed for a crop to grow to maturity.
     *
     * @see EIGIC2Bucket#calcAvgGrowthRate(TileEntityCrop, CropCard, int)
     * @param te The {@link TileEntityCrop} holding the crop
     * @param cc The {@link CropCard} of the seed
     * @return The average growth rate as a floating point number
     */
    private static double calcRealAvgGrowthRate(TileEntityCrop te, CropCard cc, HashMap<Integer, Integer> sizeAfterHarvestFrequencies) {
        // Compute growth speeds.
        int[] growthSpeeds = new int[7];
        for (int i = 0; i < 7; i++) growthSpeeds[i] = calcAvgGrowthRate(te, cc, i);

        // if it's stick reed, we know what the distribution should look like
        if (cc.getClass() == CropStickreed.class) {
            sizeAfterHarvestFrequencies.clear();
            sizeAfterHarvestFrequencies.put(1,1);
            sizeAfterHarvestFrequencies.put(2,1);
            sizeAfterHarvestFrequencies.put(3,1);
        }

        // Get the duration of all growth stages
        int[] growthDurations = new int[cc.maxSize()];
        //, index 0 is assumed to be 0 since stage 0 is usually impossible.
        // The frequency table should prevent stage 0 from having an effect on the result.
        growthDurations[0] = 0; // stage 0 doesn't usually exist.
        for (byte i = 1; i < growthDurations.length; i++) {
            te.setSize(i);
            growthDurations[i] = cc.growthDuration(te);
        }

        return calcRealAvgGrowthRate(growthSpeeds, growthDurations, sizeAfterHarvestFrequencies);
    }

    /**
     * Calculates the average number growth cycles needed for a crop to grow to maturity.
     *
     * @implNote This method is entirely self-contained and can therefore be unit tested.
     *
     * @param growthSpeeds        The speeds at which the crop can grow.
     * @param stageGoals          The total to reach for each stage
     * @param startStageFrequency How often the growth starts from a given stage
     * @return The average growth rate as a floating point number
     */
    public static double calcRealAvgGrowthRate(int[] growthSpeeds, int[] stageGoals, HashMap<Integer, Integer> startStageFrequency) {

        // taking out the zero rolls out of the calculation tends to make the math more accurate for lower speeds.
        int[] nonZeroSpeeds = Arrays.stream(growthSpeeds).filter(x-> x > 0).toArray();
        int zeroRolls = growthSpeeds.length - nonZeroSpeeds.length;
        if (zeroRolls >= growthSpeeds.length) return -1;

        // compute stage lengths and stage frequencies
        double[] avgCyclePerStage = new double[stageGoals.length];
        double[] normalizedStageFrequencies = new double[stageGoals.length];
        long frequenciesSum = startStageFrequency.values().parallelStream().mapToInt(x -> x).sum();
        for (int i = 0; i < stageGoals.length; i++) {
            avgCyclePerStage[i] = calcAvgCyclesToGoal(nonZeroSpeeds, stageGoals[i]);
            normalizedStageFrequencies[i] = startStageFrequency.getOrDefault(i, 0) * stageGoals.length / (double) frequenciesSum;
        }

        // Compute multipliers based on how often the growth starts at a given rate.
        double[] frequencyMultipliers = new double[avgCyclePerStage.length];
        Arrays.fill(frequencyMultipliers, 1.0d);
        conv1DAndCopyToSignal(frequencyMultipliers, normalizedStageFrequencies, new double[avgCyclePerStage.length]);

        // apply multipliers to length
        for (int i = 0; i < avgCyclePerStage.length; i++) avgCyclePerStage[i] *= frequencyMultipliers[i];

        // lengthen average based on number of 0 rolls.
        double average = Arrays.stream(avgCyclePerStage).average().getAsDouble();
        if (zeroRolls > 0) {
            average = average / nonZeroSpeeds.length * growthSpeeds.length;
        }

        // profit
        return average;
    }

    /**
     * Computes the average number of rolls of an N sided fair dice with irregular number progressions needed to surpass
     * a given total.
     *
     * @param speeds The speeds at which the crop grows.
     * @param goal   The total to match or surpass.
     * @return The average number of rolls of speeds to meet or surpass the goal.
     */
    private static double calcAvgCyclesToGoal(int[] speeds, int goal) {
        // even if the goal is 0, it will always take at least 1 cycle.
        if (goal <= 0) return 1;
        // condition start signal
        double[] signal = new double[goal];
        Arrays.fill(signal, 0);
        signal[0] = 1;

        // Create kernel out of our growth speeds
        double[] kernel = tabulate(speeds, 1.0d/speeds.length);
        double[] convolutionTarget = new double[signal.length];
        LinkedList<Double> P = new LinkedList<Double>();

        // Perform convolutions on the signal until it's too weak to be recognised.
        double p, avgRolls = 1;
        do {
            conv1DAndCopyToSignal(signal, kernel, convolutionTarget);
            avgRolls += p = Arrays.stream(signal).sum();
            // 1e-1 is a threshold, you can increase it for to increase the accuracy of the output.
            // 1e-1 is already accurate enough that any value beyond that is unwarranted.
        } while (p >= 1e-1/goal);
        return avgRolls;
    }

    /**
     * Creates an array that corresponds to the amount of times a number appears in a list.
     *
     * Ex: {1,2,3,4} -> {0,1,1,1,1}, {0,2,2,4} -> {1,0,2,0,1}
     *
     * @param bin        The number list to tabulate
     * @param multiplier A multiplier to apply the output list
     * @return The number to tabulate
     */
    private static double[] tabulate(int[] bin, double multiplier) {
        double[] ret = new double[bin[bin.length - 1] + 1];
        Arrays.fill(ret, 0);
        for (int i : bin) ret[i] += multiplier;
        return ret;
    }

    /**
     * Computes a 1D convolution of a signal and stores the results in the signal array.
     * Essentially performs `X <- convolve(X,rev(Y))[1:length(X)]` in R
     *
     * @param signal            The signal to apply the convolution to.
     * @param kernel            The kernel to compute with.
     * @param fixedLengthTarget A memory optimisation so we don't just create a ton of arrays since we overwrite it.
     *                          Should be the same length as the signal.
     */
    private static void conv1DAndCopyToSignal(double[] signal, double[] kernel, double[] fixedLengthTarget) {
        // for a 1d convolution we would usually use kMax = signal.length + kernel.length - 1
        // but since we are directly applying our result to our signal, there is no reason to compute
        // values where k > signal.length.
        // we could probably run this loop in parallel.
        for(int k = 0; k < signal.length; k++) {
            // I needs to be a valid index of the kernel.
            fixedLengthTarget[k] = 0;
            for(int i = Math.max(0, k - kernel.length + 1); i <= k; i++) {
                fixedLengthTarget[k] += signal[i] * kernel[k - i];
            }
        }
        System.arraycopy(fixedLengthTarget, 0, signal, 0, signal.length);
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
        int base = 3 + rngRoll + te.getGrowth();
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

    // endregion growth time approximation

}
