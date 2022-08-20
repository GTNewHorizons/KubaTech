package kubatech.api;

import cpw.mods.fml.common.registry.GameRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.nio.charset.StandardCharsets;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

public class ConstructableItemStack {
    public final GameRegistry.UniqueIdentifier itemIdentifier;
    public final int meta;
    public final int size;
    public final NBTTagCompound tagCompound;

    private ConstructableItemStack(
            GameRegistry.UniqueIdentifier itemIdentifier, int meta, int size, NBTTagCompound tagCompound) {
        this.itemIdentifier = itemIdentifier;
        this.meta = meta;
        this.size = size;
        this.tagCompound = tagCompound;
    }

    public ConstructableItemStack(ItemStack stack) {
        itemIdentifier = GameRegistry.findUniqueIdentifierFor(stack.getItem());
        meta = stack.getItemDamage();
        size = stack.stackSize;
        tagCompound = stack.stackTagCompound;
    }

    public ItemStack construct() {
        if (itemIdentifier == null) return null;
        Item it = GameRegistry.findItem(itemIdentifier.modId, itemIdentifier.name);
        if (it == null) return null;
        ItemStack stack = new ItemStack(it, size, meta);
        stack.stackTagCompound = tagCompound;
        return stack;
    }

    public boolean isSame(ConstructableItemStack stack, boolean ignoreSize) {
        if (!stack.itemIdentifier.modId.equals(itemIdentifier.modId)) return false;
        if (!stack.itemIdentifier.name.equals(itemIdentifier.name)) return false;
        return ignoreSize || stack.size == size;
    }

    private static final ByteBuf BufHelper = Unpooled.buffer();

    public void writeToByteBuf(ByteBuf byteBuf) {
        BufHelper.clear();
        byte[] bytes = itemIdentifier.modId.getBytes(StandardCharsets.UTF_8);
        BufHelper.writeInt(bytes.length);
        BufHelper.writeBytes(bytes);
        bytes = itemIdentifier.name.getBytes(StandardCharsets.UTF_8);
        BufHelper.writeInt(bytes.length);
        BufHelper.writeBytes(bytes);
        BufHelper.writeInt(meta);
        BufHelper.writeInt(size);
        BufHelper.writeBoolean(tagCompound != null);
        if (tagCompound != null) {
            try {
                bytes = CompressedStreamTools.compress(tagCompound);
            } catch (Exception ignored) {
                bytes = new byte[0];
            }
            BufHelper.writeInt(bytes.length);
            BufHelper.writeBytes(bytes);
        }
        byteBuf.writeInt(BufHelper.readableBytes());
        byteBuf.writeBytes(BufHelper);
    }

    public static ConstructableItemStack readFromByteBuf(ByteBuf byteBuf) {
        int size = byteBuf.readInt();
        byte[] bytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(bytes);
        String modid = new String(bytes, StandardCharsets.UTF_8);
        bytes = new byte[byteBuf.readInt()];
        byteBuf.readBytes(bytes);
        String name = new String(bytes, StandardCharsets.UTF_8);
        int meta = byteBuf.readInt();
        int stacksize = byteBuf.readInt();
        NBTTagCompound nbtTagCompound = null;
        if (byteBuf.readBoolean()) {
            bytes = new byte[byteBuf.readInt()];
            byteBuf.readBytes(bytes);
            try {
                nbtTagCompound = CompressedStreamTools.func_152457_a(bytes, new NBTSizeTracker(2097152L));
            } catch (Exception ignored) {
            }
        }
        return new ConstructableItemStack(
                new GameRegistry.UniqueIdentifier(modid + ":" + name), meta, stacksize, nbtTagCompound);
    }
}
