package customskinloader.fake.itf;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;

public interface IFakeSkinManagerCacheKey {
    default GameProfile profile() { return null; }
    Property packedTextures();
}
