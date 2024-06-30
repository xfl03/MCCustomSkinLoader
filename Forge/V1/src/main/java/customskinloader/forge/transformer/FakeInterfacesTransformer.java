package customskinloader.forge.transformer;

import customskinloader.forge.TransformerManager;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class FakeInterfacesTransformer {
    public static class FakeInterfaceTransformer implements TransformerManager.IClassTransformer {
        @Override
        public ClassNode transform(ClassNode cn) {
            if (cn.access == 0) {
                cn.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT;
            }
            return cn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.Minecraft"
    )
    public static class MinecraftTransformer implements TransformerManager.IClassTransformer {
        @Override
        public ClassNode transform(ClassNode cn) {
            cn.interfaces.add("customskinloader/fake/itf/IFakeMinecraft");
            return cn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.renderer.IImageBuffer"
    )
    public static class IImageBufferTransformer implements TransformerManager.IClassTransformer {
        @Override
        public ClassNode transform(ClassNode cn) {
            cn.interfaces.add("customskinloader/fake/itf/IFakeIImageBuffer");
            return cn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.resources.IResource"
    )
    public static class ClientIResourceTransformer implements TransformerManager.IClassTransformer {
        @Override
        public ClassNode transform(ClassNode cn) {
            cn.interfaces.add("customskinloader/fake/itf/IFakeIResource$V1");
            cn.interfaces.add("customskinloader/fake/itf/IFakeIResource$V2");
            cn.interfaces.add("net/minecraft/resources/IResource");
            return cn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.client.resources.IResourceManager"
    )
    public static class ClientIResourceManagerTransformer implements TransformerManager.IClassTransformer {
        @Override
        public ClassNode transform(ClassNode cn) {
            cn.interfaces.add("customskinloader/fake/itf/IFakeIResourceManager");
            cn.interfaces.add("net/minecraft/resources/IResourceManager");
            return cn;
        }
    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.resources.IResource"
    )
    public static class IResourceTransformer extends FakeInterfacesTransformer.FakeInterfaceTransformer {

    }

    @TransformerManager.TransformTarget(
        className = "net.minecraft.resources.IResourceManager"
    )
    public static class IResourceManagerTransformer extends FakeInterfacesTransformer.FakeInterfaceTransformer {

    }
}
