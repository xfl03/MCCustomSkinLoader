package customskinloader.config;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import customskinloader.CustomSkinLoader;
import customskinloader.loader.ProfileLoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.plugin.PluginLoader;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.Version;
import org.apache.commons.io.FileUtils;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Config {
    //Program
    public String version;
    public int buildNumber;
    public List<SkinSiteProfile> loadlist;

    //Function
    public boolean enableDynamicSkull = true;
    public boolean enableTransparentSkin = true;
    public boolean forceLoadAllTextures = true;
    public boolean enableCape = true;
    public int threadPoolSize = 8;
    /**
     * Can logger write message to standard output(System.out).
     * Because standard output won't write to latest.log after Forge 1.17,
     * printing to standard output is meaningless,
     * which makes this switch default value is <code>false</code>.
     * @since 14.15
     */
    public boolean enableLogStdOut = false;

    //Profile Cache
    public int cacheExpiry = 30;
    public boolean forceUpdateSkull = false;
    public boolean enableLocalProfileCache = false;

    //Network Cache
    public boolean enableCacheAutoClean = false;
    public boolean forceDisableCache = false;

    // Used by Gson to create an instance with default value.
    public Config() {
        this(new ArrayList<>());
    }

    //Init config
    public Config(List<SkinSiteProfile> loadlist) {
        this.version = CustomSkinLoader.CustomSkinLoader_VERSION;
        this.buildNumber = CustomSkinLoader.CustomSkinLoader_BUILD_NUMBER;
        this.loadlist = loadlist;
    }

    public static Config loadConfig0() {
        Config config = loadConfig();

        //LoadList null checker
        if (config.loadlist == null) {
            config.loadlist = new ArrayList<>();
        } else {
            for (int i = 0; i < config.loadlist.size(); i++) {
                if (config.loadlist.get(i) == null)
                    config.loadlist.remove(i--);
            }
        }

        //Init program
        config.loadExtraList();
        config.updateLoadlist();
        config.initLocalFolder();
        config.threadPoolSize = Math.max(config.threadPoolSize, 1);
        if (config.enableCacheAutoClean && !config.enableLocalProfileCache) {
            try {
                FileUtils.deleteDirectory(HttpRequestUtil.CACHE_DIR);
                FileUtils.deleteDirectory(HttpTextureUtil.getCacheDir());
                CustomSkinLoader.logger.info("Successfully cleaned cache.");
            } catch (Exception e) {
                CustomSkinLoader.logger.warning("Exception occurs while cleaning cache: " + e);
            }
        }

        //Check config version
        Version configVersion = Version.of(config.version);
        if (CustomSkinLoader.CustomSkinLoader_BUILD_NUMBER == 0 // Custom builds
                || configVersion.compareTo(CustomSkinLoader.CustomSkinLoader_VERSION) < 0
                || config.buildNumber < CustomSkinLoader.CustomSkinLoader_BUILD_NUMBER) {
            CustomSkinLoader.logger.info("Config File is out of date: " + config.version +
                    ", build number: " + config.buildNumber);
            config.version = CustomSkinLoader.CustomSkinLoader_VERSION;
            config.buildNumber = CustomSkinLoader.CustomSkinLoader_BUILD_NUMBER;
        }
        writeConfig(config, true);

        //Output config
        for (Field field : config.getClass().getDeclaredFields()) {
            try {
                Object value = field.get(config);
                CustomSkinLoader.logger.info(field.getName() + " : " + value);
            } catch (Exception ignored) {
            }
        }

        return config;
    }

    private static Config loadConfig() {
        CustomSkinLoader.logger.info("Config File: " + CustomSkinLoader.CONFIG_FILE.getAbsolutePath());
        if (!CustomSkinLoader.CONFIG_FILE.exists()) {
            CustomSkinLoader.logger.info("Config file not found, use default instead.");
            return initConfig();
        }
        try {
            CustomSkinLoader.logger.info("Try to load config.");
            String json = FileUtils.readFileToString(CustomSkinLoader.CONFIG_FILE, "UTF-8");
            Config config = Objects.requireNonNull(CustomSkinLoader.GSON.fromJson(json, Config.class));
            CustomSkinLoader.logger.info("Successfully load config.");
            return config;
        } catch (Exception e) {
            CustomSkinLoader.logger.info("Failed to load config, use default instead.(" + e + ")");
            createBrokenFile(CustomSkinLoader.CONFIG_FILE);
            return initConfig();
        }
    }

    private void loadExtraList() {
        File listAddition = new File(CustomSkinLoader.DATA_DIR, "ExtraList");
        if (!listAddition.isDirectory()) {
            listAddition.mkdirs();
            return;
        }
        List<SkinSiteProfile> adds = new ArrayList<>();
        File[] files = listAddition.listFiles();
        for (File file : files != null ? files : new File[0]) {
            if (!file.getName().toLowerCase().endsWith(".json") && !file.getName().toLowerCase().endsWith(".txt"))
                continue;
            try {
                CustomSkinLoader.logger.info("Try to load Extra List.(" + file.getName() + ")");
                String json = FileUtils.readFileToString(file, "UTF-8");
                SkinSiteProfile ssp = CustomSkinLoader.GSON.fromJson(json, SkinSiteProfile.class);
                CustomSkinLoader.logger.info("Successfully load Extra List.");
                if (ssp.type != null) {
                    ProfileLoader.IProfileLoader loader = ProfileLoader.LOADERS.get(ssp.type.toLowerCase());
                    if (loader == null) {
                        CustomSkinLoader.logger.info("Extra List will be ignored: Type '" + ssp.type +
                                "' is not defined.");
                        continue;
                    }
                    boolean duplicate = false;
                    for (SkinSiteProfile ssp0 : this.loadlist) {
                        if (!ssp0.type.equalsIgnoreCase(ssp.type))
                            continue;
                        if (loader.compare(ssp0, ssp)) {
                            duplicate = true;
                            break;
                        }
                    }
                    if (!duplicate) {
                        adds.add(ssp);
                        CustomSkinLoader.logger.info("Successfully apply Extra List.(" + ssp.name + ")");
                    } else {
                        CustomSkinLoader.logger.info("Extra List will be ignored: Duplicate.(" + ssp.name + ")");
                    }
                    file.delete();
                } else {
                    CustomSkinLoader.logger.info("Extra List is invalid: Type is not defined.(" +
                            file.getName() + ")");
                    createBrokenFile(file);
                }
            } catch (Exception e) {
                CustomSkinLoader.logger.info("Failed to load Extra List.(" + e + ")");
                createBrokenFile(file);
            }
        }
        if (adds.size() != 0) {
            adds.addAll(this.loadlist);
            this.loadlist = adds;
        }
    }

    private void updateLoadlist() {
        PluginLoader.PLUGINS.stream()
                .map(ICustomSkinLoaderPlugin::getDefaultProfiles)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .forEach(profile -> this.loadlist.stream()
                        .filter(ssp -> profile.getName().equals(ssp.name))
                        .forEach(profile::updateSkinSiteProfile));
    }

    private void initLocalFolder() {
        for (SkinSiteProfile ssp : this.loadlist) {
            if (ssp.type == null)
                continue;
            ProfileLoader.IProfileLoader loader = ProfileLoader.LOADERS.get(ssp.type.toLowerCase());
            if (loader == null)
                continue;
            loader.init(ssp);
        }
    }

    private static void createBrokenFile(File file) {
        try {
            File brokenFile = new File(file.getParentFile(), file.getName() + ".broken");
            if (brokenFile.exists()) {
                brokenFile.delete();
            }
            file.renameTo(brokenFile);
        } catch (Exception e) {
            CustomSkinLoader.logger.warning("Failed to create broken file. (" + file.getName() + ")");
            CustomSkinLoader.logger.warning(e);
        }
    }

    // The config file does not exist or was broken.
    private static Config initConfig() {
        List<ICustomSkinLoaderPlugin.IDefaultProfile> profiles = new ArrayList<>();
        for (ICustomSkinLoaderPlugin plugin : PluginLoader.PLUGINS) {
            List<ICustomSkinLoaderPlugin.IDefaultProfile> defaultProfiles = plugin.getDefaultProfiles();
            if (defaultProfiles != null) {
                profiles.addAll(defaultProfiles);
            }
        }
        profiles.sort(Comparator.comparingInt(ICustomSkinLoaderPlugin.IDefaultProfile::getPriority));

        List<SkinSiteProfile> loadlist = new ArrayList<>();
        for (ICustomSkinLoaderPlugin.IDefaultProfile profile : profiles) {
            SkinSiteProfile ssp = new SkinSiteProfile();
            ssp.name = profile.getName();
            profile.updateSkinSiteProfile(ssp);
            loadlist.add(ssp);
        }

        Config config = new Config(loadlist);
        writeConfig(config, false);
        return config;
    }

    private static void writeConfig(Config config, boolean update) {
        String json = CustomSkinLoader.GSON.toJson(config);
        if (CustomSkinLoader.CONFIG_FILE.exists())
            CustomSkinLoader.CONFIG_FILE.delete();
        try {
            CustomSkinLoader.CONFIG_FILE.createNewFile();
            FileUtils.write(CustomSkinLoader.CONFIG_FILE, json, "UTF-8");
            CustomSkinLoader.logger.info("Successfully " + (update ? "update" : "create") + " config.");
        } catch (Exception e) {
            CustomSkinLoader.logger.info("Failed to " + (update ? "update" : "create") + " config.(" + e + ")");
        }
    }
}
