package customskinloader.mixin;

import java.awt.image.BufferedImage;

import customskinloader.fake.itf.IFakeThreadDownloadImageData;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ThreadDownloadImageData.class) // This mixin is only for 1.12.2-
public abstract class MixinThreadDownloadImageDataV1 implements IFakeThreadDownloadImageData {
    @Shadow
    private BufferedImage bufferedImage;

    @Shadow
    private boolean textureUploaded;

    @Override
    public void resetNewBufferedImage(BufferedImage image) {
        this.textureUploaded = false;
        this.bufferedImage = image;
    }
}
