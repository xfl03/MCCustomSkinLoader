package customskinloader.fake.itf;

import com.mojang.blaze3d.platform.NativeImage;

public interface IFakeNativeImage {
    default int getPixel(int x, int y) {
        return ((NativeImage) this).getPixelRGBA(x, y);
    }

    default void setPixel(int x, int y, int color) {
        ((NativeImage) this).setPixelRGBA(x, y, color);
    }
}
