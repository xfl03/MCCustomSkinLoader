package customskinloader.loader;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import com.google.common.collect.Lists;
import com.mojang.authlib.GameProfile;
import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpUtil0;

public class LegacyLoader implements ICustomSkinLoaderPlugin, ProfileLoader.IProfileLoader {

    @Override
    public ProfileLoader.IProfileLoader getProfileLoader() {
        return this;
    }

    @Override
    public List<IDefaultProfile> getDefaultProfiles() {
        return Lists.newArrayList(new LocalSkin(this),
                getCapeProfile(this, "OptiFine", "https://optifine.net/capes/{USERNAME}.png", 810),
                getCapeProfile(this, "CloakPlus", "http://161.35.130.99/capes/{USERNAME}.png", 840),
                getCapeProfile(this, "LabyMod", "https://dl.labymod.net/capes/{STANDARD_UUID}", 850),
                getCapeProfile(this, "Cosmetica", "https://api.cosmetica.cc/get/cloak?username={USERNAME}&uuid={STANDARD_UUID}&nothirdparty", 860)
                );
    }

    public abstract static class DefaultProfile implements ICustomSkinLoaderPlugin.IDefaultProfile {
        protected final LegacyLoader loader;

        public DefaultProfile(LegacyLoader loader) {
            this.loader = loader;
        }

        @Override
        public void updateSkinSiteProfile(SkinSiteProfile ssp) {
            ssp.type = this.loader.getName();
            if (ssp.checkPNG == null) {
                ssp.checkPNG = false;
            }
            if (ssp.model == null) {
                ssp.model = "auto";
            }
            //Set texture url when it is empty
            //Remote texture url could be changed, it will be auto updated here
            if (ssp.skin == null || !HttpUtil0.isLocal(this.getSkinRoot())) {
                ssp.skin = this.getSkinRoot();
            }
            if (ssp.cape == null || !HttpUtil0.isLocal(this.getCapeRoot())) {
                ssp.cape = this.getCapeRoot();
            }
            if (ssp.elytra == null || !HttpUtil0.isLocal(this.getElytraRoot())) {
                ssp.elytra = this.getElytraRoot();
            }
        }

        public abstract String getSkinRoot();

        public abstract String getCapeRoot();

        public abstract String getElytraRoot();
    }

    public static class LocalSkin extends LegacyLoader.DefaultProfile {
        public LocalSkin(LegacyLoader loader) {
            super(loader);
        }

        @Override
        public String getName() {
            return "LocalSkin";
        }

        @Override
        public int getPriority() {
            return 710;
        }

        @Override
        public String getSkinRoot() {
            return "LocalSkin/skins/{USERNAME}.png";
        }

        @Override
        public String getCapeRoot() {
            return "LocalSkin/capes/{USERNAME}.png";
        }

        @Override
        public String getElytraRoot() {
            return "LocalSkin/elytras/{USERNAME}.png";
        }
    }

    /**
     * Get cape Profile¬
     *
     * @since 14.16
     */
    public static LegacyLoader.DefaultProfile getCapeProfile(LegacyLoader loader, String name, String capeRoot, int priority) {
        return new DefaultProfile(loader) {
            @Override
            public String getSkinRoot() {
                return null;
            }

            @Override
            public String getCapeRoot() {
                return capeRoot;
            }

            @Override
            public String getElytraRoot() {
                return null;
            }

            @Override
            public String getName() {
                return name;
            }

            @Override
            public int getPriority() {
                return priority;
            }
        };
    }

    public static final String USERNAME_PLACEHOLDER = "{USERNAME}";
    public static final String UUID_PLACEHOLDER = "{UUID}";
    /**
     * Mojang UUID with -
     *
     * @since 14.16
     */
    public static final String STANDARD_UUID_PLACEHOLDER = "{STANDARD_UUID}";

    @Override
    public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile) {
        UserProfile profile = new UserProfile();
        //Try to load all textures
        getTextureUrl(ssp, gameProfile, ssp.skin, it -> {
            profile.skinUrl = it;
            profile.model = ssp.model;
        });
        getTextureUrl(ssp, gameProfile, ssp.cape, it -> profile.capeUrl = it);
        getTextureUrl(ssp, gameProfile, ssp.elytra, it -> profile.elytraUrl = it);

        if (profile.isEmpty()) {
            CustomSkinLoader.logger.info("No texture could be found.");
            return null;
        }
        return profile;
    }

    private void getTextureUrl(
            SkinSiteProfile ssp, GameProfile gameProfile, String baseUrl, Consumer<String> onSuccess) {
        //Base URL is empty
        if (baseUrl == null || baseUrl.isEmpty()) {
            return;
        }
        String url = expandURL(baseUrl, gameProfile.getName());
        //No texture can be loaded
        if (url == null) {
            return;
        }
        //Local texture file logic
        if (HttpUtil0.isLocal(url)) {
            File file = new File(CustomSkinLoader.DATA_DIR, url);
            if (file.exists() && file.isFile()) {
                String fakeUrl = HttpTextureUtil.getLocalLegacyFakeUrl(url,
                        HttpTextureUtil.getHash(url, file.length(), file.lastModified()));
                onSuccess.accept(fakeUrl);
            }
            return;
        }
        //Remote texture logic
        HttpRequestUtil.HttpResponce responce = HttpRequestUtil.makeHttpRequest(new HttpRequestUtil.HttpRequest(url)
                .setUserAgent(ssp.userAgent).setCheckPNG(ssp.checkPNG != null && ssp.checkPNG).setLoadContent(false));
        if (responce.success) {
            onSuccess.accept(HttpTextureUtil.getLegacyFakeUrl(url));
        }
    }

    @Override
    public boolean compare(SkinSiteProfile ssp0, SkinSiteProfile ssp1) {
        return Objects.equals(ssp0.skin, ssp1.skin) && Objects.equals(ssp0.cape, ssp1.cape) &&
                Objects.equals(ssp0.elytra, ssp1.elytra);
    }

    @Override
    public String getName() {
        return "Legacy";
    }

    @Override
    public void init(SkinSiteProfile ssp) {
        initFolder(ssp.skin);
        initFolder(ssp.cape);
        initFolder(ssp.elytra);
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void initFolder(String target) {
        //Only local skin should init folder
        if (!HttpUtil0.isLocal(target)) {
            return;
        }
        String file = target.replace(USERNAME_PLACEHOLDER, "init");
        File folder = new File(CustomSkinLoader.DATA_DIR, file).getParentFile();
        if (folder != null && !folder.exists()) {
            folder.mkdirs();
        }
    }

    private String expandURL(String url, String username) {
        String t = url.replace(USERNAME_PLACEHOLDER, username);
        if (!t.contains(UUID_PLACEHOLDER) && !t.contains(STANDARD_UUID_PLACEHOLDER)) {
            return t;
        }
        String uuid = MojangAPILoader.getMojangUuidByUsername(username, true);
        //If Mojang uuid not found, won't load the texture
        if (uuid == null) {
            return null;
        }
        String styledUuid = uuid.replace("-", "");
        return t.replace(UUID_PLACEHOLDER, styledUuid).replace(STANDARD_UUID_PLACEHOLDER, uuid);
    }
}
