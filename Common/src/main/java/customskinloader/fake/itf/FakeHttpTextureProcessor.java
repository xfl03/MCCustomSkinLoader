package customskinloader.fake.itf;

import java.awt.image.BufferedImage;

import com.mojang.blaze3d.platform.NativeImage;

public interface FakeHttpTextureProcessor extends IFakeHttpTextureProcessor {
    BufferedImage process(BufferedImage image);
    NativeImage process(NativeImage image);
    void onTextureDownloaded();
}
