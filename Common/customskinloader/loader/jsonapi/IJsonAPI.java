package customskinloader.loader.jsonapi;

import customskinloader.profile.UserProfile;

public interface IJsonAPI {
	public String toJsonUrl(String root,String username);
	public UserProfile toUserProfile(String root,String json,boolean local);
}
