package customskinloader.fabric;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.Lists;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.entrypoint.EntrypointTransformer;
import net.fabricmc.loader.game.MinecraftGameProvider;
import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.SimpleRemapper;
import org.objectweb.asm.tree.ClassNode;

public class DevEnvRemapper extends SimpleRemapper {
    private static Map<String, List<String>> remappedClasses = new HashMap<>();

    static {
        // TODO: Remove hardcoded class names
        remappedClasses.put("net.minecraft.class_1060", Lists.newArrayList("customskinloader.fake.FakeSkinManager", "customskinloader.fake.itf.IFakeTextureManager_1", "customskinloader.fake.itf.IFakeTextureManager_2"));
    }

    @SuppressWarnings("unchecked")
    public static void initRemapper() {
        try {
            if (FabricLoader.getInstance().isDevelopmentEnvironment()) {
                ClassLoader cl = Thread.currentThread().getContextClassLoader();
                Field patchedClassesField = EntrypointTransformer.class.getDeclaredField("patchedClasses");
                patchedClassesField.setAccessible(true);
                Map<String, byte[]> patchedClasses = (Map<String, byte[]>) patchedClassesField.get(MinecraftGameProvider.TRANSFORMER);

                for (Map.Entry<String, List<String>> entry : remappedClasses.entrySet()) {
                    for (String clazz : entry.getValue()) {
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

    private boolean isGettingIntermediaryDesc = false;
    private final String owner;

    public DevEnvRemapper(String owner) {
        super(new HashMap<>());
        this.owner = owner;
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        // Method desc should be unmapped in the development environment.
        this.isGettingIntermediaryDesc = true;
        desc = this.mapDesc(desc);
        this.isGettingIntermediaryDesc = false;

        String s = FabricLoader.getInstance().getMappingResolver().mapMethodName("intermediary", this.owner, name, desc);
        return s == null ? name : s;
    }

    @Override
    public String map(String key) {
        if (this.isGettingIntermediaryDesc) {
            return FabricLoader.getInstance().getMappingResolver().unmapClassName("intermediary", Type.getType("L" + key + ";").getClassName()).replace(".", "/");
        }
        return super.map(key);
    }
}
