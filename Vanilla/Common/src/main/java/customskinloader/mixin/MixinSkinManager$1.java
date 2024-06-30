package customskinloader.mixin;

import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.properties.Property;
import customskinloader.fake.FakeSkinManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SkinManager$1;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(SkinManager$1.class)
public abstract class MixinSkinManager$1 {
    @ModifyArgs(
        method = "Lnet/minecraft/client/resources/SkinManager$1;load(Lnet/minecraft/client/resources/SkinManager$CacheKey;)Ljava/util/concurrent/CompletableFuture;",
        at = @At(
            value = "INVOKE",
            target = "Ljava/util/concurrent/CompletableFuture;supplyAsync(Ljava/util/function/Supplier;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;",
            remap = false
        )
    )
    private void modifyArgs_load(Args args, SkinManager.CacheKey cacheKey) {
        Object[] argsArr = new Object[args.size()];
        for (int i = 0; i < argsArr.length; i++) {
            argsArr[i] = args.get(i);
        }
        args.setAll(FakeSkinManager.loadProfileTextures(ImmutableList.copyOf(argsArr), cacheKey));
    }

    // 23w31a ~ 23w41a
    @Group(
        name = "lambda$load$0",
        min = 1,
        max = 2
    )
    @Redirect(
        method = "Lnet/minecraft/client/resources/SkinManager$1;lambda$load$0(Lcom/mojang/authlib/minecraft/MinecraftSessionService;Lcom/mojang/authlib/GameProfile;)Lnet/minecraft/client/resources/SkinManager$TextureInfo;",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;getTextures(Lcom/mojang/authlib/GameProfile;Z)Ljava/util/Map;",
            remap = false
        )
    )
    private static Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> redirect_lambda$load$0(MinecraftSessionService sessionService, GameProfile profile, boolean requireSecure) {
        return FakeSkinManager.loadSkinFromCache(sessionService, profile, requireSecure);
    }

    // 23w42a+
    @Coerce
    @Group(
        name = "lambda$load$0",
        min = 1,
        max = 2
    )
    @Redirect(
        method = "Lnet/minecraft/client/resources/SkinManager$1;lambda$load$0(Lnet/minecraft/client/resources/SkinManager$CacheKey;Lcom/mojang/authlib/minecraft/MinecraftSessionService;)Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;",
        at = @At(
            value = "INVOKE",
            target = "Lcom/mojang/authlib/minecraft/MinecraftSessionService;unpackTextures(Lcom/mojang/authlib/properties/Property;)Lcom/mojang/authlib/minecraft/MinecraftProfileTextures;",
            remap = false
        )
    )
    private static Object redirect_lambda$load$0(MinecraftSessionService sessionService, Property property) {
        return FakeSkinManager.loadSkinFromCache(sessionService, property);
    }
}
