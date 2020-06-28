package customskinloader.mixin;

import java.io.File;
import java.util.Map;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import customskinloader.fake.FakeSkinManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SkinManager.class)
public abstract class MixinSkinManager {
    private FakeSkinManager fakeManager;

    @Inject(
        method = "Lnet/minecraft/client/resources/SkinManager;<init>(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V",
        at = @At("RETURN")
    )
    private void inject_init(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService, CallbackInfo callbackInfo) {
        this.fakeManager = new FakeSkinManager(textureManagerInstance, skinCacheDirectory, sessionService);
    }

    @Inject(
        method = "Lnet/minecraft/client/resources/SkinManager;loadSkin(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_loadSkin(MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager.SkinAvailableCallback skinAvailableCallback, CallbackInfoReturnable<ResourceLocation> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(this.fakeManager.loadSkin(profileTexture, textureType, skinAvailableCallback));
    }

    @Inject(
        method = "Lnet/minecraft/client/resources/SkinManager;loadProfileTextures(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_loadProfileTextures(GameProfile profile, SkinManager.SkinAvailableCallback skinAvailableCallback, boolean requireSecure, CallbackInfo callbackInfo) {
        this.fakeManager.loadProfileTextures(profile, skinAvailableCallback, requireSecure);
        callbackInfo.cancel();
    }

    @Inject(
        method = "Lnet/minecraft/client/resources/SkinManager;loadSkinFromCache(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;",
        at = @At("HEAD"),
        cancellable = true
    )
    private void inject_loadSkinFromCache(GameProfile profile, CallbackInfoReturnable<Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> callbackInfoReturnable) {
        callbackInfoReturnable.setReturnValue(this.fakeManager.loadSkinFromCache(profile));
    }
}
