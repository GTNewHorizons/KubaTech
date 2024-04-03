package kubatech.api.implementations;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFlower;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import kubatech.api.eig.EIGBucket;
import kubatech.api.eig.EIGDropTable;
import kubatech.api.eig.IEIGBucketFactory;
import kubatech.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeIndustrialGreenhouse;

public class EIGFlowerBucket extends EIGBucket {

    public final static IEIGBucketFactory factory = new EIGFlowerBucket.Factory();
    private static final String NBT_IDENTIFIER = "FLOWER";
    private static final int REVISION_NUMBER = 0;

    public static class Factory implements IEIGBucketFactory {

        @Override
        public String getNBTIdentifier() {
            return NBT_IDENTIFIER;
        }

        @Override
        public EIGBucket tryCreateBucket(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse, ItemStack input,
            int maxConsume) {
            // Check if input is a flower, reed or cacti. They all drop their source item multiplied by their seed count
            Item item = input.getItem();
            Block block = Block.getBlockFromItem(item);
            if (item != Items.reeds && block != Blocks.cactus && !(block instanceof BlockFlower)) return null;
            EIGBucket bucket = new EIGFlowerBucket(input, maxConsume);
            input.stackSize -= maxConsume;
            return bucket;
        }

        @Override
        public EIGBucket restore(NBTTagCompound nbt) {
            return new EIGFlowerBucket(nbt);
        }
    }

    private EIGFlowerBucket(ItemStack input, int seedCount) {
        super(input, seedCount, null);
    }

    private EIGFlowerBucket(NBTTagCompound nbt) {
        super(nbt);
    }

    @Override
    public NBTTagCompound save() {
        NBTTagCompound nbt = super.save();
        nbt.setInteger("version", REVISION_NUMBER);
        return nbt;
    }

    @Override
    protected String getNBTIdentifier() {
        return NBT_IDENTIFIER;
    }

    @Override
    public void addProgress(double timeDelta, EIGDropTable tracker) {
        tracker.addDrop(this.seed, this.seedCount);
    }

    @Override
    public boolean revalidate(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse) {
        return this.isValid();
    }
}
