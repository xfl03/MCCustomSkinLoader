package customskinloader.fabric;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (!mixinClassName.equals("customskinloader.mixin.MixinThreadDownloadImageData")) {
            return true;
        }

        // ProcessTask didn't exist in ThreadDownloadImageData before 1.15-snapshot, Mixin shouldn't apply this.
        try (InputStream is = this.getClass().getResourceAsStream("/" + targetClassName.replace(".", "/") + ".class")) {
            if (is == null) {
                return true;
            }
            ClassNode cn = new ClassNode();
            new ClassReader(IOUtils.toByteArray(is)).accept(cn, 0);
            for (FieldNode fn : cn.fields) {
                if (fn.desc.equals(Type.getDescriptor(Runnable.class))) {
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    // To be compatible with 0.7.11
    public void preApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    // To be compatible with 0.7.11
    public void postApply(String targetClassName, org.spongepowered.asm.lib.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
