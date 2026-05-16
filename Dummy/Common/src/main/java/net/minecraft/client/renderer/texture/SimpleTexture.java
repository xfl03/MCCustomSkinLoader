package net.minecraft.client.renderer.texture;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;

public class SimpleTexture extends ReloadableTexture {
    public SimpleTexture(Identifier location) {}
    public TextureContents loadContents(ResourceManager resourceManager) { return null; }
}
