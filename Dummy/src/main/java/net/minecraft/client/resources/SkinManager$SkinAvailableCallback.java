package net.minecraft.client.resources;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.util.ResourceLocation;

// put into mod jar to prevent https://github.com/cpw/modlauncher/issues/39
public interface SkinManager$SkinAvailableCallback {
    default void skinAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
        this.onSkinTextureAvailable(typeIn, location, profileTexture);
    }

    default void onSkinTextureAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
        ((SkinManager$ISkinAvailableCallback) this).onSkinTextureAvailable(typeIn, location, profileTexture);
    }
}
