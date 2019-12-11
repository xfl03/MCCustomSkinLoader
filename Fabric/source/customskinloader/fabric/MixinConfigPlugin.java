package customskinloader.fabric;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class MixinConfigPlugin implements IMixinConfigPlugin {
    public static boolean hasITextureObject = true;

    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
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
        if (!hasITextureObject && targetClassName.equals("customskinloader.fake.FakeSkinManager")) {
            for (MethodNode mn : targetClass.methods) {
                if (mn.name.equals("loadSkin") && mn.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/class_1071$class_1072;)Lnet/minecraft/class_2960;")) {
                    for (AbstractInsnNode ain : mn.instructions.toArray()) {
                        if (ain instanceof MethodInsnNode) {
                            MethodInsnNode min = (MethodInsnNode) ain;
                            min.desc = min.desc.replace("Lnet/minecraft/class_1062;", "Lnet/minecraft/class_1044;");
                        }
                    }
                }
            }
        }
    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
