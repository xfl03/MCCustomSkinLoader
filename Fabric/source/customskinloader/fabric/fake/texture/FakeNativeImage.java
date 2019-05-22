package customskinloader.fabric.fake.texture;

import customskinloader.fake.texture.FakeImage;
import net.minecraft.client.texture.NativeImage;

public class FakeNativeImage implements FakeImage {
    private NativeImage image;

    public FakeNativeImage(int width, int height) {
        this(new NativeImage(width, height, true));
    }

    public FakeNativeImage(NativeImage image) {
        this.image = image;
    }

    public NativeImage getImage() {
        return this.image;
    }

    @Override
    public FakeImage createImage(int width, int height) {
        return new FakeNativeImage(width, height);
    }

    @Override
    public int getWidth() {
        return this.image.getWidth();
    }

    @Override
    public int getHeight() {
        return this.image.getHeight();
    }

    @Override
    public int getRGBA(int x, int y) {
        return this.image.getPixelRGBA(x, y);
    }

    @Override
    public void setRGBA(int x, int y, int rgba) {
        this.image.setPixelRGBA(x, y, rgba);
    }

    @Override
    public void copyImageData(FakeImage image) {
        if (image instanceof FakeNativeImage) {
            this.image.copyFrom(((FakeNativeImage) image).getImage());
        }
    }

    @Override
    public void fillArea(int x0, int y0, int width, int height) {
        this.image.fillRGBA(x0, y0, width, height, 0);
    }

    @Override
    public void copyArea(int x0, int y0, int dx, int dy, int width, int height, boolean reversex, boolean reversey) {
        this.image.method_4304(x0, y0, dx, dy, width, height, reversex, reversey);
    }

    @Override
    public void close() {
    }
}
