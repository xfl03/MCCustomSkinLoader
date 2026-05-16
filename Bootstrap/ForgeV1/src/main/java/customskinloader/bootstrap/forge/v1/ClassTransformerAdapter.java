package customskinloader.bootstrap.forge.v1;

import net.minecraft.launchwrapper.IClassTransformer;

public final class ClassTransformerAdapter implements IClassTransformer {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null) {
            return null;
        }

        String className = transformedName != null && !transformedName.isEmpty() ? transformedName : name;
        return TransformerBootstrap.transform(className.replace('.', '/'), basicClass);
    }
}
