package customskinloader.loader;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.response.MinecraftProfilePropertiesResponse;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.profile.ModelManager0;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.HttpRequestUtil.HttpRequest;
import customskinloader.utils.HttpRequestUtil.HttpResponce;
import customskinloader.utils.MinecraftUtil;

public class MojangAPILoader implements ProfileLoader.IProfileLoader {

    public static final MinecraftSessionService defaultSessionService=MinecraftUtil.getSessionService();
    @Override
    public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile) throws Exception {
        Map<MinecraftProfileTexture.Type,MinecraftProfileTexture> map=getTextures(gameProfile);
        if(!map.isEmpty()){
            CustomSkinLoader.logger.info("Default profile will be used.");
            return ModelManager0.toUserProfile(map);
        }
        String username=gameProfile.getName();
        GameProfile newGameProfile=loadGameProfile(username);
        if(newGameProfile==null){
            CustomSkinLoader.logger.info("Profile not found.("+username+"'s profile not found.)");
            return null;
        }
        newGameProfile=fillGameProfile(newGameProfile);
        map=getTextures(newGameProfile);
        if(!map.isEmpty()){
            gameProfile.getProperties().putAll(newGameProfile.getProperties());
            return ModelManager0.toUserProfile(map);
        }
        CustomSkinLoader.logger.info("Profile not found.("+username+" doesn't have skin/cape.)");
        return null;
    }
    
    //Username -> UUID
    public static GameProfile loadGameProfile(String username) throws Exception{
        //Doc (http://wiki.vg/Mojang_API#Username_-.3E_UUID_at_time)
        HttpResponce responce=HttpRequestUtil.makeHttpRequest(new HttpRequest("https://api.mojang.com/users/profiles/minecraft/"+username).setCacheTime(0));
        if(StringUtils.isEmpty(responce.content))
            return null;
        
        Gson gson=new GsonBuilder().registerTypeAdapter(UUID.class, new UUIDTypeAdapter()).create();
        GameProfile gameProfile=gson.fromJson(responce.content, GameProfile.class);
        
        if(gameProfile.getId()==null)
            return null;
        return new GameProfile(gameProfile.getId(),gameProfile.getName());
    }
    //UUID -> Profile
    public static GameProfile fillGameProfile(GameProfile profile) throws Exception{
        //Doc (http://wiki.vg/Mojang_API#UUID_-.3E_Profile_.2B_Skin.2FCape)
        HttpResponce responce=HttpRequestUtil.makeHttpRequest(new HttpRequest("https://sessionserver.mojang.com/session/minecraft/profile/"+UUIDTypeAdapter.fromUUID(profile.getId())).setCacheTime(90));
        if(StringUtils.isEmpty(responce.content))
            return profile;
        
        Gson gson=new GsonBuilder()
                .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
                .registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer())
                .create();
        MinecraftProfilePropertiesResponse propertiesResponce=gson.fromJson(responce.content, MinecraftProfilePropertiesResponse.class);
        GameProfile newGameProfile=new GameProfile(propertiesResponce.getId(),propertiesResponce.getName());
        newGameProfile.getProperties().putAll(propertiesResponce.getProperties());
        
        return newGameProfile;
    }
    
    public static Map<MinecraftProfileTexture.Type,MinecraftProfileTexture> getTextures(GameProfile gameProfile) throws Exception{
        if(gameProfile==null)
            return new HashMap();
        Property textureProperty=(Property)Iterables.getFirst(gameProfile.getProperties().get("textures"), null);
        if (textureProperty==null)
            return new HashMap();
        String value=textureProperty.getValue();
        if(StringUtils.isBlank(value))
            return new HashMap();
        String json=new String(Base64.decodeBase64(value),Charsets.UTF_8);
        Gson gson=new GsonBuilder().registerTypeAdapter(UUID.class,new UUIDTypeAdapter()).create();
        MinecraftTexturesPayload result=gson.fromJson(json,MinecraftTexturesPayload.class);
        if (result==null||result.getTextures()==null)
            return new HashMap();
        return result.getTextures();
    }
    
    @Override
    public boolean compare(SkinSiteProfile ssp0, SkinSiteProfile ssp1) {
        return true;
    }
    @Override
    public String getName() {
        return "MojangAPI";
    }

    @Override
    public void initLocalFolder(SkinSiteProfile ssp) {
    }
}
