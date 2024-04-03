package kubatech.api.eig;

import static kubatech.api.utils.ItemUtils.readItemStackFromNBT;
import static kubatech.api.utils.ItemUtils.writeItemStackToNBT;

import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import com.gtnewhorizon.gtnhlib.util.map.ItemStackMap;

public class EIGDropTable {

    private static final String NBT_DROP_TABLE_ITEM_KEY = "item";
    private static final String NBT_DROP_TABLE_COUNT_KEY = "count";

    private final ItemStackMap<Double> dropTable;

    /**
     * Initialises a new empty drop table.
     */
    public EIGDropTable() {
        this.dropTable = new ItemStackMap<>(true);
    }

    /**
     * Loads a serialised drop table from nbt.
     *
     * @param nbt The nbt tag that contains the key for a drop table
     * @param key The name of the key name for the drop table.
     */
    public EIGDropTable(NBTTagCompound nbt, String key) {
        this();
        NBTTagList dropTableNBT = nbt.getTagList(key, 10);
        for (int i = 0; i < dropTableNBT.tagCount(); i++) {
            NBTTagCompound drop = dropTableNBT.getCompoundTagAt(i);
            dropTable.merge(
                readItemStackFromNBT(drop.getCompoundTag(NBT_DROP_TABLE_ITEM_KEY)),
                drop.getDouble(NBT_DROP_TABLE_COUNT_KEY),
                Double::sum);
        }
    }

    /**
     * Serialises the drop table to nbt
     *
     * @return The serialised drop table.
     */
    public NBTTagList save() {
        NBTTagList nbt = new NBTTagList();
        for (Map.Entry<ItemStack, Double> entry : this.dropTable.entrySet()) {
            NBTTagCompound entryNBT = new NBTTagCompound();
            entryNBT.setTag(NBT_DROP_TABLE_ITEM_KEY, writeItemStackToNBT(entry.getKey()));
            entryNBT.setDouble(NBT_DROP_TABLE_COUNT_KEY, entry.getValue());
            nbt.appendTag(entryNBT);
        }
        return nbt;
    }

    /**
     * Adds a drop to the drop table
     *
     * @param itemStack The item to add to the table.
     * @param amount    The amount to add to the table.
     */
    public void addDrop(ItemStack itemStack, double amount) {
        ItemStack key = itemStack.copy();
        key.stackSize = 1;
        this.dropTable.merge(key, amount, Double::sum);
    }

    /**
     * Adds the values from this drop table to another, but multiplies the amount by a random amount bound by variance.
     *
     * @param target   The drop table that you want to add the value to.
     * @param variance How much to vary the amounts of this drop table to, 0 < x < 1 plz
     * @param rand     The random source for the variance.
     */
    public void addTo(EIGDropTable target, double variance, Random rand) {
        this.addTo(target, 1.0, variance, rand);
    }

    /**
     * Adds the values from this drop table to another, but multiplies the amount by a multiplier and a random amount
     * bound by variance.
     *
     * @param target     The drop table that you want to add the value to.
     * @param multiplier A multiplier to apply to all amounts from this drop table.
     * @param variance   How much to vary the amounts of this drop table to, 0 < x < 1 plz.
     * @param rand       The random source for the variance.
     */
    public void addTo(EIGDropTable target, double multiplier, double variance, Random rand) {
        this.addTo(target, variance * (rand.nextDouble() - 0.5) * multiplier);
    }

    /**
     * Adds the values from this drop table to another.
     *
     * @param target The drop table that you want to add the value to.
     */
    public void addTo(EIGDropTable target) {
        this.addTo(target, 1.0);
    }

    /**
     * Adds the values from this drop table to another but multiplies the values by a multiplier.
     *
     * @param target     The drop table that you want to add the value to.
     * @param multiplier A multiplier to apply to all amounts from this drop table.
     */
    public void addTo(EIGDropTable target, double multiplier) {
        for (Map.Entry<ItemStack, Double> entry : this.dropTable.entrySet()) {
            target.dropTable.merge(entry.getKey(), entry.getValue() * multiplier, Double::sum);
        }
    }

    /**
     * Checks if the drop table is empty;
     *
     * @return true if empty.
     */
    public boolean isEmpty() {
        return this.dropTable.isEmpty();
    }

    public Set<Map.Entry<ItemStack, Double>> getEntries() {
        return this.dropTable.entrySet();
    }

}
