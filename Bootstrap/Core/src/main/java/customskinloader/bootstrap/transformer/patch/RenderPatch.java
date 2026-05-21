package customskinloader.bootstrap.transformer.patch;

import customskinloader.bootstrap.transformer.ClassTransformationContext;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

public final class RenderPatch extends PatchSupport {
    public RenderPatch() {
        super(
            "customskinloader:render-patch", 1000,
            targets(
                PLAYER_TAB_OVERLAY, "",
                CAPE_LAYER, "[556,]", // 19w39a+ (1.15+)
                PLAYER_RENDERER, "[556,767],[801,803],[0x40000001,0x400000CC]"  // 19w39a ~ 1.21.1 (1.15 ~ 1.21.1)
            )
        );
    }

    @Override
    public boolean transform(ClassTransformationContext context) {
        boolean modified = false;

        if (context.isTarget(PLAYER_TAB_OVERLAY)) {
            modified |= this.requireModified("player-tab-overlay", this.patchPlayerTabOverlay(context));
        }
        if (context.isTarget(CAPE_LAYER)) {
            modified |= this.requireModified("cape-layer", this.patchCapeLayer(context), false);
        }
        if (context.isTarget(PLAYER_RENDERER)) {
            modified |= this.requireModified("player-renderer", this.patchPlayerRenderer(context), false);
        }

        return modified;
    }

    private boolean patchPlayerTabOverlay(ClassTransformationContext context) {
        boolean modified = false;
        // 20w16a- (1.15.2-)
        modified |= this.applyIfMatches("[,712]", "player-tab-overlay.render.v1", () -> replaceMinecraftLocalServerCheck(
            context, context.findMethod(PLAYER_TAB_OVERLAY, "render", "(I" + objectDesc(SCOREBOARD) + objectDesc(OBJECTIVE) + ")V"), MINECRAFT, "isLocalServer"
        ));
        // 20w17a ~ 23w14a (1.16 ~ 1.19.4)
        modified |= this.applyIfMatches("[713,762],[801,803],[0x40000001,0x40000082]", "player-tab-overlay.render.v2", () -> replaceMinecraftLocalServerCheck(
            context, context.findMethod(PLAYER_TAB_OVERLAY, "render", "(" + objectDesc(POSE_STACK) + "I" + objectDesc(SCOREBOARD) + objectDesc(OBJECTIVE) + ")V"), MINECRAFT, "isLocalServer"
        ));
        // 23w16a ~ 26.1-snapshot-11 (1.20 ~ 1.21.11)
        modified |= this.applyIfMatches("[763,774],[0x40000083,0x40000129]", "player-tab-overlay.render.v3", () -> replaceMinecraftLocalServerCheck(
            context, context.findMethod(PLAYER_TAB_OVERLAY, "render", "(" + objectDesc(GUI_GRAPHICS) + "I" + objectDesc(SCOREBOARD) + objectDesc(OBJECTIVE) + ")V"), MINECRAFT, "isLocalServer"
        ));
        // 26.1-pre-1 ~ 26.2-snapshot-6 (26.1 ~ 26.1.2)
        modified |= this.applyIfMatches("775,[0x4000012A,0x40000138]", "player-tab-overlay.extract-render-state.v1", () -> replaceMinecraftLocalServerCheck(
            context, context.findMethod(PLAYER_TAB_OVERLAY, "extractRenderState", "(" + objectDesc(GUI_GRAPHICS_EXTRACTOR) + "I" + objectDesc(SCOREBOARD) + objectDesc(OBJECTIVE) + ")V"), MINECRAFT, "isLocalServer"
        ));
        // 26.2-snapshot-7+ (26.2+)
        modified |= this.applyIfMatches("[776,800],[804,0x40000000],[0x40000139,]", "player-tab-overlay.extract-render-state.v2", () -> replaceMinecraftLocalServerCheck(
            context, context.findMethod(PLAYER_TAB_OVERLAY, "extractRenderState", "(" + objectDesc(GUI_GRAPHICS_EXTRACTOR) + "I" + objectDesc(SCOREBOARD) + objectDesc(OBJECTIVE) + ")V"), CLIENT_PACKET_LISTENER, "onlineMode"
        ));
        return modified;
    }

    private boolean replaceMinecraftLocalServerCheck(ClassTransformationContext context, MethodNode methodNode, String owner, String name) {
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        String remappedOwner = context.remapClassName(owner);
        String remappedName = context.remapMethodName(owner, name, "()Z");
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!remappedOwner.equals(methodInsnNode.owner) || !remappedName.equals(methodInsnNode.name) || !"()Z".equals(methodInsnNode.desc)) {
                continue;
            }

            InsnList replacement = new InsnList();
            replacement.add(new InsnNode(POP));
            replacement.add(new InsnNode(ICONST_1));
            methodNode.instructions.insertBefore(instruction, replacement);
            methodNode.instructions.remove(instruction);
            modified = true;
        }

        return modified;
    }


    private boolean patchCapeLayer(ClassTransformationContext context) {
        boolean modified = false;
        // 19w39a ~ 19w44a
        modified |= this.applyIfMatches("[556,560]", "cape-layer.render.v1", () -> replaceEntitySolidWithTranslucent(
            context, context.findMethod(CAPE_LAYER, "render", "(" + objectDesc(POSE_STACK) + objectDesc(MULTI_BUFFER_SOURCE) + "I" + objectDesc(ABSTRACT_CLIENT_PLAYER) + "FFFFFFF)V"),
            RENDER_TYPE
        ));
        // 19w45a ~ 1.21.1 (1.15 ~ 1.21.1)
        modified |= this.applyIfMatches("[561,767],[801,803],[0x40000001,0x400000CC]", "cape-layer.render.v2", () -> replaceEntitySolidWithTranslucent(
            context, context.findMethod(CAPE_LAYER, "render", "(" + objectDesc(POSE_STACK) + objectDesc(MULTI_BUFFER_SOURCE) + "I" + objectDesc(ABSTRACT_CLIENT_PLAYER) + "FFFFFF)V"),
            RENDER_TYPE
        ));
        // 24w33a ~ 1.21.8 (1.21.2 ~ 1.21.8)
        modified |= this.applyIfMatches("[768,772],[0x400000CD,0x40000103]", "cape-layer.render.v3", () -> replaceEntitySolidWithTranslucent(
            context, context.findMethod(CAPE_LAYER, "render", "(" + objectDesc(POSE_STACK) + objectDesc(MULTI_BUFFER_SOURCE) + "I" + objectDesc(PLAYER_RENDER_STATE) + "FF)V"),
            RENDER_TYPE
        ));
        // 25w31a ~ 25w42a (1.21.9 ~ 1.21.10)
        modified |= this.applyIfMatches("773,[0x40000104,0x40000112]", "cape-layer.submit.v1", () -> replaceEntitySolidWithTranslucent(
            context, context.findMethod(CAPE_LAYER, "submit", "(" + objectDesc(POSE_STACK) + objectDesc(SUBMIT_NODE_COLLECTOR) + "I" + objectDesc(AVATAR_RENDER_STATE) + "FF)V"),
            RENDER_TYPE
        ));
        // 25w43a+ (1.21.11+)
        modified |= this.applyIfMatches("[774,800],[804,0x40000000],[0x40000113,]", "cape-layer.submit.v2", () -> replaceEntitySolidWithTranslucent(
            context, context.findMethod(CAPE_LAYER, "submit", "(" + objectDesc(POSE_STACK) + objectDesc(SUBMIT_NODE_COLLECTOR) + "I" + objectDesc(AVATAR_RENDER_STATE) + "FF)V"),
            RENDER_TYPES
        ));
        return modified;
    }

    private boolean patchPlayerRenderer(ClassTransformationContext context) {
        boolean modified = false;
        // 19w39a ~ 19w44a
        modified |= this.applyIfMatches("[556,560]", "player-renderer.render-hand.v1", () -> replaceEntitySolidWithTranslucent(
            context, context.findMethod(PLAYER_RENDERER, "renderHand", "(" + objectDesc(POSE_STACK) + objectDesc(MULTI_BUFFER_SOURCE) + objectDesc(ABSTRACT_CLIENT_PLAYER) + objectDesc(MODEL_PART) + objectDesc(MODEL_PART) + ")V"),
            RENDER_TYPE
        ));
        // 19w45a ~ 1.21.1 (1.15 ~ 1.21.1)
        modified |= this.applyIfMatches("[561,767],[801,803],[0x40000001,0x400000CC]", "player-renderer.render-hand.v2", () -> replaceEntitySolidWithTranslucent(
            context, context.findMethod(PLAYER_RENDERER, "renderHand", "(" + objectDesc(POSE_STACK) + objectDesc(MULTI_BUFFER_SOURCE) + "I" + objectDesc(ABSTRACT_CLIENT_PLAYER) + objectDesc(MODEL_PART) + objectDesc(MODEL_PART) + ")V"),
            RENDER_TYPE
        ));
        return modified;
    }

    private boolean replaceEntitySolidWithTranslucent(ClassTransformationContext context, MethodNode methodNode, String owner) {
        if (methodNode == null) {
            return false;
        }

        boolean modified = false;
        String remappedOwner = context.remapClassName(owner);
        String desc = "(" + objectDesc(IDENTIFIER) + ")" + objectDesc(RENDER_TYPE);
        String remappedDesc = context.remapMethodDescriptor(desc);
        String originalName = context.remapMethodName(owner, "entitySolid", desc);
        String replacementName = context.remapMethodName(owner, "entityTranslucent", desc);
        for (AbstractInsnNode instruction : methodNode.instructions.toArray()) {
            if (!(instruction instanceof MethodInsnNode)) {
                continue;
            }

            MethodInsnNode methodInsnNode = (MethodInsnNode) instruction;
            if (!remappedOwner.equals(methodInsnNode.owner) || !originalName.equals(methodInsnNode.name) || !remappedDesc.equals(methodInsnNode.desc)) {
                continue;
            }

            methodNode.instructions.set(instruction, new MethodInsnNode(INVOKESTATIC, remappedOwner, replacementName, remappedDesc, false));
            modified = true;
        }

        return modified;
    }
}
