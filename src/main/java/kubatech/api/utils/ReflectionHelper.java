package kubatech.api.utils;

import java.lang.reflect.Field;
import java.util.HashMap;

public class ReflectionHelper {
    private static final HashMap<String, HashMap<String, Field>> fields = new HashMap<>();

    public static <T> T getField(Object obj, String fieldName, boolean useBasicTypes, T defaultvalue) {
        Class<?> cl = obj.getClass();
        String clName = cl.getName();
        HashMap<String, Field> classmap = fields.computeIfAbsent(clName, s -> new HashMap<>());
        try {
            if (classmap.containsKey(fieldName)) {
                return (T) classmap.get(fieldName).get(obj);
            }
            boolean exceptionDetected = false;
            Field f = null;
            do {
                try {
                    f = cl.getDeclaredField(fieldName);
                    f.setAccessible(true);
                } catch (Exception ex) {
                    exceptionDetected = true;
                    cl = cl.getSuperclass();
                }
            } while (exceptionDetected && !cl.equals(Object.class));
            if (f == null) return defaultvalue;
            classmap.put(fieldName, f);
            return (T) f.get(obj);
        } catch (Exception ex) {
            return defaultvalue;
        }
    }

    public static <T> T getField(Object obj, String fieldName, boolean useBasicTypes) {
        return getField(obj, fieldName, useBasicTypes, null);
    }

    public static <T> T getField(Object obj, String fieldName) {
        return getField(obj, fieldName, true, null);
    }
}
