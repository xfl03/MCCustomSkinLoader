package customskinloader.bootstrap.transformer.patch;

import java.util.function.Predicate;

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
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class SkinManagerPatch extends PatchSupport {
    public SkinManagerPatch() {
        super("customskinloader:skin-manager-patch", 1000);

        // 23w31a+ (1.20.2+)
        this.rule("skin-manager.cache-key-inner-class", SKIN_MANAGER, "[764,800],[804,0x40000000],[0x40000090,]", context ->
            this.makeInnerClassPublicNonFinal(context.getCurrentClassNode(), context.remapClassName(SKIN_MANAGER_CACHE_KEY)));

        // 1.20.1-
        this.rule("skin-manager.constructor.v1", SKIN_MANAGER, "[,763],[801,803],[0x40000001,0x4000008E]", context ->
            this.injectSetSkinCacheDir(context, "(" + objectDesc(TEXTURE_MANAGER) + objectDesc(FILE) + objectDesc(MINECRAFT_SESSION_SERVICE) + ")V", 2, objectDesc(FILE)));
        // 23w31a ~ 24w45a (1.20.2 ~ 1.21.3)
        this.rule("skin-manager.constructor.v2", SKIN_MANAGER, "[764,768],[0x40000090,0x400000DD]", context ->
            this.injectSetSkinCacheDir(context, "(" + objectDesc(TEXTURE_MANAGER) + objectDesc(PATH) + objectDesc(MINECRAFT_SESSION_SERVICE) + objectDesc(EXECUTOR) + ")V", 2, objectDesc(PATH)));
        // 24w46a ~ 25w33a (1.21.4 ~ 1.21.8)
        this.rule("skin-manager.constructor.v3", SKIN_MANAGER, "[769,772],[0x400000DE,0x40000106]", context ->
            this.injectSetSkinCacheDir(context, "(" + objectDesc(PATH) + objectDesc(MINECRAFT_SESSION_SERVICE) + objectDesc(EXECUTOR) + ")V", 1, objectDesc(PATH)));
        // 25w34a ~ 25w34b
        this.rule("skin-manager.constructor.v4", SKIN_MANAGER, "[0x40000107,0x40000108]", context ->
            this.injectSetSkinCacheDir(context, "(" + objectDesc(PATH) + objectDesc(SERVICES) + objectDesc(EXECUTOR) + ")V", 1, objectDesc(PATH)));
        // 25w35a+ (1.21.9+)
        this.rule("skin-manager.constructor.v5", SKIN_MANAGER, "[773,800],[804,0x40000000],[0x40000109,]", context ->
            this.injectSetSkinCacheDir(context, "(" + objectDesc(PATH) + objectDesc(SERVICES) + objectDesc(SKIN_TEXTURE_DOWNLOADER) + objectDesc(EXECUTOR) + ")V", 1, objectDesc(PATH)));

        // 19w37a- (1.14.4-)
        this.rule("skin-manager.register-texture.v1", SKIN_MANAGER, "[,553]", context -> {
            InsnList loadTextureType = new InsnList();
            loadTextureType.add(new VarInsnNode(ALOAD, 2));
            return this.redirectHttpTextureConstructorToFake(
                context,
                context.findMethod(SKIN_MANAGER, "registerTexture", "(" + objectDesc(MINECRAFT_PROFILE_TEXTURE) + objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE) + objectDesc(SKIN_MANAGER_SKIN_TEXTURE_CALLBACK) + ")" + objectDesc(IDENTIFIER)),
                context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + objectDesc(HTTP_TEXTURE_PROCESSOR) + ")V"),
                context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + objectDesc(HTTP_TEXTURE_PROCESSOR) + objectDesc(MINECRAFT_PROFILE_TEXTURE) + objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE) + ")V"),
                loadTextureType
            );
        });
        // 19w38a ~ 1.20.1 (1.15 ~ 1.20.1)
        this.rule("skin-manager.register-texture.v2", SKIN_MANAGER, "[554,763],[801,803],[0x40000001,0x4000008E]", context -> {
            InsnList loadTextureType = new InsnList();
            loadTextureType.add(new VarInsnNode(ALOAD, 2));
            return this.redirectHttpTextureConstructorToFake(
                context,
                context.findMethod(SKIN_MANAGER, "registerTexture", "(" + objectDesc(MINECRAFT_PROFILE_TEXTURE) + objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE) + objectDesc(SKIN_MANAGER_SKIN_TEXTURE_CALLBACK) + ")" + objectDesc(IDENTIFIER)),
                context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + "Z" + objectDesc(RUNNABLE) + ")V"),
                context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + "Z" + objectDesc(RUNNABLE) + objectDesc(MINECRAFT_PROFILE_TEXTURE) + objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE) + ")V"),
                loadTextureType
            );
        });

        // 19w37a- (1.14.4-)
        this.rule("skin-manager.register-skins.v1", SKIN_MANAGER, "[,553]", context ->
            this.replaceExecutorProfileLoadAtInvocation(
                context.findMethod(SKIN_MANAGER, "registerSkins", "(" + objectDesc(GAME_PROFILE) + objectDesc(SKIN_MANAGER_SKIN_TEXTURE_CALLBACK) + "Z)V"),
                EXECUTOR_SERVICE, "submit", "(" + objectDesc(RUNNABLE) + ")" + objectDesc(FUTURE)));
        // 19w38a ~ 1.18-exp7 (1.15 ~ 1.17.1)
        this.rule("skin-manager.register-skins.v2", SKIN_MANAGER, "[554,756],[801,803],[0x40000001,0x4000002F]+[2205,2831]", context ->
            this.replaceExecutorProfileLoadAtInvocation(
                context.findMethod(SKIN_MANAGER, "registerSkins", "(" + objectDesc(GAME_PROFILE) + objectDesc(SKIN_MANAGER_SKIN_TEXTURE_CALLBACK) + "Z)V"),
                EXECUTOR, "execute", "(" + objectDesc(RUNNABLE) + ")V"));
        // 21w37a ~ 1.20.1 (1.18 ~ 1.20.1)
        this.rule("skin-manager.register-skins.v3", SKIN_MANAGER, "[757,763],[0x40000029,0x4000008E]+[2834,3465]", context ->
            this.replaceExecutorProfileLoadAtInvocation(
                context.findMethod(SKIN_MANAGER, "registerSkins", "(" + objectDesc(GAME_PROFILE) + objectDesc(SKIN_MANAGER_SKIN_TEXTURE_CALLBACK) + "Z)V"),
                EXECUTOR_SERVICE, "execute", "(" + objectDesc(RUNNABLE) + ")V"));

        this.rule("skin-manager.get-insecure-skin-information", SKIN_MANAGER, "[,763],[801,803],[0x40000001,0x4000008E]", context -> {
            MethodNode methodNode = context.findMethod(SKIN_MANAGER, "getInsecureSkinInformation", "(" + objectDesc(GAME_PROFILE) + ")" + objectDesc(MAP));
            if (methodNode == null) {
                return false;
            }
            InsnList injection = new InsnList();
            injection.add(new VarInsnNode(ALOAD, 1));
            injection.add(new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "loadSkinFromCache", "(" + objectDesc(GAME_PROFILE) + ")" + objectDesc(MAP), false));
            injection.add(new InsnNode(ARETURN));
            methodNode.instructions.insert(injection);
            return true;
        });

        // 1.13.2 ~ 1.20.1
        this.rule("skin-manager.lambda-register-skins-4", SKIN_MANAGER, "[404,763],[801,803],[0x40000001,0x4000008E]", context ->
            this.replaceGetTexturesWithFakeUserProfile(context.findMethod(SKIN_MANAGER, "lambda$registerSkins$4",
                "(" + objectDesc(GAME_PROFILE) + "Z" + objectDesc(SKIN_MANAGER_SKIN_TEXTURE_CALLBACK) + ")V")));

        // 23w42a ~ 25w33a (1.20.3 ~ 1.21.8)
        this.rule("skin-manager.cache-key.v1", SKIN_MANAGER, "[765,772],[0x4000009D,0x40000106]", context ->
            this.redirectCacheKeyConstruction(context, context.findMethod(SKIN_MANAGER, "getOrLoad", "(" + objectDesc(GAME_PROFILE) + ")" + objectDesc(COMPLETABLE_FUTURE))));
        // 25w34a+ (1.21.9+)
        this.rule("skin-manager.cache-key.v2", SKIN_MANAGER, "[773,800],[804,0x40000000],[0x40000107,]", context ->
            this.redirectCacheKeyConstruction(context, context.findMethod(SKIN_MANAGER, "get", "(" + objectDesc(GAME_PROFILE) + ")" + objectDesc(COMPLETABLE_FUTURE))));

        // 23w31a+ (1.20.2+)
        this.rule("skin-manager-1.executor", SKIN_MANAGER_1, "[764,800],[804,0x40000000],[0x40000090,]", context -> {
            MethodNode methodNode = context.findMethod(SKIN_MANAGER_1, "load", "(" + objectDesc(SKIN_MANAGER_CACHE_KEY) + ")" + objectDesc(COMPLETABLE_FUTURE));
            if (methodNode == null) {
                return false;
            }
            boolean modified = false;
            for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                if (COMPLETABLE_FUTURE.equals(methodInsnNode.owner)
                    && (("supplyAsync".equals(methodInsnNode.name) && ("(" + objectDesc(SUPPLIER) + objectDesc(EXECUTOR) + ")" + objectDesc(COMPLETABLE_FUTURE)).equals(methodInsnNode.desc))
                    || ("thenComposeAsync".equals(methodInsnNode.name) && ("(" + objectDesc(FUNCTION) + objectDesc(EXECUTOR) + ")" + objectDesc(COMPLETABLE_FUTURE)).equals(methodInsnNode.desc)))) {
                    methodNode.instructions.insertBefore(instruction, new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "loadProfileTextures", "(" + objectDesc(EXECUTOR) + ")" + objectDesc(EXECUTOR), false));
                    modified = true;
                }
            }
            return modified;
        });

        // 23w31a ~ 23w41a (1.20.2)
        this.rule("skin-manager-1.lambda-load-0.v1", SKIN_MANAGER_1, "764,[0x40000090,0x4000009C]", context -> {
            MethodNode methodNode = context.findMethod(SKIN_MANAGER_1, "lambda$load$0", "(" + objectDesc(MINECRAFT_SESSION_SERVICE) + objectDesc(GAME_PROFILE) + ")" + objectDesc(SKIN_MANAGER_TEXTURE_INFO));
            if (methodNode == null) {
                return false;
            }
            boolean modified = false;
            for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) {
                    continue;
                }
                MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
                if (MINECRAFT_SESSION_SERVICE.equals(methodInsnNode.owner) && "getTextures".equals(methodInsnNode.name)
                    && ("(" + objectDesc(GAME_PROFILE) + "Z)" + objectDesc(MAP)).equals(methodInsnNode.desc)) {
                    this.replaceInstructionSafely(methodNode, instruction, new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "loadSkinFromCache", "(" + objectDesc(OBJECT) + objectDesc(GAME_PROFILE) + "Z)" + objectDesc(MAP), false));
                    modified = true;
                }
            }
            return modified;
        });
        // 23w42a ~ 25w33a (1.20.3 ~ 1.21.8)
        this.rule("skin-manager-1.lambda-load-0.v2", SKIN_MANAGER_1, "[765,772],[0x4000009D,0x40000106]", context ->
            this.replaceUnpackTexturesWithFakeSkinCache(context, context.findMethod(SKIN_MANAGER_1, "lambda$load$0",
                "(" + objectDesc(SKIN_MANAGER_CACHE_KEY) + objectDesc(MINECRAFT_SESSION_SERVICE) + ")" + objectDesc(MINECRAFT_PROFILE_TEXTURES)), MINECRAFT_SESSION_SERVICE));
        // 25w34a ~ 26.3-snapshot-1 (1.21.9 ~ 26.2)
        this.rule("skin-manager-1.lambda-load-0.v3", SKIN_MANAGER_1, "[773,776],[0x40000107,0x40000143]", context ->
            this.replaceUnpackTexturesWithFakeSkinCache(context, context.findMethod(SKIN_MANAGER_1, "lambda$load$0",
                "(" + objectDesc(SKIN_MANAGER_CACHE_KEY) + objectDesc(SERVICES) + ")" + objectDesc(MINECRAFT_PROFILE_TEXTURES)), MINECRAFT_SESSION_SERVICE));
        // 26.3-snapshot-2+ (26.3+)
        this.rule("skin-manager-1.lambda-load-0.v4", SKIN_MANAGER_1, "[777,800],[804,0x40000000],[0x40000144,]", context ->
            this.replaceUnpackTexturesWithFakeSkinCache(context, context.findMethod(SKIN_MANAGER_1, "lambda$load$0",
                "(" + objectDesc(SKIN_MANAGER_CACHE_KEY) + objectDesc(SERVICES) + ")" + objectDesc(MINECRAFT_PROFILE_TEXTURES)), SESSION_SERVICE));

        // 1.12.2-
        this.rule("skin-manager-3.get-user-profile", SKIN_MANAGER_3, "[,340]", context ->
            this.replaceGetTexturesWithFakeUserProfile(context.findMethod(SKIN_MANAGER_3, "run", "()V")));

        // 23w31a+ (1.20.2+)
        this.rule("skin-manager-cache-key.access", SKIN_MANAGER_CACHE_KEY, "[764,800],[804,0x40000000],[0x40000090,]", context -> {
            boolean modified = this.makeClassPublicNonFinal(context.getCurrentClassNode());
            modified |= this.makeMethodPublicNonFinal(context.findMethod(SKIN_MANAGER_CACHE_KEY, "<init>", "(" + objectDesc(UUID) + objectDesc(PROPERTY) + ")V"));
            return modified;
        });

        // 23w31a ~ 24w45a (1.20.2 ~ 1.21.3)
        this.rule("skin-manager-texture-cache.http-texture", SKIN_MANAGER_TEXTURE_CACHE, "[764,768],[0x40000090,0x400000DD]", context -> {
            InsnList loadType = new InsnList();
            loadType.add(new VarInsnNode(ALOAD, 0));
            loadType.add(new FieldInsnNode(GETFIELD, context.remapClassName(SKIN_MANAGER_TEXTURE_CACHE), context.remapFieldName(SKIN_MANAGER_TEXTURE_CACHE, "type"), objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE)));
            return this.redirectHttpTextureConstructorToFake(
                context,
                context.findMethod(SKIN_MANAGER_TEXTURE_CACHE, "registerTexture", "(" + objectDesc(MINECRAFT_PROFILE_TEXTURE) + ")" + objectDesc(COMPLETABLE_FUTURE)),
                context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + "Z" + objectDesc(RUNNABLE) + ")V"),
                context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + "Z" + objectDesc(RUNNABLE) + objectDesc(MINECRAFT_PROFILE_TEXTURE) + objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE) + ")V"),
                loadType
            );
        });
        // 24w46a ~ 25w34b (1.21.4 ~ 1.21.8)
        this.rule("skin-manager-texture-cache.skin-texture-downloader.v1", SKIN_MANAGER_TEXTURE_CACHE, "[769,772],[0x400000DE,0x40000108]", context ->
            this.redirectSkinTextureDownloaderToFake(context, context.findMethod(SKIN_MANAGER_TEXTURE_CACHE, "registerTexture",
                "(" + objectDesc(MINECRAFT_PROFILE_TEXTURE) + ")" + objectDesc(COMPLETABLE_FUTURE)), INVOKESTATIC));
        // 25w35a+ (1.21.9+)
        this.rule("skin-manager-texture-cache.skin-texture-downloader.v2", SKIN_MANAGER_TEXTURE_CACHE, "[773,800],[804,0x40000000],[0x40000109,]", context ->
            this.redirectSkinTextureDownloaderToFake(context, context.findMethod(SKIN_MANAGER_TEXTURE_CACHE, "registerTexture",
                "(" + objectDesc(MINECRAFT_PROFILE_TEXTURE) + ")" + objectDesc(COMPLETABLE_FUTURE)), INVOKEVIRTUAL));
    }

    private boolean injectSetSkinCacheDir(ClassTransformationContext context, String constructorDesc, int localIndex, String argumentDesc) {
        MethodNode methodNode = context.findMethod(SKIN_MANAGER, "<init>", constructorDesc);
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        String remappedArgumentDesc = context.remapMethodDescriptor("(" + argumentDesc + ")V");
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (instruction.getOpcode() != RETURN) {
                continue;
            }

            InsnList injection = new InsnList();
            injection.add(new VarInsnNode(ALOAD, localIndex));
            injection.add(new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "setSkinCacheDir", remappedArgumentDesc, false));
            methodNode.instructions.insertBefore(instruction, injection);
            modified = true;
        }
        return modified;
    }

    private boolean redirectHttpTextureConstructorToFake(ClassTransformationContext context, MethodNode methodNode, String originalDesc, String replacementDesc, InsnList loadTextureType) {
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        String originalOwner = context.remapClassName(HTTP_TEXTURE);
        String replacementOwner = context.remapClassName(FAKE_HTTP_TEXTURE_V1);
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (methodInsnNode.getOpcode() != INVOKESPECIAL || !originalOwner.equals(methodInsnNode.owner) || !"<init>".equals(methodInsnNode.name) || !originalDesc.equals(methodInsnNode.desc)) {
                continue;
            }

            AbstractInsnNode newNode = this.findPreviousTypeInstruction(methodInsnNode, NEW, originalOwner);
            if (!(newNode instanceof TypeInsnNode)) {
                continue;
            }

            ((TypeInsnNode) newNode).desc = replacementOwner;
            InsnList injection = new InsnList();
            injection.add(new VarInsnNode(ALOAD, 1));
            injection.add(this.clone(loadTextureType));
            methodNode.instructions.insertBefore(methodInsnNode, injection);
            methodInsnNode.owner = replacementOwner;
            methodInsnNode.desc = replacementDesc;
            modified = true;
        }
        return modified;
    }

    private boolean redirectSkinTextureDownloaderToFake(ClassTransformationContext context, MethodNode methodNode, int opcode) {
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        String owner = context.remapClassName(SKIN_TEXTURE_DOWNLOADER);
        String desc = context.remapMethodDescriptor("(" + objectDesc(IDENTIFIER) + objectDesc(PATH) + objectDesc(STRING) + "Z)" + objectDesc(COMPLETABLE_FUTURE));
        String name = context.remapMethodName(SKIN_TEXTURE_DOWNLOADER, "downloadAndRegisterSkin", "(" + objectDesc(IDENTIFIER) + objectDesc(PATH) + objectDesc(STRING) + "Z)" + objectDesc(COMPLETABLE_FUTURE));
        String replacementDesc = context.remapMethodDescriptor("(" + objectDesc(FUNCTION4) + objectDesc(IDENTIFIER) + objectDesc(PATH) + objectDesc(STRING) + "Z" + objectDesc(MINECRAFT_PROFILE_TEXTURE) + ")" + objectDesc(COMPLETABLE_FUTURE));
        String functionDesc = context.remapMethodDescriptor("(" + objectDesc(IDENTIFIER) + objectDesc(PATH) + objectDesc(STRING) + objectDesc(BOOLEAN) + ")" + objectDesc(COMPLETABLE_FUTURE));
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (methodInsnNode.getOpcode() != opcode || !owner.equals(methodInsnNode.owner) || !name.equals(methodInsnNode.name) || !desc.equals(methodInsnNode.desc)) {
                continue;
            }

            InsnList injection = new InsnList();
            if (opcode == INVOKESTATIC) {
                if (!replaceSkinTextureDownloaderReceiver(methodNode, methodInsnNode, owner, name, desc, "", functionDesc, insnNode -> insnNode.getOpcode() == ASTORE, H_INVOKESTATIC)) {
                    continue;
                }
            } else {
                String skinManagerOwner = context.remapClassName(SKIN_MANAGER);
                String fieldName = context.remapFieldName(SKIN_MANAGER, "skinTextureDownloader");
                if (!replaceSkinTextureDownloaderReceiver(methodNode, methodInsnNode, owner, name, desc, objectDesc(owner), functionDesc, insnNode -> {
                    if (insnNode.getOpcode() == GETFIELD) {
                        FieldInsnNode fieldInsnNode = (FieldInsnNode) insnNode;
                        return skinManagerOwner.equals(fieldInsnNode.owner) && fieldName.equals(fieldInsnNode.name) && objectDesc(owner).equals(fieldInsnNode.desc);
                    }
                    return false;
                }, H_INVOKEVIRTUAL)) {
                    continue;
                }
            }
            injection.add(new VarInsnNode(ALOAD, 1));
            methodNode.instructions.insertBefore(methodInsnNode, injection);
            methodNode.instructions.set(methodInsnNode, new MethodInsnNode(INVOKESTATIC, FAKE_HTTP_TEXTURE_V2, "downloadAndRegisterSkin", replacementDesc, false));
            modified = true;
        }
        return modified;
    }

    private boolean replaceSkinTextureDownloaderReceiver(MethodNode methodNode, MethodInsnNode invoke, String owner, String name, String desc, String ownerDesc, String functionDesc, Predicate<AbstractInsnNode> predicate, int handleTag) {
        AbstractInsnNode current = invoke.getPrevious();
        while (current != null) {
            if (predicate.test(current)) {
                methodNode.instructions.insert(current, new InvokeDynamicInsnNode(
                    "apply", "(" + ownerDesc + ")" + objectDesc(FUNCTION4),
                    new Handle(H_INVOKESTATIC, LAMBDA_METAFACTORY, "metafactory", "(" + objectDesc(METHOD_HANDLES_LOOKUP) + objectDesc(STRING) + objectDesc(METHOD_TYPE) + objectDesc(METHOD_TYPE) + objectDesc(METHOD_HANDLE) + objectDesc(METHOD_TYPE) + ")" + objectDesc(CALL_SITE), false),
                    Type.getType("(" + objectDesc(OBJECT) + objectDesc(OBJECT) + objectDesc(OBJECT) + objectDesc(OBJECT) + ")" + objectDesc(OBJECT)),
                    new Handle(handleTag, owner, name, desc, false),
                    Type.getType(functionDesc)
                ));
                return true;
            }
            current = current.getPrevious();
        }
        return false;
    }

    private boolean replaceExecutorProfileLoadAtInvocation(MethodNode methodNode, String owner, String name, String desc) {
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!owner.equals(methodInsnNode.owner) || !name.equals(methodInsnNode.name) || !desc.equals(methodInsnNode.desc)) {
                continue;
            }

            this.replaceInstructionSafely(methodNode, instruction, methodInsnNode = new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "loadProfileTextures", "(" + objectDesc(OBJECT) + objectDesc(RUNNABLE) + ")V", false));
            if (!desc.endsWith("V")) {
                methodNode.instructions.insert(methodInsnNode, new InsnNode(ACONST_NULL));
            }
            modified = true;
            break;
        }
        return modified;
    }

    private boolean replaceGetTexturesWithFakeUserProfile(MethodNode methodNode) {
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!MINECRAFT_SESSION_SERVICE.equals(methodInsnNode.owner) || !"getTextures".equals(methodInsnNode.name) || !("(" + objectDesc(GAME_PROFILE) + "Z)" + objectDesc(MAP)).equals(methodInsnNode.desc)) {
                continue;
            }

            this.replaceInstructionSafely(methodNode, instruction, new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "getUserProfile", "(" + objectDesc(OBJECT) + objectDesc(GAME_PROFILE) + "Z)" + objectDesc(MAP), false));
            modified = true;
        }
        return modified;
    }

    private boolean redirectCacheKeyConstruction(ClassTransformationContext context, MethodNode methodNode) {
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        String owner = context.remapClassName(SKIN_MANAGER_CACHE_KEY);
        String desc = context.remapMethodDescriptor("(" + objectDesc(UUID) + objectDesc(PROPERTY) + ")V");
        String replacementDesc = context.remapMethodDescriptor("(" + objectDesc(UUID) + objectDesc(PROPERTY) + objectDesc(GAME_PROFILE) + ")" + objectDesc(SKIN_MANAGER_CACHE_KEY));

        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!owner.equals(methodInsnNode.owner) || !"<init>".equals(methodInsnNode.name) || !desc.equals(methodInsnNode.desc)) {
                continue;
            }

            InsnList replacement = new InsnList();
            replacement.add(new InsnNode(DUP2_X2));
            replacement.add(new InsnNode(POP2));
            replacement.add(new InsnNode(POP2));
            replacement.add(new VarInsnNode(ALOAD, 1));
            MethodInsnNode createFakeCacheKey = new MethodInsnNode(INVOKESTATIC, FAKE_CACHE_KEY, "createFakeCacheKey", replacementDesc, false);
            this.replaceInstructionSafely(methodNode, methodInsnNode, createFakeCacheKey);
            methodNode.instructions.insertBefore(createFakeCacheKey, replacement);
            modified = true;
        }

        return modified;
    }

    private boolean replaceUnpackTexturesWithFakeSkinCache(ClassTransformationContext context, MethodNode methodNode, String owner) {
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        String desc = context.remapMethodDescriptor("(" + objectDesc(OBJECT) + objectDesc(SKIN_MANAGER_CACHE_KEY) + ")" + objectDesc(OBJECT));
        String returnType = context.remapClassName(MINECRAFT_PROFILE_TEXTURES);

        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!owner.equals(methodInsnNode.owner) || !"unpackTextures".equals(methodInsnNode.name) || !("(" + objectDesc(PROPERTY) + ")" + objectDesc(MINECRAFT_PROFILE_TEXTURES)).equals(methodInsnNode.desc)) {
                continue;
            }

            InsnList insnList = new InsnList();
            insnList.add(new VarInsnNode(ALOAD, 0));
            insnList.add(new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "loadSkinFromCache", desc, false));
            insnList.add(new TypeInsnNode(CHECKCAST, returnType));
            methodNode.instructions.insert(instruction, insnList);
            modified = true;
        }
        return modified;
    }
}
