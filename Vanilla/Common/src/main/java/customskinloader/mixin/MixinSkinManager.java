package customskinloader.mixin;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import customskinloader.fake.FakeSkinManager;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@SuppressWarnings("target")
public abstract class MixinSkinManager {
    // 18w43b ~ 1.20.1
    @Mixin(SkinManager.class)
    public abstract static class V1 {
        @Inject(
                method = "Lnet/minecraft/client/resources/SkinManager;<init>(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V",
                at = @At("RETURN")
        )
        private void inject_init(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService, CallbackInfo callbackInfo) {
            FakeSkinManager.setSkinCacheDir(skinCacheDirectory);
        }

        // 18w43b ~ 19w37a
        @Group(
            name = "modifyArgs_loadSkin",
            min = 1
        )
        @ModifyArgs(
            method = "Lnet/minecraft/client/resources/SkinManager;loadSkin(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/ThreadDownloadImageData;<init>(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/util/ResourceLocation;Lnet/minecraft/client/renderer/IImageBuffer;)V"
            )
        )
        private void modifyArgs_loadSkin_0(Args args, MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager.SkinAvailableCallback skinAvailableCallback) {
            Object[] argsArr = new Object[args.size()];
            for (int i = 0; i < argsArr.length; i++) {
                argsArr[i] = args.get(i);
            }
            argsArr = FakeSkinManager.createThreadDownloadImageData(ImmutableList.copyOf(argsArr), profileTexture, textureType);
            args.setAll(argsArr);
        }

        // 19w38a ~ 1.20.1
        @Group(
            name = "modifyArgs_loadSkin",
            min = 1
        )
        @ModifyArgs(
            method = "Lnet/minecraft/client/resources/SkinManager;loadSkin(Lcom/mojang/authlib/minecraft/MinecraftProfileTexture;Lcom/mojang/authlib/minecraft/MinecraftProfileTexture$Type;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)Lnet/minecraft/util/ResourceLocation;",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/ThreadDownloadImageData;<init>(Ljava/io/File;Ljava/lang/String;Lnet/minecraft/util/ResourceLocation;ZLjava/lang/Runnable;)V"
            )
        )
        private void modifyArgs_loadSkin_1(Args args, MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager.SkinAvailableCallback skinAvailableCallback) {
            this.modifyArgs_loadSkin_0(args, profileTexture, textureType, skinAvailableCallback);
        }

        // 18w43b ~ 19w37a
        @Group(
            name = "redirect_loadProfileTextures",
            min = 1
        )
        @Redirect(
            method = "Lnet/minecraft/client/resources/SkinManager;loadProfileTextures(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V",
            at = @At(
                value = "INVOKE",
                target = "Ljava/util/concurrent/ExecutorService;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/Future;",
                remap = false
            )
        )
        private Future<?> redirect_loadProfileTextures_0(ExecutorService executor, Runnable task, GameProfile profile, SkinManager.SkinAvailableCallback skinAvailableCallback, boolean requireSecure) {
            FakeSkinManager.loadProfileTextures(task, profile);
            return null;
        }

        // 19w38a ~ 1.18-exp7
        @Group(
            name = "redirect_loadProfileTextures",
            min = 1
        )
        @Redirect(
            method = "Lnet/minecraft/client/resources/SkinManager;loadProfileTextures(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V",
            at = @At(
                value = "INVOKE",
                target = "Ljava/util/concurrent/Executor;execute(Ljava/lang/Runnable;)V",
                remap = false
            )
        )
        private void redirect_loadProfileTextures_1(Executor executor, Runnable task, GameProfile profile, SkinManager.SkinAvailableCallback skinAvailableCallback, boolean requireSecure) {
            FakeSkinManager.loadProfileTextures(task, profile);
        }

        // 21w37a ~ 1.20.1
        @Group(
            name = "redirect_loadProfileTextures",
            min = 1
        )
        @Redirect(
            method = "Lnet/minecraft/client/resources/SkinManager;loadProfileTextures(Lcom/mojang/authlib/GameProfile;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;Z)V",
            at = @At(
                value = "INVOKE",
                target = "Ljava/util/concurrent/ExecutorService;execute(Ljava/lang/Runnable;)V",
                remap = false
            )
        )
        private void redirect_loadProfileTextures_2(ExecutorService executor, Runnable task, GameProfile profile, SkinManager.SkinAvailableCallback skinAvailableCallback, boolean requireSecure) {
            FakeSkinManager.loadProfileTextures(task, profile);
        }

        @Inject(
            method = "Lnet/minecraft/client/resources/SkinManager;loadSkinFromCache(Lcom/mojang/authlib/GameProfile;)Ljava/util/Map;",
            at = @At("HEAD"),
            cancellable = true
        )
        private void inject_loadSkinFromCache(GameProfile profile, CallbackInfoReturnable<Map<MinecraftProfileTexture.Type, MinecraftProfileTexture>> callbackInfoReturnable) {
            callbackInfoReturnable.setReturnValue(FakeSkinManager.loadSkinFromCache(profile));
        }

        @Redirect(
            method = "Lnet/minecraft/client/resources/SkinManager;func_210275_a(Lcom/mojang/authlib/GameProfile;ZLnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)V",
            at = @At(
                value = "INVOKE",
                target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getTextures(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;",
                remap = false
            )
        )
        private Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> redirect_func_210275_a(MinecraftSessionService sessionService, GameProfile profile, boolean requireSecure) {
            return FakeSkinManager.getUserProfile(sessionService, profile, requireSecure);
        }

        // 18w43b ~ 19w37a
        @Group(
            name = "loadElytraTexture",
            min = 1
        )
        @Inject(
            method = "Lnet/minecraft/client/resources/SkinManager;func_210276_a(Ljava/util/Map;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)V",
            at = @At(
                value = "INVOKE",
                target = "Ljava/util/Map;containsKey(Ljava/lang/Object;)Z",
                ordinal = 0,
                remap = false
            )
        )
        private void inject_func_210276_a(Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map, SkinManager.SkinAvailableCallback skinAvailableCallback, CallbackInfo callbackInfo) {
            FakeSkinManager.loadElytraTexture((SkinManager) (Object) this, map, skinAvailableCallback);
        }

        // 19w38a ~ 1.20.1
        @Group(
            name = "loadElytraTexture",
            min = 1
        )
        @Redirect(
            method = "Lnet/minecraft/client/resources/SkinManager;func_210276_a(Ljava/util/Map;Lnet/minecraft/client/resources/SkinManager$SkinAvailableCallback;)V",
            at = @At(
                value = "INVOKE",
                target = "Lcom/google/common/collect/ImmutableList;of(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableList;",
                remap = false
            )
        )
        private ImmutableList<MinecraftProfileTexture.Type> redirect_func_229297_b_(Object e1, Object e2) {
            return ImmutableList.copyOf(MinecraftProfileTexture.Type.values());
        }
    }

    // 23w31a+
    @Mixin(SkinManager.class)
    public abstract static class V2 {
        @Inject(
            method = "Lnet/minecraft/client/resources/SkinManager;<init>(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/nio/file/Path;Lcom/mojang/authlib/minecraft/MinecraftSessionService;Ljava/util/concurrent/Executor;)V",
            at = @At("RETURN")
        )
        private void inject_init(TextureManager textureManager, Path path, MinecraftSessionService minecraftSessionService, Executor executor, CallbackInfo callbackInfo) {
            FakeSkinManager.setSkinCacheDir(path);
        }

        @ModifyArg(
            method = "Lnet/minecraft/client/resources/SkinManager;<init>(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/nio/file/Path;Lcom/mojang/authlib/minecraft/MinecraftSessionService;Ljava/util/concurrent/Executor;)V",
            at = @At(
                value = "INVOKE",
                target = "Lcom/google/common/cache/CacheBuilder;build(Lcom/google/common/cache/CacheLoader;)Lcom/google/common/cache/LoadingCache;",
                remap = false
            )
        )
        private CacheLoader<Object, ?> modifyArg_init(CacheLoader<Object, ?> cacheLoader) {
            return FakeSkinManager.setCacheLoader(cacheLoader);
        }

        @Inject(
            method = "Lnet/minecraft/client/resources/SkinManager;getInsecureSkin(Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/resources/PlayerSkin;",
            at = @At("HEAD")
        )
        private void inject_getInsecureSkin(GameProfile profile, CallbackInfoReturnable<?> callbackInfoReturnable) {
            FakeSkinManager.setSkullType(profile);
        }

        @Redirect(
            method = "Lnet/minecraft/client/resources/SkinManager;getOrLoad(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;",
            at = @At(
                value = "INVOKE",
                target = "Lcom/google/common/cache/LoadingCache;getUnchecked(Ljava/lang/Object;)Ljava/lang/Object;",
                remap = false
            )
        )
        private Object redirect_getOrLoad(LoadingCache<?, ?> loadingCache, Object cacheKey, GameProfile profile) throws Exception {
            return FakeSkinManager.loadCache(loadingCache, cacheKey, profile);
        }
    }

    // 23w42a+
    @Mixin(SkinManager.class)
    public abstract static class V3 {
        @ModifyArg(
            method = "Lnet/minecraft/client/resources/SkinManager;getOrLoad(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;",
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/resources/SkinManager$CacheKey;<init>(Ljava/util/UUID;Lcom/mojang/authlib/properties/Property;)V"
            )
        )
        private Property modifyArg_getOrLoad(Property property) {
            return FakeSkinManager.createProperty(property);
        }
    }
}
