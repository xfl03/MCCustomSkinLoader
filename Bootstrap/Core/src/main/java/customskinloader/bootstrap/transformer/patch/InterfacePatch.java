package customskinloader.bootstrap.transformer.patch;

public final class InterfacePatch extends PatchSupport {
    public InterfacePatch() {
        super("customskinloader:interface-patch", 1000);

        // 19w37a- (1.14.4-)
        this.rule("http-texture-processor.interface", HTTP_TEXTURE_PROCESSOR, "[,553]", context ->
            this.addInterface(context.getCurrentClassNode(), FAKE_HTTP_TEXTURE_PROCESSOR));

        this.rule("resource.interfaces", RESOURCE, "", context ->
            this.addInterface(context.getCurrentClassNode(), FAKE_I_RESOURCE_V1) | this.addInterface(context.getCurrentClassNode(), FAKE_I_RESOURCE_V2));

        this.rule("resource-manager.interfaces", RESOURCE_MANAGER, "", context ->
            this.addInterface(context.getCurrentClassNode(), FAKE_I_RESOURCE_MANAGER_V1) | this.addInterface(context.getCurrentClassNode(), FAKE_I_RESOURCE_MANAGER_V2));

        // 1.13.2+
        this.rule("native-image.interface", NATIVE_IMAGE, "[404,]", context ->
            this.addInterface(context.getCurrentClassNode(), FAKE_NATIVE_IMAGE));

        this.rule("identifier.constructor", IDENTIFIER, "", context ->
            this.makeMethodPublicNonFinal(context.findMethod(IDENTIFIER, "<init>", "(" + objectDesc(STRING) + objectDesc(STRING) + ")V")));
    }
}
