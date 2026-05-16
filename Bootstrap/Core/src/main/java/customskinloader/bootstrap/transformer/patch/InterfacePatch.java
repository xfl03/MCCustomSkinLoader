package customskinloader.bootstrap.transformer.patch;

import customskinloader.bootstrap.transformer.ClassTransformationContext;

public final class InterfacePatch extends PatchSupport {
    public InterfacePatch() {
        super(
            "customskinloader:interface-patch", 1000,
            targets(
                HTTP_TEXTURE_PROCESSOR, "[,553]", // 19w37a- (1.14.4-)
                RESOURCE, "",
                RESOURCE_MANAGER, "",
                NATIVE_IMAGE, "[404,]", // 1.13.2+
                IDENTIFIER, ""
            )
        );
    }

    @Override
    public boolean transform(ClassTransformationContext context) {
        boolean modified = false;

        if (context.isTarget(HTTP_TEXTURE_PROCESSOR)) {
            modified |= this.requireModified("http-texture-processor.interface", this.addInterface(context.getCurrentClassNode(), FAKE_HTTP_TEXTURE_PROCESSOR));
        }
        if (context.isTarget(RESOURCE)) {
            boolean resourceModified = false;
            resourceModified |= this.addInterface(context.getCurrentClassNode(), FAKE_I_RESOURCE_V1);
            resourceModified |= this.addInterface(context.getCurrentClassNode(), FAKE_I_RESOURCE_V2);
            modified |= this.requireModified("resource.interfaces", resourceModified);
        }
        if (context.isTarget(RESOURCE_MANAGER)) {
            boolean resourceManagerModified = false;
            resourceManagerModified |= this.addInterface(context.getCurrentClassNode(), FAKE_I_RESOURCE_MANAGER_V1);
            resourceManagerModified |= this.addInterface(context.getCurrentClassNode(), FAKE_I_RESOURCE_MANAGER_V2);
            modified |= this.requireModified("resource-manager.interfaces", resourceManagerModified);
        }
        if (context.isTarget(NATIVE_IMAGE)) {
            modified |= this.requireModified("native-image.interface", this.addInterface(context.getCurrentClassNode(), FAKE_NATIVE_IMAGE));
        }
        if (context.isTarget(IDENTIFIER)) {
            modified |= this.requireModified("resource-location.patch", this.makeMethodPublicNonFinal(context.findMethod(IDENTIFIER, "<init>", "(" + objectDesc(STRING) + objectDesc(STRING) + ")V")));
        }

        return modified;
    }
}
