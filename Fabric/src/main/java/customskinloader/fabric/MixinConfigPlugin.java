package customskinloader.fabric;

public class MixinConfigPlugin extends customskinloader.mixin.core.MixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {
        super.onLoad(mixinPackage);

        // This mod will remap extra classes when in the development environment.
        DevEnvRemapper.initRemapper();
    }
}
