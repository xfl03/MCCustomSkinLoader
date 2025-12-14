package net.minecraft.client.renderer.texture;

import net.minecraft.resources.Identifier;
import net.minecraft.util.ResourceLocation;

public class TextureManager {
    public ITextureObject getTexture(ResourceLocation location) { return null; }
    public void loadTexture(ResourceLocation location, AbstractTexture texture) {}
    public boolean loadTexture(ResourceLocation location, ITextureObject texture) { return false; }
    public void registerAndLoad(ResourceLocation resourceLocation, ReloadableTexture reloadableTexture) {}
    public void registerAndLoad(Identifier identifier, ReloadableTexture reloadableTexture) {}
}
