var Handle = Java.type('org.objectweb.asm.Handle');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var Type = Java.type('org.objectweb.asm.Type');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var InvokeDynamicInsnNode = Java.type('org.objectweb.asm.tree.InvokeDynamicInsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

// Compare method and field names, doesn't support forge-1.13.2-25.0.194 and earlier version.
function checkName(name, srgName) {
    return name.equals(mapName(srgName));
}

// De-obfuscate method and field names.
function mapName(srgName) {
    try {
        var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
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
                'name': 'net.minecraft.client.resources.SkinManager'
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
                    } else if (checkName(mn.name, "func_152789_a")
                        && (mn.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;") // 1.13.2-
                            || mn.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$ISkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;"))) { // 1.14.2+
                        var first = mn.instructions.getFirst();
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 0));
                        mn.instructions.insertBefore(first, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 2));
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 3));
                        mn.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "customskinloader/fake/FakeSkinManager", "loadSkin", "(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;", false));
                        mn.instructions.insertBefore(first, new InsnNode(Opcodes.ARETURN));
                    } else if (checkName(mn.name, "func_152790_a")
                        && (mn.desc.equals("(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V") // 1.13.2-
                            || mn.desc.equals("(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$ISkinAvailableCallback;Z)V"))) { // 1.14.2+
                        var first = mn.instructions.getFirst();
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 0));
                        mn.instructions.insertBefore(first, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 1));
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 2));
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ILOAD, 3));
                        mn.instructions.insertBefore(first, new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "customskinloader/fake/FakeSkinManager", "loadProfileTextures", "(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V", false));
                        mn.instructions.insertBefore(first, new InsnNode(Opcodes.RETURN));
                    } else if (checkName(mn.name, "func_152788_a") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;")) {
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
        'TileEntitySkullTransformer': {
            'target': {
                'type': 'CLASS',
                'names': function (target) {
                    return ['net/minecraft/tileentity/TileEntitySkull', 'net/minecraft/tileentity/SkullTileEntity'];
                }
            },
            'transformer': function (cn) {
                cn.methods.forEach(function (mn) {
                    if (checkName(mn.name, "func_174884_b") && mn.desc.equals("(Lcom/mojang/authlib/GameProfile;)Lcom/mojang/authlib/GameProfile;")) {
                        var first = mn.instructions.getFirst();
                        var label = new LabelNode();
                        mn.instructions.insertBefore(first, new FieldInsnNode(Opcodes.GETSTATIC, "customskinloader/CustomSkinLoader", "config", "Lcustomskinloader/config/Config;"));
                        mn.instructions.insertBefore(first, new FieldInsnNode(Opcodes.GETFIELD, "customskinloader/config/Config", "forceFillSkullNBT", "Z"));
                        mn.instructions.insertBefore(first, new JumpInsnNode(Opcodes.IFNE, label));
                        mn.instructions.insertBefore(first, new VarInsnNode(Opcodes.ALOAD, 0));
                        mn.instructions.insertBefore(first, new InsnNode(Opcodes.ARETURN));
                        mn.instructions.insertBefore(first, label);
                    }
                });
                return cn;
            }
        },

        // For 1.13+
        'AbstractTextureTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/renderer/texture/AbstractTexture'
            },
            'transformer': function (cn) {
                cn.interfaces.add("net/minecraft/client/renderer/texture/Texture");
                return cn;
            }
        },
        'TextureTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/renderer/texture/Texture'
            },
            'transformer': function (cn) {
                if (cn.access === 0) {
                    cn.access = Opcodes.ACC_PUBLIC | Opcodes.ACC_INTERFACE | Opcodes.ACC_ABSTRACT;
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

                var mn = new MethodNode(Opcodes.ACC_PUBLIC, "getResourceFromResourceLocation", "(Lnet/minecraft/util/ResourceLocation;)Ljava/io/InputStream;", null, null);
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/client/Minecraft", mapName("func_195551_G"), "()Lnet/minecraft/resources/IResourceManager;", false));
                mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/resources/IResourceManager", mapName("func_199002_a"), "(Lnet/minecraft/util/ResourceLocation;)Lnet/minecraft/resources/IResource;", true));
                mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, "net/minecraft/resources/IResource", mapName("func_199027_b"), "()Ljava/io/InputStream;", true));
                mn.instructions.add(new InsnNode(Opcodes.ARETURN));
                cn.methods.add(mn);

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
        'TextureManagerTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/renderer/texture/TextureManager'
            },
            'transformer': function (cn) {
                cn.interfaces.add("customskinloader/fake/itf/IFakeTextureManager_1");
                cn.interfaces.add("customskinloader/fake/itf/IFakeTextureManager_2");
                return cn;
            }
        },
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