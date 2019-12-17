var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var MethodNode = Java.type('org.objectweb.asm.tree.MethodNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

var Opcodes = Java.type('org.objectweb.asm.Opcodes');

var hasITextureObject = true;

function PlayerTabTransformer(cn, mn) {
    var iterator = mn.instructions.iterator();
    while (iterator.hasNext()) {
        var node = iterator.next();
        if (node instanceof VarInsnNode) {
            var varNode = node;
            if (varNode.getOpcode() === Opcodes.ISTORE && varNode.var === 11) {
                mn.instructions.set(varNode.getPrevious(), new InsnNode(Opcodes.ICONST_1));
            }
        }
    }
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
                    if (mn.name === 'func_175249_a')
                        PlayerTabTransformer(cn, mn);
                });
                return cn;
            }
        },
        'SkinAvailableCallbackTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/resources/SkinManager$SkinAvailableCallback'
            },
            'transformer': function (cn) {
                cn.version = Opcodes.V1_8;
                cn.methods.forEach(function (mn) {
                    mn.access -= Opcodes.ACC_ABSTRACT;
                    for (var j = 0; j < 4; j++) {
                        mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, j));
                    }
                    mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEINTERFACE, cn.name, mn.name.equals("func_180521_a") ? "onSkinTextureAvailable" : "func_180521_a", mn.desc, true));
                    mn.instructions.add(new InsnNode(Opcodes.RETURN));
                });
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
                if (!cn.methods.stream().anyMatch(function(mn) {
                    return mn.name.equals("func_152344_a");
                })) {
                    var methodNode = new MethodNode(Opcodes.ACC_PUBLIC, "func_152344_a", "(Ljava/lang/Runnable;)Lcom/google/common/util/concurrent/ListenableFuture;", "(Ljava/lang/Runnable;)Lcom/google/common/util/concurrent/ListenableFuture<Ljava/lang/Object;>;", null);
                    methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
                    methodNode.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
                    methodNode.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "net/minecraft/util/concurrent/ThreadTaskExecutor", "execute", "(Ljava/lang/Runnable;)V", false));
                    methodNode.instructions.add(new InsnNode(Opcodes.ACONST_NULL));
                    methodNode.instructions.add(new InsnNode(Opcodes.ARETURN));
                    cn.methods.add(methodNode);
                }
                return cn;
            }
        },

        // For 1.15+
        'ITextureObjectTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/renderer/texture/ITextureObject'
            },
            'transformer': function (cn) {
                hasITextureObject = !cn.methods.isEmpty();
                return cn;
            }
        },
        'FakeSkinManagerTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'customskinloader/fake/FakeSkinManager'
            },
            'transformer': function (cn) {
                if (!hasITextureObject) {
                    cn.methods.stream().filter(function (mn) {
                        return mn.name.equals("loadSkin") && mn.desc.equals("(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;");
                    }).forEach(function (mn) {
                        mn.instructions.iterator().forEachRemaining(function (ain) {
                            if (ain instanceof MethodInsnNode && ain.owner.equals("net/minecraft/client/renderer/texture/TextureManager")) {
                                if (ain.name.equals("func_110579_a")) ain.name = "func_229263_a_";
                                if (ain.name.equals("func_110581_b")) ain.name = "func_229267_b_";
                                ain.desc = ain.desc.replace("Lnet/minecraft/client/renderer/texture/ITextureObject;", "Lnet/minecraft/client/renderer/texture/Texture;");
                            }
                        });
                    });
                }
                return cn;
            }
        }
    };
}