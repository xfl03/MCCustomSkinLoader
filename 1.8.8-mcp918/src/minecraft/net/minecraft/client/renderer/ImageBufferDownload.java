package net.minecraft.client.renderer;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;

public class ImageBufferDownload implements IImageBuffer
{
    private int[] imageData;
    private int imageWidth;
    private int imageHeight;

    public BufferedImage parseUserSkin(BufferedImage image)
    {
        if (image == null)
        {
            return null;
        }
        else
        {
        	int ratio=image.getWidth()/64;
            this.imageWidth = 64 * ratio;
            this.imageHeight = 64 * ratio;
            BufferedImage var2 = new BufferedImage(this.imageWidth, this.imageHeight, 2);
            Graphics var3 = var2.getGraphics();
            var3.drawImage(image, 0, 0, (ImageObserver)null);

            if (image.getHeight() == 32 * ratio)
            {
                var3.drawImage(var2, 24 * ratio, 48 * ratio, 20 * ratio, 52 * ratio,  4 * ratio, 16 * ratio,  8 * ratio, 20 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 28 * ratio, 48 * ratio, 24 * ratio, 52 * ratio,  8 * ratio, 16 * ratio, 12 * ratio, 20 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 20 * ratio, 52 * ratio, 16 * ratio, 64 * ratio,  8 * ratio, 20 * ratio, 12 * ratio, 32 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 24 * ratio, 52 * ratio, 20 * ratio, 64 * ratio,  4 * ratio, 20 * ratio,  8 * ratio, 32 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 28 * ratio, 52 * ratio, 24 * ratio, 64 * ratio,  0 * ratio, 20 * ratio,  4 * ratio, 32 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 32 * ratio, 52 * ratio, 28 * ratio, 64 * ratio, 12 * ratio, 20 * ratio, 16 * ratio, 32 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 40 * ratio, 48 * ratio, 36 * ratio, 52 * ratio, 44 * ratio, 16 * ratio, 48 * ratio, 20 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 44 * ratio, 48 * ratio, 40 * ratio, 52 * ratio, 48 * ratio, 16 * ratio, 52 * ratio, 20 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 36 * ratio, 52 * ratio, 32 * ratio, 64 * ratio, 48 * ratio, 20 * ratio, 52 * ratio, 32 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 40 * ratio, 52 * ratio, 36 * ratio, 64 * ratio, 44 * ratio, 20 * ratio, 48 * ratio, 32 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 44 * ratio, 52 * ratio, 40 * ratio, 64 * ratio, 40 * ratio, 20 * ratio, 44 * ratio, 32 * ratio, (ImageObserver)null);
                var3.drawImage(var2, 48 * ratio, 52 * ratio, 44 * ratio, 64 * ratio, 52 * ratio, 20 * ratio, 56 * ratio, 32 * ratio, (ImageObserver)null);
            }

            var3.dispose();
            this.imageData = ((DataBufferInt)var2.getRaster().getDataBuffer()).getData();
            this.setAreaOpaque(0, 0, 32 * ratio, 16 * ratio);
            this.setAreaTransparent(32 * ratio, 0, 64 * ratio, 32 * ratio);
            this.setAreaOpaque(0, 16 * ratio, 64 * ratio, 32 * ratio);
            this.setAreaTransparent(0, 32 * ratio, 16 * ratio, 48 * ratio);
            this.setAreaTransparent(16 * ratio, 32 * ratio, 40 * ratio, 48 * ratio);
            this.setAreaTransparent(40 * ratio, 32 * ratio, 56 * ratio, 48 * ratio);
            this.setAreaTransparent(0, 48 * ratio, 16 * ratio, 64 * ratio);
            this.setAreaOpaque(16 * ratio, 48 * ratio, 48 * ratio, 64 * ratio);
            this.setAreaTransparent(48 * ratio, 48 * ratio, 64 * ratio, 64 * ratio);
            return var2;
        }
    }

    public void skinAvailable()
    {
    }

    /**
     * Makes the given area of the image transparent if it was previously completely opaque (used to remove the outer
     * layer of a skin around the head if it was saved all opaque; this would be redundant so it's assumed that the skin
     * maker is just using an image editor without an alpha channel)
     */
    private void setAreaTransparent(int p_78434_1_, int p_78434_2_, int p_78434_3_, int p_78434_4_)
    {
        if (!this.hasTransparency(p_78434_1_, p_78434_2_, p_78434_3_, p_78434_4_))
        {
            for (int i = p_78434_1_; i < p_78434_3_; ++i)
            {
                for (int j = p_78434_2_; j < p_78434_4_; ++j)
                {
                    this.imageData[i + j * this.imageWidth] &= 16777215;
                }
            }
        }
    }

    /**
     * Makes the given area of the image opaque
     */
    private void setAreaOpaque(int p_78433_1_, int p_78433_2_, int p_78433_3_, int p_78433_4_)
    {
        for (int i = p_78433_1_; i < p_78433_3_; ++i)
        {
            for (int j = p_78433_2_; j < p_78433_4_; ++j)
            {
                this.imageData[i + j * this.imageWidth] |= -16777216;
            }
        }
    }

    /**
     * Returns true if the given area of the image contains transparent pixels
     */
    private boolean hasTransparency(int p_78435_1_, int p_78435_2_, int p_78435_3_, int p_78435_4_)
    {
        for (int i = p_78435_1_; i < p_78435_3_; ++i)
        {
            for (int j = p_78435_2_; j < p_78435_4_; ++j)
            {
                int k = this.imageData[i + j * this.imageWidth];

                if ((k >> 24 & 255) < 128)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
