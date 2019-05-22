package customskinloader.fabric.fake;

import customskinloader.CustomSkinLoader;
import customskinloader.fabric.fake.texture.FakeNativeImage;
import customskinloader.fake.texture.FakeImage;
import net.minecraft.client.texture.ImageFilter;
import net.minecraft.client.texture.NativeImage;

public class FakeSkinBuffer implements ImageFilter {

    private int ratio = 1;
    private FakeImage image = null;

    //parseUserSkin for 1.13+
    @Override
    public NativeImage filterImage(NativeImage image) {
        if (image == null) {
            return null;
        }
        FakeImage img = parseUserSkin(new FakeNativeImage(image));
        if (img instanceof FakeNativeImage) {
            return ((FakeNativeImage) img).getImage();
        }
        CustomSkinLoader.logger.warning("Failed to parseUserSkin(func_195786_a).");
        return null;
    }

    public FakeImage parseUserSkin(FakeImage image) {
        if (image == null) {
            return null;
        }
        this.ratio = image.getWidth() / 64;

        if (image.getHeight() != image.getWidth()) {//Single Layer
            //Create a new image and copy origin image data
            FakeImage img = image.createImage(64 * this.ratio, 64 * this.ratio);
            img.copyImageData(image); image.close(); image = img;
            image.fillArea(0 * this.ratio, 32 * this.ratio, 64 * this.ratio, 32 * this.ratio);

            //Right Leg -> Left Leg
            image.copyArea( 4 * this.ratio, 16 * this.ratio, 16 * this.ratio, 32 * this.ratio, 4 * this.ratio,  4 * this.ratio, true, false);//Top
            image.copyArea( 8 * this.ratio, 16 * this.ratio, 16 * this.ratio, 32 * this.ratio, 4 * this.ratio,  4 * this.ratio, true, false);//Bottom
            image.copyArea( 0 * this.ratio, 20 * this.ratio, 24 * this.ratio, 32 * this.ratio, 4 * this.ratio, 12 * this.ratio, true, false);//Right
            image.copyArea( 4 * this.ratio, 20 * this.ratio, 16 * this.ratio, 32 * this.ratio, 4 * this.ratio, 12 * this.ratio, true, false);//Front
            image.copyArea( 8 * this.ratio, 20 * this.ratio,  8 * this.ratio, 32 * this.ratio, 4 * this.ratio, 12 * this.ratio, true, false);//Left
            image.copyArea(12 * this.ratio, 20 * this.ratio, 16 * this.ratio, 32 * this.ratio, 4 * this.ratio, 12 * this.ratio, true, false);//Back
            //Right Arm -> Left Arm
            image.copyArea(44 * this.ratio, 16 * this.ratio, -8 * this.ratio, 32 * this.ratio, 4 * this.ratio,  4 * this.ratio, true, false);//Top
            image.copyArea(48 * this.ratio, 16 * this.ratio, -8 * this.ratio, 32 * this.ratio, 4 * this.ratio,  4 * this.ratio, true, false);//Bottom
            image.copyArea(40 * this.ratio, 20 * this.ratio,  0 * this.ratio, 32 * this.ratio, 4 * this.ratio, 12 * this.ratio, true, false);//Right
            image.copyArea(44 * this.ratio, 20 * this.ratio, -8 * this.ratio, 32 * this.ratio, 4 * this.ratio, 12 * this.ratio, true, false);//Front
            image.copyArea(48 * this.ratio, 20 * this.ratio,-16 * this.ratio, 32 * this.ratio, 4 * this.ratio, 12 * this.ratio, true, false);//Left
            image.copyArea(52 * this.ratio, 20 * this.ratio, -8 * this.ratio, 32 * this.ratio, 4 * this.ratio, 12 * this.ratio, true, false);//Back
        }

        this.image = image;
        this.setAreaDueToConfig( 0 * this.ratio,  0 * this.ratio, 32 * this.ratio, 16 * this.ratio);//Head - 1
        this.setAreaTransparent(32 * this.ratio,  0 * this.ratio, 64 * this.ratio, 16 * this.ratio);//Head - 2
        this.setAreaDueToConfig(16 * this.ratio, 16 * this.ratio, 40 * this.ratio, 32 * this.ratio);//Body - 1
        this.setAreaTransparent(16 * this.ratio, 32 * this.ratio, 40 * this.ratio, 48 * this.ratio);//Body - 2

        this.setAreaDueToConfig(40 * this.ratio, 16 * this.ratio, 56 * this.ratio, 32 * this.ratio);//Right Arm - 1
        this.setAreaTransparent(40 * this.ratio, 32 * this.ratio, 56 * this.ratio, 48 * this.ratio);//Right Arm - 2
        this.setAreaDueToConfig( 0 * this.ratio, 16 * this.ratio, 16 * this.ratio, 32 * this.ratio);//Right Leg - 1
        this.setAreaTransparent( 0 * this.ratio, 32 * this.ratio, 16 * this.ratio, 48 * this.ratio);//Right Leg - 2

        this.setAreaDueToConfig(32 * this.ratio, 48 * this.ratio, 48 * this.ratio, 64 * this.ratio);//Left Arm - 1
        this.setAreaTransparent(48 * this.ratio, 48 * this.ratio, 64 * this.ratio, 64 * this.ratio);//Left Arm - 2
        this.setAreaDueToConfig(16 * this.ratio, 48 * this.ratio, 32 * this.ratio, 64 * this.ratio);//Left Leg - 1
        this.setAreaTransparent( 0 * this.ratio, 48 * this.ratio, 16 * this.ratio, 64 * this.ratio);//Left Leg - 2
        return image;
    }

    /**
     * Judge the type of skin
     * Must be called after parseUserSkin
     *
     * @return type of skin (slim / default)
     * @since 14.9
     */
    public String judgeType() {
        if (this.image == null) {
            return null;
        }
        if (((this.image.getRGBA(55, 20) & B) >>> 24) == 0) {//if (55,20) is transparent
            return "slim";
        }
        return "default";
    }

    /* 2^24-1
     * 00000000 11111111 11111111 11111111 */
    private static final int A = 16777215;
    private static final int WHITE = getARGB(255, 255, 255, 255);
    private static final int BLACK = getARGB(255, 0, 0, 0);

    private boolean isFilled(int x0, int y0, int x1, int y1) {
        int data = this.image.getRGBA(x0, y0);
        if (data != WHITE && data != BLACK) {
            return false;
        }
        for (int x = x0; x < x1; ++x) {
            for (int y = y0; y < y1; ++y) {
                if (this.image.getRGBA(x, y) != data) {
                    return false;
                }
            }
        }
        return true;
    }

    private void setAreaTransparent(int x0, int y0, int x1, int y1) {
        if (!isFilled(x0, y0, x1, y1)) {
            return;
        }
        for (int x = x0; x < x1; ++x) {
            for (int y = y0; y < y1; ++y) {
                this.image.setRGBA(x, y, this.image.getRGBA(x,y) & A);
            }
        }
    }

    /* -2^24
     *  00000001 00000000 00000000 00000000 ->
     *  11111110 11111111 11111111 11111111 ->
     *  11111111 00000000 00000000 00000000 */
    private static final int B = -16777216;

    private void setAreaOpaque(int x0, int y0, int x1, int y1) {
        for (int x = x0; x < x1; ++x) {
            for (int y = y0; y < y1; ++y) {
                this.image.setRGBA(x, y, this.image.getRGBA(x,y) | B);
            }
        }
    }

    private void setAreaDueToConfig(int x0, int y0, int x1, int y1) {
        if (CustomSkinLoader.config.enableTransparentSkin) {
            this.setAreaTransparent(x0, y0, x1, y1);
        } else {
            this.setAreaOpaque(x0, y0, x1, y1);
        }
    }

    @Override
    public void method_3238() {
        //A callback when skin loaded, nothing to do
    }

    private static int getARGB(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }
}
