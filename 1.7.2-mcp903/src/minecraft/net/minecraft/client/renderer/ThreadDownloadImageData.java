package net.minecraft.client.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import idv.jlchntoz.CustomSkinLoader;

public class ThreadDownloadImageData extends SimpleTexture
{
    private static final Logger logger = LogManager.getLogger();
    private static final AtomicInteger field_147643_d = new AtomicInteger(0);
    private final String imageUrl;
    private final IImageBuffer imageBuffer;
    private BufferedImage bufferedImage;
    private Thread imageThread;
    private boolean textureUploaded;
    private static final String __OBFID = "CL_00001049";
	public CustomSkinLoader customSkinLoader=new CustomSkinLoader();
	
	public boolean enabled;//For Optifine

    public ThreadDownloadImageData(String par1Str, ResourceLocation par2ResourceLocation, IImageBuffer par3IImageBuffer)
    {
        super(par2ResourceLocation);
        this.imageUrl = par1Str;
        this.imageBuffer = par3IImageBuffer;
    }

    private void func_147640_e()
    {
        if (!this.textureUploaded)
        {
            if (this.bufferedImage != null)
            {
                if (this.textureLocation != null)
                {
                    this.func_147631_c();
                }

                TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
                this.textureUploaded = true;
            }
        }
    }

    public int getGlTextureId()
    {
        this.func_147640_e();
        return super.getGlTextureId();
    }

    public void func_147641_a(BufferedImage p_147641_1_)
    {
        this.bufferedImage = p_147641_1_;
    }

    public void loadTexture(IResourceManager par1ResourceManager) throws IOException
    {
        if (this.bufferedImage == null && this.textureLocation != null)
        {
            super.loadTexture(par1ResourceManager);
        }

        if (this.imageThread == null)
        {
            this.imageThread = new Thread("Texture Downloader #" + field_147643_d.incrementAndGet())
            {
                private static final String __OBFID = "CL_00001050";
                public void run()
                {
					try
					{
						BufferedImage var2 = ImageIO.read(
							ThreadDownloadImageData.this.customSkinLoader.getPlayerSkinStream(
									ThreadDownloadImageData.this.imageUrl));
						if (ThreadDownloadImageData.this.imageBuffer != null){
							var2 = ThreadDownloadImageData.this.imageBuffer.parseUserSkin(var2);
						}
						ThreadDownloadImageData.this.func_147641_a(var2);
						customSkinLoader.disconnect();
					}catch (Exception var6){
						ThreadDownloadImageData.logger.error("Couldn\'t download http texture", var6);
						return;
					}

				}
			};
            this.imageThread.setDaemon(true);
            //this.imageThread.setName("Skin downloader: " + this.imageUrl);
            this.imageThread.start();
        }
    }

    public boolean isTextureUploaded()
    {
        this.func_147640_e();
        return this.textureUploaded;
    }
}
