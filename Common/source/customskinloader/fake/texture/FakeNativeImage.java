package customskinloader.fake.texture;

import net.minecraft.client.renderer.texture.NativeImage;

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

    public int getWidth() {
        return image.func_195702_a();
    }

    public int getHeight() {
        return image.func_195714_b();
    }

    public int getRGBA(int x, int y) {
        int abgr = image.func_195709_a(x, y);
        int b = abgr >>> 16 & 0xFF, r = abgr >>> 0 & 0xFF;
        return abgr & 0xFF00FF00 | (r << 16 | b);
    }

    public void setRGBA(int x, int y, int rgba) {
        int b = rgba >>> 0 & 0xFF, r = rgba >>> 16 & 0xFF;
        image.func_195700_a(x, y, rgba & 0xFF00FF00 | (b << 16 | r));
    }

    public void copyImageData(FakeImage image) {
        if (!(image instanceof FakeNativeImage)) return;
        this.image.func_195703_a(((FakeNativeImage) image).getImage());
    }

    public void fillArea(int x0, int y0, int width, int height) {
        image.func_195715_a(x0, y0, width, height, 0);
    }

    public void copyArea(int x0, int y0, int dx, int dy, int width, int height, boolean reversex, boolean reversey) {
        image.func_195699_a(x0, y0, dx, dy, width, height, reversex, reversey);
    }

    public void close() {
    }
}
