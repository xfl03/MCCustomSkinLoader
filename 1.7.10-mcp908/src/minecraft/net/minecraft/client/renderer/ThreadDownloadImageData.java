package net.minecraft.client.renderer;

import idv.jlchntoz.CustomSkinLoader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.imageio.ImageIO;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ThreadDownloadImageData extends SimpleTexture
{
    private static final Logger logger = LogManager.getLogger();
    private static final AtomicInteger field_147643_d = new AtomicInteger(0);
    private final File field_152434_e;
    private final String imageUrl;
    private final IImageBuffer imageBuffer;
    private BufferedImage bufferedImage;
    private Thread imageThread;
    private boolean textureUploaded;
    private static final String __OBFID = "CL_00001049";

    public ThreadDownloadImageData(File p_i1049_1_, String p_i1049_2_, ResourceLocation p_i1049_3_, IImageBuffer p_i1049_4_)
    {
        super(p_i1049_3_);
        this.field_152434_e = p_i1049_1_;
        this.imageUrl = p_i1049_2_;
        this.imageBuffer = p_i1049_4_;
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

        if (this.imageBuffer != null)
        {
            this.imageBuffer.func_152634_a();
        }
    }

    public void loadTexture(IResourceManager p_110551_1_) throws IOException
    {
        if (this.bufferedImage == null && this.textureLocation != null)
        {
            super.loadTexture(p_110551_1_);
        }

        if (this.imageThread == null)
        {
            if (this.field_152434_e != null && this.field_152434_e.isFile())
            {
                logger.debug("Loading http texture from local cache ({})", new Object[] {this.field_152434_e});

                try
                {
                    this.bufferedImage = ImageIO.read(this.field_152434_e);

                    if (this.imageBuffer != null)
                    {
                        this.func_147641_a(this.imageBuffer.parseUserSkin(this.bufferedImage));
                    }
                }
                catch (IOException var3)
                {
                    logger.error("Couldn\'t load skin " + this.field_152434_e, var3);
                    this.func_152433_a();
                }
            }
            else
            {
                this.func_152433_a();
            }
        }
    }

    protected void func_152433_a()
    {
        this.imageThread = new Thread("Texture Downloader #" + field_147643_d.incrementAndGet())
        {
            private static final String __OBFID = "CL_00001050";
            public void run()
            {
                ThreadDownloadImageData.logger.debug("Downloading http texture from {} to {}", new Object[] {ThreadDownloadImageData.this.imageUrl, ThreadDownloadImageData.this.field_152434_e});

                try
                {
                	CustomSkinLoader loader=new CustomSkinLoader();
					InputStream var9=loader.getPlayerSkinStream(ThreadDownloadImageData.this.imageUrl);


                    BufferedImage var2;
                    /*
                    if (ThreadDownloadImageData.this.field_152434_e != null)
                    {
                        FileUtils.copyInputStreamToFile(var9, ThreadDownloadImageData.this.field_152434_e);
                        var2 = ImageIO.read(ThreadDownloadImageData.this.field_152434_e);
                    }
                    else
                    {
                        
                    }*/
                    var2 = ImageIO.read(var9);
                    if (ThreadDownloadImageData.this.imageBuffer != null)
                    {
                        var2 = ThreadDownloadImageData.this.imageBuffer.parseUserSkin(var2);
                    }

                    ThreadDownloadImageData.this.func_147641_a(var2);
                    loader.disconnect();
                }
                catch (Exception var6)
                {
                    ThreadDownloadImageData.logger.error("Couldn\'t download http texture", var6);
                }
            }
        };
        this.imageThread.setDaemon(true);
        this.imageThread.start();
    }
}
