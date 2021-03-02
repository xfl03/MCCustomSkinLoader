var ASMAPI = Java.type('net.minecraftforge.coremod.api.ASMAPI');
var Opcodes = Java.type('org.objectweb.asm.Opcodes');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var JumpInsnNode = Java.type('org.objectweb.asm.tree.JumpInsnNode');
var LabelNode = Java.type('org.objectweb.asm.tree.LabelNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

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
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                                mn.instructions.insertBefore(node, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/texture/DownloadingTexture", mapName("field_229155_i_"), "Ljava/lang/Runnable;"));
                                iterator.set(new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinBuffer", "processLegacySkin", "(Lnet/minecraft/client/renderer/texture/NativeImage;Ljava/lang/Runnable;)Lnet/minecraft/client/renderer/texture/NativeImage;", false));
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