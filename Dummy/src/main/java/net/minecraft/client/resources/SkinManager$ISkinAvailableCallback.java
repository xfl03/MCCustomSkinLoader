package net.minecraft.client.resources;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.util.ResourceLocation;

public interface SkinManager$ISkinAvailableCallback {
    void onSkinTextureAvailable(MinecraftProfileTexture.Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture);
}
