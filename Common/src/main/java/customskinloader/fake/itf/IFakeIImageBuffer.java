package customskinloader.fake.itf;

import net.minecraft.client.renderer.IImageBuffer;

public interface IFakeIImageBuffer extends Runnable {
    @Override
    default void run() {
        ((IImageBuffer) this).skinAvailable();
    }
}
