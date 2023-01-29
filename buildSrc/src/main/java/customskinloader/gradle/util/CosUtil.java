package customskinloader.gradle.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class CosUtil {
    public static String getKey(String filename) {
        String name = filename.substring(0, filename.lastIndexOf('.'));
        if (name.indexOf('-') == -1) {
            return null;
        }
        if (filename.endsWith(".jar")) {
            if (filename.contains("Fabric") || filename.contains("Forge")) {
                return String.format(
                        "mods/%s",
                        filename
                );
            }
        }
        return filename;
    }

    private static final String BUCKET_NAME = System.getenv("COS_BUCKET");
    private static COSClient cosClient0 = null;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static COSClient getCosClient() {
        if (cosClient0 != null) {
            return cosClient0;
        }

        COSCredentials cred = new BasicCOSCredentials(
                System.getenv("COS_SECRET_ID"),
                System.getenv("COS_SECRET_KEY")
        );
        ClientConfig clientConfig = new ClientConfig(new Region("ap-shanghai"));
        cosClient0 = new COSClient(cred, clientConfig);

        return cosClient0;
    }

    public static void uploadFile(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, key, file);
        getCosClient().putObject(putObjectRequest);
    }

    public static void writeAndUploadObject(String filename, Object obj) throws IOException {
        File file = new File("build/libs/" + filename);
        FileUtils.write(file, gson.toJson(obj), StandardCharsets.UTF_8);
        CosUtil.uploadFile(filename, file);
    }

    public static void downloadFile(String key, File file) {
        GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET_NAME, key);
        getCosClient().getObject(getObjectRequest, file);
    }
}
