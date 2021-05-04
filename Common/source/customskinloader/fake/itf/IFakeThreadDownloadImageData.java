package customskinloader.fake.itf;

import java.awt.image.BufferedImage;

// This interface is only available before 1.12.2
public interface IFakeThreadDownloadImageData {
    /**
     * Reset {@link net.minecraft.client.renderer.ThreadDownloadImageData#textureUploaded} to false to refresh texture.
     */
    void resetTextureUploaded();

    /**
     * Reset {@link net.minecraft.client.renderer.ThreadDownloadImageData#bufferedImage}.
     */
    void resetNewBufferedImage(BufferedImage image);
}
