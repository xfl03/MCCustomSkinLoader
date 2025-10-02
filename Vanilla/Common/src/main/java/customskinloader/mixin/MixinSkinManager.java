package customskinloader.mixin;

import java.io.File;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import customskinloader.fake.FakeSkinManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SkinManager$CacheKey;
import net.minecraft.client.resources.SkinManager$SkinAvailableCallback;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
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
    // 18w43b+
    @Mixin(SkinManager.class)
    public abstract static class V1 {
        // 18w43b ~ 1.20.1
        @Group(
            name = "inject_init",
            min = 1
        )
        @Inject(
            method = "Lnet/minecraft/client/resources/SkinManager;<init>(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/io/File;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)V",
            at = @At("RETURN")
        )
        private void inject_init(@Coerce Object textureManager, File skinCacheDirectory, @Coerce Object service, CallbackInfo callbackInfo) {
            FakeSkinManager.setSkinCacheDir(skinCacheDirectory);
        }

        // 23w31a ~ 24w45a
        @Group(
            name = "inject_init",
            min = 1
        )
        @Inject(
            method = "Lnet/minecraft/client/resources/SkinManager;<init>(Lnet/minecraft/client/renderer/texture/TextureManager;Ljava/nio/file/Path;Lcom/mojang/authlib/minecraft/MinecraftSessionService;Ljava/util/concurrent/Executor;)V",
            at = @At("RETURN")
        )
        private void inject_init(@Coerce Object textureManager, Path path, @Coerce Object service, @Coerce Object executor, CallbackInfo callbackInfo) {
            FakeSkinManager.setSkinCacheDir(path);
        }

        // 24w46a ~ 25w34b
        @Group(
            name = "inject_init",
            min = 1
        )
        @Inject(
            method = {
                "Lnet/minecraft/client/resources/SkinManager;<init>(Ljava/nio/file/Path;Lcom/mojang/authlib/minecraft/MinecraftSessionService;Ljava/util/concurrent/Executor;)V", // 24w46a ~ 25w33a
                "Lnet/minecraft/client/resources/SkinManager;<init>(Ljava/nio/file/Path;Lnet/minecraft/server/Services;Ljava/util/concurrent/Executor;)V" // 25w34a ~ 25w34b
            },
            at = @At("RETURN")
        )
        private void inject_init(Path path, @Coerce Object service, @Coerce Object executor, CallbackInfo callbackInfo) {
            FakeSkinManager.setSkinCacheDir(path);
        }

        // 25w35a+
        @Group(
            name = "inject_init",
            min = 1
        )
        @Inject(
            method = "Lnet/minecraft/client/resources/SkinManager;<init>(Ljava/nio/file/Path;Lnet/minecraft/server/Services;Lnet/minecraft/client/renderer/texture/SkinTextureDownloader;Ljava/util/concurrent/Executor;)V",
            at = @At("RETURN")
        )
        private void inject_init(Path path, @Coerce Object service, @Coerce Object downloader, @Coerce Object executor, CallbackInfo callbackInfo) {
            FakeSkinManager.setSkinCacheDir(path);
        }
    }

    // 18w43b ~ 1.20.1
    @Mixin(SkinManager.class)
    public abstract static class V2 {
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
        private void modifyArgs_loadSkin_0(Args args, MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager$SkinAvailableCallback skinAvailableCallback) {
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
        private void modifyArgs_loadSkin_1(Args args, MinecraftProfileTexture profileTexture, MinecraftProfileTexture.Type textureType, SkinManager$SkinAvailableCallback skinAvailableCallback) {
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
        private Future<?> redirect_loadProfileTextures_0(ExecutorService executor, Runnable task) {
            FakeSkinManager.loadProfileTextures(task);
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
        private void redirect_loadProfileTextures_1(Executor executor, Runnable task) {
            FakeSkinManager.loadProfileTextures(task);
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
        private void redirect_loadProfileTextures_2(ExecutorService executor, Runnable task) {
            FakeSkinManager.loadProfileTextures(task);
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
        private void inject_func_210276_a(Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map, SkinManager$SkinAvailableCallback skinAvailableCallback, CallbackInfo callbackInfo) {
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

    // 23w42a+
    @Mixin(SkinManager.class)
    public abstract static class V3 {
        @ModifyArg(
            method = {
                "Lnet/minecraft/client/resources/SkinManager;getOrLoad(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;", // 23w42a ~ 25w33a
                "Lnet/minecraft/client/resources/SkinManager;get(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;" // 25w34a+
            },
            at = @At(
                value = "INVOKE",
                target = "Lnet/minecraft/client/resources/SkinManager$CacheKey;<init>(Ljava/util/UUID;Lcom/mojang/authlib/properties/Property;)V"
            )
        )
        private Property modifyArg_getOrLoad(Property property) {
            return FakeSkinManager.createProperty(property);
        }

        @Redirect(
            method = {
                "Lnet/minecraft/client/resources/SkinManager;getOrLoad(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;", // 23w42a ~ 25w33a
                "Lnet/minecraft/client/resources/SkinManager;get(Lcom/mojang/authlib/GameProfile;)Ljava/util/concurrent/CompletableFuture;" // 25w34a+
            },
            at = @At(
                value = "NEW",
                target = "(Ljava/util/UUID;Lcom/mojang/authlib/properties/Property;)Lnet/minecraft/client/resources/SkinManager$CacheKey;"
            )
        )
        private SkinManager$CacheKey redirect_getOrLoad(UUID uuid, Property property, GameProfile profile) {
            return FakeSkinManager.FakeCacheKey.createFakeCacheKey(uuid, property, profile);
        }
    }
}
