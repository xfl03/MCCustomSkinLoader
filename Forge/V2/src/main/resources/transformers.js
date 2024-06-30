var ASMAPI = getJavaType('net.minecraftforge.coremod.api.ASMAPI');
var Handle = getJavaType('org.objectweb.asm.Handle');
var Opcodes = getJavaType('org.objectweb.asm.Opcodes');
var Type = getJavaType('org.objectweb.asm.Type');
var FieldInsnNode = getJavaType('org.objectweb.asm.tree.FieldInsnNode');
var InsnNode = getJavaType('org.objectweb.asm.tree.InsnNode');
var IntInsnNode =  getJavaType('org.objectweb.asm.tree.IntInsnNode');
var InvokeDynamicInsnNode = getJavaType('org.objectweb.asm.tree.InvokeDynamicInsnNode');
var MethodInsnNode = getJavaType('org.objectweb.asm.tree.MethodInsnNode');
var TypeInsnNode = getJavaType('org.objectweb.asm.tree.TypeInsnNode');
var VarInsnNode = getJavaType('org.objectweb.asm.tree.VarInsnNode');

function getJavaType(name) {
    return Java.type(name);
}

// Compare method and field names.
function checkName(name, srgName) {
    return name.equals(mapName(srgName));
}

// De-obfuscate method and field names.
function mapName(srgName) {
    if (srgName.startsWith("f_")) return ASMAPI.mapField(srgName);
    if (srgName.startsWith("m_")) return ASMAPI.mapMethod(srgName);
    return srgName;
}

function initializeCoreMod() {
    return {
        'SkinManagerTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/resources/SkinManager'
            },
            'transformer': function (cn) {
                cn.methods.forEach(function (mn) {
                    if (checkName(mn.name, "<init>") && mn.desc.equals("(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V")) { // 1.20.1-
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.RETURN) {
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 2));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "setSkinCacheDir", "(Ljava/io/File;)V", false));
                            }
                        }
                    } else if (checkName(mn.name, "<init>") && mn.desc.equals("(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/nio/file/Path;Lcom/mojang/authlib/minecraft/MinecraftSessionService;Ljava/util/concurrent/Executor;)V")) { // 1.20.2+
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.RETURN) {
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 2));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "setSkinCacheDir", "(Ljava/nio/file/Path;)V", false));
                            } else if (node.getOpcode() === Opcodes.INVOKEVIRTUAL && node.owner.equals("com/google/common/cache/CacheBuilder") && checkName(node.name, "build") && node.desc.equals("(Lcom/google/common/cache/CacheLoader;)Lcom/google/common/cache/LoadingCache;")) {
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "setCacheLoader", "(Lcom/google/common/cache/CacheLoader;)Lcom/google/common/cache/CacheLoader;", false));
                            }
                        }
                    } else if (checkName(mn.name, "m_118828_") && mn.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinTextureCallback;)Lnet/minecraft/resources/ResourceLocation;")) { // 1.20.1-
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESPECIAL && (node.owner.equals("net/minecraft/client/renderer/texture/HttpTexture") && checkName(node.name, "<init>") && node.desc.equals("(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation;ZLjava/lang/Runnable;)V"))) {
                                var s = "(";
                                var args = Type.getType(node.desc).getArgumentTypes();
                                for (var i = 0; i < args.length; i++) {
                                    s = s + "Ljava/lang/Object;";
                                }
                                mn.instructions.insertBefore(node, new InsnNode(Opcodes.SWAP));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false));
                                mn.instructions.insertBefore(node, new InsnNode(Opcodes.SWAP));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/google/common/collect/ImmutableList", "of", s + ")Lcom/google/common/collect/ImmutableList;", false));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 2));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "createThreadDownloadImageData", "(Lcom/google/common/collect/ImmutableList;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;)[Ljava/lang/Object;", false));
                                for (var i = 0; i < args.length; i++) {
                                    mn.instructions.insertBefore(node, new InsnNode(Opcodes.DUP));
                                    mn.instructions.insertBefore(node, new IntInsnNode(Opcodes.BIPUSH, i));
                                    mn.instructions.insertBefore(node, new InsnNode(Opcodes.AALOAD));
                                    if (args[i].getInternalName().equals("Z")) {
                                        mn.instructions.insertBefore(node, new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"));
                                        mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
                                    } else {
                                        mn.instructions.insertBefore(node, new TypeInsnNode(Opcodes.CHECKCAST, args[i].getInternalName()));
                                    }
                                    mn.instructions.insertBefore(node, new InsnNode(Opcodes.SWAP));
                                }
                                mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP));
                            }
                        }
                    } else if (checkName(mn.name, "m_118817_") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinTextureCallback;Z)V")) { // 1.20.1-
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKEINTERFACE
                                && (node.owner.equals("java/util/concurrent/Executor") // 1.17.1
                                    || node.owner.equals("java/util/concurrent/ExecutorService")) // 1.18+
                                && checkName(node.name, "execute") && node.desc.equals("(Ljava/lang/Runnable;)V")) {
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                                mn.instructions.insert(node, new InsnNode(Opcodes.POP));
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadProfileTextures", "(Ljava/lang/Runnable;Lcom/mojang/authlib/GameProfile;)V", false));
                            }
                        }
                    } else if (checkName(mn.name, "m_118821_") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/resources/SkinManager$SkinTextureCallback;)V")) { // 1.20.1-
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKEINTERFACE && node.owner.equals("com/mojang/authlib/minecraft/MinecraftSessionService") && checkName(node.name, "getTextures") && node.desc.equals("(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;")) {
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "getUserProfile", "(Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;", false));
                                break;
                            }
                        }
                    } else if (checkName(mn.name, "m_174849_") && mn.desc.equals("(Ljava/util/Map;Lnet/minecraft/client/resources/SkinManager$SkinTextureCallback;)V")) { // 1.20.1-
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESTATIC && node.owner.equals("com/google/common/collect/ImmutableList") && checkName(node.name, "of") && node.desc.equals("(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;")) {
                                mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP2));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mojang/authlib/minecraft/MinecraftProfileTexture$Type", "values", "()[Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;"));
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/google/common/collect/ImmutableList", "copyOf", "([Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"));
                            }
                        }
                    } else if (checkName(mn.name, "m_118815_") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;")) { // 1.20.1-
                        var first = mn.instructions.getFirst();
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
                        mn.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadSkinFromCache", "(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;", false));
                        mn.instructions.insertBefore(first, new InsnNode(Opcodes.ARETURN));
                    } else if (checkName(mn.name, "m_293307_") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/resources/PlayerSkin;")) { // 1.20.2+
                        var first = mn.instructions.getFirst();
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
                        mn.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "setSkullType", "(Lcom/mojang/authlib/GameProfile;)V", false));
                    } else if (checkName(mn.name, "m_293351_") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;")) { // 1.20.2+
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESPECIAL && node.owner.equals("net/minecraft/client/resources/SkinManager$CacheKey") && checkName(node.name, "<init>") && node.desc.equals("(Ljava/util/UUID;Lcom/mojang/authlib/properties/Property;)V")) {
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "createProperty", "(Lcom/mojang/authlib/properties/Property;)Lcom/mojang/authlib/properties/Property;", false));
                            } else if (node.getOpcode() === Opcodes.INVOKEINTERFACE && node.owner.equals("com/google/common/cache/LoadingCache") && checkName(node.name, "getUnchecked") && node.desc.equals("(Ljava/lang/Object;)Ljava/lang/Object;")) {
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadCache", "(Lcom/google/common/cache/LoadingCache;Ljava/lang/Object;Lcom/mojang/authlib/GameProfile;)Ljava/lang/Object;", false));
                            }
                        }
                    }
                });
                return cn;
            }
        },
        'SkinManager$1Transformer': { // 1.20.2+
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/resources/SkinManager$1'
            },
            'transformer': function (cn) {
                cn.methods.forEach(function (mn) {
                    if (checkName(mn.name, "load") && mn.desc.equals("(Lnet/minecraft/client/resources/SkinManager$CacheKey;)Ljava/util/concurrent/CompletableFuture;")) {
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESTATIC && node.owner.equals("java/util/concurrent/CompletableFuture") && checkName(node.name, "supplyAsync") && node.desc.equals("(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;")) {
                                var s = "(";
                                var args = Type.getType(node.desc).getArgumentTypes();
                                for (var i = 0; i < args.length; i++) {
                                    s = s + "Ljava/lang/Object;";
                                }
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/google/common/collect/ImmutableList", "of", s + ")Lcom/google/common/collect/ImmutableList;", false));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadProfileTextures", "(Lcom/google/common/collect/ImmutableList;Ljava/lang/Object;)[Ljava/lang/Object;", false));
                                for (var i = 0; i < args.length; i++) {
                                    mn.instructions.insertBefore(node, new InsnNode(Opcodes.DUP));
                                    mn.instructions.insertBefore(node, new IntInsnNode(Opcodes.BIPUSH, i));
                                    mn.instructions.insertBefore(node, new InsnNode(Opcodes.AALOAD));
                                    mn.instructions.insertBefore(node, new TypeInsnNode(Opcodes.CHECKCAST, args[i].getInternalName()));
                                    mn.instructions.insertBefore(node, new InsnNode(Opcodes.SWAP));
                                }
                                mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP));
                            }
                        }
                    } else if (checkName(mn.name, "m_293645_") && mn.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/resources/SkinManager$TextureInfo;")) { // 1.20.2
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKEINTERFACE && node.owner.equals("com/mojang/authlib/minecraft/MinecraftSessionService") && checkName(node.name, "getTextures") && node.desc.equals("(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;")) {
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadSkinFromCache", "(Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;", false));
                            }
                        }
                    } else if (checkName(mn.name, "m_304063_") && mn.desc.equals("(Lnet/minecraft/client/resources/SkinManager$CacheKey;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;")) { // 1.20.3+
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKEINTERFACE && node.owner.equals("com/mojang/authlib/minecraft/MinecraftSessionService") && checkName(node.name, "unpackTextures") && node.desc.equals("(Lcom/mojang/authlib/properties/Property;)Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;")) {
                                mn.instructions.insert(node, new TypeInsnNode(Opcodes.CHECKCAST, "com/mojang/authlib/minecraft/MinecraftProfileTextures"));
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadSkinFromCache", "(Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/properties/Property;)Ljava/lang/Object;", false));
                            }
                        }
                    }
                });
                return cn;
            }
        },
        'SkinManager$CacheKeyTransformer': { // 1.20.2+
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/resources/SkinManager$CacheKey'
            },
            'transformer': function (cn) {
                cn.interfaces.add("customskinloader/fake/itf/IFakeSkinManagerCacheKey");
                return cn;
            }
        },
        'IResourceTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/server/packs/resources/Resource'
            },
            'transformer': function (cn) {
                cn.interfaces.add("customskinloader/fake/itf/IFakeIResource$V1");
                cn.interfaces.add("customskinloader/fake/itf/IFakeIResource$V2");
                return cn;
            }
        },
        'IResourceManagerTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/server/packs/resources/ResourceManager'
            },
            'transformer': function (cn) {
                cn.interfaces.add("customskinloader/fake/itf/IFakeIResourceManager");
                return cn;
            }
        },
        'MinecraftTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/Minecraft'
            },
            'transformer': function (cn) {
                cn.interfaces.add("customskinloader/fake/itf/IFakeMinecraft");
                return cn;
            }
        },
        'PlayerTabOverlayTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/gui/components/PlayerTabOverlay'
            },
            'transformer': function (cn) {
                cn.methods.forEach(function (mn) {
                    if ((checkName(mn.name, "m_94544_") && mn.desc.equals("(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V")) // 1.19.4-
                        || (checkName(mn.name, "m_280406_") && mn.desc.equals("(Lnet/minecraft/client/gui/GuiGraphics;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V"))) { // 1.20+
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKEVIRTUAL && node.owner.equals("net/minecraft/client/Minecraft") && checkName(node.name, "m_91090_") && node.desc.equals("()Z")) {
                                mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP));
                                mn.instructions.set(node, new InsnNode(Opcodes.ICONST_1));
                            }
                        }
                    }
                });
                return cn;
            }
        },
        'RenderPlayer_LayerCapeTransformer': {
            'target': {
                'type': 'CLASS',
                'names': function (target) {
                    return ['net/minecraft/client/renderer/entity/player/PlayerRenderer', 'net/minecraft/client/renderer/entity/layers/CapeLayer'];
                }
            },
            'transformer': function (cn) {
                cn.methods.forEach(function (mn) {
                    if ((checkName(mn.name, "m_117775_") && mn.desc.equals("(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;Lnet/minecraft/client/model/geom/ModelPart;Lnet/minecraft/client/model/geom/ModelPart;)V")) // PlayerRenderer.renderHand
                        || (checkName(mn.name, "m_6494_") && mn.desc.equals("(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/client/player/AbstractClientPlayer;FFFFFF)V"))) { // CapeLayer.render
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESTATIC && node.owner.equals("net/minecraft/client/renderer/RenderType") && checkName(node.name, "m_110446_") && node.desc.equals("(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;")) { // RenderType.entitySolid
                                node.name = mapName("m_110473_"); // RenderType.entityTranslucent
                            }
                        }
                    }
                });
                return cn;
            }
        },

        'SkinManager$TextureCacheTransformer': { // 1.20.2+
            'target': {
                'type': 'METHOD',
                'class': 'net/minecraft/client/resources/SkinManager$TextureCache',
                'methodName': 'm_294542_',
                'methodDesc': '(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;)Ljava/util/concurrent/CompletableFuture;'
            },
            'transformer': function (mn) {
                for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                    var node = iterator.next();
                    if (node.getOpcode() === Opcodes.INVOKESPECIAL && (node.owner.equals("net/minecraft/client/renderer/texture/HttpTexture") && checkName(node.name, "<init>") && node.desc.equals("(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/resources/ResourceLocation;ZLjava/lang/Runnable;)V"))) {
                        var s = "(";
                        var args = Type.getType(node.desc).getArgumentTypes();
                        for (var i = 0; i < args.length; i++) {
                            s = s + "Ljava/lang/Object;";
                        }
                        mn.instructions.insertBefore(node, new InsnNode(Opcodes.SWAP));
                        mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false));
                        mn.instructions.insertBefore(node, new InsnNode(Opcodes.SWAP));
                        mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/google/common/collect/ImmutableList", "of", s + ")Lcom/google/common/collect/ImmutableList;", false));
                        mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                        mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                        mn.instructions.insertBefore(node, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager$TextureCache", mapName("f_291290_"), "Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;"));
                        mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "createThreadDownloadImageData", "(Lcom/google/common/collect/ImmutableList;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;)[Ljava/lang/Object;", false));
                        for (var i = 0; i < args.length; i++) {
                            mn.instructions.insertBefore(node, new InsnNode(Opcodes.DUP));
                            mn.instructions.insertBefore(node, new IntInsnNode(Opcodes.BIPUSH, i));
                            mn.instructions.insertBefore(node, new InsnNode(Opcodes.AALOAD));
                            if (args[i].getInternalName().equals("Z")) {
                                mn.instructions.insertBefore(node, new TypeInsnNode(Opcodes.CHECKCAST, "java/lang/Boolean"));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false));
                            } else {
                                mn.instructions.insertBefore(node, new TypeInsnNode(Opcodes.CHECKCAST, args[i].getInternalName()));
                            }
                            mn.instructions.insertBefore(node, new InsnNode(Opcodes.SWAP));
                        }
                        mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP));
                    }
                }
                return mn;
            }
        },
        'HttpTextureTransformer': {
            'target': {
                'type': 'METHOD',
                'class': 'net/minecraft/client/renderer/texture/HttpTexture',
                'methodName': 'm_118018_',
                'methodDesc': '(Ljava/io/InputStream;)Lcom/mojang/blaze3d/platform/NativeImage;'
            },
            'transformer': function (mn) {
                for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                    var node = iterator.next();
                    if ((node.getOpcode() === Opcodes.INVOKEVIRTUAL || node.getOpcode() === Opcodes.INVOKESPECIAL) && node.owner.equals("net/minecraft/client/renderer/texture/HttpTexture") && checkName(node.name, "m_118032_") && node.desc.equals("(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;")) {
                        // FakeSkinBuffer.processLegacySkin(image, this.onDownloaded, this::processLegacySkin);
                        mn.instructions.insertBefore(node, new InsnNode(Opcodes.SWAP));
                        mn.instructions.insertBefore(node, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/texture/HttpTexture", mapName("f_117997_"), "Ljava/lang/Runnable;"));
                        mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                        mn.instructions.insertBefore(node, new InvokeDynamicInsnNode("apply", "(Lnet/minecraft/client/renderer/texture/HttpTexture;)Ljava/util/function/Function;",
                            /*   bsm   */ new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false),
                            /* bsmArgs */ Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"),
                            /* bsmArgs */ new Handle(Opcodes.H_INVOKEVIRTUAL, "net/minecraft/client/renderer/texture/HttpTexture", mapName("m_118032_"), "(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;", false),
                            /* bsmArgs */ Type.getType("(Lcom/mojang/blaze3d/platform/NativeImage;)Lcom/mojang/blaze3d/platform/NativeImage;")));
                        iterator.set(new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinBuffer", "processLegacySkin", "(Lcom/mojang/blaze3d/platform/NativeImage;Ljava/lang/Runnable;Ljava/util/function/Function;)Lcom/mojang/blaze3d/platform/NativeImage;", false));
                    }
                }
                return mn;
            }
        },
    };
}
