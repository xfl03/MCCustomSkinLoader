package customskinloader.loader;

import customskinloader.CustomSkinLoader;
import customskinloader.UserProfile;
import customskinloader.config.SkinSiteProfile;
import customskinloader.utils.HttpUtil0;

public class LegacyLoader implements IProfileLoader {
	public static final String USERNAME_REGEX="\\{USERNAME\\}";
	@Override
	public UserProfile loadProfile(SkinSiteProfile ssp, String username) throws Exception {
		UserProfile profile=new UserProfile();
		if(ssp.skin!=null && !ssp.skin.equals("")){
			String skin=HttpUtil0.getRealUrl(ssp.skin.replaceAll(USERNAME_REGEX, username));
			if(skin!=null&&!skin.equals("")){
				profile.skinUrl=skin;
				profile.model=(ssp.model==null?"default":ssp.model);
			}
		}
		if(ssp.cape!=null && !ssp.cape.equals("")){
			String cape=HttpUtil0.getRealUrl(ssp.cape.replaceAll(USERNAME_REGEX, username));
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

}
