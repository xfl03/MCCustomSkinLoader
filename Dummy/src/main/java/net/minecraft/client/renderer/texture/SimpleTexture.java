package net.minecraft.client.renderer.texture;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ResourceLocation;

public class SimpleTexture extends ReloadableTexture implements ITextureObject {
    public SimpleTexture(ResourceLocation location) {}
    public SimpleTexture(Identifier identifier) {}
    public TextureContents loadContents(IResourceManager resourceManager) { return null; }
}
