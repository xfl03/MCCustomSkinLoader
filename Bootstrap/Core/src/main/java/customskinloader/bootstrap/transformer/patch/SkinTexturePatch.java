package customskinloader.bootstrap.transformer.patch;

import customskinloader.bootstrap.transformer.ClassTransformationContext;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class SkinTexturePatch extends PatchSupport {
    public SkinTexturePatch() {
        super(
            "customskinloader:skin-texture-patch", 1000,
            targets(
                HTTP_TEXTURE, "[,768],[801,803],[0x40000001,0x400000DD]", // 24w45a- (1.21.3-)
                SKIN_TEXTURE_DOWNLOADER, "[769,800],[804,0x40000000],[0x400000DE,]" // 24w46a+ (1.21.4+)
            )
        );
    }

    @Override
    public boolean transform(ClassTransformationContext context) {
        boolean modified = false;

        if (context.isTarget(HTTP_TEXTURE)) {
            // 24w45a- (1.21.3-)
            modified |= this.applyIfMatches("[,768],[801,803],[0x40000001,0x400000DD]", "http-texture.uploaded", () -> this.makeFieldPublicNonFinal(context.findField(HTTP_TEXTURE, "uploaded")));
            // 19w38a ~ 24w45a (1.15 ~ 1.21.3)
            modified |= this.applyIfMatches("[554,768],[801,803],[0x40000001,0x400000DD]", "http-texture.patch", () -> this.patchHttpTexture(context));
        }
        if (context.isTarget(SKIN_TEXTURE_DOWNLOADER)) {
            modified |= this.requireModified("skin-texture-downloader.patch", this.patchSkinTextureDownloader(context));
        }

        return modified;
    }

    private boolean patchHttpTexture(ClassTransformationContext context) {
        return this.replaceHttpTextureProcessLegacySkin(
            context, context.findMethod(HTTP_TEXTURE, "load", "(" + objectDesc(INPUT_STREAM) + ")" + objectDesc(NATIVE_IMAGE))
        );
    }

    private boolean patchSkinTextureDownloader(ClassTransformationContext context) {
        boolean modified = false;
        modified |= this.requireModified("skin-texture-downloader.then-compose", this.replaceThenComposeTextureFunction(
            context, context.findMethod(SKIN_TEXTURE_DOWNLOADER, "downloadAndRegisterSkin", "(" + objectDesc(IDENTIFIER) + objectDesc(PATH) + objectDesc(STRING) + "Z)" + objectDesc(COMPLETABLE_FUTURE))
        ));
        // 24w46a+ (1.21.4+)
        modified |= this.applyIfMatches("[769,800],[804,0x40000000],[0x400000DE,]", "skin-texture-downloader.process-legacy-skin", () -> replaceSkinTextureDownloaderProcessLegacySkin(context));
        return modified;
    }

    private boolean replaceThenComposeTextureFunction(ClassTransformationContext context, MethodNode methodNode) {
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        int locationLocal = (methodNode.access & ACC_STATIC) == 0 ? 1 : 0;
        int booleanLocal = locationLocal + 3;
        String remappedHookDesc = context.remapMethodDescriptor("(" + objectDesc(FUNCTION) + objectDesc(IDENTIFIER) + "Z)" + objectDesc(FUNCTION));
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!COMPLETABLE_FUTURE.equals(methodInsnNode.owner) || !"thenCompose".equals(methodInsnNode.name) || !("(" + objectDesc(FUNCTION) + ")" + objectDesc(COMPLETABLE_FUTURE)).equals(methodInsnNode.desc)) {
                continue;
            }

            InsnList injection = new InsnList();
            injection.add(new VarInsnNode(ALOAD, locationLocal));
            injection.add(new VarInsnNode(ILOAD, booleanLocal));
            injection.add(new MethodInsnNode(INVOKESTATIC, FAKE_HTTP_TEXTURE_V2, "createTexture", remappedHookDesc, false));
            methodNode.instructions.insertBefore(instruction, injection);
            modified = true;
        }
        return modified;
    }

    private boolean replaceSkinTextureDownloaderProcessLegacySkin(ClassTransformationContext context) {
        boolean modified = false;
        // 24w46a ~ 25w37a (1.21.4 ~ 1.21.8)
        modified |= this.applyIfMatches("[769,772],[0x400000DE,0x4000010C]", "skin-texture-downloader.process-legacy-skin.string", () -> replaceSkinTextureDownloaderProcessLegacySkin(
            context, context.findMethod(SKIN_TEXTURE_DOWNLOADER, "lambda$downloadAndRegisterSkin$0", "(" + objectDesc(PATH) + objectDesc(STRING) + "Z)" + objectDesc(NATIVE_IMAGE))
        ));
        // 1.21.9-pre1+ (1.21.9+)
        modified |= this.applyIfMatches("[773,800],[804,0x40000000],[0x4000010D,]", "skin-texture-downloader.process-legacy-skin.downloaded-texture", () -> replaceSkinTextureDownloaderProcessLegacySkin(
            context, context.findMethod(SKIN_TEXTURE_DOWNLOADER, "lambda$downloadAndRegisterSkin$0", "(" + objectDesc(PATH) + objectDesc(CLIENT_ASSET_DOWNLOADED_TEXTURE) + "Z)" + objectDesc(NATIVE_IMAGE))
        ));
        return modified;
    }

    private boolean replaceSkinTextureDownloaderProcessLegacySkin(ClassTransformationContext context, MethodNode methodNode) {
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        String owner = context.remapClassName(SKIN_TEXTURE_DOWNLOADER);
        String originalDesc = context.remapMethodDescriptor("(" + objectDesc(NATIVE_IMAGE) + objectDesc(STRING) + ")" + objectDesc(NATIVE_IMAGE));
        String originalName = context.remapMethodName(SKIN_TEXTURE_DOWNLOADER, "processLegacySkin", "(" + objectDesc(NATIVE_IMAGE) + objectDesc(STRING) + ")" + objectDesc(NATIVE_IMAGE));
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (methodInsnNode.getOpcode() != INVOKESTATIC || !owner.equals(methodInsnNode.owner) || !originalName.equals(methodInsnNode.name) || !originalDesc.equals(methodInsnNode.desc)) {
                continue;
            }

            methodNodeSet(methodNode, instruction, new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_BUFFER, "processLegacySkin", originalDesc, false));
            modified = true;
        }
        return modified;
    }

    private boolean replaceHttpTextureProcessLegacySkin(ClassTransformationContext context, MethodNode load) {
        if (load == null) {
            return false;
        }

        boolean modified = false;
        String owner = context.remapClassName(HTTP_TEXTURE);
        String originalName = context.remapMethodName(HTTP_TEXTURE, "processLegacySkin", "(" + objectDesc(NATIVE_IMAGE) + ")" + objectDesc(NATIVE_IMAGE));
        String imageDesc = context.remapMethodDescriptor("(" + objectDesc(NATIVE_IMAGE) + ")" + objectDesc(NATIVE_IMAGE));
        String replacementDesc = context.remapMethodDescriptor("(" + objectDesc(NATIVE_IMAGE) + objectDesc(RUNNABLE) + objectDesc(FUNCTION) + ")" + objectDesc(NATIVE_IMAGE));
        String processTaskField = context.remapFieldName(HTTP_TEXTURE, "onDownloaded");

        for (AbstractInsnNode instruction : load.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!owner.equals(methodInsnNode.owner) || !originalName.equals(methodInsnNode.name) || !imageDesc.equals(methodInsnNode.desc)) {
                continue;
            }

            boolean staticProcessLegacySkin = methodInsnNode.getOpcode() == INVOKESTATIC;
            InsnList injection = new InsnList();
            int implementationHandleTag;
            String functionFactoryDesc;
            if (staticProcessLegacySkin) {
                injection.add(new VarInsnNode(ALOAD, 0));
                injection.add(new FieldInsnNode(GETFIELD, owner, processTaskField, objectDesc(RUNNABLE)));
                implementationHandleTag = H_INVOKESTATIC;
                functionFactoryDesc = "()" + objectDesc(FUNCTION);
            } else {
                injection.add(new InsnNode(SWAP));
                injection.add(new FieldInsnNode(GETFIELD, owner, processTaskField, objectDesc(RUNNABLE)));
                injection.add(new VarInsnNode(ALOAD, 0));
                implementationHandleTag = H_INVOKEVIRTUAL;
                functionFactoryDesc = "(" + objectDesc(owner) + ")" + objectDesc(FUNCTION);
            }
            injection.add(new InvokeDynamicInsnNode(
                "apply", functionFactoryDesc,
                new Handle(H_INVOKESTATIC, LAMBDA_METAFACTORY, "metafactory", "(" + objectDesc(METHOD_HANDLES_LOOKUP) + objectDesc(STRING) + objectDesc(METHOD_TYPE) + objectDesc(METHOD_TYPE) + objectDesc(METHOD_HANDLE) + objectDesc(METHOD_TYPE) + ")" + objectDesc(CALL_SITE), false),
                Type.getType("(" + objectDesc(OBJECT) + ")" + objectDesc(OBJECT)),
                new Handle(implementationHandleTag, owner, originalName, imageDesc, false),
                Type.getType(imageDesc)
            ));
            load.instructions.insertBefore(instruction, injection);
            methodNodeSet(load, instruction, new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_BUFFER, "processLegacySkin", replacementDesc, false));
            modified = true;
        }
        return modified;
    }
}
