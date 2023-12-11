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
    try {
        return Java.type(name);
    } catch (ignored) {
        // forge-1.13.2-25.0.23 ~ 1.13.2-25.0.41
        return null;
    }
}

// Compare method and field names, doesn't support forge-1.13.2-25.0.194 and earlier version.
function checkName(name, srgName) {
    return name.equals(mapName(srgName));
}

// De-obfuscate method and field names.
function mapName(srgName) {
    try {
        if (srgName.startsWith("field_")) return ASMAPI.mapField(srgName);
        if (srgName.startsWith("func_")) return ASMAPI.mapMethod(srgName);
    } catch (ignored) {
        // Before forge-1.13.2-25.0.194
    }
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
                    if (checkName(mn.name, "<init>") && mn.desc.equals("(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V")) {
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.RETURN) {
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 2));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "setSkinCacheDir", "(Ljava/io/File;)V", false));
                            }
                        }
                    } else if (checkName(mn.name, "func_152789_a")
                        && (mn.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;") // 1.13.2-
                            || mn.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$ISkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;"))) { // 1.14.2+
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESPECIAL
                                && (node.owner.equals("net/minecraft/client/renderer/texture/ThreadDownloadImageData") // 1.13.2-
                                    || node.owner.equals("net/minecraft/client/renderer/texture/DownloadingTexture")) // 1.14.2+
                                && checkName(node.name, "<init>")
                                && (node.desc.equals("(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/IImageBuffer;)V") // 1.14.4-
                                    || node.desc.equals("(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/util/ResourceLocation;ZLjava/lang/Runnable;)V"))) { // 1.15+
                                var args = Type.getType(node.desc).getArgumentTypes();
                                var s = "(";
                                for (var i = 0; i < args.length; i++) {
                                    s = s + "Ljava/lang/Object;";
                                }
                                if (node.desc.contains("Z")) {
                                    mn.instructions.insertBefore(node, new InsnNode(Opcodes.SWAP));
                                    mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false));
                                    mn.instructions.insertBefore(node, new InsnNode(Opcodes.SWAP));
                                }
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
                    } else if (checkName(mn.name, "func_152790_a")
                        && (mn.desc.equals("(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V") // 1.13.2-
                            || mn.desc.equals("(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$ISkinAvailableCallback;Z)V"))) { // 1.14.2+
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKEINTERFACE
                                && ((node.owner.equals("java/util/concurrent/ExecutorService") && checkName(node.name, "submit") && node.desc.equals("(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;")) // 1.14.4-
                                    || (node.owner.equals("java/util/concurrent/Executor") && checkName(node.name, "execute") && node.desc.equals("(Ljava/lang/Runnable;)V")))) { // 1.15+
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                                if (node.desc.endsWith("V")) {
                                    mn.instructions.insert(node, new InsnNode(Opcodes.POP));
                                }
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadProfileTextures", "(Ljava/lang/Runnable;Lcom/mojang/authlib/GameProfile;)V", false));
                            }
                        }
                    } else if (((checkName(mn.name, "func_210275_a") || checkName(mn.name, "lambda$loadProfileTextures$1")) // 1.14.4-
                            || (checkName(mn.name, "func_229293_a_") || checkName(mn.name, "lambda$loadProfileTextures$4"))) // 1.15+
                        && (mn.desc.equals("(Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)V") // 1.13.2-
                            || mn.desc.equals("(Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/resources/SkinManager$ISkinAvailableCallback;)V"))) { // 1.14.2+
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKEINTERFACE && node.owner.equals("com/mojang/authlib/minecraft/MinecraftSessionService") && checkName(node.name, "getTextures") && node.desc.equals("(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;")) {
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "getUserProfile", "(Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;", false));
                                break;
                            }
                        }
                    } else if ((checkName(mn.name, "func_210276_a") || checkName(mn.name, "lambda$null$0"))
                        && (mn.desc.equals("(Ljava/util/Map;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)V") // 1.13.2-
                            || mn.desc.equals("(Ljava/util/Map;Lnet/minecraft/client/resources/SkinManager$ISkinAvailableCallback;)V"))) { // 1.14.2 ~ 1.14.4
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.RETURN) {
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 1));
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 2));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadElytraTexture", "(Lnet/minecraft/client/resources/SkinManager;" + mn.desc.substring(1), false));
                            }
                        }
                    } else if ((checkName(mn.name, "func_229297_b_") || checkName(mn.name, "lambda$null$2")) && mn.desc.equals("(Ljava/util/Map;Lnet/minecraft/client/resources/SkinManager$ISkinAvailableCallback;)V")) { // 1.15+
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESTATIC && node.owner.equals("com/google/common/collect/ImmutableList") && checkName(node.name, "of") && node.desc.equals("(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;")) {
                                mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP2));
                                mn.instructions.insertBefore(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/mojang/authlib/minecraft/MinecraftProfileTexture$Type", "values", "()[Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;"))
                                mn.instructions.set(node, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/google/common/collect/ImmutableList", "copyOf", "([Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;"))
                            }
                        }
                    } else if (checkName(mn.name, "func_152788_a") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;")) {
                        var first = mn.instructions.getFirst();
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
                        mn.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinManager", "loadSkinFromCache", "(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;", false));
                        mn.instructions.insertBefore(first, new InsnNode(Opcodes.ARETURN));
                    }
                });
                return cn;
            }
        },
        'PlayerTabTransformer': {
            'target': {
                'type': 'CLASS',
                'names': function (target) {
                    return ['net/minecraft/client/gui/GuiPlayerTabOverlay', 'net/minecraft/client/gui/overlay/PlayerTabOverlayGui'];
                }
            },
            'transformer': function (cn) {
                cn.methods.forEach(function (mn) {
                    if ((checkName(mn.name, "func_175249_a") && mn.desc.equals("(ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreObjective;)V")) // 1.15.2-
                        || (checkName(mn.name, "func_238523_a_") && mn.desc.equals("(Lcom/mojang/blaze3d/matrix/MatrixStack;ILnet/minecraft/scoreboard/Scoreboard;Lnet/minecraft/scoreboard/ScoreObjective;)V"))) { // 1.16.1+
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKEVIRTUAL && node.owner.equals("net/minecraft/client/Minecraft") && checkName(node.name, "func_71387_A") && node.desc.equals("()Z")) {
                                mn.instructions.insertBefore(node, new InsnNode(Opcodes.POP));
                                mn.instructions.set(node, new InsnNode(Opcodes.ICONST_1));
                            }
                        }
                    }
                });
                return cn;
            }
        },
        'IImageBufferTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/renderer/IImageBuffer'
            },
            'transformer': function (cn) {
                if (!cn.interfaces.contains("customskinloader/fake/itf/IFakeIImageBuffer")) {
                    cn.interfaces.add("customskinloader/fake/itf/IFakeIImageBuffer");
                }
                return cn;
            }
        },

        // For 1.14+
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
        'IResourceTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/resources/IResource'
            },
            'transformer': function (cn) {
                cn.interfaces.add("customskinloader/fake/itf/IFakeIResource$V1");
                cn.interfaces.add("customskinloader/fake/itf/IFakeIResource$V2");
                cn.interfaces.add("net/minecraft/client/resources/IResource");
                return cn;
            }
        },
        'IResourceManagerTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/resources/IResourceManager'
            },
            'transformer': function (cn) {
                cn.interfaces.add("customskinloader/fake/itf/IFakeIResourceManager");
                cn.interfaces.add("net/minecraft/client/resources/IResourceManager");
                return cn;
            }
        },
        'ISkinAvailableCallbackTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/resources/SkinManager$ISkinAvailableCallback'
            },
            'transformer': function (cn) {
                cn.interfaces.add("net/minecraft/client/resources/SkinManager$SkinAvailableCallback");
                return cn;
            }
        },

        // For 1.15+
        'DownloadingTextureTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/renderer/texture/DownloadingTexture'
            },
            'transformer': function (cn) {
                cn.methods.forEach(function (mn) {
                    if (checkName(mn.name, "func_229159_a_") && mn.desc.equals("(Ljava/io/InputStream;)Lnet/minecraft/client/renderer/texture/NativeImage;")) {
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESTATIC && node.owner.equals("net/minecraft/client/renderer/texture/DownloadingTexture") && checkName(node.name, "func_229163_c_") && node.desc.equals("(Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/client/renderer/texture/NativeImage;")) {
                                // FakeSkinBuffer.processLegacySkin(image, this.processTask, DownloadingTexture::processLegacySkin);
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                                mn.instructions.insertBefore(node, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/texture/DownloadingTexture", mapName("field_229155_i_"), "Ljava/lang/Runnable;"));
                                mn.instructions.insertBefore(node, new InvokeDynamicInsnNode("apply", "()Ljava/util/function/Function;",
                                    /*   bsm   */ new Handle(Opcodes.H_INVOKESTATIC, "java/lang/invoke/LambdaMetafactory", "metafactory", "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;", false),
                                    /* bsmArgs */ Type.getType("(Ljava/lang/Object;)Ljava/lang/Object;"),
                                    /* bsmArgs */ new Handle(Opcodes.H_INVOKESTATIC, "net/minecraft/client/renderer/texture/DownloadingTexture", mapName("func_229163_c_"), "(Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/client/renderer/texture/NativeImage;", false),
                                    /* bsmArgs */ Type.getType("(Lnet/minecraft/client/renderer/texture/NativeImage;)Lnet/minecraft/client/renderer/texture/NativeImage;")));
                                iterator.set(new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinBuffer", "processLegacySkin", "(Lnet/minecraft/client/renderer/texture/NativeImage;Ljava/lang/Runnable;Ljava/util/function/Function;)Lnet/minecraft/client/renderer/texture/NativeImage;", false));
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
                    return ['net/minecraft/client/renderer/entity/PlayerRenderer', 'net/minecraft/client/renderer/entity/layers/CapeLayer'];
                }
            },
            'transformer': function (cn) {
                cn.methods.forEach(function (mn) {
                    if ((checkName(mn.name, "func_229145_a_") && mn.desc.equals("(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;Lnet/minecraft/client/renderer/model/ModelRenderer;Lnet/minecraft/client/renderer/model/ModelRenderer;)V")) // PlayerRenderer.renderItem
                        || (checkName(mn.name, "func_225628_a_") && mn.desc.equals("(Lcom/mojang/blaze3d/matrix/MatrixStack;Lnet/minecraft/client/renderer/IRenderTypeBuffer;ILnet/minecraft/client/entity/player/AbstractClientPlayerEntity;FFFFFF)V"))) { // CapeLayer.render
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESTATIC && node.owner.equals("net/minecraft/client/renderer/RenderType") && checkName(node.name, "func_228634_a_") && node.desc.equals("(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;")) { // RenderType.getEntitySolid
                                node.name = mapName("func_228644_e_"); // RenderType.getEntityTranslucent
                            }
                        }
                    }
                });
                return cn;
            }
        }
    };
}
