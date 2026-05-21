package customskinloader.bootstrap.forge.v1;

import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;

import java.util.Map;

@IFMLLoadingPlugin.Name("CustomSkinLoaderBootstrap")
@IFMLLoadingPlugin.SortingIndex(1010)
@IFMLLoadingPlugin.TransformerExclusions("customskinloader.bootstrap.")
public final class LoadingPlugin implements IFMLLoadingPlugin {
    @Override
    public String[] getASMTransformerClass() {
        return new String[] { "customskinloader.bootstrap.forge.v1.ClassTransformerAdapter" };
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return "customskinloader.bootstrap.forge.v1.CallHook";
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
