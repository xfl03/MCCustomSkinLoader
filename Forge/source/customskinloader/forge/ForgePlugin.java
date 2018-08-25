package customskinloader.forge;

import java.util.Map;

import customskinloader.forge.transformer.LaunchWrapper;
import customskinloader.forge.transformer.ModLauncher;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;

@Name("CustomSkinLoader")
public class ForgePlugin implements IFMLLoadingPlugin {

    @Override
    public String[] getASMTransformerClass() {
        try {
            Class.forName("cpw.mods.modlauncher.api.ITransformer");
            return new String[]{"customskinloader.forge.transformer.ModLauncher"};//ModLauncher
        } catch (ClassNotFoundException ignored) {
            return new String[]{"customskinloader.forge.transformer.LaunchWrapper"};//LaunchWrapper
        }
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {
    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

}