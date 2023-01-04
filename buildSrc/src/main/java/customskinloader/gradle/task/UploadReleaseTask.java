package customskinloader.gradle.task;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

public class UploadReleaseTask extends UploadBaseTask{
    @TaskAction
    public void upload() throws IOException, TencentCloudSDKException {
        uploadBase("latest.json","detail.json");
    }
}
