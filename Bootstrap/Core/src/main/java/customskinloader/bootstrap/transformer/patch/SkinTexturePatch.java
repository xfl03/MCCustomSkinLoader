package customskinloader.bootstrap.transformer.patch;

import customskinloader.bootstrap.transformer.ClassTransformationContext;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class SkinTexturePatch extends PatchSupport {
    public SkinTexturePatch() {
        super("customskinloader:skin-texture-patch", 1000);

        // 24w45a- (1.21.3-)
        this.rule("http-texture.uploaded", HTTP_TEXTURE, "[,768],[801,803],[0x40000001,0x400000DD]", context ->
            this.makeFieldPublicNonFinal(context.findField(HTTP_TEXTURE, "uploaded")));

        // 19w38a ~ 24w45a (1.15 ~ 1.21.3)
        this.rule("http-texture.process-legacy-skin", HTTP_TEXTURE, "[554,768],[801,803],[0x40000001,0x400000DD]", context -> {
            MethodNode load = context.findMethod(HTTP_TEXTURE, "load", "(" + objectDesc(INPUT_STREAM) + ")" + objectDesc(NATIVE_IMAGE));
            if (load == null) {
                return false;
            }

            boolean modified = false;
            String owner = context.remapClassName(HTTP_TEXTURE);
            String originalName = context.remapMethodName(HTTP_TEXTURE, "processLegacySkin", "(" + objectDesc(NATIVE_IMAGE) + ")" + objectDesc(NATIVE_IMAGE));
            String imageDesc = context.remapMethodDescriptor("(" + objectDesc(NATIVE_IMAGE) + ")" + objectDesc(NATIVE_IMAGE));
            String conditionDesc = context.remapMethodDescriptor("(" + objectDesc(RUNNABLE) + ")Z");
            String replacementDesc = context.remapMethodDescriptor("(" + objectDesc(NATIVE_IMAGE) + objectDesc(RUNNABLE) + ")" + objectDesc(NATIVE_IMAGE));
            String processTaskField = context.remapFieldName(HTTP_TEXTURE, "onDownloaded");
            for (AbstractInsnNode instruction : load.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }

                MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                if (!owner.equals(methodInsnNode.owner) || !originalName.equals(methodInsnNode.name) || !imageDesc.equals(methodInsnNode.desc)) {
                    continue;
                }

                load.instructions.insertBefore(instruction, new VarInsnNode(ALOAD, 0));
                load.instructions.insertBefore(instruction, new FieldInsnNode(GETFIELD, owner, processTaskField, objectDesc(RUNNABLE)));
                MethodInsnNode processLegacySkin = new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_BUFFER, "processLegacySkin", replacementDesc, false);
                this.replaceInstructionSafely(load, new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_BUFFER, "shouldProcessLegacySkin", conditionDesc, false), IFNE, instruction, processLegacySkin);

                InsnList injection = new InsnList();
                if (methodInsnNode.getOpcode() == INVOKESTATIC) {
                    injection.add(new VarInsnNode(ALOAD, 0));
                } else {
                    injection.add(new InsnNode(SWAP));
                }
                injection.add(new FieldInsnNode(GETFIELD, owner, processTaskField, objectDesc(RUNNABLE)));
                load.instructions.insertBefore(processLegacySkin, injection);
                modified = true;
            }
            return modified;
        });

        // 24w46a+ (1.21.4+)
        this.rule("skin-texture-downloader.then-compose", SKIN_TEXTURE_DOWNLOADER, "[769,800],[804,0x40000000],[0x400000DE,]", context -> {
            MethodNode methodNode = context.findMethod(SKIN_TEXTURE_DOWNLOADER, "downloadAndRegisterSkin",
                "(" + objectDesc(IDENTIFIER) + objectDesc(PATH) + objectDesc(STRING) + "Z)" + objectDesc(COMPLETABLE_FUTURE));
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
                if (!COMPLETABLE_FUTURE.equals(methodInsnNode.owner) || !"thenCompose".equals(methodInsnNode.name)
                    || !("(" + objectDesc(FUNCTION) + ")" + objectDesc(COMPLETABLE_FUTURE)).equals(methodInsnNode.desc)) {
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
        });

        // 24w46a ~ 25w37a (1.21.4 ~ 1.21.8)
        this.rule("skin-texture-downloader.process-legacy-skin.v1", SKIN_TEXTURE_DOWNLOADER, "[769,772],[0x400000DE,0x4000010C]", context ->
            this.replaceSkinTextureDownloaderProcessLegacySkin(
                context, context.findMethod(SKIN_TEXTURE_DOWNLOADER, "lambda$downloadAndRegisterSkin$0",
                    "(" + objectDesc(PATH) + objectDesc(STRING) + "Z)" + objectDesc(NATIVE_IMAGE))));
        // 1.21.9-pre1+ (1.21.9+)
        this.rule("skin-texture-downloader.process-legacy-skin.v2", SKIN_TEXTURE_DOWNLOADER, "[773,800],[804,0x40000000],[0x4000010D,]", context ->
            this.replaceSkinTextureDownloaderProcessLegacySkin(
                context, context.findMethod(SKIN_TEXTURE_DOWNLOADER, "lambda$downloadAndRegisterSkin$0",
                    "(" + objectDesc(PATH) + objectDesc(CLIENT_ASSET_DOWNLOADED_TEXTURE) + "Z)" + objectDesc(NATIVE_IMAGE))));
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
            if (methodInsnNode.getOpcode() == INVOKESTATIC && owner.equals(methodInsnNode.owner) && originalName.equals(methodInsnNode.name) && originalDesc.equals(methodInsnNode.desc)) {
                this.replaceInstructionSafely(methodNode, instruction, new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_BUFFER, "processLegacySkin", originalDesc, false));
                modified = true;
            }
        }
        return modified;
    }
}
