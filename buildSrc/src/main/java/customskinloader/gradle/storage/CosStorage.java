package customskinloader.gradle.storage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import customskinloader.gradle.util.CdnUtil;

import java.nio.file.Path;

/**
 * Tencent Cloud COS
 */
public class CosStorage implements Storage {
    private static final String BUCKET_NAME = System.getenv("COS_BUCKET");
    private COSClient cosClient = null;

    private COSClient getCosClient() {
        if (cosClient != null) {
            return cosClient;
        }

        synchronized (this) {
            if (cosClient != null) {
                return cosClient;
            }

            COSCredentials cred = new BasicCOSCredentials(
                    System.getenv("COS_SECRET_ID"),
                    System.getenv("COS_SECRET_KEY")
            );
            ClientConfig clientConfig = new ClientConfig(new Region("ap-shanghai"));
            cosClient = new COSClient(cred, clientConfig);

            return cosClient;
        }
    }

    @Override
    public void put(String key, Path file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, key, file.toFile());
        getCosClient().putObject(putObjectRequest);
    }

    @Override
    public String getPublicBaseUrl() {
        return CdnUtil.TENCENT_CDN_ROOT;
    }
}
