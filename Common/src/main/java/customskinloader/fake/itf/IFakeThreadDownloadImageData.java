package customskinloader.fake.itf;

import java.awt.image.BufferedImage;

// This interface is only available before 1.12.2
public interface IFakeThreadDownloadImageData {
    /**
     * Reset {@link net.minecraft.client.renderer.ThreadDownloadImageData#bufferedImage} and
     * {@link net.minecraft.client.renderer.ThreadDownloadImageData#textureUploaded} to false to refresh texture.
     */
    void resetNewBufferedImage(BufferedImage image);
}
