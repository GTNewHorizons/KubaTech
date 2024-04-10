package kubatech.api.enums;

import static kubatech.kubatech.error;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import kubatech.api.eig.EIGBucket;
import kubatech.api.eig.IEIGBucketFactory;
import kubatech.tileentity.gregtech.multiblock.eigbuckets.EIGFlowerBucket;
import kubatech.tileentity.gregtech.multiblock.eigbuckets.EIGIC2Bucket;
import kubatech.tileentity.gregtech.multiblock.eigbuckets.EIGSeedBucket;
import kubatech.tileentity.gregtech.multiblock.eigbuckets.EIGStemBucket;
import kubatech.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeIndustrialGreenhouse;

// TODO: Make this into instance classes
public enum EIGMode {

    Normal(0, "normal", GT_MetaTileEntity_ExtremeIndustrialGreenhouse.EIG_BALANCE_REGULAR_MODE_MIN_TIER, 64,
        (i) -> (1 << i), 1, 2),
    IC2(1, "IC2", GT_MetaTileEntity_ExtremeIndustrialGreenhouse.EIG_BALANCE_IC2_MODE_MIN_TIER, 1, (i) -> 4 << (2 * i),
        5, 40)

    ;

    public final int id;
    public final String name;
    public final int minTier;
    public final int seedPerSlot;
    private final Function<Integer, Integer> slotCalculator;
    public final int weedEXMultiplier;
    public final int fertilizerUsagePerSeed;
    /**
     * Used to resolve factory type to an identifier.
     */
    private final HashMap<String, IEIGBucketFactory> factories;
    /**
     * A way to have other mods submit custom buckets that can be prioritized over our default buckets
     */
    private final LinkedList<IEIGBucketFactory> orderedFactories;

    EIGMode(int id, String name, int minTier, int seedPerSlot, Function<Integer, Integer> slotCalculator,
        int weedEXMultiplier, int fertilizerUsagePerSeed) {
        this.id = id;
        this.name = name;
        this.minTier = minTier;
        this.seedPerSlot = seedPerSlot;
        this.slotCalculator = slotCalculator;
        this.weedEXMultiplier = weedEXMultiplier;
        this.fertilizerUsagePerSeed = fertilizerUsagePerSeed;
        this.factories = new HashMap<>();
        this.orderedFactories = new LinkedList<>();
    }

    /**
     * Adds a bucket factory to the EIG mode and gives it a low priority. Factories with using existing IDs will
     * overwrite each other.
     *
     * @see EIGMode#factories
     * @param factory The bucket factory to add.
     */
    public void addLowPriorityFactory(IEIGBucketFactory factory) {
        String factoryId = factory.getNBTIdentifier();
        dealWithDuplicateFactoryId(factoryId);
        // add factory as lowest priority
        this.factories.put(factoryId, factory);
        this.orderedFactories.addLast(factory);
    }

    /**
     * Adds a bucket factory to the EIG mode and gives it a high priority. Factories with using existing IDs will
     * overwrite each other.
     *
     * @see EIGMode#factories
     * @param factory The bucket factory to add.
     */
    public void addHighPriorityFactory(IEIGBucketFactory factory) {
        String factoryId = factory.getNBTIdentifier();
        dealWithDuplicateFactoryId(factoryId);
        // add factory as lowest priority
        this.factories.put(factoryId, factory);
        this.orderedFactories.addFirst(factory);
    }

    /**
     * Attempts to create a new bucket from a given item. Returns if the item cannot be inserted into the EIG.
     *
     * @see IEIGBucketFactory#tryCreateBucket(GT_MetaTileEntity_ExtremeIndustrialGreenhouse, ItemStack, int)
     * @param greenhouse The {@link GT_MetaTileEntity_ExtremeIndustrialGreenhouse} that will contain the seed.
     * @param input      The {@link ItemStack} for the input item.
     * @param maxConsume The maximum amount of items to consume.
     * @param simulate   Whether to actually consume the seed.
     * @return Null if no bucket could be created from the item.
     */
    public EIGBucket tryCreateNewBucket(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse, ItemStack input,
        int maxConsume, boolean simulate) {
        // Validate inputs
        if (input == null) return null;
        maxConsume = Math.min(input.stackSize, maxConsume);
        if (maxConsume <= 0) return null;
        for (IEIGBucketFactory factory : this.orderedFactories) {
            EIGBucket bucket = factory.tryCreateBucket(greenhouse, input);
            if (bucket == null || !bucket.isValid()) continue;
            if (!simulate) input.stackSize--;
            maxConsume--;
            bucket.tryAddSeed(greenhouse, input, maxConsume, simulate);
            return bucket;
        }
        return null;
    }

    /**
     * A standardized way to deal with duplicate factory type identifiers.
     *
     * @param factoryId The ID of the factory
     */
    private void dealWithDuplicateFactoryId(String factoryId) {
        if (this.factories.containsKey(factoryId)) {
            // TODO: Check with devs to see if they want a throw instead.
            error("Duplicate EIG bucket index detected!!!: " + factoryId);
            // remove duplicate from ordered list
            this.orderedFactories.remove(this.factories.get(factoryId));
        }
    }

    /**
     * Restores the buckets of an EIG for the given mode.
     *
     * @see IEIGBucketFactory#restore(NBTTagCompound)
     * @param bucketNBTList The
     */
    public void restoreBuckets(NBTTagList bucketNBTList, List<EIGBucket> loadTo) {
        for (int i = 0; i < bucketNBTList.tagCount(); i++) {
            // validate nbt
            NBTTagCompound bucketNBT = bucketNBTList.getCompoundTagAt(i);
            if (bucketNBT.hasNoTags()) {
                error("Empty nbt bucket found in EIG nbt.");
                continue;
            }
            if (!bucketNBT.hasKey("type", 8)) {
                error("Failed to identify bucket type in EIG nbt.");
                continue;
            }
            // identify bucket type
            String bucketType = bucketNBT.getString("type");
            IEIGBucketFactory factory = factories.getOrDefault(bucketType, null);
            if (factory == null) {
                error("failed to find EIG bucket factory for type: " + bucketType);
                continue;
            }
            // restore bucket
            loadTo.add(factory.restore(bucketNBT));
        }
    }

    public int getSlotCount(int voltageTier) {
        if (voltageTier < this.minTier) return 0;
        return this.slotCalculator.apply(voltageTier - this.minTier);
    }

    public EIGMode nextMode() {
        EIGMode[] modes = EIGMode.values();
        int pos = 1;
        for (EIGMode mode : modes) if (mode == this) break;
        else pos++;
        return modes[pos % modes.length];
    }
}
