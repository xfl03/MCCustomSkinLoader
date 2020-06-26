var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var InsnList = Java.type('org.objectweb.asm.tree.InsnList');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

var Opcodes = Java.type('org.objectweb.asm.Opcodes');

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
                    if (mn.name === 'func_175249_a' || mn.name === 'func_238523_a_') {
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKEVIRTUAL && node.name === "func_71387_A") {
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
                    if (mn.name === 'func_174884_b') {
                        var il = new InsnList();
                        il.add(new VarInsnNode(Opcodes.ALOAD, 0));
                        il.add(new InsnNode(Opcodes.ARETURN));
                        mn.instructions.insert(il);
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
                    if (mn.name === 'func_229159_a_') {
                        for (var iterator = mn.instructions.iterator(); iterator.hasNext();) {
                            var node = iterator.next();
                            if (node.getOpcode() === Opcodes.INVOKESTATIC && node.name === "func_229163_c_") {
                                mn.instructions.insertBefore(node, new VarInsnNode(Opcodes.ALOAD, 0));
                                mn.instructions.insertBefore(node, new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/renderer/texture/DownloadingTexture", "field_229155_i_", "Ljava/lang/Runnable;"));
                                iterator.set(new MethodInsnNode(Opcodes.INVOKESTATIC, "customskinloader/fake/FakeSkinBuffer", "processLegacySkin", "(Lnet/minecraft/client/renderer/texture/NativeImage;Ljava/lang/Runnable;)Lnet/minecraft/client/renderer/texture/NativeImage;", false));
                            }
                        }
                    }
                });
                return cn;
            }
        }
    };
}