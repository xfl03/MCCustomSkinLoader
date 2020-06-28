package net.minecraft.client.renderer.texture;

import java.io.File;

import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.util.ResourceLocation;

public class DownloadingTexture extends SimpleTexture {
    public DownloadingTexture(File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation, IImageBuffer imageBufferIn) {
        super(textureResourceLocation);
    }

    public DownloadingTexture(File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation, boolean legacySkinIn, Runnable processTaskIn) {
        super(textureResourceLocation);
    }
}
