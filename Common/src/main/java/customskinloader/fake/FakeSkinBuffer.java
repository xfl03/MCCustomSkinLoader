package customskinloader.fake;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.function.Function;
import java.util.function.Predicate;

import customskinloader.CustomSkinLoader;
import customskinloader.fake.itf.FakeInterfaceManager;
import customskinloader.fake.texture.FakeBufferedImage;
import customskinloader.fake.texture.FakeImage;
import customskinloader.fake.texture.FakeNativeImage;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;

public class FakeSkinBuffer implements IImageBuffer {
    FakeImage image = null;

    /**
     * 24w46a+
     * Invoked from {@link SkinTextureDownloader#lambda$downloadAndRegisterSkin$0(Path, String, boolean)}
     */
    public static NativeImage processLegacySkin(NativeImage image, String url) {
        if (image == null)
            return null;

        FakeImage img = parseUserSkin0(new FakeNativeImage(image));
        if (img instanceof FakeNativeImage) {
            image = ((FakeNativeImage) img).getImage();
            FakeInterfaceManager.NativeImage_setFakeImage(image, img);
            return image;
        }

        CustomSkinLoader.logger.warning("Failed to parseUserSkin(downloadAndRegisterSkin).");
        return null;
    }

    /**
     * 19w38a ~ 24w45a
     * Invoked from {@link ThreadDownloadImageData#loadTexture(InputStream)}
     */
    public static NativeImage processLegacySkin(NativeImage image, Runnable processTask, Function<NativeImage, NativeImage> processLegacySkin) {
        if (processTask instanceof IImageBuffer) {
            return ((IImageBuffer) processTask).func_195786_a(image);
        } else if (processLegacySkin != null) {
            return processLegacySkin.apply(image);
        } else {
            return image;
        }
    }

    //parseUserSkin for 1.13+
    public NativeImage func_195786_a(NativeImage image) {
        if (image == null)
            return null;

        FakeImage img = parseUserSkin(new FakeNativeImage(image));
        if (img instanceof FakeNativeImage)
            return ((FakeNativeImage) img).getImage();

        CustomSkinLoader.logger.warning("Failed to parseUserSkin(func_195786_a).");
        return null;
    }

    //parseUserSkin for 1.12.2-
    public BufferedImage parseUserSkin(BufferedImage image) {
        if (image == null)
            return null;

        FakeImage img = parseUserSkin(new FakeBufferedImage(image));
        if (img instanceof FakeBufferedImage)
            return ((FakeBufferedImage) img).getImage();

        CustomSkinLoader.logger.warning("Failed to parseUserSkin.");
        return null;
    }

    public FakeImage parseUserSkin(FakeImage image) {
        return this.image = parseUserSkin0(image);
    }

    public static FakeImage parseUserSkin0(FakeImage image) {
        if (image == null) return null;
        int ratio = image.getWidth() / 64;

        if (image.getHeight() != image.getWidth()) {//Single Layer
            //Create a new image and copy origin image data
            FakeImage img = image.createImage(64 * ratio, 64 * ratio);
            img.copyImageData(image);
            image.close();
            image = img;
            image.fillArea(0 * ratio, 32 * ratio, 64 * ratio, 32 * ratio);

            //Right Leg -> Left Leg
            image.copyArea(4 * ratio, 16 * ratio, 16 * ratio, 32 * ratio, 4 * ratio, 4 * ratio, true, false);//Top
            image.copyArea(8 * ratio, 16 * ratio, 16 * ratio, 32 * ratio, 4 * ratio, 4 * ratio, true, false);//Bottom
            image.copyArea(0 * ratio, 20 * ratio, 24 * ratio, 32 * ratio, 4 * ratio, 12 * ratio, true, false);//Right
            image.copyArea(4 * ratio, 20 * ratio, 16 * ratio, 32 * ratio, 4 * ratio, 12 * ratio, true, false);//Front
            image.copyArea(8 * ratio, 20 * ratio, 8 * ratio, 32 * ratio, 4 * ratio, 12 * ratio, true, false);//Left
            image.copyArea(12 * ratio, 20 * ratio, 16 * ratio, 32 * ratio, 4 * ratio, 12 * ratio, true, false);//Back
            //Right Arm -> Left Arm
            image.copyArea(44 * ratio, 16 * ratio, -8 * ratio, 32 * ratio, 4 * ratio, 4 * ratio, true, false);//Top
            image.copyArea(48 * ratio, 16 * ratio, -8 * ratio, 32 * ratio, 4 * ratio, 4 * ratio, true, false);//Bottom
            image.copyArea(40 * ratio, 20 * ratio, 0 * ratio, 32 * ratio, 4 * ratio, 12 * ratio, true, false);//Right
            image.copyArea(44 * ratio, 20 * ratio, -8 * ratio, 32 * ratio, 4 * ratio, 12 * ratio, true, false);//Front
            image.copyArea(48 * ratio, 20 * ratio, -16 * ratio, 32 * ratio, 4 * ratio, 12 * ratio, true, false);//Left
            image.copyArea(52 * ratio, 20 * ratio, -8 * ratio, 32 * ratio, 4 * ratio, 12 * ratio, true, false);//Back
        }

        setAreaDueToConfig(image, 0 * ratio, 0 * ratio, 32 * ratio, 16 * ratio);//Head - 1
        setAreaTransparent(image, 32 * ratio, 0 * ratio, 64 * ratio, 16 * ratio);//Head - 2
        setAreaDueToConfig(image, 16 * ratio, 16 * ratio, 40 * ratio, 32 * ratio);//Body - 1
        setAreaTransparent(image, 16 * ratio, 32 * ratio, 40 * ratio, 48 * ratio);//Body - 2

        setAreaDueToConfig(image, 40 * ratio, 16 * ratio, 56 * ratio, 32 * ratio);//Right Arm - 1
        setAreaTransparent(image, 40 * ratio, 32 * ratio, 56 * ratio, 48 * ratio);//Right Arm - 2
        setAreaDueToConfig(image, 0 * ratio, 16 * ratio, 16 * ratio, 32 * ratio);//Right Leg - 1
        setAreaTransparent(image, 0 * ratio, 32 * ratio, 16 * ratio, 48 * ratio);//Right Leg - 2

        setAreaDueToConfig(image, 32 * ratio, 48 * ratio, 48 * ratio, 64 * ratio);//Left Arm - 1
        setAreaTransparent(image, 48 * ratio, 48 * ratio, 64 * ratio, 64 * ratio);//Left Arm - 2
        setAreaDueToConfig(image, 16 * ratio, 48 * ratio, 32 * ratio, 64 * ratio);//Left Leg - 1
        setAreaTransparent(image, 0 * ratio, 48 * ratio, 16 * ratio, 64 * ratio);//Left Leg - 2

        image.setRatio(ratio);
        return image;
    }

    /** Judge if the color is transparent or same with background */
    static final Function<Integer, Predicate<Integer>> EQU_BG = bgColor -> getA(bgColor) == 0 ? (c) -> getA(c) == 0 : (c) -> c.equals(bgColor);

    public String judgeType() {
        return judgeType0(this.image);
    }

    /**
     * Judge the type of skin
     * Must be called after parseUserSkin
     *
     * @return type of skin (slim / default)
     * @since 14.9
     */
    public static String judgeType0(FakeImage image) {
        if (image == null)
            return "default";
        int ratio = image.getRatio();
        int bgColor = image.getRGBA(63 * ratio, 20 * ratio);
        /*
         * If background is transparent, all the pixels in ((54, 20), (55, 31)) areas is transparent,
         * which means it is Alex model.
         * If background is opaque, all the pixels in ((54, 20), (55, 31)) areas is same with background,
         * which means it is Alex model.
         * Otherwise, it is Steve model.
         * */
        Predicate<Integer> predicate = EQU_BG.apply(bgColor);
        for (int x = 54 * ratio; x <= 55 * ratio; ++x) {
            for (int y = 20 * ratio; y <= 31 * ratio; ++y) {
                int color = image.getRGBA(x, y);
                if (!predicate.test(color)) {
                    return "default";
                }
            }
        }
        return "slim";
    }

    /* 2^24-1
     * 00000000 11111111 11111111 11111111 */
    private static final int A = 16777215;
    private static final int WHITE = getARGB(255, 255, 255, 255);
    private static final int BLACK = getARGB(255, 0, 0, 0);

    private static boolean isFilled(FakeImage image, int x0, int y0, int x1, int y1) {
        int data = image.getRGBA(x0, y0);
        if (data != WHITE && data != BLACK)
            return false;
        for (int x = x0; x < x1; ++x)
            for (int y = y0; y < y1; ++y)
                if (image.getRGBA(x, y) != data)
                    return false;
        return true;
    }

    private static void setAreaTransparent(FakeImage image, int x0, int y0, int x1, int y1) {
        if (!isFilled(image, x0, y0, x1, y1))
            return;
        for (int x = x0; x < x1; ++x)
            for (int y = y0; y < y1; ++y)
                image.setRGBA(x, y, image.getRGBA(x, y) & A);
    }

    /* -2^24
     *  00000001 00000000 00000000 00000000 ->
     *  11111110 11111111 11111111 11111111 ->
     *  11111111 00000000 00000000 00000000 */
    private static final int B = -16777216;

    private static void setAreaOpaque(FakeImage image, int x0, int y0, int x1, int y1) {
        for (int x = x0; x < x1; ++x)
            for (int y = y0; y < y1; ++y)
                image.setRGBA(x, y, image.getRGBA(x, y) | B);
    }

    private static void setAreaDueToConfig(FakeImage image, int x0, int y0, int x1, int y1) {
        if (customskinloader.CustomSkinLoader.config.enableTransparentSkin)
            setAreaTransparent(image, x0, y0, x1, y1);
        else
            setAreaOpaque(image, x0, y0, x1, y1);
    }

    public void skinAvailable() {
        //A callback when skin loaded, nothing to do
    }

    private static int getARGB(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int getA(int argb) {
        return (argb & B) >>> 24;
    }
}
