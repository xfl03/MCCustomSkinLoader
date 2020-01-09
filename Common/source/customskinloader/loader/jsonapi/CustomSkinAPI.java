package customskinloader.loader.jsonapi;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader.IJsonAPI;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.ModelManager0.Model;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpTextureUtil;

public class CustomSkinAPI implements IJsonAPI {
    private static final String TEXTURES="textures/";
    private static final String SUFFIX=".json";

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
            Model enumModel=ModelManager0.getEnumModel(model);
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
    private class CustomSkinAPIProfile{
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

    @Override
    public boolean checkRoot() {
        return true;
    }
}
