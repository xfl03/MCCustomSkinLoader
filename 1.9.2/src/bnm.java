import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.awt.image.ImageObserver;

public class bnm implements bng {
    private int[] a;//imageData
    private int b;//imageWidth
    private int c;//imageHeight

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
            if(image.getHeight() == 32 * ratio) {
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
                this.b( 0 * ratio,  0 * ratio, 32 * ratio, 16 * ratio);
                this.a(32 * ratio,  0 * ratio, 64 * ratio, 32 * ratio);
                this.b( 0 * ratio, 16 * ratio, 64 * ratio, 32 * ratio);
                this.a( 0 * ratio, 32 * ratio, 16 * ratio, 48 * ratio);
                this.a(16 * ratio, 32 * ratio, 40 * ratio, 48 * ratio);
                this.a(40 * ratio, 32 * ratio, 56 * ratio, 48 * ratio);
                this.a( 0 * ratio, 48 * ratio, 16 * ratio, 64 * ratio);
                this.b(16 * ratio, 48 * ratio, 48 * ratio, 64 * ratio);
                this.a(48 * ratio, 48 * ratio, 64 * ratio, 64 * ratio);
            }
            return bufferedimage;
        }
    }

    public void a() {
    }

    //setAreaTransparent
    private void a(int p1, int p2, int p3, int p4) {
        if(!this.c(p1, p2, p3, p4)) {
            for(int i = p1; i < p3; ++i) {
                for(int j = p2; j < p4; ++j) {
                    this.a[i + j * this.b] &= 16777215;
                }
            }

        }
    }

    //setAreaOpaque
    private void b(int p1, int p2, int p3, int p4) {
        for(int i = p1; i < p3; ++i) {
            for(int j = p2; j < p4; ++j) {
                this.a[i + j * this.b] |= -16777216;
            }
        }

    }

    //hasTransparency
    private boolean c(int p1, int p2, int p3, int p4) {
        for(int i = p1; i < p3; ++i) {
            for(int j = p2; j < p4; ++j) {
                int k = this.a[i + j * this.b];
                if((k >> 24 & 255) < 128) {
                    return true;
                }
            }
        }

        return false;
    }
}
