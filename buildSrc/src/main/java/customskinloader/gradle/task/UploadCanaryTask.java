package customskinloader.gradle.task;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class UploadCanaryTask extends UploadTask {
    @TaskAction
    public void uploadCanary() throws IOException, TencentCloudSDKException {
        uploadBase("latest-canary.json","detail-canary.json");
    }
}
