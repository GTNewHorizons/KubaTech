/*
 * KubaTech - Gregtech Addon
 * Copyright (C) 2022  kuba6000
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <https://www.gnu.org/licenses/>.
 *
 */

package kubatech.api.utils;

import com.google.gson.*;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;

public class GSONUtils {
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.FIELD)
    public @interface SkipGSON {}

    private static final ExclusionStrategy GSONStrategy = new ExclusionStrategy() {
        @Override
        public boolean shouldSkipField(FieldAttributes f) {
            return f.getAnnotation(SkipGSON.class) != null;
        }

        @Override
        public boolean shouldSkipClass(Class<?> clazz) {
            return false;
        }
    };

    private static final JsonSerializer<NBTTagCompound> NBTTagCompoundSerializer =
            new JsonSerializer<NBTTagCompound>() {

                @Override
                public JsonElement serialize(NBTTagCompound src, Type typeOfSrc, JsonSerializationContext context) {
                    try {
                        JsonArray array = new JsonArray();
                        for (byte b : CompressedStreamTools.compress(src)) {
                            array.add(new JsonPrimitive(b));
                        }
                        return array;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

    private static final JsonDeserializer<NBTTagCompound> NBTTagCompoundDeserializer =
            new JsonDeserializer<NBTTagCompound>() {
                @Override
                public NBTTagCompound deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                        throws JsonParseException {
                    try {
                        if (!(json instanceof JsonArray)) return null;
                        byte[] bytes = new byte[((JsonArray) json).size()];
                        for (int i = 0; i < bytes.length; i++)
                            bytes[i] = ((JsonArray) json).get(i).getAsByte();
                        return CompressedStreamTools.func_152457_a(bytes, new NBTSizeTracker(2097152L));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            };

    public static final GsonBuilder GSON_BUILDER = new GsonBuilder()
            .addSerializationExclusionStrategy(GSONStrategy)
            .addDeserializationExclusionStrategy(GSONStrategy)
            .registerTypeAdapter(NBTTagCompound.class, NBTTagCompoundDeserializer)
            .registerTypeAdapter(NBTTagCompound.class, NBTTagCompoundSerializer)
            .serializeNulls();
    public static final GsonBuilder GSON_BUILDER_PRETTY = new GsonBuilder()
            .addSerializationExclusionStrategy(GSONStrategy)
            .addDeserializationExclusionStrategy(GSONStrategy)
            .registerTypeAdapter(NBTTagCompound.class, NBTTagCompoundDeserializer)
            .registerTypeAdapter(NBTTagCompound.class, NBTTagCompoundSerializer)
            .serializeNulls()
            .setPrettyPrinting();

    public static <T> T readFile(Gson gson, File file, Class<T> tClass) {
        if (!file.exists()) return null;
        if (!file.isFile()) return null;
        T t = null;
        Reader reader = null;
        try {
            reader = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
            t = gson.fromJson(reader, tClass);
        } catch (Exception ignored) {
        } finally {
            if (reader != null)
                try {
                    reader.close();
                } catch (Exception ignored) {
                }
        }
        return t;
    }
}
