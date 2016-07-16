package net.minecraft.client.renderer;

import java.awt.Color;
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
            int ratio = image.getWidth() / 64;
            this.imageWidth = 64 * ratio;
            this.imageHeight = 64 * ratio;
            BufferedImage bufferedimage = new BufferedImage(this.imageWidth, this.imageHeight, 2);
            Graphics graphics = bufferedimage.getGraphics();
            graphics.drawImage(image, 0, 0, (ImageObserver)null);
            boolean flag = image.getHeight() == 32 * ratio;

            if (flag)
            {
                if(!customskinloader.CustomSkinLoader.config.enableTransparentSkin){
                    graphics.setColor(new Color(0, 0, 0, 0));
                    graphics.fillRect(0, 32, 64, 32);
                }
                graphics.drawImage(bufferedimage, 24 * ratio, 48 * ratio, 20 * ratio, 52 * ratio,  4 * ratio, 16 * ratio,  8 * ratio, 20 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 28 * ratio, 48 * ratio, 24 * ratio, 52 * ratio,  8 * ratio, 16 * ratio, 12 * ratio, 20 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 20 * ratio, 52 * ratio, 16 * ratio, 64 * ratio,  8 * ratio, 20 * ratio, 12 * ratio, 32 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 24 * ratio, 52 * ratio, 20 * ratio, 64 * ratio,  4 * ratio, 20 * ratio,  8 * ratio, 32 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 28 * ratio, 52 * ratio, 24 * ratio, 64 * ratio,  0 * ratio, 20 * ratio,  4 * ratio, 32 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 32 * ratio, 52 * ratio, 28 * ratio, 64 * ratio, 12 * ratio, 20 * ratio, 16 * ratio, 32 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 40 * ratio, 48 * ratio, 36 * ratio, 52 * ratio, 44 * ratio, 16 * ratio, 48 * ratio, 20 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 44 * ratio, 48 * ratio, 40 * ratio, 52 * ratio, 48 * ratio, 16 * ratio, 52 * ratio, 20 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 36 * ratio, 52 * ratio, 32 * ratio, 64 * ratio, 48 * ratio, 20 * ratio, 52 * ratio, 32 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 40 * ratio, 52 * ratio, 36 * ratio, 64 * ratio, 44 * ratio, 20 * ratio, 48 * ratio, 32 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 44 * ratio, 52 * ratio, 40 * ratio, 64 * ratio, 40 * ratio, 20 * ratio, 44 * ratio, 32 * ratio, (ImageObserver)null);
                graphics.drawImage(bufferedimage, 48 * ratio, 52 * ratio, 44 * ratio, 64 * ratio, 52 * ratio, 20 * ratio, 56 * ratio, 32 * ratio, (ImageObserver)null);
            }

            graphics.dispose();
            this.imageData = ((DataBufferInt)bufferedimage.getRaster().getDataBuffer()).getData();
            if(!customskinloader.CustomSkinLoader.config.enableTransparentSkin){
                this.setAreaOpaque(0, 0, 32, 16);

                if (flag)
                {
                    this.func_189559_a(32, 0, 64, 32);
                }

                this.setAreaOpaque(0, 16, 64, 32);
                this.setAreaOpaque(16, 48, 48, 64);
            }
            return bufferedimage;
        }
    }

    public void skinAvailable()
    {
    }

    private void func_189559_a(int p_189559_1_, int p_189559_2_, int p_189559_3_, int p_189559_4_)
    {
        for (int i = p_189559_1_; i < p_189559_3_; ++i)
        {
            for (int j = p_189559_2_; j < p_189559_4_; ++j)
            {
                int k = this.imageData[i + j * this.imageWidth];

                if ((k >> 24 & 255) < 128)
                {
                    return;
                }
            }
        }

        for (int l = p_189559_1_; l < p_189559_3_; ++l)
        {
            for (int i1 = p_189559_2_; i1 < p_189559_4_; ++i1)
            {
                this.imageData[l + i1 * this.imageWidth] &= 16777215;
            }
        }
    }

    /**
     * Makes the given area of the image opaque
     */
    private void setAreaOpaque(int x, int y, int width, int height)
    {
        for (int i = x; i < width; ++i)
        {
            for (int j = y; j < height; ++j)
            {
                this.imageData[i + j * this.imageWidth] |= -16777216;
            }
        }
    }
}
