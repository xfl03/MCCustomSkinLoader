package customskinloader.loader;

import java.io.File;
import java.io.FileInputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import com.mojang.authlib.GameProfile;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.jsonapi.*;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.HttpUtil0;
import customskinloader.utils.HttpRequestUtil.HttpRequest;
import customskinloader.utils.HttpRequestUtil.HttpResponce;

public class JsonAPILoader implements ProfileLoader.IProfileLoader {
    
    public interface IJsonAPI {
        public String toJsonUrl(String root,String username);
        public String getPayload(SkinSiteProfile ssp);
        public UserProfile toUserProfile(String root,String json,boolean local);
        public String getName();
    }
    public static class ErrorProfile{
        public int errno;
        public String msg;
    }
    public static enum Type{
        CustomSkinAPI(new CustomSkinAPI()),CustomSkinAPIPlus(new CustomSkinAPIPlus()),UniSkinAPI(new UniSkinAPI());
        public IJsonAPI jsonAPI;
        private Type(IJsonAPI jsonAPI){
            this.jsonAPI=jsonAPI;
        }
    }
    
    private Type type;
    public JsonAPILoader(Type type){
        this.type=type;
    }
    
    @Override
    public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile) throws Exception {
        String username=gameProfile.getName();
        boolean local=HttpUtil0.isLocal(ssp.root);
        if(ssp.root==null||ssp.root.equals("")){
            CustomSkinLoader.logger.info("Root not defined.");
            return null;
        }
        String jsonUrl=type.jsonAPI.toJsonUrl(ssp.root, username);
        String json;
        if(local){
            File jsonFile=new File(jsonUrl);
            if(!jsonFile.exists()){
                CustomSkinLoader.logger.info("Profile File not found.");
                return null;
            }
            json=IOUtils.toString(new FileInputStream(jsonFile));
        }else{
            HttpResponce responce=HttpRequestUtil.makeHttpRequest(new HttpRequest(jsonUrl).setCacheTime(60).setUserAgent(ssp.userAgent).setPayload(type.jsonAPI.getPayload(ssp)));
            json=responce.content;
        }
        if(json==null||json.equals("")){
            CustomSkinLoader.logger.info("Profile not found.");
            return null;
        }
        
        ErrorProfile profile=CustomSkinLoader.GSON.fromJson(json, ErrorProfile.class);
        if(profile.errno!=0){
            CustomSkinLoader.logger.info("Error "+profile.errno+": "+profile.msg);
            return null;
        }
        
        UserProfile p=type.jsonAPI.toUserProfile(ssp.root, json, local);
        if(p==null||p.isEmpty()){
            CustomSkinLoader.logger.info("Both skin and cape not found.");
            return null;
        }else{
            return p;
        }
    }
    @Override
    public boolean compare(SkinSiteProfile ssp0, SkinSiteProfile ssp1) {
        return StringUtils.isNoneEmpty(ssp0.root)?ssp0.root.equalsIgnoreCase(ssp1.root):true;
    }
    @Override
    public String getName() {
        return type.jsonAPI.getName();
    }
    @Override
    public void initLocalFolder(SkinSiteProfile ssp) {
        if(HttpUtil0.isLocal(ssp.root)){
            File f=new File(ssp.root);
            if(!f.exists())
                f.mkdirs();
        }
    }
}
