var ASMAPI = getJavaType('net.minecraftforge.coremod.api.ASMAPI');
var Handle = getJavaType('org.objectweb.asm.Handle');
var Opcodes = getJavaType('org.objectweb.asm.Opcodes');
var Type = getJavaType('org.objectweb.asm.Type');
var FieldInsnNode = getJavaType('org.objectweb.asm.tree.FieldInsnNode');
var FieldNode = getJavaType('org.objectweb.asm.tree.FieldNode');
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
                cn.fields.removeIf(function (fn) {
                    return fn.name.equals("fakeManager") && fn.desc.equals("Lcustomskinloader/fake/FakeSkinManager;");
                });
                cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;", null, null));

                cn.methods.forEach(function (mn) {
                    if (checkName(mn.name, "<init>") && mn.desc.equals("(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V")) {
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.RETURN) {
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                                mn.instructions.insertBefore(node, new TypeInsnNode(Opcodes.NEW, "customskinloader/fake/FakeSkinManager"));
                                mn.instructions.insertBefore(node, new InsnNode(Opcodes.DUP));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 2));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 3));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESPECIAL, "customskinloader/fake/FakeSkinManager", "<init>", "(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V", false));
                                mn.instructions.insertBefore(node, new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
                            }
                        }
                    } else if (checkName(mn.name, "m_118828_") && mn.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinTextureCallback;)Lnet/minecraft/resources/ResourceLocation;")) {
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if ((node.getOpcode() === Opcodes.INVOKESPECIAL && node.owner.equals("net/minecraft/resources/ResourceLocation") && checkName(node.name, "<init>") && node.desc.equals("(Ljava/lang/String;)V")) // 1.19-
                                || (node.getOpcode() === Opcodes.INVOKESTATIC && node.owner.equals("net/minecraft/client/resources/SkinManager") && checkName(node.name, "m_242632_") && node.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Ljava/lang/String;)Lnet/minecraft/resources/ResourceLocation;"))) { // 1.19.1+
                                mn.instructions.insert(node, node = new VarInsnNode(Opcodes.ALOAD, 1));
                                mn.instructions.insert(node, node = new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "setResourceLocation", "(Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;)Lnet/minecraft/resources/ResourceLocation;", false));
                            }

                            if (node.getOpcode() === Opcodes.INVOKEINTERFACE && node.owner.equals("net/minecraft/client/resources/SkinManager$SkinTextureCallback") && checkName(node.name, "onSkinTextureAvailable") && node.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/resources/ResourceLocation;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;)V")) {
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                                mn.instructions.insertBefore(node, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 5));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "getModelCache", "(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcustomskinloader/fake/FakeSkinManager;Lnet/minecraft/resources/ResourceLocation;)Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;", false));
                            }

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
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                                mn.instructions.insertBefore(node, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 2));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 3));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "createThreadDownloadImageData", "(Lcom/google/common/collect/ImmutableList;Lcustomskinloader/fake/FakeSkinManager;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinTextureCallback;)[Ljava/lang/Object;", false));
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
                    } else if (checkName(mn.name, "m_118817_") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinTextureCallback;Z)V")) {
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
                    } else if (checkName(mn.name, "m_118821_") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/resources/SkinManager$SkinTextureCallback;)V")) {
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKEINTERFACE && node.owner.equals("com/mojang/authlib/minecraft/MinecraftSessionService") && checkName(node.name, "getTextures") && node.desc.equals("(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;")) {
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "getUserProfile", "(Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;", false));
                                break;
                            }
                        }
                    } else if (checkName(mn.name, "m_174849_") && mn.desc.equals("(Ljava/util/Map;Lnet/minecraft/client/resources/SkinManager$SkinTextureCallback;)V")) {
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESTATIC && node.owner.equals("com/google/common/collect/ImmutableList") && checkName(node.name, "of") && node.desc.equals("(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;")) {
                                mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP2));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mojang/authlib/minecraft/MinecraftProfileTexture$Type", "values", "()[Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;"))
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/google/common/collect/ImmutableList", "copyOf", "([Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"))
                            }
                        }
                    } else if (checkName(mn.name, "m_118815_") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;")) {
                        var first = mn.instructions.getFirst();
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 0));
                        mn.instructions.insertBefore(first, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
                        mn.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "customskinloader/fake/FakeSkinManager", "loadSkinFromCache", "(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;", false));
                        mn.instructions.insertBefore(first, new InsnNode(Opcodes.ARETURN));
                    }
                });
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
        'PlayerTabOverlayTransformer': {
            'target': {
                'type': 'METHOD',
                'class': 'net/minecraft/client/gui/components/PlayerTabOverlay',
                'methodName': 'm_94544_',
                'methodDesc': '(Lcom/mojang/blaze3d/vertex/PoseStack;ILnet/minecraft/world/scores/Scoreboard;Lnet/minecraft/world/scores/Objective;)V'
            },
            'transformer': function (mn) {
                for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                    var node = iterator.next();
                    if (node.getOpcode() === Opcodes.INVOKEVIRTUAL && node.owner.equals("net/minecraft/client/Minecraft") && checkName(node.name, "m_91090_") && node.desc.equals("()Z")) {
                        mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP));
                        mn.instructions.set(node, new InsnNode(Opcodes.ICONST_1));
                    }
                }
                return mn;
            }
        },
    };
}
