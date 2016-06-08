import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;

//ImageBufferDownload implements IImageBuffer
public class boi implements boc {
   private int[] a;//imageData
   private int b;//imageWidth
   private int c;//imageHeight

   //BufferedImage parseUserSkin(BufferedImage image)
   public BufferedImage a(BufferedImage image) {
      if(image == null) {
         return null;
      } else {
         int ratio = image.getWidth() / 64;
         this.b = 64 * ratio;
         this.c = 64 * ratio;
         BufferedImage bufferedimage = new BufferedImage(this.b, this.c, 2);
         Graphics graphics = bufferedimage.getGraphics();
         graphics.drawImage(image, 0, 0, (ImageObserver)null);
         boolean flag = image.getHeight() == 32 * ratio;
         if(flag) {
            if(!customskinloader.CustomSkinLoader.config.enableTransparentSkin){
               graphics.setColor(new Color(0, 0, 0, 0));
               graphics.fillRect(0, 32 * ratio, 64 * ratio, 32 * ratio);
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
         this.a = ((DataBufferInt)bufferedimage.getRaster().getDataBuffer()).getData();
         if(!customskinloader.CustomSkinLoader.config.enableTransparentSkin){
            this.b(0, 0, 32 * ratio, 16 * ratio);
            if(flag) {
               this.a(32 * ratio, 0, 64 * ratio, 32 * ratio);
            }

            this.b( 0 * ratio, 16 * ratio, 64 * ratio, 32 * ratio);
            this.b(16 * ratio, 48 * ratio, 48 * ratio, 64 * ratio);
         }
         return bufferedimage;
      }
   }

   //skinAvailable
   public void a() {
   }

   //doTransparencyHack(int p_189559_1_, int p_189559_2_, int p_189559_3_, int p_189559_4_)
   private void a(int x0, int y0, int x, int y) {
      for(int i = x0; i < x; ++i) {
         for(int j = y0; j < y; ++j) {
            int k = this.a[i + j * this.b];
            if((k >> 24 & 255) < 128) {
               return;
            }
         }
      }

      for(int i = x0; i < x; ++i) {
         for(int j = y0; j < y; ++j) {
            this.a[i + j * this.b] &= 16777215;
         }
      }

   }

   //setAreaOpaque(int x, int y, int width, int height)
   private void b(int x0, int y0, int x, int y) {
      for(int i = x0; i < x; ++i) {
         for(int j = y0; j < y; ++j) {
            this.a[i + j * this.b] |= -16777216;
         }
      }

   }
}
