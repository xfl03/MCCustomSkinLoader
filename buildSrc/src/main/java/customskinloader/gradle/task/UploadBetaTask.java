package customskinloader.gradle.task;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class UploadBetaTask extends UploadBaseTask {
    @TaskAction
    public void uploadBeta() throws IOException, TencentCloudSDKException {
        uploadBase("latest-beta.json", "detail-beta.json");
    }
}
