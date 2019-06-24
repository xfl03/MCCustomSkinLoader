package net.minecraft.client.resources;

import java.io.File;
import java.util.Map;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import customskinloader.fake.FakeSkinManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.versions.mcp.MCPVersion;

public class SkinManager {
    static {
        if (!MCPVersion.getMCVersion().startsWith("1.13")) {
            try {
                // Make sure that DownloadingTexture is initialized earlier than FakeSkinManager.
                Class.forName("net.minecraft.client.renderer.texture.DownloadingTexture");
            } catch (ClassNotFoundException e) {}
        }
    }

    public SkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) {
        throw new RuntimeException("This method must be transformed!");
    }

    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType) {
        return this.loadSkin(profileTexture, textureType, null);
    }

    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager.SkinAvailableCallback skinAvailableCallback) {
        throw new RuntimeException("This method must be transformed!");
    }

    // For 1.14+
    public ResourceLocation func_152789_a(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager.ISkinAvailableCallback skinAvailableCallback) {
        return this.loadSkin(profileTexture, textureType, skinAvailableCallback);
    }

    public void loadProfileTextures(GameProfile profile, SkinManager.SkinAvailableCallback skinAvailableCallback, boolean requireSecure) {
        throw new RuntimeException("This method must be transformed!");
    }

    // For 1.14+
    public void func_152790_a(GameProfile profile, SkinManager.ISkinAvailableCallback skinAvailableCallback, boolean requireSecure) {
        this.loadProfileTextures(profile, skinAvailableCallback, requireSecure);
    }

    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) {
        throw new RuntimeException("This method must be transformed!");
    }

    public interface SkinAvailableCallback {
        void skinAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture);
    }

    public interface ISkinAvailableCallback extends SkinAvailableCallback {
        void onSkinTextureAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture);
    }
}