package customskinloader.forge;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.module.Configuration;
import java.lang.module.ModuleReference;
import java.lang.module.ResolvedModule;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import cpw.mods.cl.JarModuleFinder;
import cpw.mods.jarhandling.SecureJar;
import sun.misc.Unsafe;

public class MixinConfigPlugin extends customskinloader.mixin.core.MixinConfigPlugin {
    private final static MethodHandles.Lookup IMPL_LOOKUP = ((Supplier<MethodHandles.Lookup>) () -> {
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Unsafe theUnsafe = (Unsafe) theUnsafeField.get(null);
            Field implLookupField = MethodHandles.Lookup.class.getDeclaredField("IMPL_LOOKUP");
            return (MethodHandles.Lookup) theUnsafe.getObject(theUnsafe.staticFieldBase(implLookupField), theUnsafe.staticFieldOffset(implLookupField));
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }).get();

    static {
        try {
            fixMixinModifyArgs();
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    // Dynamically creates and configures a fake module to ensure the package (org.spongepowered.asm.synthetic.args) is correctly loaded.
    @SuppressWarnings("unchecked")
    private static void fixMixinModifyArgs() throws Throwable {
        ClassLoader loader = MixinConfigPlugin.class.getClassLoader();
        Map<String, MethodHandle> handles = findPackageLookup(loader.getClass());
        if (handles != null) {
            Map<String, Object> packageToOurModules = (Map<String, Object>) handles.get("packageLookup").invokeWithArguments(loader);
            String packageName = "org.spongepowered.asm.synthetic.args";
            if (packageToOurModules.get(packageName) == null) {
                Path moduleRoot = Paths.get("./CustomSkinLoader/FakeModule");
                Path classPath = Files.createDirectories(moduleRoot.resolve(packageName.replace('.', '/')));
                Path classFile = classPath.resolve("package-info.class");
                if (!Files.exists(classFile)) {
                    Files.createFile(classFile);
                }

                Configuration config = Configuration.resolve(JarModuleFinder.of(SecureJar.from(moduleRoot)), ImmutableList.of(ModuleLayer.boot().configuration()), JarModuleFinder.of(), ImmutableList.of("FakeModule"));
                ResolvedModule module = config.findModule("FakeModule").orElse(null);
                packageToOurModules.put(packageName, module);

                Object configuration = handles.get("configuration").invokeWithArguments(loader);
                Map<String, ResolvedModule> nameToModule = new HashMap<>((Map<String, ResolvedModule>) handles.get("nameToModuleGetter").invokeWithArguments(configuration));
                nameToModule.put("FakeModule", module);
                handles.get("nameToModuleSetter").invokeWithArguments(configuration, nameToModule);

                MethodHandle resolvedRootsGetter = handles.get("resolvedRoots");
                if (resolvedRootsGetter != null) {
                    ((Map<String, ModuleReference>) resolvedRootsGetter.invokeWithArguments(loader)).put("FakeModule", module.reference());
                }
            }
        }
    }

    private static Map<String, MethodHandle> findPackageLookup(Class<?> cl) throws Throwable {
        if (!ClassLoader.class.equals(cl)) {
            Map<String, MethodHandle> map = new HashMap<>();
            Field[] fields = cl.getDeclaredFields();
            for (Field field : fields) {
                Type type = field.getGenericType();
                if (type instanceof ParameterizedType) {
                    ParameterizedType paramType = (ParameterizedType) type;
                    if (Map.class.equals(paramType.getRawType())) {
                        Class<?> classJarModuleReference = findClass("cpw.mods.cl.JarModuleFinder$JarModuleReference");
                        if (Arrays.equals(new Class<?>[] { String.class, ResolvedModule.class }, paramType.getActualTypeArguments())) {
                            map.put("packageLookup", IMPL_LOOKUP.findGetter(cl, field.getName(), field.getType())); // Forge & NeoForge
                        } else if (Arrays.equals(new Class<?>[] { String.class, classJarModuleReference }, paramType.getActualTypeArguments())) {
                            map.put("resolvedRoots", IMPL_LOOKUP.findGetter(cl, field.getName(), field.getType())); // NeoForge
                        }
                    }
                } else if (Configuration.class.equals(field.getType())) {
                    map.put("configuration", IMPL_LOOKUP.findGetter(cl, field.getName(), field.getType())); // NeoForge
                    map.put("nameToModuleGetter", IMPL_LOOKUP.findGetter(field.getType(), "nameToModule", Map.class)); // NeoForge
                    map.put("nameToModuleSetter", IMPL_LOOKUP.findSetter(field.getType(), "nameToModule", Map.class)); // NeoForge
                }
            }
            if (!map.isEmpty()) {
                return map;
            }
            return findPackageLookup(cl.getSuperclass());
        }
        return null;
    }

    private static Class<?> findClass(String name) {
        try {
            return Class.forName(name);
        } catch (Throwable t) {
            return null;
        }
    }
}
