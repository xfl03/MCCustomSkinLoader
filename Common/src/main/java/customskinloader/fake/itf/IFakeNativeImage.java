package customskinloader.fake.itf;

import net.minecraft.client.renderer.texture.NativeImage;

public interface IFakeNativeImage {
    default int getPixel(int x, int y) {
        return ((NativeImage) this).func_195709_a(x, y);
    }
}
