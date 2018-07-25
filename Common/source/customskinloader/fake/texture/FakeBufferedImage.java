package customskinloader.fake.texture;

import java.awt.*;
import java.awt.image.BufferedImage;

public class FakeBufferedImage implements FakeImage {
    private BufferedImage image;
    private Graphics graphics;

    public FakeBufferedImage(int width, int height) {
        this(new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB));
    }

    public FakeBufferedImage(BufferedImage image) {
        this.image = image;
        graphics = image.getGraphics();
    }

    public BufferedImage getImage() {
        graphics.dispose();
        return image;
    }

    public FakeImage createImage(int width, int height) {
        return new FakeBufferedImage(width, height);
    }

    public int getWidth() {
        return image.getWidth();
    }

    public int getHeight() {
        return image.getHeight();
    }

    public int getRGBA(int x, int y) {
        return image.getRGB(x, y);
    }

    public void setRGBA(int x, int y, int rgba) {
        image.setRGB(x, y, rgba);
    }

    public void copyImageData(FakeImage image) {
        if (!(image instanceof FakeBufferedImage)) return;
        BufferedImage img = ((FakeBufferedImage) image).getImage();
        graphics.drawImage(img, 0, 0, null);
    }

    public void fillArea(int x0, int y0, int width, int height) {
        graphics.setColor(new Color(0, 0, 0, 0));
        graphics.fillRect(x0, y0, width, height);
    }

    public void copyArea(int x0, int y0, int dx, int dy, int width, int height, boolean reversex, boolean reversey) {
        int x1 = x0 + width, x2 = x0 + dx, x3 = x2 + width;
        int y1 = y0 + height, y2 = y0 + dy, y3 = y2 + height;
        graphics.drawImage(image,
                reversex ? x3 : x2, reversey ? y3 : y2,
                reversex ? x2 : x3, reversey ? y2 : y3,
                x0, y0, x1, y1, null);
    }

    public void close() {
        graphics.dispose();
    }
}
