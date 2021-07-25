package customskinloader.forge.platform;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import com.google.common.base.Strings;
import customskinloader.forge.ForgePlugin;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.objectweb.asm.commons.Remapper;

public class DefaultFMLPlatform implements IFMLPlatform {
    private final static String FML_PLUGIN_WRAPPER = "net.minecraftforge.fml.relauncher.CoreModManager$FMLPluginWrapper";

    @Override
    public Result init(Set<IFMLPlatform> otherPlatforms) {
        return otherPlatforms.size() == 0 ? Result.ACCEPT : Result.REJECT;
    }

    @Override
    public String getSide() {
        return FMLLaunchHandler.side().name();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<String> getIgnoredMods() {
        try {
            return (List<String>) CoreModManager.class.getMethod("getLoadedCoremods").invoke(null); // 1.8
        } catch (Exception e) {
            return CoreModManager.getIgnoredMods(); // 1.8.8~1.12.2
        }
    }

    @Override
    public String getName() {
        Annotation name = this.getNameAnnotation();
        if (name != null && !Strings.isNullOrEmpty(this.getNameValue(name))) {
            return this.getNameValue(name);
        }
        return this.getFMLLoadingPluginClass().getSimpleName();
    }

    @Override
    public int getSortingIndex() {
        Annotation index = this.getSortingIndexAnnotation();
        return index != null ? this.getSortingIndexValue(index) : 0;
    }

    @Override
    public ITweaker createFMLPluginWrapper(String name, File location, int sortIndex) throws Exception {
        Constructor<?> constructor = Class.forName(FML_PLUGIN_WRAPPER).getDeclaredConstructor(String.class, IFMLLoadingPlugin.class, File.class, int.class, String[].class);
        constructor.setAccessible(true);
        return (ITweaker) constructor.newInstance(name, this.getFMLLoadingPluginClass().newInstance(), location, sortIndex, new String[0]);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addLoadPlugins(ITweaker tweaker) throws Exception {
        Field field = CoreModManager.class.getDeclaredField("loadPlugins");
        field.setAccessible(true);
        ((List<ITweaker>) field.get(null)).add(tweaker);
    }

    @Override
    public Remapper getRemapper() {
        // DO NOT replace with lambda or replace call with method body.
        return new Supplier<Remapper>() {
            @Override
            public Remapper get() {
                return FMLDeobfuscatingRemapper.INSTANCE;
            }
        }.get();
    }

    protected Class<?> getFMLLoadingPluginClass() {
        return ForgePlugin.class;
    }

    protected Annotation getNameAnnotation() {
        return this.getFMLLoadingPluginClass().getAnnotation(IFMLLoadingPlugin.Name.class);
    }

    protected String getNameValue(Annotation annotationIn) {
        return ((IFMLLoadingPlugin.Name) annotationIn).value();
    }

    protected Annotation getSortingIndexAnnotation() {
        return this.getFMLLoadingPluginClass().getAnnotation(IFMLLoadingPlugin.SortingIndex.class);
    }

    protected int getSortingIndexValue(Annotation annotationIn) {
        return ((IFMLLoadingPlugin.SortingIndex) annotationIn).value();
    }
}
