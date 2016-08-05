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
	public UserProfile loadProfile(SkinSiteProfile ssp, GameProfile gameProfile, boolean local) throws Exception {
		String username=gameProfile.getName();
		UserProfile profile=new UserProfile();
		if(ssp.skin!=null && !ssp.skin.equals("")){
			String skin=ssp.skin.replaceAll(USERNAME_REGEX, username);
			if(local){
				File skinFile=new File(CustomSkinLoader.DATA_DIR,skin);
				if(skinFile.exists()&&skinFile.isFile())
					profile.skinUrl=HttpTextureUtil.getLocalLegacyFakeUrl(skin, HttpTextureUtil.getHash(skin, skinFile.length(), skinFile.lastModified()));
			}else{
				String fakeSkinUrl=HttpUtil0.getFakeUrl(skin,ssp.userAgent);
				if(fakeSkinUrl!=null&&!fakeSkinUrl.equals(""))
					profile.skinUrl=fakeSkinUrl;
			}
			profile.model=profile.hasSkinUrl()?ssp.model:null;
		}
		if(ssp.cape!=null && !ssp.cape.equals("")){
			String cape=ssp.cape.replaceAll(USERNAME_REGEX, username);
			if(local){
				File capeFile=new File(CustomSkinLoader.DATA_DIR,cape);
				if(capeFile.exists()&&capeFile.isFile())
					profile.capeUrl=HttpTextureUtil.getLocalLegacyFakeUrl(cape, HttpTextureUtil.getHash(cape, capeFile.length(), capeFile.lastModified()));
			}else{
				String fakeCapeUrl=HttpUtil0.getFakeUrl(cape,ssp.userAgent);
				if(fakeCapeUrl!=null&&fakeCapeUrl.equals(""))
					profile.capeUrl=fakeCapeUrl;
			}
		}
		if(profile.isEmpty()){
			CustomSkinLoader.logger.info("Both skin and cape not found.");
			return null;
		}
		return profile;
	}
}
