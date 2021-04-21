package net.minecraft.client.resources;

import java.io.File;
import java.util.Map;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

// exclude
public class SkinManager {
    public SkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) { }
    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType) { return null; }
    public ResourceLocation loadSkin(final MinecraftProfileTexture profileTexture, final MinecraftProfileTexture.Type textureType, final SkinManager.SkinAvailableCallback skinAvailableCallback) { return null; }
    public void loadProfileTextures(final GameProfile profile, final SkinManager.SkinAvailableCallback skinAvailableCallback, final boolean requireSecure) { }
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) { return null; }

    // put into mod jar to prevent https://github.com/cpw/modlauncher/issues/39
    public interface SkinAvailableCallback {
        default void skinAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
            this.onSkinTextureAvailable(typeIn, location, profileTexture);
        }

        default void onSkinTextureAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
            ((SkinManager.ISkinAvailableCallback) this).onSkinTextureAvailable(typeIn, location, profileTexture);
        }
    }

    // exclude
    public interface ISkinAvailableCallback {
        void onSkinTextureAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture);
    }
}