package customskinloader.bootstrap.util;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

public class ReflectionUtils {
    private static final Map<String, Class<?>> CLASSES = new ConcurrentHashMap<>();
    private static final Map<Pair<Class<?>, String>, Field> FIELDS = new ConcurrentHashMap<>();
    private static final Map<Triple<Class<?>, String, List<Class<?>>>, Method> METHODS = new ConcurrentHashMap<>();

    public static Class<?> findClass(String className) {
        return CLASSES.computeIfAbsent(className, k -> {
            try {
                return Class.forName(k);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Field findField(Class<?> clazz, String fieldName) {
        return FIELDS.computeIfAbsent(Pair.of(clazz, fieldName), k -> {
            try {
                Field f = k.getLeft().getDeclaredField(k.getRight());
                f.setAccessible(true);
                return f;
            } catch (NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... parameterTypes) {
        return METHODS.computeIfAbsent(Triple.of(clazz, methodName, Arrays.asList(parameterTypes)), k -> {
            try {
                Method m = k.getLeft().getDeclaredMethod(k.getMiddle(), k.getRight().toArray(new Class<?>[0]));
                m.setAccessible(true);
                return m;
            } catch (NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
