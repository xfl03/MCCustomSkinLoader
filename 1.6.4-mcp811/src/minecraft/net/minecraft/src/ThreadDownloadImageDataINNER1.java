package net.minecraft.src;

import idv.jlchntoz.CustomSkinLoader;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import javax.imageio.ImageIO;

class ThreadDownloadImageDataINNER1 extends Thread
{
    final ThreadDownloadImageData theThreadDownloadImageData;

    ThreadDownloadImageDataINNER1(ThreadDownloadImageData par1ThreadDownloadImageData)
    {
        this.theThreadDownloadImageData = par1ThreadDownloadImageData;
    }

    public void run()
    {
        
		//HttpURLConnection var1 = null;
		CustomSkinLoader loader=new CustomSkinLoader();
        try
        {
			/*
            var1 = (HttpURLConnection)(new URL(ThreadDownloadImageData.getImageUrl(this.theThreadDownloadImageData))).openConnection(Minecraft.getMinecraft().getProxy());
            var1.setDoInput(true);
            var1.setDoOutput(false);
            var1.connect();

            if (var1.getResponseCode() / 100 != 2)
            {
                return;
            }

            BufferedImage var2 = ImageIO.read(var1.getInputStream());
			*/
			BufferedImage var2 = ImageIO.read(loader.getPlayerSkinStream(
									ThreadDownloadImageData.getImageUrl(this.theThreadDownloadImageData)));
            if (ThreadDownloadImageData.getImageBuffer(this.theThreadDownloadImageData) != null)
            {
                var2 = ThreadDownloadImageData.getImageBuffer(this.theThreadDownloadImageData).parseUserSkin(var2);
            }

            this.theThreadDownloadImageData.getBufferedImage(var2);
        }
        catch (Exception var6)
        {
            var6.printStackTrace();
        }
        finally
        {
			/*
            if (var1 != null)
            {
                var1.disconnect();
            }*/
			loader.disconnect();
        }
    }
}
