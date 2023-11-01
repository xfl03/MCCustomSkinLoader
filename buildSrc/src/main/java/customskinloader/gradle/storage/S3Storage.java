package customskinloader.gradle.storage;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.annotation.Nonnull;
import java.nio.file.Path;

/**
 * Amazon Web Service Simple Storage Service
 * There is also plenty of S3 compatible API.
 */
public abstract class S3Storage implements Storage {
    private S3Client client;

    /**
     * The S3 client will be initialized only once.
     * @return S3 client
     */
    protected abstract @Nonnull S3Client initClient();

    private @Nonnull S3Client getClient() {
        if (client != null) {
            return client;
        }
        synchronized (this) {
            if (client != null) {
                return client;
            }
            client = initClient();
            return client;
        }
    }

    /**
     * Get bucket for object.
     * @return bucket
     */
    protected abstract @Nonnull String getBucket();

    @Override
    public void put(String key, Path file) {
        getClient().putObject(
                PutObjectRequest.builder()
                        .bucket(getBucket())
                        .key(key).build(),
                file
        );
    }
}
