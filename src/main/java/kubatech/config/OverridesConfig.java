package kubatech.config;

import com.google.common.io.Files;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.util.*;
import kubatech.Tags;
import kubatech.api.ConstructableItemStack;
import kubatech.api.mobhandler.MobDrop;
import kubatech.api.utils.GSONUtils;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class OverridesConfig {

    private static final Logger LOG = LogManager.getLogger(Tags.MODID + "[Config-Overrides]");

    public static class MobDropSimplified {
        @GSONUtils.SkipGSON
        ItemStack stack;

        ConstructableItemStack stackConstructable;
        MobDrop.DropType type;

        private MobDropSimplified() {}

        public MobDropSimplified(ItemStack stack, MobDrop.DropType type) {
            stackConstructable = new ConstructableItemStack(stack);
            this.type = type;
        }

        public void reconstructStack() {
            stack = stackConstructable.construct();
        }

        public boolean isMatching(MobDrop drop) {
            return stackConstructable.isSame(drop.reconstructableStack, true);
        }

        private static final ByteBuf BufHelper = Unpooled.buffer();

        public void writeToByteBuf(ByteBuf byteBuf) {
            BufHelper.clear();
            stackConstructable.writeToByteBuf(BufHelper);
            BufHelper.writeInt(type.ordinal());
            byteBuf.writeInt(BufHelper.readableBytes());
            byteBuf.writeBytes(BufHelper);
        }

        public static MobDropSimplified readFromByteBuf(ByteBuf byteBuf) {
            MobDropSimplified mobDropSimplified = new MobDropSimplified();
            int size = byteBuf.readInt();
            mobDropSimplified.stackConstructable = ConstructableItemStack.readFromByteBuf(byteBuf);
            mobDropSimplified.type = MobDrop.DropType.get(byteBuf.readInt());
            mobDropSimplified.reconstructStack();
            return mobDropSimplified;
        }
    }

    public static class MobOverride {
        public boolean removeAll = false;
        public List<MobDrop> additions = new ArrayList<>();
        public List<MobDropSimplified> removals = new ArrayList<>();

        private static final ByteBuf BufHelper = Unpooled.buffer();

        public void writeToByteBuf(ByteBuf byteBuf) {
            BufHelper.clear();
            BufHelper.writeBoolean(removeAll);
            BufHelper.writeInt(additions.size());
            additions.forEach(drop -> drop.writeToByteBuf(BufHelper));
            BufHelper.writeInt(removals.size());
            removals.forEach(drop -> drop.writeToByteBuf(BufHelper));
            byteBuf.writeInt(BufHelper.readableBytes());
            byteBuf.writeBytes(BufHelper);
        }

        public static MobOverride readFromByteBuf(ByteBuf byteBuf) {
            int size = byteBuf.readInt();
            MobOverride mobOverride = new MobOverride();
            mobOverride.removeAll = byteBuf.readBoolean();
            int additionssize = byteBuf.readInt();
            for (int i = 0; i < additionssize; i++) mobOverride.additions.add(MobDrop.readFromByteBuf(byteBuf));
            int removalssize = byteBuf.readInt();
            for (int i = 0; i < removalssize; i++) mobOverride.removals.add(MobDropSimplified.readFromByteBuf(byteBuf));
            return mobOverride;
        }
    }

    public static Map<String, MobOverride> overrides = new HashMap<>();
    private static File overrideFile = null;

    private static final Gson gson = GSONUtils.GSON_BUILDER_PRETTY.create();

    @SuppressWarnings("UnstableApiUsage")
    public static void LoadConfig() {
        if (overrideFile == null) overrideFile = Config.getConfigFile("MobOverrides.cfg");
        if (!overrideFile.exists()) writeExampleFile();
        Reader reader = null;
        try {
            reader = Files.newReader(overrideFile, StandardCharsets.UTF_8);
            overrides = gson.fromJson(reader, new TypeToken<Map<String, MobOverride>>() {}.getType());
            overrides.remove("ExampleMob");
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private static void writeExampleFile() {
        Writer writer = null;
        try {
            writer = Files.newWriter(overrideFile, StandardCharsets.UTF_8);
            Map<String, MobOverride> example = new HashMap<>(1);
            MobOverride ex1 = new MobOverride();
            ex1.removals.add(new MobDropSimplified(new ItemStack(Items.rotten_flesh, 1), MobDrop.DropType.Normal));
            HashMap<Integer, Integer> exdamages = new HashMap<>(3);
            exdamages.put(1, 1);
            exdamages.put(2, 5);
            exdamages.put(3, 10);
            ex1.additions.add(
                    new MobDrop(new ItemStack(Items.diamond_sword), MobDrop.DropType.Rare, 500, 20, exdamages));
            example.put("ExampleMob", ex1);
            gson.toJson(example, writer);
            writer.flush();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            if (writer != null)
                try {
                    writer.close();
                } catch (Exception ignored) {
                }
        }
    }
}
