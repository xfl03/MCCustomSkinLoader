package customskinloader.gradle.util;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;

import java.io.File;

public class CosUtil {
    public static String getKey(String filename) {
        String name = filename.substring(0, filename.lastIndexOf('.'));
        if (name.indexOf('-') == -1) {
            return null;
        }
        if (filename.endsWith(".json"))
            return String.format(
                    "versions/%s/%s",
                    name,
                    filename
            );
        if (filename.endsWith(".jar")) {
            if (filename.contains("Fabric") || filename.contains("Forge")) {
                return String.format(
                        "mods/%s",
                        filename
                );
            } else {
                return String.format(
                        "libraries/customskinloader/%s/%s/%s",
                        name.substring(0, name.indexOf('-')),
                        name.substring(name.indexOf('-') + 1),
                        filename
                );
            }
        }
        return filename;
    }
    private static final String bucketName = System.getenv("COS_BUCKET");
    public static final String cosUrl = "https://csl.littleservice.cn/";
    private static COSClient cosClient0 = null;

    private static COSClient getCosClient() {
        if (cosClient0 != null) {
            return cosClient0;
        }

        COSCredentials cred = new BasicCOSCredentials(System.getenv("COS_SECRET_ID"),
                System.getenv("COS_SECRET_KEY"));
        ClientConfig clientConfig = new ClientConfig(new Region("ap-shanghai"));
        cosClient0 = new COSClient(cred, clientConfig);

        return cosClient0;
    }

    public static void uploadFile(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, file);
        getCosClient().putObject(putObjectRequest);
    }

    public static void downloadFile(String key, File file) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(bucketName, key);
        getCosClient().getObject(getObjectRequest, file);
    }
}
