package customskinloader.profile;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.Lists;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.properties.Property;
import com.mojang.util.UUIDTypeAdapter;

import customskinloader.CustomSkinLoader;
import customskinloader.utils.HttpRequestUtil;
import customskinloader.utils.HttpRequestUtil.HttpRequest;
import customskinloader.utils.HttpRequestUtil.HttpResponce;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpUtil0;

public class DynamicSkullManager {
    public static class SkullTexture{
        public Map<Type,MinecraftProfileTexture> textures;
        public String index;//Index File
        public ArrayList<String> skins;
        public int interval;
        public boolean fromZero;
        //For program
        public long startTime;
        public int period;
    }
    private Map<GameProfile, SkullTexture> dynamicTextures = new ConcurrentHashMap<>();
    private Map<GameProfile, Map<Type, MinecraftProfileTexture>> staticTextures = new ConcurrentHashMap<>();
    private List<GameProfile> loadingList=new ArrayList<GameProfile>();
    
    private void parseGameProfile(GameProfile profile){
        Property textureProperty = Iterables.getFirst(profile.getProperties().get("textures"), null);
        if (textureProperty==null){
            staticTextures.put(profile, Maps.<Type, MinecraftProfileTexture>newHashMap());
            return;
        }
        String value=textureProperty.getValue();
        if(StringUtils.isBlank(value)){
            staticTextures.put(profile, Maps.<Type, MinecraftProfileTexture>newHashMap());
            return;
        }
        @SuppressWarnings("deprecation") String json = new String(Base64.decodeBase64(value), Charsets.UTF_8);
        Gson gson=new GsonBuilder().registerTypeAdapter(UUID.class,new UUIDTypeAdapter()).create();
        SkullTexture result=gson.fromJson(json,SkullTexture.class);
        if(result==null){
            staticTextures.put(profile, Maps.<Type, MinecraftProfileTexture>newHashMap());
            return;
        }
        staticTextures.put(profile, (result.textures==null || !result.textures.containsKey(Type.SKIN)) ?
                Maps.<Type, MinecraftProfileTexture>newHashMap() : parseTextures(result.textures));
        
        if(StringUtils.isNotEmpty(result.index)){
            File indexFile=new File(CustomSkinLoader.DATA_DIR,result.index);
            try{
                String index=FileUtils.readFileToString(indexFile, "UTF-8");
                if(StringUtils.isNotEmpty(index)){
                    String[] skins = CustomSkinLoader.GSON.fromJson(index, String[].class);
                    if(skins != null && skins.length != 0)
                        result.skins = Lists.newArrayList(skins);
                }
            }catch(Exception e){
                CustomSkinLoader.logger.warning("Exception occurs while parsing index file: "+e.toString());
            }
        }
        
        if(!CustomSkinLoader.config.enableDynamicSkull||result.skins==null||result.skins.isEmpty())
            return;
        
        CustomSkinLoader.logger.info("Try to load Dynamic Skull: "+json);
        
        for(int i=0;i<result.skins.size();i++){//check and cache skins
            String skin=result.skins.get(i);
            if(HttpUtil0.isLocal(skin)){
                //Local Skin
                File skinFile=new File(CustomSkinLoader.DATA_DIR,skin);
                if(skinFile.isFile()&&skinFile.length()>0){
                    //Skin found
                    String fakeUrl=HttpTextureUtil.getLocalFakeUrl(skin);
                    result.skins.set(i, fakeUrl);
                }else{
                    //Could not find skin
                    result.skins.remove(i--);
                }
            }else{
                //Online Skin
                HttpResponce responce=HttpRequestUtil.makeHttpRequest(new HttpRequest(skin).setCacheFile(HttpTextureUtil.getCacheFile(FilenameUtils.getBaseName(skin))).setCacheTime(0).setLoadContent(false));
                if(!responce.success){
                    //Could not load skin
                    result.skins.remove(i--);
                }
            }
        }
        
        if(result.skins.isEmpty()){//Nothing loaded
            CustomSkinLoader.logger.info("Failed: Nothing loaded.");
            return;
        }
        result.interval=Math.max(result.interval, 50);
        if(result.fromZero)
            result.startTime=System.currentTimeMillis();
        result.period=result.interval*result.skins.size();
        CustomSkinLoader.logger.info("Successfully loaded Dynamic Skull: "+new Gson().toJson(result));
        dynamicTextures.put(profile, result);
        staticTextures.remove(profile);
    }
    
    //Support local skin for skull
    public Map<Type, MinecraftProfileTexture> parseTextures(Map<Type, MinecraftProfileTexture> textures) {
        MinecraftProfileTexture skin=textures.get(Type.SKIN);
        String skinUrl=skin.getUrl();
        if(!HttpUtil0.isLocal(skinUrl))
            return textures;
        File skinFile=new File(CustomSkinLoader.DATA_DIR,skinUrl);
        if(!skinFile.isFile())
            return Maps.newHashMap();
        textures.put(Type.SKIN, ModelManager0.getProfileTexture(HttpTextureUtil.getLocalFakeUrl(skinUrl), null));
        return textures;
    }

    public Map<Type,MinecraftProfileTexture> getTexture(final GameProfile profile){
        if(staticTextures.get(profile)!=null)
            return staticTextures.get(profile);
        if(loadingList.contains(profile))
            return Maps.newHashMap();
        if(dynamicTextures.containsKey(profile)){
            SkullTexture texture=dynamicTextures.get(profile);
            long time=System.currentTimeMillis()-texture.startTime;
            int index=(int)Math.floor((time%texture.period)/texture.interval);
            Map<Type,MinecraftProfileTexture> map=Maps.newHashMap();
            map.put(Type.SKIN, ModelManager0.getProfileTexture(texture.skins.get(index), null));
            return map;
        }
        
        loadingList.add(profile);
        Thread loadThread=new Thread(){
            public void run(){
                parseGameProfile(profile);//Load in thread
                loadingList.remove(profile);
            }
        };
        loadThread.setName("Skull "+profile.hashCode());
        loadThread.start();
        return Maps.newHashMap();
    }
}
