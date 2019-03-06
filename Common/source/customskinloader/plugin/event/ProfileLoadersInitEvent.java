package customskinloader.plugin.event;

import java.util.HashMap;

import customskinloader.loader.ProfileLoader;

public class ProfileLoadersInitEvent {
	private HashMap<String, ProfileLoader.IProfileLoader> profileLoaders;
	
	public ProfileLoadersInitEvent(HashMap<String, ProfileLoader.IProfileLoader> profileLoaders) {
		this.profileLoaders = profileLoaders;
	}
	
	public HashMap<String, ProfileLoader.IProfileLoader> getProfileLoaders() {
		return this.profileLoaders;
	}
}
