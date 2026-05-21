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
        super(
            "customskinloader:skin-manager-patch", 1000,
            targets(
                SKIN_MANAGER, "",
                SKIN_MANAGER_1, "[764,800],[804,0x40000000],[0x40000090,]", // 23w31a+ (1.20.2+)
                SKIN_MANAGER_3, "[,340]", // 1.12.2-
                SKIN_MANAGER_CACHE_KEY, "[764,800],[804,0x40000000],[0x40000090,]", // 23w31a+ (1.20.2+)
                SKIN_MANAGER_TEXTURE_CACHE, "[764,800],[804,0x40000000],[0x40000090,]" // 23w31a+ (1.20.2+)
            )
        );
    }

    @Override
    public boolean transform(ClassTransformationContext context) {
        boolean modified = false;

        if (context.isTarget(SKIN_MANAGER)) {
            // 23w31a+ (1.20.2+)
            modified |= this.applyIfMatches("[764,800],[804,0x40000000],[0x40000090,]", "skin-manager.cache-key-inner-class", () -> makeInnerClassPublicNonFinal(context.getCurrentClassNode(), context.remapClassName(SKIN_MANAGER_CACHE_KEY)));
            modified |= this.requireModified("skin-manager", this.patchSkinManager(context));
        }
        if (context.isTarget(SKIN_MANAGER_1)) {
            modified |= this.requireModified("skin-manager-1", this.patchSkinManagerLoader(context));
        }
        if (context.isTarget(SKIN_MANAGER_3)) {
            MethodNode getUserProfile = context.findMethod(SKIN_MANAGER_3, "run", "()V");
            modified |= this.requireModified("skin-manager-3", getUserProfile != null && this.replaceGetTexturesWithFakeUserProfile(getUserProfile));
        }
        if (context.isTarget(SKIN_MANAGER_CACHE_KEY)) {
            modified |= this.requireModified("skin-manager-cache-key", this.patchSkinManagerCacheKeyClass(context));
        }
        if (context.isTarget(SKIN_MANAGER_TEXTURE_CACHE)) {
            modified |= this.requireModified("skin-manager-texture-cache", this.patchSkinManagerTextureCache(context));
        }

        return modified;
    }

    private boolean patchSkinManager(ClassTransformationContext context) {
        boolean modified = false;
        modified |= this.injectSkinManagerConstructors(context);
        // 1.20.1-
        modified |= this.applyIfMatches("[,763],[801,803],[0x40000001,0x4000008E]", "skin-manager.v1", () -> patchLegacySkinManager(context));
        // 23w42a+ (1.20.3+)
        modified |= this.applyIfMatches("[765,800],[804,0x40000000],[0x4000009D,]", "skin-manager.v2", () -> patchSkinManagerCacheKey(context));
        return modified;
    }

    private boolean patchSkinManagerCacheKeyClass(ClassTransformationContext context) {
        boolean modified = this.makeClassPublicNonFinal(context.getCurrentClassNode());
        modified |= this.makeMethodPublicNonFinal(context.findMethod(SKIN_MANAGER_CACHE_KEY, "<init>", "(" + objectDesc(UUID) + objectDesc(PROPERTY) + ")V"));
        return modified;
    }

    private boolean injectSkinManagerConstructors(ClassTransformationContext context) {
        boolean modified = false;
        // 1.20.1-
        modified |= this.applyIfMatches("[,763],[801,803],[0x40000001,0x4000008E]", "skin-manager.<init>.v1", () -> injectSetSkinCacheDir(
            context, "(" + objectDesc(TEXTURE_MANAGER) + objectDesc(FILE) + objectDesc(MINECRAFT_SESSION_SERVICE) + ")V", 2, objectDesc(FILE)
        ));
        // 23w31a ~ 24w45a (1.20.2 ~ 1.21.3)
        modified |= this.applyIfMatches("[764,768],[0x40000090,0x400000DD]", "skin-manager.<init>.v2", () -> injectSetSkinCacheDir(
            context, "(" + objectDesc(TEXTURE_MANAGER) + objectDesc(PATH) + objectDesc(MINECRAFT_SESSION_SERVICE) + objectDesc(EXECUTOR) + ")V", 2, objectDesc(PATH)
        ));
        // 24w46a ~ 25w33a (1.21.4 ~ 1.21.8)
        modified |= this.applyIfMatches("[769,772],[0x400000DE,0x40000106]", "skin-manager.<init>.v3", () -> injectSetSkinCacheDir(
            context, "(" + objectDesc(PATH) + objectDesc(MINECRAFT_SESSION_SERVICE) + objectDesc(EXECUTOR) + ")V", 1, objectDesc(PATH)
        ));
        // 25w34a ~ 25w34b
        modified |= this.applyIfMatches("[0x40000107,0x40000108]", "skin-manager.<init>.v4", () -> injectSetSkinCacheDir(
            context, "(" + objectDesc(PATH) + objectDesc(SERVICES) + objectDesc(EXECUTOR) + ")V", 1, objectDesc(PATH)
        ));
        // 25w35a+ (1.21.9+)
        modified |= this.applyIfMatches("[773,800],[804,0x40000000],[0x40000109,]", "skin-manager.<init>.v5", () -> injectSetSkinCacheDir(
            context, "(" + objectDesc(PATH) + objectDesc(SERVICES) + objectDesc(SKIN_TEXTURE_DOWNLOADER) + objectDesc(EXECUTOR) + ")V", 1, objectDesc(PATH)
        ));
        return modified;
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

    private boolean patchLegacySkinManager(ClassTransformationContext context) {
        boolean modified = false;
        modified |= this.requireModified("skin-manager.v1.register-texture", this.patchLegacySkinManagerRegisterTexture(context));
        modified |= this.requireModified("skin-manager.v1.register-skins", this.patchLegacySkinManagerRegisterSkins(context));
        modified |= this.requireModified("skin-manager.v1.get-insecure-skin-information", this.patchLegacySkinManagerGetInsecureSkinInformation(context));
        // 1.13.2 ~ 1.20.1
        modified |= this.applyIfMatches("[404,763],[801,803],[0x40000001,0x4000008E]", "skin-manager.v1.lambda-register-skins-4", () -> patchLegacySkinManagerGetUserProfile(context));
        return modified;
    }

    private boolean patchLegacySkinManagerRegisterTexture(ClassTransformationContext context) {
        MethodNode registerTexture = context.findMethod(SKIN_MANAGER, "registerTexture", "(" + objectDesc(MINECRAFT_PROFILE_TEXTURE) + objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE) + objectDesc(SKIN_MANAGER_SKIN_TEXTURE_CALLBACK) + ")" + objectDesc(IDENTIFIER));
        if (registerTexture == null) {
            return false;
        }

        InsnList insnList = new InsnList();
        insnList.add(new VarInsnNode(ALOAD, 2));
        return this.patchCreateThreadDownloadImageData(context, registerTexture, insnList);
    }

    private boolean patchLegacySkinManagerRegisterSkins(ClassTransformationContext context) {
        MethodNode registerSkins = context.findMethod(SKIN_MANAGER, "registerSkins", "(" + objectDesc(GAME_PROFILE) + objectDesc(SKIN_MANAGER_SKIN_TEXTURE_CALLBACK) + "Z)V");
        if (registerSkins == null) {
            return false;
        }

        boolean modified = false;
        // 19w37a- (1.14.4-)
        modified |= this.applyIfMatches("[,553]", "skin-manager.register-skins.v1", () -> replaceExecutorProfileLoadAtInvocation(registerSkins, EXECUTOR_SERVICE, "submit", "(" + objectDesc(RUNNABLE) + ")" + objectDesc(FUTURE), true));
        // 19w38a ~ 1.18-exp7 (1.15 ~ 1.17.1)
        modified |= this.applyIfMatches("[554,756],[801,803],[0x40000001,0x4000002F]+[2205,2831]", "skin-manager.register-skins.v2", () -> replaceExecutorProfileLoadAtInvocation(registerSkins, EXECUTOR, "execute", "(" + objectDesc(RUNNABLE) + ")V", false));
        // 21w37a ~ 1.20.1 (1.18 ~ 1.20.1)
        modified |= this.applyIfMatches("[757,763],[0x40000029,0x4000008E]+[2834,3465]", "skin-manager.register-skins.v3", () -> replaceExecutorProfileLoadAtInvocation(registerSkins, EXECUTOR_SERVICE, "execute", "(" + objectDesc(RUNNABLE) + ")V", false));
        return modified;
    }

    private boolean patchLegacySkinManagerGetInsecureSkinInformation(ClassTransformationContext context) {
        MethodNode loadSkinFromCache = context.findMethod(SKIN_MANAGER, "getInsecureSkinInformation", "(" + objectDesc(GAME_PROFILE) + ")" + objectDesc(MAP));
        if (loadSkinFromCache == null) {
            return false;
        }

        return this.replaceLoadSkinFromCacheMethod(loadSkinFromCache);
    }

    private boolean patchLegacySkinManagerGetUserProfile(ClassTransformationContext context) {
        MethodNode getUserProfile = context.findMethod(SKIN_MANAGER, "lambda$registerSkins$4", "(" + objectDesc(GAME_PROFILE) + "Z" + objectDesc(SKIN_MANAGER_SKIN_TEXTURE_CALLBACK) + ")V");
        if (getUserProfile == null) {
            return false;
        }

        return this.replaceGetTexturesWithFakeUserProfile(getUserProfile);
    }

    private boolean patchSkinManagerCacheKey(ClassTransformationContext context) {
        boolean modified = false;
        // 23w42a ~ 25w33a (1.20.3 ~ 1.21.8)
        modified |= this.applyIfMatches("[765,772],[0x4000009D,0x40000106]", "skin-manager.cache-key.v1", () -> {
            MethodNode getOrLoad = context.findMethod(SKIN_MANAGER, "getOrLoad", "(" + objectDesc(GAME_PROFILE) + ")" + objectDesc(COMPLETABLE_FUTURE));
            return getOrLoad != null && redirectCacheKeyConstruction(context, getOrLoad);
        });
        // 25w34a+ (1.21.9+)
        modified |= this.applyIfMatches("[773,800],[804,0x40000000],[0x40000107,]", "skin-manager.cache-key.v2", () -> {
            MethodNode get = context.findMethod(SKIN_MANAGER, "get", "(" + objectDesc(GAME_PROFILE) + ")" + objectDesc(COMPLETABLE_FUTURE));
            return get != null && redirectCacheKeyConstruction(context, get);
        });
        return modified;
    }

    private boolean patchSkinManagerLoader(ClassTransformationContext context) {
        boolean modified = false;
        MethodNode load = context.findMethod(SKIN_MANAGER_1, "load", "(" + objectDesc(SKIN_MANAGER_CACHE_KEY) + ")" + objectDesc(COMPLETABLE_FUTURE));
        modified |= this.requireModified("skin-manager-1.executor", load != null && this.replaceCompletableFutureExecutor(load));

        modified |= this.patchSkinManagerLoaderLambda(context);

        return modified;
    }

    private boolean patchSkinManagerLoaderLambda(ClassTransformationContext context) {
        boolean modified = false;
        // 23w31a ~ 23w41a (1.20.2)
        modified |= this.applyIfMatches("764,[0x40000090,0x4000009C]", "skin-manager-1.lambda-load-0.v1", () -> {
            MethodNode oldLambda = context.findMethod(SKIN_MANAGER_1, "lambda$load$0", "(" + objectDesc(MINECRAFT_SESSION_SERVICE) + objectDesc(GAME_PROFILE) + ")" + objectDesc(SKIN_MANAGER_TEXTURE_INFO));
            return oldLambda != null && replaceGetTexturesWithFakeSkinCache(oldLambda);
        });
        // 23w42a ~ 25w33a (1.20.3 ~ 1.21.8)
        modified |= this.applyIfMatches("[765,772],[0x4000009D,0x40000106]", "skin-manager-1.lambda-load-0.v2", () -> {
            MethodNode lambdaWithSession = context.findMethod(SKIN_MANAGER_1, "lambda$load$0", "(" + objectDesc(SKIN_MANAGER_CACHE_KEY) + objectDesc(MINECRAFT_SESSION_SERVICE) + ")" + objectDesc(MINECRAFT_PROFILE_TEXTURES));
            return lambdaWithSession != null && replaceUnpackTexturesWithFakeSkinCache(context, lambdaWithSession);
        });
        // 25w34a+ (1.21.9+)
        modified |= this.applyIfMatches("[773,800],[804,0x40000000],[0x40000107,]", "skin-manager-1.lambda-load-0.v3", () -> {
            MethodNode lambdaWithServices = context.findMethod(SKIN_MANAGER_1, "lambda$load$0", "(" + objectDesc(SKIN_MANAGER_CACHE_KEY) + objectDesc(SERVICES) + ")" + objectDesc(MINECRAFT_PROFILE_TEXTURES));
            return lambdaWithServices != null && replaceUnpackTexturesWithFakeSkinCache(context, lambdaWithServices);
        });
        return modified;
    }

    private boolean patchSkinManagerTextureCache(ClassTransformationContext context) {
        MethodNode registerTexture = context.findMethod(SKIN_MANAGER_TEXTURE_CACHE, "registerTexture", "(" + objectDesc(MINECRAFT_PROFILE_TEXTURE) + ")" + objectDesc(COMPLETABLE_FUTURE));
        if (registerTexture == null) {
            return false;
        }

        String fieldName = context.remapFieldName(SKIN_MANAGER_TEXTURE_CACHE, "type");
        InsnList loadType = new InsnList();
        loadType.add(new VarInsnNode(ALOAD, 0));
        loadType.add(new FieldInsnNode(GETFIELD, context.remapClassName(SKIN_MANAGER_TEXTURE_CACHE), fieldName, objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE)));

        boolean modified = false;
        // 23w31a ~ 24w45a (1.20.2 ~ 1.21.3)
        modified |= this.applyIfMatches("[764,768],[0x40000090,0x400000DD]", "skin-manager-texture-cache.http-texture", () -> redirectHttpTextureConstructorToFake(
            context, registerTexture, context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + "Z" + objectDesc(RUNNABLE) + ")V"),
            context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + "Z" + objectDesc(RUNNABLE) + objectDesc(MINECRAFT_PROFILE_TEXTURE) + objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE) + ")V"),
            loadType
        ));
        // 24w46a ~ 25w34b (1.21.4 ~ 1.21.8)
        modified |= this.applyIfMatches("[769,772],[0x400000DE,0x40000108]", "skin-manager-texture-cache.skin-texture-downloader.v1", () -> redirectSkinTextureDownloaderToFake(
            context, registerTexture, INVOKESTATIC
        ));
        // 25w35a+ (1.21.9+)
        modified |= this.applyIfMatches("[773,800],[804,0x40000000],[0x40000109,]", "skin-manager-texture-cache.skin-texture-downloader.v2", () -> redirectSkinTextureDownloaderToFake(
            context, registerTexture, INVOKEVIRTUAL
        ));
        return modified;
    }


    private boolean patchCreateThreadDownloadImageData(ClassTransformationContext context, MethodNode methodNode, InsnList loadTextureType) {
        boolean modified = false;
        // 19w37a- (1.14.4-)
        modified |= this.applyIfMatches("[,553]", "skin-manager.http-texture.v1", () -> redirectHttpTextureConstructorToFake(
            context, methodNode, context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + objectDesc(HTTP_TEXTURE_PROCESSOR) + ")V"),
            context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + objectDesc(HTTP_TEXTURE_PROCESSOR) + objectDesc(MINECRAFT_PROFILE_TEXTURE) + objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE) + ")V"),
            loadTextureType
        ));
        // 19w38a ~ 1.20.1 (1.15 ~ 1.20.1)
        modified |= this.applyIfMatches("[554,763],[801,803],[0x40000001,0x4000008E]", "skin-manager.http-texture.v2", () -> redirectHttpTextureConstructorToFake(
            context, methodNode, context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + "Z" + objectDesc(RUNNABLE) + ")V"),
            context.remapMethodDescriptor("(" + objectDesc(FILE) + objectDesc(STRING) + objectDesc(IDENTIFIER) + "Z" + objectDesc(RUNNABLE) + objectDesc(MINECRAFT_PROFILE_TEXTURE) + objectDesc(MINECRAFT_PROFILE_TEXTURE_TYPE) + ")V"),
            loadTextureType
        ));
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
            methodNodeSet(methodNode, methodInsnNode, new MethodInsnNode(INVOKESTATIC, FAKE_HTTP_TEXTURE_V2, "downloadAndRegisterSkin", replacementDesc, false));
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

    private boolean replaceExecutorProfileLoadAtInvocation(MethodNode methodNode, String owner, String name, String desc, boolean pushNullReturn) {
        boolean modified = false;
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!owner.equals(methodInsnNode.owner) || !name.equals(methodInsnNode.name) || !desc.equals(methodInsnNode.desc)) {
                continue;
            }

            InsnList replacement = new InsnList();
            replacement.add(new InsnNode(SWAP));
            replacement.add(new InsnNode(POP));
            replacement.add(new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "loadProfileTextures", "(" + objectDesc(RUNNABLE) + ")V", false));
            if (pushNullReturn) {
                replacement.add(new InsnNode(ACONST_NULL));
            }
            methodNode.instructions.insertBefore(instruction, replacement);
            methodNode.instructions.remove(instruction);
            modified = true;
        }
        return modified;
    }

    private boolean replaceLoadSkinFromCacheMethod(MethodNode methodNode) {
        methodNode.instructions.clear();
        methodNode.tryCatchBlocks.clear();
        methodNode.instructions.add(new VarInsnNode(ALOAD, 1));
        methodNode.instructions.add(new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "loadSkinFromCache", "(" + objectDesc(GAME_PROFILE) + ")" + objectDesc(MAP), false));
        methodNode.instructions.add(new InsnNode(ARETURN));
        methodNode.maxLocals = Math.max(methodNode.maxLocals, 2);
        return true;
    }

    private boolean replaceGetTexturesWithFakeUserProfile(MethodNode methodNode) {
        boolean modified = false;
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!MINECRAFT_SESSION_SERVICE.equals(methodInsnNode.owner) || !"getTextures".equals(methodInsnNode.name) || !("(" + objectDesc(GAME_PROFILE) + "Z)" + objectDesc(MAP)).equals(methodInsnNode.desc)) {
                continue;
            }

            methodNode.instructions.set(instruction, new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "getUserProfile", "(" + objectDesc(MINECRAFT_SESSION_SERVICE) + objectDesc(GAME_PROFILE) + "Z)" + objectDesc(MAP), false));
            modified = true;
        }
        return modified;
    }

    private boolean redirectCacheKeyConstruction(ClassTransformationContext context, MethodNode methodNode) {
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

            AbstractInsnNode newNode = this.findPreviousTypeInstruction(methodInsnNode, NEW, owner);
            AbstractInsnNode dupNode = this.nextRealInstruction(newNode);
            if (newNode == null || dupNode == null || dupNode.getOpcode() != DUP) {
                continue;
            }

            methodNode.instructions.remove(newNode);
            methodNode.instructions.remove(dupNode);

            InsnList replacement = new InsnList();
            replacement.add(new VarInsnNode(ALOAD, 1));
            replacement.add(new MethodInsnNode(INVOKESTATIC, FAKE_CACHE_KEY, "createFakeCacheKey", replacementDesc, false));
            methodNode.instructions.insertBefore(methodInsnNode, replacement);
            methodNode.instructions.remove(methodInsnNode);
            modified = true;
        }

        return modified;
    }

    private boolean replaceCompletableFutureExecutor(MethodNode methodNode) {
        boolean modified = false;
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!COMPLETABLE_FUTURE.equals(methodInsnNode.owner)) {
                continue;
            }
            if (("supplyAsync".equals(methodInsnNode.name) && ("(" + objectDesc(SUPPLIER) + objectDesc(EXECUTOR) + ")" + objectDesc(COMPLETABLE_FUTURE)).equals(methodInsnNode.desc))
                || ("thenComposeAsync".equals(methodInsnNode.name) && ("(" + objectDesc(FUNCTION) + objectDesc(EXECUTOR) + ")" + objectDesc(COMPLETABLE_FUTURE)).equals(methodInsnNode.desc))) {
                InsnList replacement = new InsnList();
                replacement.add(new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "loadProfileTextures", "(" + objectDesc(EXECUTOR) + ")" + objectDesc(EXECUTOR), false));
                methodNode.instructions.insertBefore(instruction, replacement);
                modified = true;
            }
        }
        return modified;
    }

    private boolean replaceGetTexturesWithFakeSkinCache(MethodNode methodNode) {
        boolean modified = false;
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!MINECRAFT_SESSION_SERVICE.equals(methodInsnNode.owner) || !"getTextures".equals(methodInsnNode.name) || !("(" + objectDesc(GAME_PROFILE) + "Z)" + objectDesc(MAP)).equals(methodInsnNode.desc)) {
                continue;
            }

            methodNode.instructions.set(instruction, new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "loadSkinFromCache", "(" + objectDesc(MINECRAFT_SESSION_SERVICE) + objectDesc(GAME_PROFILE) + "Z)" + objectDesc(MAP), false));
            modified = true;
        }
        return modified;
    }

    private boolean replaceUnpackTexturesWithFakeSkinCache(ClassTransformationContext context, MethodNode methodNode) {
        boolean modified = false;
        String desc = context.remapMethodDescriptor("(" + objectDesc(MINECRAFT_SESSION_SERVICE) + objectDesc(PROPERTY) + objectDesc(SKIN_MANAGER_CACHE_KEY) + ")" + objectDesc(OBJECT));
        String returnType = context.remapClassName(MINECRAFT_PROFILE_TEXTURES);

        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!MINECRAFT_SESSION_SERVICE.equals(methodInsnNode.owner) || !"unpackTextures".equals(methodInsnNode.name) || !("(" + objectDesc(PROPERTY) + ")" + objectDesc(MINECRAFT_PROFILE_TEXTURES)).equals(methodInsnNode.desc)) {
                continue;
            }

            InsnList replacement = new InsnList();
            replacement.add(new VarInsnNode(ALOAD, 0));
            replacement.add(new MethodInsnNode(INVOKESTATIC, FAKE_SKIN_MANAGER, "loadSkinFromCache", desc, false));
            replacement.add(new TypeInsnNode(CHECKCAST, returnType));
            methodNode.instructions.insertBefore(instruction, replacement);
            methodNode.instructions.remove(instruction);
            modified = true;
        }
        return modified;
    }
}
