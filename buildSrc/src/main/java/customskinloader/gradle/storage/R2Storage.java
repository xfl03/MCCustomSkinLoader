package customskinloader.gradle.storage;

import customskinloader.gradle.util.CdnUtil;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import javax.annotation.Nonnull;
import java.net.URI;

/**
 * CloudFlare R2
 */
public class R2Storage extends S3Storage {
    @Nonnull
    @Override
    protected S3Client initClient() {
        try {
            return S3Client.builder()
                    .endpointOverride(new URI(System.getenv("R2_BASE_URL")))
                    .credentialsProvider(
                            StaticCredentialsProvider.create(
                                    AwsBasicCredentials.create(
                                            System.getenv("R2_SECRET_ID"),
                                            System.getenv("R2_SECRET_KEY"))))
                    .region(Region.of("auto"))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nonnull
    @Override
    protected String getBucket() {
        return System.getenv("R2_BUCKET");
    }

    @Override
    public String getPublicBaseUrl() {
        return CdnUtil.CLOUDFLARE_CDN_ROOT;
    }
}
