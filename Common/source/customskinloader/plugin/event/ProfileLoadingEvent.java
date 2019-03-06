package customskinloader.plugin.event;

import java.util.HashMap;

import com.mojang.authlib.GameProfile;

import customskinloader.loader.ProfileLoader;
import customskinloader.profile.UserProfile;

public class ProfileLoadingEvent {
    private GameProfile gameProfile;
    
    public ProfileLoadingEvent(GameProfile gameProfile) {
        this.gameProfile = gameProfile;
    }
    
    public GameProfile getGameProfile() {
        return this.gameProfile;
    }
    
    public static class Pre extends ProfileLoadingEvent {
        public Pre(GameProfile gameProfile) {
            super(gameProfile);
        }
    }
    
    public static class Post extends ProfileLoadingEvent {
        private UserProfile userProfile;
        
        public Post(GameProfile gameProfile, UserProfile userProfile) {
            super(gameProfile);
            this.userProfile = userProfile;
        }
        
        public UserProfile getUserProfile() {
            return this.userProfile;
        }
    }
}
