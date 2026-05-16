package customskinloader.fake.texture;

import java.io.InputStream;

import customskinloader.fake.itf.FakeInterfaceManager;
import com.mojang.blaze3d.platform.NativeImage;

public class FakeNativeImage implements FakeImage {
    private NativeImage image;

    public FakeNativeImage(int width, int height) {
        this(new NativeImage(width, height, true));
    }

    public FakeNativeImage(NativeImage image) {
        this.image = image;
    }

    public NativeImage getImage() {
        return image;
    }

    public FakeImage createImage(int width, int height) {
        return new FakeNativeImage(width, height);
    }

    public FakeImage createImage(InputStream is) {
        return new FakeNativeImage(NativeImage.read(is));
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int getRGBA(int x, int y) {
        return FakeInterfaceManager.NativeImage_getPixel(image, x, y);
    }

    public void setRGBA(int x, int y, int rgba) {
        FakeInterfaceManager.NativeImage_setPixel(image, x, y, rgba);
    }

    public void copyImageData(FakeImage image) {
        if (!(image instanceof FakeNativeImage)) return;
        this.image.copyFrom(((FakeNativeImage) image).getImage());
    }

    public void fillArea(int x0, int y0, int width, int height) {
        image.fillRect(x0, y0, width, height, 0);
    }

    public void copyArea(int x0, int y0, int dx, int dy, int width, int height, boolean reversex, boolean reversey) {
        image.copyRect(x0, y0, dx, dy, width, height, reversex, reversey);
    }

    public void close() {
        image.close();
    }
}
