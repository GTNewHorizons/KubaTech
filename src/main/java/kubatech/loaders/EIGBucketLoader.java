package kubatech.loaders;

import kubatech.api.LoaderReference;
import kubatech.api.enums.EIGMode;
import kubatech.tileentity.gregtech.multiblock.eigbuckets.EIGFlowerBucket;
import kubatech.tileentity.gregtech.multiblock.eigbuckets.EIGIC2Bucket;
import kubatech.tileentity.gregtech.multiblock.eigbuckets.EIGRainbowCactusBucket;
import kubatech.tileentity.gregtech.multiblock.eigbuckets.EIGSeedBucket;
import kubatech.tileentity.gregtech.multiblock.eigbuckets.EIGStemBucket;

public class EIGBucketLoader {

    public static void LoadEIGBuckets() {
        // IC2 buckets
        EIGMode.IC2.addLowPriorityFactory(EIGIC2Bucket.factory);

        // Regular Mode Buckets
        if (LoaderReference.ThaumicBases) {
            EIGMode.Normal.addLowPriorityFactory(EIGRainbowCactusBucket.factory);
        }
        EIGMode.Normal.addLowPriorityFactory(EIGFlowerBucket.factory);
        EIGMode.Normal.addLowPriorityFactory(EIGStemBucket.factory);
        EIGMode.Normal.addLowPriorityFactory(EIGSeedBucket.factory);
    }

}
