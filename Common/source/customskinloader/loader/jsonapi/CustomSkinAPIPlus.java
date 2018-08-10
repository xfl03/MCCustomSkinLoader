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
                clientID=FileUtils.readFileToString(clientIDFile, "UTF-8");
            }catch(Exception e){
                e.printStackTrace();
            }
        if(clientID==null){
            clientID=UUID.randomUUID().toString();
            try {
                FileUtils.write(clientIDFile, clientID, "UTF-8");
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
        return new Gson().toJson(new CustomSkinAPIPlusPayload());
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
        String gameVersion;//minecraft version
        String modVersion;//mod version
        String serverAddress;//ip:port
        String clientID;//Minecraft Client ID
        CustomSkinAPIPlusPayload(){
                gameVersion = MinecraftUtil.getMinecraftMainVersion();
                modVersion = CustomSkinLoader.CustomSkinLoader_VERSION;
                serverAddress = MinecraftUtil.isLanServer() ? null : MinecraftUtil.getStandardServerAddress();
                clientID = CustomSkinAPIPlus.clientID;
        }
    }
}
