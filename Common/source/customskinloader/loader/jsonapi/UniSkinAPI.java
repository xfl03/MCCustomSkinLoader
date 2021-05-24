package customskinloader.loader.jsonapi;

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

public class UniSkinAPI implements JsonAPILoader.IJsonAPI {

    public static class SkinMe extends JsonAPILoader.DefaultProfile {
        public SkinMe(JsonAPILoader loader) { super(loader); }
        @Override public String getName()   { return "SkinMe"; }
        @Override public int getPriority()  { return 500; }
        @Override public String getRoot()   { return "http://www.skinme.cc/uniskin/"; }
    }

    private static final String TEXTURES="textures/";
    private static final String SUFFIX=".json";

    @Override
    public List<ICustomSkinLoaderPlugin.IDefaultProfile> getDefaultProfiles(JsonAPILoader loader) {
        return Lists.newArrayList(new SkinMe(loader));
    }

    @Override
    public String toJsonUrl(String root, String username) {
        return root + username + SUFFIX;
    }

    @Override
    public UserProfile toUserProfile(String root, String json, boolean local) {
        UniSkinAPIProfile profile=CustomSkinLoader.GSON.fromJson(json, UniSkinAPIProfile.class);
        UserProfile p=new UserProfile();
        
        if(StringUtils.isNotBlank(profile.cape)){
            p.capeUrl=root+TEXTURES+profile.cape;
            if(local)
                p.capeUrl=HttpTextureUtil.getLocalFakeUrl(p.capeUrl);
        }
        
        if(profile.skins==null||profile.skins.isEmpty())
            return p;
        if(profile.model_preference==null||profile.model_preference.isEmpty())
            return p;
        
        boolean hasSkin=false;
        for(String model:profile.model_preference){
            ModelManager0.Model enumModel=ModelManager0.getEnumModel(model);
            if(enumModel==null||StringUtils.isEmpty(profile.skins.get(model)))
                continue;
            if(ModelManager0.isSkin(enumModel))
                if(hasSkin)
                    continue;
                else
                    hasSkin=true;
            String url=root+TEXTURES+profile.skins.get(model);
            if(local)
                url=HttpTextureUtil.getLocalFakeUrl(url);
            p.put(enumModel, url);
        }
        
        return p;
    }
    /**
     * Json profile for UniSkinAPI
     * Source Code: https://github.com/RecursiveG/UniSkinMod/blob/master/src/main/java/org/devinprogress/uniskinmod/UniSkinApiProfile.java#L18-L22
     * @author RecursiveG
     */
    private class UniSkinAPIProfile{
        public String player_name;
        public long last_update;
        public List<String> model_preference;
        public Map<String,String> skins;
        
        public String cape;
    }
    @Override
    public String getPayload(SkinSiteProfile ssp) {
        return null;
    }
    @Override
    public String getName() {
        return "UniSkinAPI";
    }
}
