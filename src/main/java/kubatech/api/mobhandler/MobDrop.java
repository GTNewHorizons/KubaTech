package kubatech.api.mobhandler;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.util.HashMap;
import kubatech.api.ConstructableItemStack;
import kubatech.api.utils.GSONUtils;
import net.minecraft.item.ItemStack;

public class MobDrop {
    public enum DropType {
        Normal,
        Rare,
        Additional,
        Infernal;
        private static final DropType[] values = values();

        public static DropType get(int ordinal) {
            return values[ordinal];
        }
    }

    @GSONUtils.SkipGSON
    public ItemStack stack;

    public ConstructableItemStack reconstructableStack;
    public DropType type;
    public int chance;
    public Integer enchantable;
    public HashMap<Integer, Integer> damages;
    public boolean playerOnly = false;

    private MobDrop() {}

    public MobDrop(
            ItemStack stack,
            DropType type,
            int chance,
            Integer enchantable,
            HashMap<Integer, Integer> damages,
            boolean playerOnly) {
        this.stack = stack;
        this.reconstructableStack = new ConstructableItemStack(stack);
        this.type = type;
        this.chance = chance;
        this.enchantable = enchantable;
        this.damages = damages;
        this.playerOnly = playerOnly;
    }

    public void reconstructStack() {
        this.stack = reconstructableStack.construct();
    }

    private static final ByteBuf BufHelper = Unpooled.buffer();

    public void writeToByteBuf(ByteBuf byteBuf) {
        BufHelper.clear();
        reconstructableStack.writeToByteBuf(BufHelper);
        BufHelper.writeInt(type.ordinal());
        BufHelper.writeInt(chance);
        BufHelper.writeBoolean(enchantable != null);
        if (enchantable != null) BufHelper.writeInt(enchantable);
        BufHelper.writeBoolean(damages != null);
        if (damages != null) {
            BufHelper.writeInt(damages.size());
            damages.forEach((k, v) -> {
                BufHelper.writeInt(k);
                BufHelper.writeInt(v);
            });
        }
        BufHelper.writeBoolean(playerOnly);
        byteBuf.writeInt(BufHelper.readableBytes());
        byteBuf.writeBytes(BufHelper);
    }

    public static MobDrop readFromByteBuf(ByteBuf byteBuf) {
        MobDrop mobDrop = new MobDrop();
        int size = byteBuf.readInt();
        mobDrop.reconstructableStack = ConstructableItemStack.readFromByteBuf(byteBuf);
        mobDrop.type = DropType.get(byteBuf.readInt());
        mobDrop.chance = byteBuf.readInt();
        if (byteBuf.readBoolean()) mobDrop.enchantable = byteBuf.readInt();
        else mobDrop.enchantable = null;
        if (byteBuf.readBoolean()) {
            mobDrop.damages = new HashMap<>();
            int damagessize = byteBuf.readInt();
            for (int i = 0; i < damagessize; i++) mobDrop.damages.put(byteBuf.readInt(), byteBuf.readInt());
        } else mobDrop.damages = null;
        mobDrop.playerOnly = byteBuf.readBoolean();
        mobDrop.reconstructStack();
        return mobDrop;
    }
}
