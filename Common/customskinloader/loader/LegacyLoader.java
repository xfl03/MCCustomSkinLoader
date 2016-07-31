package customskinloader.loader;

import java.io.File;

import com.mojang.authlib.GameProfile;

import customskinloader.CustomSkinLoader;
import customskinloader.config.SkinSiteProfile;
import customskinloader.profile.UserProfile;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpUtil0;

public class LegacyLoader implements IProfileLoader {
	public static final String USERNAME_REGEX="\\{USERNAME\\}";
	@Override
	public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile) throws Exception {
		String username=gameProfile.getName();
		UserProfile profile=new UserProfile();
		if(ssp.skin!=null && !ssp.skin.equals("")){
			String skin=HttpUtil0.getFakeUrl(ssp.skin.replaceAll(USERNAME_REGEX, username),ssp.userAgent);
			if(skin!=null&&!skin.equals("")){
				profile.skinUrl=skin;
				profile.model=(ssp.model==null?"default":ssp.model);
			}
		}
		if(ssp.cape!=null && !ssp.cape.equals("")){
			String cape=HttpUtil0.getFakeUrl(ssp.cape.replaceAll(USERNAME_REGEX, username),ssp.userAgent);
			if(cape!=null&&!cape.equals("")){
				profile.capeUrl=cape;
			}
		}
		if(profile.isEmpty()){
			CustomSkinLoader.logger.info("Both skin and cape not found.");
			return null;
		}else{
			return profile;
		}
	}
	@Override
	public UserProfile loadLocalProfile(SkinSiteProfile ssp, GameProfile gameProfile) throws Exception {
		String username=gameProfile.getName();
		UserProfile profile=new UserProfile();
		if(ssp.skin!=null && !ssp.skin.equals("")){
			String skin=ssp.skin.replaceAll(USERNAME_REGEX, username);
			if(skin!=null&&!skin.equals("")){
				File skinFile=new File(CustomSkinLoader.DATA_DIR,skin);
				if(skinFile.exists()&&skinFile.isFile()){
					profile.skinUrl=HttpTextureUtil.getLocalLegacyFakeUrl(skin, HttpTextureUtil.getHash(skin, skinFile.length(), skinFile.lastModified()));
					profile.model=(ssp.model==null?"default":ssp.model);
				}
			}
		}
		if(ssp.cape!=null && !ssp.cape.equals("")){
			String cape=ssp.cape.replaceAll(USERNAME_REGEX, username);
			if(cape!=null&&!cape.equals("")){
				File capeFile=new File(CustomSkinLoader.DATA_DIR,cape);
				if(capeFile.exists()&&capeFile.isFile()){
					profile.capeUrl=HttpTextureUtil.getLocalLegacyFakeUrl(cape, HttpTextureUtil.getHash(cape, capeFile.length(), capeFile.lastModified()));
				}
			}
		}
		if(profile.isEmpty()){
			CustomSkinLoader.logger.info("Both skin and cape not found.");
			return null;
		}else{
			return profile;
		}
	}

}
