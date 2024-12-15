package net.minecraft.client.resources;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class SkinManager {
    public SkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) { }
    public ResourceLocation loadSkin(final MinecraftProfileTexture profileTexture, final MinecraftProfileTexture.Type textureType, final SkinManager$SkinAvailableCallback skinAvailableCallback) { return null; }
    public void loadProfileTextures(final GameProfile profile, final SkinManager$SkinAvailableCallback skinAvailableCallback, final boolean requireSecure) { }
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile) { return null; }
    public CompletableFuture<?> getOrLoad(GameProfile profile) { return null; }
    public PlayerSkin getInsecureSkin(GameProfile profile) { return null; }
    public void /* lambda$loadProfileTextures$1 */ func_210275_a(GameProfile profile, boolean b, SkinManager$SkinAvailableCallback callback) {}
    public void /* lambda$null$0 */ func_210276_a(Map<?, ?> map, SkinManager$SkinAvailableCallback callback) {}
    public static PlayerSkin lambda$registerTextures$1(CompletableFuture<?> skin, String url, CompletableFuture<?> cape, CompletableFuture<?> elytra, PlayerSkin$Model model, MinecraftProfileTextures textures, Void _void) { return null; }
}
