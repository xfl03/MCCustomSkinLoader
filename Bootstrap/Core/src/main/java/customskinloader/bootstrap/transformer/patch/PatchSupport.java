package customskinloader.bootstrap.transformer.patch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;

import customskinloader.bootstrap.BootstrapLogger;
import customskinloader.bootstrap.mapping.VersionMetadataReader;
import customskinloader.bootstrap.transformer.TargetedClassTransformer;
import customskinloader.bootstrap.util.RangeMatcher;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

abstract class PatchSupport extends TargetedClassTransformer implements Opcodes {
    private static final String IGNORE_PATCH_FAILURE_PROPERTY = "customskinloader.ignorePatchFailure";
    private static final Logger LOGGER = BootstrapLogger.LOGGER;

    protected static final String BOOLEAN = "java/lang/Boolean";
    protected static final String OBJECT = "java/lang/Object";
    protected static final String STRING = "java/lang/String";
    protected static final String CALL_SITE = "java/lang/invoke/CallSite";
    protected static final String LAMBDA_METAFACTORY = "java/lang/invoke/LambdaMetafactory";
    protected static final String METHOD_HANDLE = "java/lang/invoke/MethodHandle";
    protected static final String METHOD_HANDLES_LOOKUP = "java/lang/invoke/MethodHandles$Lookup";
    protected static final String METHOD_TYPE = "java/lang/invoke/MethodType";
    protected static final String FILE = "java/io/File";
    protected static final String INPUT_STREAM = "java/io/InputStream";
    protected static final String PATH = "java/nio/file/Path";
    protected static final String RUNNABLE = "java/lang/Runnable";
    protected static final String MAP = "java/util/Map";
    protected static final String UUID = "java/util/UUID";
    protected static final String COMPLETABLE_FUTURE = "java/util/concurrent/CompletableFuture";
    protected static final String EXECUTOR = "java/util/concurrent/Executor";
    protected static final String EXECUTOR_SERVICE = "java/util/concurrent/ExecutorService";
    protected static final String FUTURE = "java/util/concurrent/Future";
    protected static final String FUNCTION = "java/util/function/Function";
    protected static final String SUPPLIER = "java/util/function/Supplier";
    protected static final String FUNCTION4 = "com/mojang/datafixers/util/Function4";

    protected static final String NATIVE_IMAGE = "com/mojang/blaze3d/platform/NativeImage";
    protected static final String POSE_STACK = "com/mojang/blaze3d/vertex/PoseStack";

    protected static final String MINECRAFT = "net/minecraft/client/Minecraft";
    protected static final String GUI_GRAPHICS = "net/minecraft/client/gui/GuiGraphics";
    protected static final String GUI_GRAPHICS_EXTRACTOR = "net/minecraft/client/gui/GuiGraphicsExtractor";
    protected static final String PLAYER_TAB_OVERLAY = "net/minecraft/client/gui/components/PlayerTabOverlay";
    protected static final String CLIENT_PACKET_LISTENER = "net/minecraft/client/multiplayer/ClientPacketListener";
    protected static final String HTTP_TEXTURE_PROCESSOR = "customskinloader/fake/itf/FakeHttpTextureProcessor"; // net/minecraft/client/renderer/HttpTextureProcessor
    protected static final String MULTI_BUFFER_SOURCE = "net/minecraft/client/renderer/MultiBufferSource";
    protected static final String SUBMIT_NODE_COLLECTOR = "net/minecraft/client/renderer/SubmitNodeCollector";
    protected static final String MODEL_PART = "net/minecraft/client/model/geom/ModelPart";
    protected static final String ABSTRACT_CLIENT_PLAYER = "net/minecraft/client/player/AbstractClientPlayer";
    protected static final String CAPE_LAYER = "net/minecraft/client/renderer/entity/layers/CapeLayer";
    protected static final String AVATAR_RENDER_STATE = "net/minecraft/client/renderer/entity/state/AvatarRenderState";
    protected static final String PLAYER_RENDER_STATE = "net/minecraft/client/renderer/entity/state/PlayerRenderState";
    protected static final String PLAYER_RENDERER = "net/minecraft/client/renderer/entity/player/PlayerRenderer";
    protected static final String RENDER_TYPE = "net/minecraft/client/renderer/rendertype/RenderType";
    protected static final String RENDER_TYPES = "net/minecraft/client/renderer/rendertype/RenderTypes";
    protected static final String SKIN_MANAGER = "net/minecraft/client/resources/SkinManager";
    protected static final String SKIN_MANAGER_1 = "net/minecraft/client/resources/SkinManager$1";
    protected static final String SKIN_MANAGER_3 = "net/minecraft/client/resources/SkinManager$3";
    protected static final String SKIN_MANAGER_CACHE_KEY = "net/minecraft/client/resources/SkinManager$CacheKey";
    protected static final String SKIN_MANAGER_SKIN_TEXTURE_CALLBACK = "net/minecraft/client/resources/SkinManager$SkinTextureCallback";
    protected static final String SKIN_MANAGER_TEXTURE_CACHE = "net/minecraft/client/resources/SkinManager$TextureCache";
    protected static final String SKIN_MANAGER_TEXTURE_INFO = "net/minecraft/client/resources/SkinManager$TextureInfo";
    protected static final String SKIN_TEXTURE_DOWNLOADER = "net/minecraft/client/renderer/texture/SkinTextureDownloader";
    protected static final String TEXTURE_MANAGER = "net/minecraft/client/renderer/texture/TextureManager";
    protected static final String HTTP_TEXTURE = "net/minecraft/client/renderer/texture/HttpTexture";
    protected static final String CLIENT_ASSET_DOWNLOADED_TEXTURE = "net/minecraft/core/ClientAsset$DownloadedTexture";
    protected static final String IDENTIFIER = "net/minecraft/resources/Identifier";
    protected static final String SERVICES = "net/minecraft/server/Services";
    protected static final String RESOURCE = "net/minecraft/server/packs/resources/Resource";
    protected static final String RESOURCE_MANAGER = "net/minecraft/server/packs/resources/ResourceManager";
    protected static final String OBJECTIVE = "net/minecraft/world/scores/Objective";
    protected static final String SCOREBOARD = "net/minecraft/world/scores/Scoreboard";

    protected static final String FAKE_HTTP_TEXTURE_PROCESSOR = "customskinloader/fake/itf/IFakeHttpTextureProcessor";
    protected static final String FAKE_I_RESOURCE_V1 = "customskinloader/fake/itf/IFakeIResource$V1";
    protected static final String FAKE_I_RESOURCE_V2 = "customskinloader/fake/itf/IFakeIResource$V2";
    protected static final String FAKE_I_RESOURCE_MANAGER_V1 = "customskinloader/fake/itf/IFakeIResourceManager$V1";
    protected static final String FAKE_I_RESOURCE_MANAGER_V2 = "customskinloader/fake/itf/IFakeIResourceManager$V2";
    protected static final String FAKE_NATIVE_IMAGE = "customskinloader/fake/itf/IFakeNativeImage";

    protected static final String FAKE_SKIN_BUFFER = "customskinloader/fake/FakeSkinBuffer";
    protected static final String FAKE_SKIN_MANAGER = "customskinloader/fake/FakeSkinManager";
    protected static final String FAKE_CACHE_KEY = "customskinloader/fake/FakeSkinManager$FakeCacheKey";
    protected static final String FAKE_HTTP_TEXTURE_V1 = "customskinloader/fake/texture/FakeHttpTexture$V1";
    protected static final String FAKE_HTTP_TEXTURE_V2 = "customskinloader/fake/texture/FakeHttpTexture$V2";

    protected static final String MINECRAFT_PROFILE_TEXTURE = "com/mojang/authlib/minecraft/MinecraftProfileTexture";
    protected static final String MINECRAFT_PROFILE_TEXTURE_TYPE = "com/mojang/authlib/minecraft/MinecraftProfileTexture$Type";
    protected static final String MINECRAFT_SESSION_SERVICE = "com/mojang/authlib/minecraft/MinecraftSessionService";
    protected static final String MINECRAFT_PROFILE_TEXTURES = "com/mojang/authlib/minecraft/MinecraftProfileTextures";
    protected static final String GAME_PROFILE = "com/mojang/authlib/GameProfile";
    protected static final String PROPERTY = "com/mojang/authlib/properties/Property";

    protected static String objectDesc(String internalName) {
        return "L" + internalName + ";";
    }

    private final int protocolVersion;
    private final int worldVersion;

    protected PatchSupport(String name, int priority, Map<String, String> targetClassNames) {
        this(name, priority, VersionMetadataReader.PROTOCOL_VERSION, VersionMetadataReader.WORLD_VERSION, targetClassNames);
    }

    private PatchSupport(String name, int priority, int protocolVersion, int worldVersion, Map<String, String> targetClassNames) {
        super(name, priority, filterTargetClassNames(targetClassNames, protocolVersion, worldVersion));
        this.protocolVersion = protocolVersion;
        this.worldVersion = worldVersion;
    }

    protected static Map<String, String> targets(String... targetClassNameProtocolRanges) {
        if (targetClassNameProtocolRanges.length % 2 != 0) {
            throw new IllegalArgumentException("Target class names must be paired with protocol ranges");
        }

        Map<String, String> targets = new LinkedHashMap<>();
        for (int index = 0; index < targetClassNameProtocolRanges.length; index += 2) {
            targets.put(targetClassNameProtocolRanges[index], targetClassNameProtocolRanges[index + 1]);
        }
        return Collections.unmodifiableMap(targets);
    }

    private static String[] filterTargetClassNames(Map<String, String> targetClassNames, int protocolVersion, int worldVersion) {
        List<String> filteredTargetClassNames = new ArrayList<>();
        for (Map.Entry<String, String> entry : targetClassNames.entrySet()) {
            if (RangeMatcher.matches(entry.getValue(), protocolVersion, worldVersion)) {
                filteredTargetClassNames.add(entry.getKey());
            }
        }
        return filteredTargetClassNames.toArray(new String[0]);
    }

    protected boolean addInterface(ClassNode classNode, String interfaceName) {
        if (!classNode.interfaces.contains(interfaceName)) {
            classNode.interfaces.add(interfaceName);
        }
        return true;
    }

    protected boolean makeClassPublicNonFinal(ClassNode classNode) {
        boolean modified = false;
        int access = makePublicNonFinal(classNode.access);
        if (classNode.access != access) {
            classNode.access = access;
            modified = true;
        }
        modified |= this.makeInnerClassPublicNonFinal(classNode, classNode.name);
        return modified;
    }

    protected boolean makeInnerClassPublicNonFinal(ClassNode classNode, String innerName) {
        boolean modified = false;
        for (InnerClassNode innerClassNode : classNode.innerClasses) {
            if (!innerName.equals(innerClassNode.name)) {
                continue;
            }

            innerClassNode.access = makePublicNonFinal(innerClassNode.access);
            modified = true;
        }
        return modified;
    }

    protected boolean makeMethodPublicNonFinal(MethodNode methodNode) {
        if (methodNode == null) {
            return false;
        }
        methodNode.access = makePublicNonFinal(methodNode.access);
        return true;
    }

    protected boolean makeFieldPublicNonFinal(FieldNode fieldNode) {
        if (fieldNode == null) {
            return false;
        }
        fieldNode.access = makePublicNonFinal(fieldNode.access);
        return true;
    }

    private static int makePublicNonFinal(int access) {
        return (access & ~(ACC_PRIVATE | ACC_PROTECTED | ACC_FINAL)) | ACC_PUBLIC;
    }

    protected boolean matches(String range) {
        return RangeMatcher.matches(range, this.protocolVersion, this.worldVersion);
    }

    protected boolean applyIfMatches(String range, String label, BooleanSupplier operation) {
        return applyIfMatches(range, label, operation, true);
    }

    protected boolean applyIfMatches(String range, String label, BooleanSupplier operation, boolean required) {
        if (!this.matches(range)) {
            return false;
        }

        return this.requireModified(label, operation.getAsBoolean(), required);
    }

    protected boolean requireModified(String label, boolean modified) {
        return this.requireModified(label, modified, true);
    }

    protected boolean requireModified(String label, boolean modified, boolean required) {
        if (modified) {
            return true;
        }

        String message = "Patch '" + this.getName() + ":" + label + "' matched protocol " + this.protocolVersion
            + " but did not modify any bytecode. If you want to force CustomSkinLoader to ignore this patch failure, add -D"
            + IGNORE_PATCH_FAILURE_PROPERTY + "=true to the JVM arguments.";
        if ("false".equalsIgnoreCase(System.getProperty(IGNORE_PATCH_FAILURE_PROPERTY))) {
            throw new IllegalStateException(message);
        }

        if (!required || Boolean.getBoolean(IGNORE_PATCH_FAILURE_PROPERTY)) {
            LOGGER.warn(message);
            return false;
        }

        throw new IllegalStateException(message);
    }

    protected InsnList clone(InsnList source) {
        InsnList copy = new InsnList();
        for (AbstractInsnNode abstractInsnNode : source) {
            copy.add(abstractInsnNode.clone(null));
        }
        return copy;
    }

    protected AbstractInsnNode findPreviousTypeInstruction(AbstractInsnNode instruction, int opcode, String type) {
        AbstractInsnNode current = instruction == null ? null : instruction.getPrevious();
        while (current != null) {
            if (current.getOpcode() == opcode && current instanceof TypeInsnNode && type.equals(((TypeInsnNode) current).desc)) {
                return current;
            }
            current = current.getPrevious();
        }
        return null;
    }

    protected AbstractInsnNode nextRealInstruction(AbstractInsnNode instruction) {
        AbstractInsnNode current = instruction == null ? null : instruction.getNext();
        while (current != null && current.getOpcode() < 0) {
            current = current.getNext();
        }
        return current;
    }

    protected static void methodNodeSet(MethodNode methodNode, AbstractInsnNode oldInstruction, AbstractInsnNode newInstruction) {
        methodNode.instructions.set(oldInstruction, newInstruction);
    }
}
