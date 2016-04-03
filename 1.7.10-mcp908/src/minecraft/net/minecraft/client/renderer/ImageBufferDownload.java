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
    private static final String __OBFID = "CL_00000956";

    public BufferedImage parseUserSkin(BufferedImage p_78432_1_)
    {
        if (p_78432_1_ == null)
        {
            return null;
        }
        else
        {
        	int radio=p_78432_1_.getWidth()/64;
            this.imageWidth = 64*radio;
            this.imageHeight = 32*radio;
            BufferedImage var2 = new BufferedImage(this.imageWidth, this.imageHeight, 2);
            Graphics var3 = var2.getGraphics();
            if(imageWidth == p_78432_1_.getHeight()){
            	var3.drawImage(p_78432_1_, 0 *radio, 0 *radio, 64*radio, 32*radio, 0 *radio, 0 *radio, 64*radio, 32*radio, (ImageObserver)null);
            	var3.drawImage(p_78432_1_, 0 *radio, 16*radio, 64*radio, 32*radio, 0 *radio, 32*radio, 64*radio, 48*radio, (ImageObserver)null);
            }else{
            	var3.drawImage(p_78432_1_, 0, 0, (ImageObserver)null);
            }
            var3.dispose();
            this.imageData = ((DataBufferInt)var2.getRaster().getDataBuffer()).getData();
            if(!customskinloader.CustomSkinLoader.config.enableTransparentSkin){
                this.setAreaOpaque(0, 0, 32*radio, 16*radio);
                this.setAreaTransparent(32*radio, 0, 64*radio, 32*radio);
                this.setAreaOpaque(0, 16*radio, 64*radio, 32*radio);
            }
            return var2;
        }
    }

    public void func_152634_a() {}

    /**
     * Makes the given area of the image transparent if it was previously completely opaque (used to remove the outer
     * layer of a skin around the head if it was saved all opaque; this would be redundant so it's assumed that the skin
     * maker is just using an image editor without an alpha channel)
     */
    private void setAreaTransparent(int p_78434_1_, int p_78434_2_, int p_78434_3_, int p_78434_4_)
    {
        if (!this.hasTransparency(p_78434_1_, p_78434_2_, p_78434_3_, p_78434_4_))
        {
            for (int var5 = p_78434_1_; var5 < p_78434_3_; ++var5)
            {
                for (int var6 = p_78434_2_; var6 < p_78434_4_; ++var6)
                {
                    this.imageData[var5 + var6 * this.imageWidth] &= 16777215;
                }
            }
        }
    }

    /**
     * Makes the given area of the image opaque
     */
    private void setAreaOpaque(int p_78433_1_, int p_78433_2_, int p_78433_3_, int p_78433_4_)
    {
        for (int var5 = p_78433_1_; var5 < p_78433_3_; ++var5)
        {
            for (int var6 = p_78433_2_; var6 < p_78433_4_; ++var6)
            {
                this.imageData[var5 + var6 * this.imageWidth] |= -16777216;
            }
        }
    }

    /**
     * Returns true if the given area of the image contains transparent pixels
     */
    private boolean hasTransparency(int p_78435_1_, int p_78435_2_, int p_78435_3_, int p_78435_4_)
    {
        for (int var5 = p_78435_1_; var5 < p_78435_3_; ++var5)
        {
            for (int var6 = p_78435_2_; var6 < p_78435_4_; ++var6)
            {
                int var7 = this.imageData[var5 + var6 * this.imageWidth];

                if ((var7 >> 24 & 255) < 128)
                {
                    return true;
                }
            }
        }

        return false;
    }
}
