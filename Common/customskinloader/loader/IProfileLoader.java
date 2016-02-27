package customskinloader.loader;

import customskinloader.UserProfile;
import customskinloader.config.SkinSiteProfile;

public interface IProfileLoader {
	public UserProfile loadProfile(SkinSiteProfile ssp,String username) throws Exception;
}
