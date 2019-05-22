package customskinloader.fabric.mixin;

import java.io.File;
import java.util.Map;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import customskinloader.fake.FakeSkinManager;
import net.minecraft.client.texture.PlayerSkinProvider;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = {PlayerSkinProvider.class})
public abstract class MixinPlayerSkinProvider {
    private FakeSkinManager fakeManager;
    
    @Inject(method = "<init>", at = @At("RETURN"))
    private void initFakeSkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService, CallbackInfo callbackInfo) {
        this.fakeManager = new FakeSkinManager((net.minecraft.client.renderer.texture.TextureManager) (Object) textureManagerInstance, skinCacheDirectory, sessionService);
    }
    
    @Overwrite
    public Identifier loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType) {
        return this.loadSkin(profileTexture, textureType, null);
    }
    
    @Overwrite
    public Identifier loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, PlayerSkinProvider.SkinTextureAvailableCallback skinAvailableCallback) {
        return (Identifier) (Object) this.fakeManager.loadSkin(profileTexture, textureType, (SkinManager.SkinAvailableCallback) skinAvailableCallback);
    }
    
    @Overwrite
    public void loadSkin(GameProfile profile, PlayerSkinProvider.SkinTextureAvailableCallback skinAvailableCallback, boolean requireSecure) {
        this.fakeManager.loadProfileTextures(profile, (SkinManager.SkinAvailableCallback) skinAvailableCallback, requireSecure);
    }
    
    @Overwrite
    public Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> getTextures(GameProfile profile) {
        return this.fakeManager.loadSkinFromCache(profile);
    }
}
