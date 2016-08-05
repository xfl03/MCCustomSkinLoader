package customskinloader.loader;

import com.mojang.authlib.GameProfile;

import customskinloader.config.SkinSiteProfile;
import customskinloader.profile.UserProfile;

public interface IProfileLoader {
	public UserProfile loadProfile(SkinSiteProfile ssp,GameProfile gameProfile,boolean local) throws Exception;
}
