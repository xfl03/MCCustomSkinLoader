package net.minecraft.client.renderer.texture;

import java.awt.image.BufferedImage;
import java.io.File;

import customskinloader.fake.itf.FakeHttpTextureProcessor;
import net.minecraft.resources.Identifier;

public class HttpTexture {
    public boolean uploaded;

    public HttpTexture(File cacheFileIn, String imageUrlIn, Identifier textureResourceLocation, FakeHttpTextureProcessor imageBufferIn) {

    }

    public HttpTexture(File cacheFileIn, String imageUrlIn, Identifier textureResourceLocation, boolean isLegacy, Runnable processTask) {

    }

    public void upload(BufferedImage bufferedImageIn) {

    }
}
