package net.minecraft.client.renderer.texture;

import java.io.File;

import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.util.ResourceLocation;

public class ThreadDownloadImageData extends SimpleTexture {
    public ThreadDownloadImageData(File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation, IImageBuffer imageBufferIn) {
        super(textureResourceLocation);
    }
}
