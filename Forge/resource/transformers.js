var AbstractInsnNode = Java.type('org.objectweb.asm.tree.AbstractInsnNode');
var FieldInsnNode = Java.type('org.objectweb.asm.tree.FieldInsnNode');
var FieldNode = Java.type('org.objectweb.asm.tree.FieldNode');
var InsnNode = Java.type('org.objectweb.asm.tree.InsnNode');
var MethodInsnNode = Java.type('org.objectweb.asm.tree.MethodInsnNode');
var TypeInsnNode = Java.type('org.objectweb.asm.tree.TypeInsnNode');
var VarInsnNode = Java.type('org.objectweb.asm.tree.VarInsnNode');

var Opcodes = Java.type('org.objectweb.asm.Opcodes');

function SkinManagerTransformer() {
    return {
        'InitTransformer': function (cn, mn) {
            cn.fields.add(new FieldNode(Opcodes.ACC_PRIVATE, "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;", null, null));
            mn.instructions.clear();
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            mn.instructions.add(new TypeInsnNode(Opcodes.NEW, "customskinloader/fake/FakeSkinManager"));
            mn.instructions.add(new InsnNode(Opcodes.DUP));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "customskinloader/fake/FakeSkinManager", "<init>",
                "(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V", false));
            mn.instructions.add(new FieldInsnNode(Opcodes.PUTFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            mn.instructions.add(new InsnNode(Opcodes.RETURN));
        },
        'LoadSkinTransformer': function (cn, mn) {
            mn.instructions.clear();
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 3));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "customskinloader/fake/FakeSkinManager", "loadSkin",
                "(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;", false));
            mn.instructions.add(new InsnNode(Opcodes.ARETURN));
        },
        'LoadProfileTexturesTransformer': function (cn, mn) {
            mn.instructions.clear();
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 2));
            mn.instructions.add(new VarInsnNode(Opcodes.ILOAD, 3));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "customskinloader/fake/FakeSkinManager", "loadProfileTextures",
                "(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V", false));
            mn.instructions.add(new InsnNode(Opcodes.RETURN));
        },
        'LoadSkinFromCacheTransformer': function (cn, mn) {
            mn.instructions.clear();
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 0));
            mn.instructions.add(new FieldInsnNode(Opcodes.GETFIELD, "net/minecraft/client/resources/SkinManager", "fakeManager", "Lcustomskinloader/fake/FakeSkinManager;"));
            mn.instructions.add(new VarInsnNode(Opcodes.ALOAD, 1));
            mn.instructions.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "customskinloader/fake/FakeSkinManager", "loadSkinFromCache",
                "(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;", false));
            mn.instructions.add(new InsnNode(Opcodes.ARETURN));
        }
    };
}

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

function FakeSkinManagerTransformer(cn, mn) {
    var TARGET_CLASS = "net/minecraft/client/renderer/ThreadDownloadImageData";
    var NEW_TARGET_CLASS = "net/minecraft/client/renderer/texture/ThreadDownloadImageData";
    var CALLBACK_CLASS = "net/minecraft/client/resources/SkinManager$SkinAvailableCallback";

    var il = mn.instructions;
    var li = il.iterator();
    while (li.hasNext()) {
        var ain = li.next();
        if (ain instanceof MethodInsnNode) {
            var min = ain;

            if (min.owner.equals(TARGET_CLASS)) {
                min.owner = NEW_TARGET_CLASS;
                //il.set(min, new MethodInsnNode(min.getOpcode(), NEW_TARGET_CLASS, min.name, min.desc, false));
            }
            if (min.owner.equals(CALLBACK_CLASS) && min.name.equals("func_180521_a")) {
                min.name = "onSkinTextureAvailable";
                //il.set(min, new MethodInsnNode(min.getOpcode(), min.owner, "onSkinTextureAvailable", min.desc, false));
            }
        } else if (ain instanceof TypeInsnNode) {
            var tin = ain;

            if (tin.desc.equals(TARGET_CLASS)) {
                tin.desc = NEW_TARGET_CLASS;
            }
        }
    }
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
                    if (mn.name === '<init>')
                        SkinManagerTransformer()['InitTransformer'](cn, mn);
                    else if(mn.name === 'func_152789_a')
                        SkinManagerTransformer()['LoadSkinTransformer'](cn, mn);
                    else if(mn.name === 'func_152790_a')
                        SkinManagerTransformer()['LoadProfileTexturesTransformer'](cn, mn);
                    else if(mn.name === 'func_152788_a')
                        SkinManagerTransformer()['LoadSkinFromCacheTransformer'](cn, mn);
                });
                return cn;
            }
        },
        'PlayerTabTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'net/minecraft/client/gui/GuiPlayerTabOverlay'
            },
            'transformer': function (cn) {
                cn.methods.forEach(function (mn) {
                    if (mn.name === 'func_175249_a')
                        PlayerTabTransformer(cn, mn);
                });
                return cn;
            }
        },
        'FakeSkinManagerTransformer': {
            'target': {
                'type': 'CLASS',
                'name': 'customskinloader/fake/FakeSkinManager'
            },
            'transformer': function (cn) {
                cn.methods.forEach(function (mn) {
                    //if (mn.name === '<init>')
                    FakeSkinManagerTransformer(cn, mn);
                });
                return cn;
            }
        }
    };
}