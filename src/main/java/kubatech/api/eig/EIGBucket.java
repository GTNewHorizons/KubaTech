package kubatech.api.eig;

import static kubatech.api.utils.ItemUtils.readItemStackFromNBT;
import static kubatech.api.utils.ItemUtils.writeItemStackToNBT;

import java.util.LinkedList;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import gregtech.api.util.GT_Utility;
import kubatech.tileentity.gregtech.multiblock.GT_MetaTileEntity_ExtremeIndustrialGreenhouse;

public abstract class EIGBucket extends InventoryCrafting {

    protected ItemStack seed;
    protected int seedCount;
    protected ItemStack[] supportItems;

    private EIGBucket() {
        super(null, 3, 3);
    }

    public EIGBucket(ItemStack seed, int seedCount, ItemStack[] supportItem) {
        this();
        this.seed = seed.copy();
        seed.stackSize = 1;
        this.seedCount = seedCount;
        this.supportItems = supportItem;
    }

    public EIGBucket(NBTTagCompound nbt) {
        this();
        this.seed = readItemStackFromNBT(nbt.getCompoundTag("seed"));
        this.seedCount = nbt.getInteger("count");

        // parse support items
        if (nbt.hasKey("supportItems", 9)) {
            NBTTagList supportItemsNBTList = nbt.getTagList("supportItems", 10);
            if (supportItemsNBTList.tagCount() > 0) {
                this.supportItems = new ItemStack[supportItemsNBTList.tagCount()];
                for (int i = 0; i < supportItemsNBTList.tagCount(); i++) {
                    this.supportItems[i] = readItemStackFromNBT(supportItemsNBTList.getCompoundTagAt(i));
                }
            } else {
                supportItems = null;
            }
        } else {
            supportItems = null;
        }
    }

    /**
     * Creates a persistent save of the bucket's current data.
     *
     * @return The nbt data for this bucket.
     */
    public NBTTagCompound save() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("type", this.getNBTIdentifier());
        nbt.setTag("seed", writeItemStackToNBT(this.seed));
        nbt.setInteger("count", this.seedCount);
        if (this.supportItems != null && this.supportItems.length > 0) {
            NBTTagList supportItemNBT = new NBTTagList();
            for (ItemStack supportItem : this.supportItems) {
                supportItemNBT.appendTag(writeItemStackToNBT(supportItem));
            }
            nbt.setTag("supportItems", supportItemNBT);
        }
        return nbt;
    }

    /**
     * Gets an item stack representing the seeds in this bucket
     *
     * @return an item stack representing the seeds in this bucket.
     */
    public ItemStack getSeedStack() {
        ItemStack copied = this.seed.copy();
        copied.stackSize = this.seedCount;
        return copied;
    }

    /**
     * Gets the number of seeds in this bucket
     *
     * @return gets the number of seeds in this bucket.
     */
    public int getSeedCount() {
        return this.seedCount;
    }

    /**
     * Attempts to add seeds to tbe bucket if the input is compatible
     *
     * @param input      A stack of an item that may be able to be added to our current bucket.
     * @param maxConsume The maximum amount of seeds to add to this bucket.
     * @return number of seeds consumed, 0 for wrong item, -1 if it missed the support items, -2 if you tried to consume
     *         0 or less items;
     */
    public int tryAddSeed(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse, ItemStack input, int maxConsume) {
        // Abort is input if empty
        if (input == null || input.stackSize <= 0) return -2;
        // Cap max to input count
        maxConsume = Math.min(maxConsume, input.stackSize);
        // Abort if item isn't an identical seed.
        if (!GT_Utility.areStacksEqual(this.seed, input, false)) return 0;

        // no support items, consume and exit early.
        if (this.supportItems == null || this.supportItems.length <= 0) {
            input.stackSize -= maxConsume;
            return maxConsume;
        }

        // register item as valid
        LinkedList<ItemStack> toConsumeFrom = new LinkedList<>();
        toConsumeFrom.addLast(input);

        // Check if the item is found
        supportLoop: for (ItemStack supportItem : this.supportItems) {
            for (ItemStack otherInput : greenhouse.getStoredInputs()) {
                // filter usable inputs
                if (otherInput == null || otherInput.stackSize <= 0) continue;
                if (!GT_Utility.areStacksEqual(supportItem, otherInput, false)) continue;
                // update max consume again
                maxConsume = Math.min(maxConsume, otherInput.stackSize);
                toConsumeFrom.addLast(otherInput);
                continue supportLoop;
            }
            // no support found, no seeds added
            return -1;
        }

        // consume items
        input.stackSize -= maxConsume;
        for (ItemStack stack : toConsumeFrom) {
            stack.stackSize -= maxConsume;
        }
        return maxConsume;
    }

    /**
     * Attempts to remove a seed from the bucket
     *
     * @param toRemove The maximum amount of items to remove.
     * @return The items that were removed from the bucket. Null if the bucket is empty.
     */
    public ItemStack[] tryRemoveSeed(int toRemove) {
        // validate inputs
        toRemove = Math.min(this.seedCount, toRemove);
        if (toRemove <= 0) return null;

        // consume and return output
        ItemStack[] ret = new ItemStack[1 + (this.supportItems == null ? 0 : this.supportItems.length)];
        ret[0] = this.seed.copy();
        ret[0].stackSize = toRemove;
        if (this.supportItems != null) {
            for (int i = 0; i < this.supportItems.length; i++) {
                ret[i + 1] = this.supportItems[i].copy();
                ret[i + 1].stackSize = toRemove;
            }
        }
        this.seedCount -= toRemove;
        return ret;
    }

    /**
     * Returns true if the bucket can output items.
     *
     * @return true if the bucket is valid.
     */
    public boolean isValid() {
        return this.seed != null && this.seedCount > 0;
    }

    /**
     * Gets the identifier used to identify this class during reconstruction
     *
     * @return the identifier for this bucket type.
     */
    protected abstract String getNBTIdentifier();

    /**
     * Adds item drops to the item tracker.
     *
     * @param timeDelta The amount of ticks since the last harvest
     * @param tracker   The item drop tracker
     */
    public abstract void addProgress(double timeDelta, EIGDropTable tracker);

    /**
     * Attempts to revalidate a seed bucket. If it returns false, attempt to seed and support items and delete the bucket.
     *
     * @param greenhouse The greenhouse that contains the bucket.
     * @return True if the bucket was successfully validated. {@link EIGBucket#isValid()} should also return true.
     */
    public abstract boolean revalidate(GT_MetaTileEntity_ExtremeIndustrialGreenhouse greenhouse);

    // region InventoryCrafting

    @Override
    public ItemStack getStackInSlot(int p_70301_1_) {
        if (p_70301_1_ == 0) return this.getSeedStack();
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int par1) {
        return null;
    }

    @Override
    public ItemStack decrStackSize(int par1, int par2) {
        return null;
    }

    @Override
    public void setInventorySlotContents(int par1, ItemStack par2ItemStack) {}

    // endregion InventoryCrafting
}
