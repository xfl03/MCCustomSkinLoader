package customskinloader.forge.platform;

import java.io.File;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import customskinloader.forge.ForgeTweaker;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.commons.Remapper;

public interface IFMLPlatform {
    enum Result {
        /**
         * The platform container will be used.
         */
        ACCEPT,

        /**
         * The platform container won't be used.
         */
        REJECT
    }

    class FMLPlatformInitializer {
        private static IFMLPlatform platform;

        @SuppressWarnings("unchecked")
        public static void initFMLPlatform() throws Exception {
            Set<IFMLPlatform> platforms = new HashSet<>();
            List<Throwable> throwables = new ArrayList<>();

            for (Iterator<IFMLPlatform> iterator = ServiceLoader.load(IFMLPlatform.class).iterator(); iterator.hasNext(); ) {
                try {
                    platforms.add(iterator.next());
                } catch (Throwable t) {
                    throwables.add(t);
                }
            }

            for (IFMLPlatform platform0 : platforms) {
                Set<IFMLPlatform> otherPlatforms = new HashSet<>(platforms);
                otherPlatforms.remove(platform0);
                if (platform0.init(otherPlatforms).equals(Result.ACCEPT)) {
                    if (platform == null) {
                        platform = platform0;
                    } else {
                        throw new RuntimeException("Duplicated platforms! (" + platform.getClass().getName() + ", " + platform0 + ")");
                    }
                }
            }
            if (platform == null) {
                for (int i = 0, len = throwables.size(); i < len; i++) {
                    Throwable throwable = throwables.get(i);
                    ForgeTweaker.logger.warning("Platform - %s :", i);
                    ForgeTweaker.logger.warning(throwable);
                }
                throw new RuntimeException("No available platform!");
            }

            // CustomSkinLoader is a client side mod
            if (!platform.getSide().equals("CLIENT")) {
                return;
            }

            CodeSource codeSource = FMLPlatformInitializer.class.getProtectionDomain().getCodeSource();
            if (codeSource != null) {
                URL location = codeSource.getLocation();
                File file = new File(location.toURI());
                if (file.isFile()) {
                    // This forces forge to reexamine the jar file for FML mods.
                    platform.getIgnoredMods().remove(file.getName());
                }

                ITweaker tweaker = platform.createFMLPluginWrapper(platform.getName(), file, platform.getSortingIndex());
                platform.addLoadPlugins(tweaker);
                ((List<ITweaker>) Launch.blackboard.get("Tweaks")).add(tweaker);
            } else {
                ForgeTweaker.logger.warning("No CodeSource, if this is not a development environment we might run into problems!");
                ForgeTweaker.logger.warning(FMLPlatformInitializer.class.getProtectionDomain().toString());
            }
        }

        public static IFMLPlatform getPlatform() {
            return platform;
        }
    }

    Result init(Set<IFMLPlatform> otherPlatforms);

    String getSide();

    List<String> getIgnoredMods();

    String getName();

    int getSortingIndex();

    ITweaker createFMLPluginWrapper(String name, File location, int sortIndex) throws Exception;

    void addLoadPlugins(ITweaker tweaker) throws Exception;

    Remapper getRemapper();
}
