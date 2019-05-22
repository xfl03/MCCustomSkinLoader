package customskinloader.fabric.fake;

import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.CustomSkinLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.Texture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.util.ChatUtil;
import net.minecraft.util.Identifier;

public class FakeClientPlayer {
    public static Map<Identifier, Texture> textureCache = Maps.newHashMap();
    
    //For Legacy Skin
    public static PlayerSkinTexture getDownloadImageSkin(Identifier resourceLocationIn, String username) {
        //CustomSkinLoader.logger.debug("FakeClientPlayer/getDownloadImageSkin "+username);
        TextureManager textman = MinecraftClient.getInstance().getTextureManager();
        Texture ito = textman.getTexture(resourceLocationIn);

        if (ito == null || !(ito instanceof PlayerSkinTexture)) {
            //if Legacy Skin for username not loaded yet
            PlayerSkinProvider skinman = MinecraftClient.getInstance().getSkinProvider();
            UUID offlineUUID = FakeClientPlayer.getOfflineUUID(username);
            GameProfile offlineProfile = new GameProfile(offlineUUID, username);

            //Load Default Skin
            Identifier defaultSkin = DefaultSkinHelper.getTexture(offlineUUID);
            Texture defaultSkinObj = new ResourceTexture(defaultSkin);
            textman.registerTexture(resourceLocationIn, defaultSkinObj);

            //Load Skin from SkinManager
            skinman.loadSkin(offlineProfile, new LegacyBuffer(resourceLocationIn), false);
        }

        if (ito instanceof PlayerSkinTexture) {
            return (PlayerSkinTexture) ito;
        }
        return null;
    }

    public static Identifier getLocationSkin(String username) {
        //CustomSkinLoader.logger.debug("FakeClientPlayer/getLocationSkin "+username);
        return new Identifier("skins/legacy-" + ChatUtil.stripTextFormat(username));
    }
    
    public static UUID getOfflineUUID(String username) {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes());
    }
    
    public static class LegacyBuffer implements PlayerSkinProvider.SkinTextureAvailableCallback {
        Identifier resourceLocationIn;
        boolean loaded = false;

        public LegacyBuffer(Identifier resourceLocationIn) {
            CustomSkinLoader.logger.debug("Loading Legacy Texture (" + resourceLocationIn + ")");
            this.resourceLocationIn = resourceLocationIn;
        }

        @Override
        public void onSkinTextureAvailable(MinecraftProfileTexture.Type typeIn, Identifier location, MinecraftProfileTexture profileTexture) {
            if (typeIn != MinecraftProfileTexture.Type.SKIN || this.loaded) {
                return;
            }
            TextureManager textman = MinecraftClient.getInstance().getTextureManager();
            Texture ito = textman.getTexture(location);
            if (ito == null) {
                ito = FakeClientPlayer.textureCache.get(location);
            }
            if (ito == null) {
                return;
            }
            this.loaded = true;
            textman.registerTexture(this.resourceLocationIn, ito);
            CustomSkinLoader.logger.debug("Legacy Texture (" + this.resourceLocationIn + ") Loaded as " + ito.toString() + " (" + location + ")");
        }
    }
}
