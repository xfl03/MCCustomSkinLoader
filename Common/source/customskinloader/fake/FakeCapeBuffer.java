package customskinloader.fake;

import java.io.IOException;
import java.io.InputStream;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import javax.imageio.ImageIO;

import customskinloader.fake.itf.IFakeThreadDownloadImageData;
import customskinloader.fake.texture.FakeBufferedImage;
import customskinloader.fake.texture.FakeImage;
import customskinloader.utils.MinecraftUtil;
import net.minecraft.util.ResourceLocation;

public class FakeCapeBuffer extends FakeSkinBuffer {
    private static final ResourceLocation TEXTURE_ELYTRA = new ResourceLocation("textures/entity/elytra.png");
    private static int loadedGlobal = 0;
    private static FakeImage elytraImage = loadElytra();

    public static FakeImage loadElytra() {
        loadedGlobal++;
        try {
            InputStream is = MinecraftUtil.getResourceFromResourceLocation(TEXTURE_ELYTRA);
            if (is != null) {
                FakeImage image = new FakeBufferedImage(ImageIO.read(is));
                if (image.getWidth() % 64 != 0 || image.getHeight() % 32 != 0) { // wtf?
                    return elytraImage;
                }
                image = resetImageFormat(image, 22, 0, 46, 22);
                return image;
            }
        } catch (IOException ignored) { }
        return null;
    }

    private int loaded = 0;
    private double ratioX = -1;
    private double ratioY = -1;
    private ResourceLocation location;
    private String type = null;

    public FakeCapeBuffer(ResourceLocation location) {
        this.location = location;
    }

    @Override
    public FakeImage parseUserSkin(FakeImage image) {
        if (image == null) return null;
        this.image = image;

        // When resource packs were changed, the elytra image needs to be reloaded, and here will be entered again
        if (this.loaded == loadedGlobal) {
            elytraImage = loadElytra();
        }
        this.loaded = loadedGlobal;
        if (elytraImage != null) {
            if (this.ratioX < 0)
                this.ratioX = this.image.getWidth() / 64.0D;
            if (this.ratioY < 0)
                this.ratioY = this.image.getHeight() / 32.0D;
            if (this.type == null)
                this.type = this.judgeType();

            if ("cape".equals(this.type)) {
                this.image = resetImageFormat(this.image, 0, 0, 22, 17);
                this.attachElytra(elytraImage);
                if (this.image instanceof FakeBufferedImage) { // before 1.12.2
                    this.refreshTexture((FakeBufferedImage) this.image);
                }
            }
        }
        return this.image;
    }

    /**
     * Judge the cape type
     *
     * @return "elytra" if the cape contains elytra texture, otherwise "cape"
     */
    @Override
    public String judgeType() {
        if (this.image != null && elytraImage != null) {
            // If all the pixels in ((22, 0), (45, 21)) is same as background, it means the cape doesn't contain elytra
            Predicate<Integer> predicate = EQU_BG.apply(this.image.getRGBA(this.image.getWidth() - 1, this.image.getHeight() - 1));
            return withElytraPixels((x, y) -> !predicate.test(this.image.getRGBA(x, y)), "elytra", "cape");
        }
        return "cape";
    }

    private void attachElytra(FakeImage elytraImage) {
        if (this.image != null) {
            int capeW = this.image.getWidth(), capeH = this.image.getHeight();
            int elytraW = elytraImage.getWidth(), elytraH = elytraImage.getHeight();

            // scale cape and elytra to the same size
            // cape part ((0, 0), (21, 16)) -> (22 * 17)
            if (capeW < elytraW) {
                this.image = scaleImage(this.image, true, elytraW / (double) capeW, 1, capeW / 64.0D, capeH / 32.0D, elytraW, capeH, 0, 0, 22, 17);
                capeW = elytraW;
                this.ratioX = capeW / 64.0D;
            }
            if (capeH < elytraH) {
                this.image = scaleImage(this.image, true, 1, elytraH / (double) capeH, capeW / 64.0D, capeH / 32.0D, capeW, elytraH, 0, 0, 22, 17);
                capeH = elytraH;
                this.ratioY = capeH / 32.0D;
            }
            // elytra part ((22, 0), (45, 21)) -> (24 * 22)
            if (elytraW < capeW) {
                elytraImage = scaleImage(elytraImage, false, capeW / (double) elytraW, 1, elytraW / 64.0D, elytraH / 32.0D, capeW, elytraH, 22, 0, 46, 22);
                elytraW = capeW;
            }
            if (elytraH < capeH) {
                elytraImage = scaleImage(elytraImage, false, 1, capeH / (double) elytraH, elytraW / 64.0D, elytraH / 32.0D, elytraW, capeH, 22, 0, 46, 22);
                elytraH = capeH;
            }

            // Overwrite pixels from elytra to cape
            FakeImage finalElytraImage = elytraImage;
            withElytraPixels((x, y) -> {
                this.image.setRGBA(x, y, finalElytraImage.getRGBA(x, y));
                return false;
            }, null, null);
        }
    }

    /**
     * Traverse every elytra pixel
     * @param predicate the predicate with x and y
     * @param returnValue if the condition flag equals the condition, then return this value
     * @param defaultReturnValue otherwise return this value
     */
    private <R> R withElytraPixels(BiPredicate<Integer, Integer> predicate, R returnValue, R defaultReturnValue) {
        int startX = (int) Math.ceil(22 * ratioX), endX = (int) Math.ceil(46 * ratioX);
        int startY = (int) Math.ceil(0  * ratioY), endY = (int) Math.ceil(22 * ratioY);
        int excludeX0 = (int) Math.ceil(24 * ratioX), excludeX1 = (int) Math.ceil(44 * ratioX);
        int excludeY  = (int) Math.ceil(2  * ratioY);
        for (int x = startX; x < endX; ++x) {
            for (int y = startY; y < endY; ++y) {
                if (y < excludeY && (x < excludeX0 || x >= excludeX1)) continue;
                if (predicate.test(x, y)) {
                    return returnValue;
                }
            }
        }
        return defaultReturnValue;
    }

    // TextureID won't be regenerated when changing resource packs before 1.12.2
    private void refreshTexture(FakeBufferedImage image) {
        Object textureObj = MinecraftUtil.getTextureManager().getTexture(this.location);
        if (textureObj instanceof IFakeThreadDownloadImageData) {
            // NOTICE: OptiFine modified the upload process of the texture from ThreadDownloadImageData
            // Therefore, it may not be correct to simply copy the vanilla behavior
            ((IFakeThreadDownloadImageData) textureObj).resetNewBufferedImage(image.getImage());
        }
    }

    // Some cape image doesn't support alpha channel, so reset image format to ARGB
    private static FakeImage resetImageFormat(FakeImage image, int startX, int startY, int endX, int endY) {
        if (image != null) {
            int width = image.getWidth(), height = image.getHeight();
            image = scaleImage(image, true, 1, 1, width / 64.0D, height / 32.0D, width, height, startX, startY, endX, endY);
        }
        return image;
    }

    /**
     * Scale image
     * @param image the image to scale.
     * @param closeOldImage whether close old image
     * @param scaleWidth width enlargement ratio
     * @param scaleHeight height enlargement ratio
     * @param ratioX the ratio of 64 of the old image width
     * @param ratioY the ratio of 32 of the old image height
     * @param width the width after scaling.
     * @param height the height after scaling.
     * @param startX the x where start to copy.
     * @param startY the y where start to copy.
     * @param endX the x where end to copy.
     * @param endY the y where end to copy.
     * @return the image after scaling.
     */
    private static FakeImage scaleImage(FakeImage image, boolean closeOldImage, double scaleWidth, double scaleHeight, double ratioX, double ratioY, int width, int height, int startX, int startY, int endX, int endY) {
        FakeImage newImage = image.createImage(width, height);
        startX = (int) (startX * ratioX); endX = (int) (endX * ratioX);
        startY = (int) (startY * ratioY); endY = (int) (endY * ratioY);

        int x0 = (int) (startX * scaleWidth), x1 = (int) ((startX + 1) * scaleWidth), dx0 = x1 - x0;
        for (int x = startX; x < endX; ++x) {
            int y0 = (int) (startY * scaleHeight), y1 = (int) ((startY + 1) * scaleHeight), dy0 = y1 - y0;
            for (int y = startY; y < endY; ++y) {
                int rgba = image.getRGBA(x, y);
                for (int dx = 0; dx < dx0; dx++) {
                    for (int dy = 0; dy < dy0; dy++) {
                        newImage.setRGBA(x0 + dx, y0 + dy, rgba);
                    }
                }
                y0 = y1; y1 = (int) ((y + 2) * scaleHeight); dy0 = y1 - y0;
            }
            x0 = x1; x1 = (int) ((x + 2) * scaleWidth); dx0 = x1 - x0;
        }
        if (closeOldImage)
            image.close();
        return newImage;
    }
}
