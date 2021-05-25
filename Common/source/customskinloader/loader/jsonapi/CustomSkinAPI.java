package customskinloader.loader.jsonapi;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.plugin.ICustomSkinLoaderPlugin;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpTextureUtil;
import org.apache.commons.lang3.StringUtils;

public class CustomSkinAPI implements JsonAPILoader.IJsonAPI {

    public static class LittleSkin extends JsonAPILoader.DefaultProfile {
        public LittleSkin(JsonAPILoader loader) { super(loader); }
        @Override public String getName()       { return "LittleSkin"; }
        @Override public int getPriority()      { return 200; }
        @Override public String getRoot()       { return "https://littlesk.in/csl/"; }
    }

    public static class BlessingSkin extends JsonAPILoader.DefaultProfile {
        public BlessingSkin(JsonAPILoader loader) { super(loader); }
        @Override public String getName()         { return "BlessingSkin"; }
        @Override public int getPriority()        { return 300; }
        @Override public String getRoot()         { return "http://skin.prinzeugen.net/"; }
    }

    // // OneSkin has been removed temporarily
    // public static class OneSkin extends JsonAPILoader.DefaultProfile {
    //     public OneSkin(JsonAPILoader loader) { super(loader); }
    //     @Override public String getName()    { return "OneSkin"; }
    //     @Override public int getPriority()   { return 500; }
    //     @Override public String getRoot()    { return "http://fleey.cn/skin/skin_user/skin_json.php/"; }
    // }

    private static final String TEXTURES="textures/";
    private static final String SUFFIX=".json";

    @Override
    public List<ICustomSkinLoaderPlugin.IDefaultProfile> getDefaultProfiles(JsonAPILoader loader) {
        return Lists.newArrayList(new LittleSkin(loader), new BlessingSkin(loader));
    }

    @Override
    public String toJsonUrl(String root, String username) {
        return root + username + SUFFIX;
    }

    @Override
    public UserProfile toUserProfile(String root, String json, boolean local) {
        CustomSkinAPIProfile profile=CustomSkinLoader.GSON.fromJson(json, CustomSkinAPIProfile.class);
        UserProfile p=new UserProfile();
        
        if(StringUtils.isNotBlank(profile.skin)){
            p.skinUrl=root+TEXTURES+profile.skin;
            if(local)
                p.skinUrl=HttpTextureUtil.getLocalFakeUrl(p.skinUrl);
        }
        if(StringUtils.isNotBlank(profile.cape)){
            p.capeUrl=root+TEXTURES+profile.cape;
            if(local)
                p.capeUrl=HttpTextureUtil.getLocalFakeUrl(p.capeUrl);
        }
        if(StringUtils.isNotBlank(profile.elytra)){
            p.elytraUrl=root+TEXTURES+profile.elytra;
            if(local)
                p.elytraUrl=HttpTextureUtil.getLocalFakeUrl(p.elytraUrl);
        }
        
        Map<String,String> textures=new LinkedHashMap<String,String>();
        if(profile.skins!=null)
            textures.putAll(profile.skins);
        if(profile.textures!=null)
            textures.putAll(profile.textures);
        if(textures.isEmpty())
            return p;
        
        boolean hasSkin=false;
        for(String model:textures.keySet()){
            ModelManager0.Model enumModel=ModelManager0.getEnumModel(model);
            if(enumModel==null||StringUtils.isEmpty(textures.get(model)))
                continue;
            if(ModelManager0.isSkin(enumModel))
                if(hasSkin)
                    continue;
                else
                    hasSkin=true;
            String url=root+TEXTURES+textures.get(model);
            if(local)
                url=HttpTextureUtil.getLocalFakeUrl(url);
            p.put(enumModel, url);
        }
        
        return p;
    }
    private static class CustomSkinAPIProfile{
        public String username;
        public LinkedHashMap<String,String> textures;
        
        public LinkedHashMap<String,String> skins;
        
        public String skin;
        public String cape;
        public String elytra;
    }
    @Override
    public String getPayload(SkinSiteProfile ssp) {
        return null;
    }
    @Override
    public String getName() {
        return "CustomSkinAPI";
    }
}
