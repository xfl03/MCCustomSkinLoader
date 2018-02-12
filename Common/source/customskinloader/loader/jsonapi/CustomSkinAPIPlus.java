package customskinloader.loader.jsonapi;

import java.io.File;
import java.util.UUID;

import org.apache.commons.io.FileUtils;

import com.google.gson.Gson;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.loader.JsonAPILoader;
import customskinloader.loader.JsonAPILoader.IJsonAPI;
import customskinloader.profile.UserProfile;
import customskinloader.utils.MinecraftUtil;

public class CustomSkinAPIPlus implements IJsonAPI {

    private static String clientID=null;
    public CustomSkinAPIPlus(){
        File clientIDFile=new File(CustomSkinLoader.DATA_DIR,"CustomSkinAPIPlus-ClientID");
        
        if(clientIDFile.isFile())
            try{
                clientID=FileUtils.readFileToString(clientIDFile);
            }catch(Exception e){
                e.printStackTrace();
            }
        if(clientID==null){
            clientID=UUID.randomUUID().toString();
            try {
                FileUtils.write(clientIDFile, clientID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    @Override
    public String toJsonUrl(String root, String username) {
        return JsonAPILoader.Type.CustomSkinAPI.jsonAPI.toJsonUrl(root, username);
    }

    @Override
    public String getPayload(SkinSiteProfile ssp) {
        if(ssp.privacy==null)
            ssp.privacy=new CustomSkinAPIPlusPrivacy();
        return new Gson().toJson(new CustomSkinAPIPlusPayload(ssp.privacy));
    }

    @Override
    public UserProfile toUserProfile(String root, String json, boolean local) {
        return JsonAPILoader.Type.CustomSkinAPI.jsonAPI.toUserProfile(root, json, local);
    }

    @Override
    public String getName() {
        return "CustomSKinAPIPlus";
    }

    public static class CustomSkinAPIPlusPayload{
        public String gameVersion;//minecraft version
        public String modVersion;//mod version
        public String serverAddress;//ip:port
        public String clientID;//Minecraft Client ID
        public CustomSkinAPIPlusPayload(CustomSkinAPIPlusPrivacy privacy){
            if(privacy.gameVersion)
                gameVersion=MinecraftUtil.getMinecraftMainVersion();
            if(privacy.modVersion)
                modVersion=CustomSkinLoader.CustomSkinLoader_VERSION;
            if(privacy.serverAddress)
                serverAddress=MinecraftUtil.isLanServer()?null:MinecraftUtil.getStandardServerAddress();
            if(privacy.clientID)
                clientID=CustomSkinAPIPlus.clientID;
        }
    }
    public static class CustomSkinAPIPlusPrivacy{
        public boolean gameVersion=true;
        public boolean modVersion=true;
        public boolean serverAddress=true;
        public boolean clientID=true;
    }
}
