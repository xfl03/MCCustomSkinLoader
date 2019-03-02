package customskinloader.forge;

import java.util.Map;

import net.minecraftforge.fml.common.FMLModContainer;
import net.minecraftforge.fml.common.ModContainerFactory;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin.Name;

import org.objectweb.asm.Type;

@Name("CustomSkinLoader")
public class ForgePlugin implements IFMLLoadingPlugin {
    static{
        //Transform Annotation in 1.13-
        ModContainerFactory.instance().registerContainerType(Type.getType(ModOld.class), FMLModContainer.class);
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{"customskinloader.forge.loader.LaunchWrapper"};//LaunchWrapper
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