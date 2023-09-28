package net.minecraft.client.renderer;

import java.awt.image.BufferedImage;

import customskinloader.fake.itf.IFakeIImageBuffer;
import net.minecraft.client.renderer.texture.NativeImage;

public interface IImageBuffer extends IFakeIImageBuffer {
    BufferedImage parseUserSkin(BufferedImage image);

    NativeImage func_195786_a(NativeImage image);

    void skinAvailable();
}
