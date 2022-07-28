package customskinloader.gradle.util;

import com.tencentcloudapi.cdn.v20180606.CdnClient;
import com.tencentcloudapi.cdn.v20180606.models.PurgeUrlsCacheRequest;
import com.tencentcloudapi.cdn.v20180606.models.PushUrlsCacheRequest;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

import java.util.Arrays;

public class CdnUtil {
    public static final String CDN_ROOT = "https://csl.littleservice.cn/";
    private static CdnClient cdnClient;

    private static CdnClient getCdnClient() {
        //Check if CdnClient has been created
        if (cdnClient != null) {
            return cdnClient;
        }

        //Create CdnClient
        Credential credential = new Credential(
                System.getenv("COS_SECRET_ID"),
                System.getenv("COS_SECRET_KEY")
        );
        cdnClient = new CdnClient(credential, "");

        return cdnClient;
    }

    /**
     * CDN has cache. When updating file, we should update CDN cache.
     * @param paths what to update
     * @throws TencentCloudSDKException when Tencent Cloud API has exception
     */
    public static void updateCdn(String... paths) throws TencentCloudSDKException {
        String[] urls = Arrays.stream(paths).map(it -> CDN_ROOT + it).toArray(String[]::new);

        //Purge CDN cache
        PurgeUrlsCacheRequest purgeReq = new PurgeUrlsCacheRequest();
        purgeReq.setUrls(urls);
        getCdnClient().PurgeUrlsCache(purgeReq);

        //Push to CDN cache
        PushUrlsCacheRequest pushReq = new PushUrlsCacheRequest();
        pushReq.setUrls(urls);
        getCdnClient().PushUrlsCache(pushReq);
    }
}
