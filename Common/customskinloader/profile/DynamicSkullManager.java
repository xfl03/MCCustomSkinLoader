package customskinloader.profile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
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
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpUtil0;

public class DynamicSkullManager {
	public static class SkullTexture{
		public Map<Type,MinecraftProfileTexture> textures;
		public ArrayList<String> skins;
		public int interval;
		public boolean fromZero;
		//For program
		public long startTime;
		public int period;
	}
	private Map<GameProfile,SkullTexture> dynamicTextures=new HashMap<GameProfile,SkullTexture>();
	private Map<GameProfile,Map<Type,MinecraftProfileTexture>> staticTextures=new HashMap<GameProfile,Map<Type,MinecraftProfileTexture>>();
	private List<GameProfile> loadingList=new ArrayList<GameProfile>();
	
	private void parseGameProfile(GameProfile profile){
		Property textureProperty=(Property)Iterables.getFirst(profile.getProperties().get("textures"), null);
		if (textureProperty==null){
			staticTextures.put(profile, new HashMap());
			return;
		}
		String value=textureProperty.getValue();
		if(StringUtils.isBlank(value)){
			staticTextures.put(profile, new HashMap());
			return;
		}
		String json=new String(Base64.decodeBase64(value),Charsets.UTF_8);
		Gson gson=new GsonBuilder().registerTypeAdapter(UUID.class,new UUIDTypeAdapter()).create();
		SkullTexture result=gson.fromJson(json,SkullTexture.class);
		if(result==null){
			staticTextures.put(profile, new HashMap());
			return;
		}
		staticTextures.put(profile, result.textures==null?new HashMap():result.textures);
		if(!CustomSkinLoader.config.enableDynamicSkull||result.skins==null||result.skins.isEmpty())
			return;
		
		CustomSkinLoader.logger.info("Dynamic Skull: "+json);
		
		for(int i=0;i<result.skins.size();i++){//check and cache skins
			String skin=result.skins.get(i);
			if(HttpUtil0.isLocal(skin)){
				//Local Skin
				File skinFile=new File(CustomSkinLoader.DATA_DIR,skin);
				if(skinFile.isFile()&&skinFile.length()>0){
					//Skin found
					String fakeUrl=HttpTextureUtil.getLocalLegacyFakeUrl(skin, HttpTextureUtil.getHash(skin, skinFile.length(), skinFile.lastModified()));
					result.skins.set(i, fakeUrl);
				}else{
					//Could not find skin
					result.skins.remove(i--);
					continue;
				}
			}else{
				//Online Skin
				if(!HttpUtil0.saveHttp(skin, HttpTextureUtil.getCacheFile(FilenameUtils.getBaseName(skin)))){
					//Could not load skin
					result.skins.remove(i--);
					continue;
				}
			}
		}
		
		if(result.skins.isEmpty())//Nothing loaded
			return;
		result.interval=Math.max(result.interval, 50);
		if(result.fromZero)
			result.startTime=System.currentTimeMillis();
		result.period=result.interval*result.skins.size();
		dynamicTextures.put(profile, result);
		staticTextures.remove(profile);
	}
	
	public Map<Type,MinecraftProfileTexture> getTexture(final GameProfile profile){
		if(staticTextures.get(profile)!=null)
			return staticTextures.get(profile);
		if(loadingList.contains(profile))
			return new HashMap();
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
		return new HashMap();
	}
}
