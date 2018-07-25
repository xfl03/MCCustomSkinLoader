package customskinloader.fake.texture;

public interface FakeImage {
    FakeImage createImage(int width, int height);

    int getWidth();

    int getHeight();

    int getRGBA(int x, int y);

    void setRGBA(int x, int y, int rgba);

    void copyImageData(FakeImage image);

    void fillArea(int x0, int y0, int width, int height);

    void copyArea(int x0, int y0, int dx, int dy, int width, int height, boolean reversex, boolean reversey);

    void close();
}
