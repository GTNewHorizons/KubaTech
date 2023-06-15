package kubatech.api.utils;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import kubatech.mixin.mixins.minecraft.ItemStackAccessor;

public class ItemUtils {

    public static NBTTagCompound writeItemStackToNBT(ItemStack stack) {
        NBTTagCompound compound = new NBTTagCompound();

        compound.setInteger("id", Item.getIdFromItem(((ItemStackAccessor) (Object) stack).getItem()));
        compound.setInteger("Count", stack.stackSize);
        compound.setInteger("Damage", ((ItemStackAccessor) (Object) stack).getItemDamage());

        if (stack.stackTagCompound != null) compound.setTag("tag", stack.stackTagCompound);

        return compound;
    }

    public static ItemStack readItemStackFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("id", 2)) return ItemStack.loadItemStackFromNBT(compound);

        ItemStack stack = new ItemStack(
            Item.getItemById(compound.getInteger("id")),
            compound.getInteger("Count"),
            compound.getInteger("Damage"));

        if (compound.hasKey("tag", 10)) stack.stackTagCompound = compound.getCompoundTag("tag");

        return stack;
    }
}
