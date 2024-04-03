package kubatech.api.enums;

import static kubatech.kubatech.error;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import kubatech.api.eig.EIGBucket;
import kubatech.api.eig.IEIGBucketFactory;
import kubatech.api.implementations.EIGFlowerBucket;
import kubatech.api.implementations.EIGIC2Bucket;
import kubatech.api.implementations.EIGSeedBucket;
import kubatech.api.implementations.EIGStemBucket;
import kubatech.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeIndustrialGreenhouse;

public enum EIGMode {

    Normal(EIGFlowerBucket.factory, EIGStemBucket.factory, EIGSeedBucket.factory),
    IC2(EIGIC2Bucket.factory)

    ;

    /**
     * Used to resolve factory type to an identifier.
     */
    private final HashMap<String, IEIGBucketFactory> factories;
    /**
     * A way to have other mods submit custom buckets that can be prioritized over our default buckets
     */
    private final LinkedList<IEIGBucketFactory> orderedFactories;

    EIGMode(IEIGBucketFactory... factories) {
        this.factories = new HashMap<>();
        this.orderedFactories = new LinkedList<>();
        // add our factories in the order they came in.
        for (IEIGBucketFactory factory : factories) {
            this.addLowPriorityFactory(factory);
        }
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
     * @return Null if no bucket could be created from the item.
     */
    public EIGBucket tryCreateNewBucket(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse, ItemStack input,
                                        int maxConsume) {
        // Validate inputs
        if (input == null) return null;
        maxConsume = Math.min(input.stackSize, maxConsume);
        if (maxConsume <= 0) return null;
        EIGBucket bucket = null;
        for (IEIGBucketFactory factory : this.orderedFactories) {
            // will return null on failure
            bucket = factory.tryCreateBucket(greenhouse, input, maxConsume);
            if (bucket != null) break;
        }
        return bucket;
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
     * @return A list of restored buckets, if list size doesn't equal tagCount, some buckets weren't loaded correctly.
     */
    public List<EIGBucket> restoreBuckets(NBTTagList bucketNBTList) {
        LinkedList<EIGBucket> ret = new LinkedList<EIGBucket>();
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
            ret.addLast(factory.restore(bucketNBT));
        }
        return ret;
    }
}
