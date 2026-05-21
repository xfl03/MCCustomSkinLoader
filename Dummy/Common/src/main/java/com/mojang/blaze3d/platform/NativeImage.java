package com.mojang.blaze3d.platform;

import java.io.InputStream;

/**
 * Empty stack for NativeImage
 */
public class NativeImage {
    public static NativeImage read(InputStream inputStreamIn) {
        return null;
    }

    public NativeImage(int p_i48122_1_, int p_i48122_2_, boolean p_i48122_3_) {
    }

    /**
     * getWidth
     */
    public int getWidth() {
        return 0;
    }

    /**
     * getHeight
     */
    public int getHeight() {
        return 0;
    }

    /**
     * copyImage image
     */
    public void copyFrom(NativeImage p_195703_1_) {
    }

    /**
     * fillAreaRGBA x0 y0 weight height rgba
     */
    public void fillRect(int p_195715_1_, int p_195715_2_, int p_195715_3_, int p_195715_4_, int p_195715_5_) {
    }

    /**
     * copyAreaRGBA x0 y0 dx dy weight height reversex reversey
     */
    public void copyRect(int p_195699_1_, int p_195699_2_, int p_195699_3_, int p_195699_4_, int p_195699_5_, int p_195699_6_, boolean p_195699_7_, boolean p_195699_8_) {
    }

    /**
     * getPixelRGBA x y
     */
    public int getPixelRGBA(int p_195709_1_, int p_195709_2_) {
        return 0;
    }

    /**
     * setPixelRGBA x y rgba
     */
    public void setPixelRGBA(int p_195700_1_, int p_195700_2_, int p_195700_3_) {
    }

    public void close() {
    }
}
