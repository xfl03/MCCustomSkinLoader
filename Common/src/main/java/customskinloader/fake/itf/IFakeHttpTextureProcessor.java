package customskinloader.fake.itf;

public interface IFakeHttpTextureProcessor extends Runnable {
    @Override
    default void run() {
        ((FakeHttpTextureProcessor) this).onTextureDownloaded();
    }
}
