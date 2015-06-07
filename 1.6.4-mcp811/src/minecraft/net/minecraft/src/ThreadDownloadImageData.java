package net.minecraft.src;

import java.awt.image.BufferedImage;
import java.io.IOException;

public class ThreadDownloadImageData extends AbstractTexture
{
    private final String imageUrl;
    private final IImageBuffer imageBuffer;
    private BufferedImage bufferedImage;
    private Thread imageThread;
    private SimpleTexture imageLocation;
    private boolean textureUploaded;

    public ThreadDownloadImageData(String par1Str, ResourceLocation par2ResourceLocation, IImageBuffer par3IImageBuffer)
    {
        this.imageUrl = par1Str;
        this.imageBuffer = par3IImageBuffer;
        this.imageLocation = par2ResourceLocation != null ? new SimpleTexture(par2ResourceLocation) : null;
    }

    public int getGlTextureId()
    {
        int var1 = super.getGlTextureId();

        if (!this.textureUploaded && this.bufferedImage != null)
        {
            TextureUtil.uploadTextureImage(var1, this.bufferedImage);
            this.textureUploaded = true;
        }

        return var1;
    }

    public void getBufferedImage(BufferedImage par1BufferedImage)
    {
        this.bufferedImage = par1BufferedImage;
    }

    public void loadTexture(ResourceManager par1ResourceManager) throws IOException
    {
        if (this.bufferedImage == null)
        {
            if (this.imageLocation != null)
            {
                this.imageLocation.loadTexture(par1ResourceManager);
                this.glTextureId = this.imageLocation.getGlTextureId();
            }
        }
        else
        {
            TextureUtil.uploadTextureImage(this.getGlTextureId(), this.bufferedImage);
        }

        if (this.imageThread == null)
        {
            this.imageThread = new ThreadDownloadImageDataINNER1(this);
            this.imageThread.setDaemon(true);
            //this.imageThread.setName("Skin downloader: " + this.imageUrl);
            this.imageThread.start();
        }
    }

    public boolean isTextureUploaded()
    {
        this.getGlTextureId();
        return this.textureUploaded;
    }

    static String getImageUrl(ThreadDownloadImageData par0ThreadDownloadImageData)
    {
        return par0ThreadDownloadImageData.imageUrl;
    }

    static IImageBuffer getImageBuffer(ThreadDownloadImageData par0ThreadDownloadImageData)
    {
        return par0ThreadDownloadImageData.imageBuffer;
    }
}
