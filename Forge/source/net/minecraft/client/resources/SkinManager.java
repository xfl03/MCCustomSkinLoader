package net.minecraft.client.resources;

import java.io.File;
import java.util.Map;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import customskinloader.fake.FakeSkinManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class SkinManager {
    private FakeSkinManager fakeManager;

    public SkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) {
        this.fakeManager = new FakeSkinManager(textureManagerInstance, skinCacheDirectory, sessionService);
    }

    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType) {
        return this.loadSkin(profileTexture, textureType, null);
    }

    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager.SkinAvailableCallback skinAvailableCallback) {
        return this.fakeManager.loadSkin(profileTexture, textureType, skinAvailableCallback);
    }

    // For 1.14+
    public ResourceLocation func_152789_a(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager.ISkinAvailableCallback skinAvailableCallback) {
        return this.loadSkin(profileTexture, textureType, skinAvailableCallback);
    }

    public void loadProfileTextures(GameProfile profile, SkinManager.SkinAvailableCallback skinAvailableCallback, boolean requireSecure) {
        this.fakeManager.loadProfileTextures(profile, skinAvailableCallback, requireSecure);
    }

    // For 1.14+
    public void func_152790_a(GameProfile profile, SkinManager.ISkinAvailableCallback skinAvailableCallback, boolean requireSecure) {
        this.loadProfileTextures(profile, skinAvailableCallback, requireSecure);
    }

    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
        return this.fakeManager.loadSkinFromCache(profile);
    }

    public interface SkinAvailableCallback {
        default void skinAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
            this.onSkinTextureAvailable(typeIn, location, profileTexture);
        }

        default void onSkinTextureAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
            this.skinAvailable(typeIn, location, profileTexture);
        }
    }

    public interface ISkinAvailableCallback extends SkinAvailableCallback {

    }
}