package net.minecraft.client.renderer.texture;

import net.minecraft.util.ResourceLocation;

public class TextureManager {
    public ITextureObject getTexture(ResourceLocation location) { return null; }
    public boolean loadTexture(ResourceLocation location, ITextureObject object) { return false; }
    public void registerAndLoad(ResourceLocation resourceLocation, ReloadableTexture reloadableTexture) {}
}
