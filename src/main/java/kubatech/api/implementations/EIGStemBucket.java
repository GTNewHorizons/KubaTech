package kubatech.api.implementations;

import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockStem;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSeedFood;
import net.minecraft.item.ItemSeeds;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import kubatech.api.IBlockStemAccesor;
import kubatech.api.eig.EIGBucket;
import kubatech.api.eig.EIGDropTable;
import kubatech.api.eig.IEIGBucketFactory;
import kubatech.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeIndustrialGreenhouse;
import net.minecraftforge.common.IPlantable;

public class EIGStemBucket extends EIGBucket {

    public final static IEIGBucketFactory factory = new EIGStemBucket.Factory();
    private static final String NBT_IDENTIFIER = "STEM";
    private static final int REVISION_NUMBER = 0;
    private final static int NUMBER_OF_DROPS_TO_SIMULATE = 100;

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
            if (!(item instanceof IPlantable)) return null;
            Block block = ((IPlantable) item).getPlant(greenhouse.getBaseMetaTileEntity().getWorld(), 0, 0, 0);
            if (!(block instanceof BlockStem)) return null;
            EIGBucket bucket = new EIGStemBucket(greenhouse, input, maxConsume);
            if (!bucket.isValid()) return null;
            // consume if we are valid
            input.stackSize -= maxConsume;
            bucket.tryAddSeed(greenhouse, input, maxConsume - 1);
            return bucket;
        }

        @Override
        public EIGBucket restore(NBTTagCompound nbt) {
            return new EIGStemBucket(nbt);
        }
    }

    private boolean isValid = false;
    private EIGDropTable drops;

    private EIGStemBucket(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse, ItemStack input, int seedCount) {
        super(input, seedCount, null);
        recalculateDrops(greenhouse);
    }

    private EIGStemBucket(NBTTagCompound nbt) {
        super(nbt);
        this.drops = new EIGDropTable(nbt, "drops");
        this.isValid = nbt.getInteger("version") == REVISION_NUMBER;
    }

    @Override
    public NBTTagCompound save() {
        NBTTagCompound nbt = super.save();
        nbt.setTag("drops", this.drops.save());
        nbt.setInteger("version", REVISION_NUMBER);
        return nbt;
    }

    @Override
    protected String getNBTIdentifier() {
        return NBT_IDENTIFIER;
    }

    @Override
    public void addProgress(double timeDelta, EIGDropTable tracker) {
        if (!this.isValid()) return;
        this.drops.addTo(tracker);
    }

    @Override
    public boolean isValid() {
        return super.isValid() && this.isValid;
    }

    @Override
    public boolean revalidate(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse) {
        recalculateDrops(greenhouse);
        return this.isValid();
    }

    /**
     * Attempts to predetermine what item the stem crop will drop.
     *
     * @param greenhouse The greenhouse that houses this bucket.
     */
    public void recalculateDrops(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse) {
        this.isValid = false;
        Item item = this.seed.getItem();
        if (!(item instanceof IPlantable)) return;
        Block stemBlock = ((IPlantable) item).getPlant(greenhouse.getBaseMetaTileEntity().getWorld(), 0, 0, 0);
        if (!(stemBlock instanceof BlockStem)) return;
        Block cropBlock = ((IBlockStemAccesor) stemBlock).getCropBlock();
        if (cropBlock == null || cropBlock == Blocks.air) return;
        // if we know some crops needs a specific metadata, remap here
        int metadata = 0;

        this.drops = new EIGDropTable();
        for (int i = 0; i < NUMBER_OF_DROPS_TO_SIMULATE; i++) {
            // simulate 1 round of drops
            ArrayList<ItemStack> drops = cropBlock.getDrops(
                greenhouse.getBaseMetaTileEntity()
                    .getWorld(),
                greenhouse.getBaseMetaTileEntity()
                    .getXCoord(),
                greenhouse.getBaseMetaTileEntity()
                    .getYCoord(),
                greenhouse.getBaseMetaTileEntity()
                    .getZCoord(),
                metadata,
                0);
            if (drops == null || drops.isEmpty()) continue;
            // if it drops itself assume that it only drops itself
            if (i == 0 && drops.size() == 1) {
                ItemStack drop = drops.get(0);
                if (drop != null && drop.stackSize >= 1 && drop.getItem() == Item.getItemFromBlock(cropBlock)) {
                    this.drops.addDrop(drop, drop.stackSize);
                    break;
                }
            }
            // else append all the drops
            for (ItemStack drop : drops) {
                this.drops.addDrop(drop, drop.stackSize / (double) NUMBER_OF_DROPS_TO_SIMULATE);
            }
        }
        // check that we did in fact drop something.s
        if (this.drops.isEmpty()) return;
        // all checks passed we are good to go
        this.isValid = true;
    }
}
