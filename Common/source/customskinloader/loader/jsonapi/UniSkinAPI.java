package customskinloader.loader.jsonapi;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader.IJsonAPI;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.profile.ModelManager0.Model;
import customskinloader.utils.HttpTextureUtil;

public class UniSkinAPI implements IJsonAPI {
    private static final String TEXTURES="textures/";
    private static final String SUFFIX=".json";

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
            Model enumModel=ModelManager0.getEnumModel(model);
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
    @Override
    public boolean checkRoot() {
        return true;
    }
}
