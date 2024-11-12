package customskinloader.fabric;

import java.lang.reflect.Field;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.game.patch.GameTransformer;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;

public class DevEnvRemapper extends SimpleRemapper {
    // key: correct method owner names,
    // value:
    //     left: fake method owner names,
    //     right: other classes which contain fake methods.
    private static Map<String, Map.Entry<List<String>, List<String>>> remappedClasses = new HashMap<>();

    static {
        remappedClasses.put(
            "net.minecraft.class_310",
            new AbstractMap.SimpleEntry<>(
                Lists.newArrayList("customskinloader.fake.itf.IFakeMinecraft"),
                Lists.newArrayList("customskinloader.fake.itf.FakeInterfaceManager")
            )
        );
        remappedClasses.put(
            "net.minecraft.class_1011",
            new AbstractMap.SimpleEntry<>(
                Lists.newArrayList("customskinloader.fake.itf.IFakeNativeImage"),
                Lists.newArrayList("customskinloader.fake.itf.FakeInterfaceManager")
            )
        );
        remappedClasses.put(
            "net.minecraft.class_3298",
            new AbstractMap.SimpleEntry<>(
                Lists.newArrayList("customskinloader.fake.itf.IFakeIResource$V1", "customskinloader.fake.itf.IFakeIResource$V2"),
                Lists.newArrayList("customskinloader.fake.itf.FakeInterfaceManager")
            )
        );
        remappedClasses.put(
            "net.minecraft.class_3300",
            new AbstractMap.SimpleEntry<>(
                Lists.newArrayList("customskinloader.fake.itf.IFakeIResourceManager"),
                Lists.newArrayList("customskinloader.fake.itf.FakeInterfaceManager")
            )
        );
        remappedClasses.put(
            "net.minecraft.class_1071$class_8686",
            new AbstractMap.SimpleEntry<>(
                Lists.newArrayList("customskinloader.fake.itf.IFakeSkinManagerCacheKey"),
                Lists.newArrayList("customskinloader.fake.itf.FakeInterfaceManager")
            )
        );
    }

    @SuppressWarnings("unchecked")
    public static void initRemapper() {
        try {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Field patchedClassesField = GameTransformer.class.getDeclaredField("patchedClasses");
                patchedClassesField.setAccessible(true);
                Map<String, byte[]> patchedClasses = (Map<String, byte[]>) patchedClassesField.get(FabricLoaderImpl.INSTANCE.getGameProvider().getEntrypointTransformer());

                for (Map.Entry<String, Map.Entry<List<String>, List<String>>> entry : remappedClasses.entrySet()) {
                    List<String> targetClasses = new ArrayList<>(entry.getValue().getKey());
                    targetClasses.addAll(entry.getValue().getValue());
                    for (String clazz : targetClasses) {
                        byte[] classBytes = patchedClasses.get(clazz);
                        if (classBytes == null) {
                            classBytes = IOUtils.toByteArray(Objects.requireNonNull(cl.getResourceAsStream(clazz.replace(".", "/") + ".class")));
                        }

                        patchedClasses.put(clazz, remapClass(entry.getKey(), classBytes));
                    }
                }
            }
        } catch (Throwable t) {
            MixinConfigPlugin.logger.warning(t);
        }
    }

    // Remap all method names with specific method owner name.
    public static byte[] remapClass(String owner, byte[] bytes) {
        ClassNode cn = new ClassNode();
        new ClassReader(bytes).accept(new ClassRemapper(cn, new DevEnvRemapper(owner)), ClassReader.EXPAND_FRAMES);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cn.accept(cw);
        return cw.toByteArray();
    }

    private final String owner;
    private final SimpleRemapper remapper = new SimpleRemapper(new HashMap<>()) {
        @Override
        public String map(String key) {
            return FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", Type.getType("L" + key + ";").getClassName()).replace(".", "/");
        }
    };

    public DevEnvRemapper(String owner) {
        super(new HashMap<>());
        this.owner = owner;
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        // Method desc should be unmapped in the development environment.
        desc = this.remapper.mapDesc(desc);

        String s = this.isFakeOwner(owner) ? FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", this.owner, name, desc) : name;
        return s == null ? name : s;
    }

    private boolean isFakeOwner(String owner) {
        return remappedClasses.get(this.owner).getKey().contains(owner.replace("/", "."));
    }
}
