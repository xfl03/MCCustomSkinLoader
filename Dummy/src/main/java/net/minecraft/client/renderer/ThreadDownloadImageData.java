package net.minecraft.client.renderer;

import java.io.InputStream;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.NativeImage;

public class ThreadDownloadImageData implements ITextureObject {
    public NativeImage loadTexture(InputStream stream) { return null; }
}
