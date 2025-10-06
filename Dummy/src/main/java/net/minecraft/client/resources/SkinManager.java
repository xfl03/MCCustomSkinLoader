package net.minecraft.client.resources;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class SkinManager {
    public SkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) { }
    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager$SkinAvailableCallback skinAvailableCallback) { return null; }
    public void loadProfileTextures(GameProfile profile, SkinManager$SkinAvailableCallback skinAvailableCallback, boolean requireSecure) { }
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) { return null; }
    public CompletableFuture<?> get(GameProfile profile) { return null; }
    public CompletableFuture<?> getOrLoad(GameProfile profile) { return null; }
    public void /* lambda$loadProfileTextures$1 */ func_210275_a(GameProfile profile, boolean b, SkinManager$SkinAvailableCallback callback) {}
    public void /* lambda$null$0 */ func_210276_a(Map<?, ?> map, SkinManager$SkinAvailableCallback callback) {}
}
