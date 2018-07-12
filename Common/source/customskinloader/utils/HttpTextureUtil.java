package customskinloader.utils;

import java.io.File;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;

import customskinloader.CustomSkinLoader;

public class HttpTextureUtil {
    public static class HttpTextureInfo{
        public String url="";
        public File cacheFile;
        public String hash;
    }
    private static final String 
            LEGACY_MARK="(LEGACY)",
            LOCAL_MARK="(LOCAL)",
            LOCAL_LEGACY_MARK="(LOCAL_LEGACY)";
    public static File defaultCacheDir;
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void cleanCacheDir(){
        if(defaultCacheDir!=null){
            defaultCacheDir.delete();
            defaultCacheDir.mkdirs();
        }
    }
    public static File getCacheDir(){
        return defaultCacheDir==null?new File(MinecraftUtil.getMinecraftDataDir(),"assets/skins"):defaultCacheDir;
    }
    
    public static HttpTextureInfo toHttpTextureInfo(String fakeUrl){
        HttpTextureInfo info=new HttpTextureInfo();
        if(fakeUrl.startsWith("http")){
            info.url=fakeUrl;
            info.hash=FilenameUtils.getBaseName(fakeUrl);
            info.cacheFile=getCacheFile(info.hash);
            return info;
        }
        if(fakeUrl.startsWith(LOCAL_LEGACY_MARK)){
            fakeUrl=fakeUrl.replace(LOCAL_LEGACY_MARK, "");
            String[] t=fakeUrl.split(",",2);
            if(t.length!=2)
                return info;
            info.cacheFile=new File(CustomSkinLoader.DATA_DIR,t[1]);
            info.hash=t[0];
            return info;
        }
        if(fakeUrl.startsWith(LOCAL_MARK)){
            fakeUrl=fakeUrl.replace(LOCAL_MARK, "");
            info.cacheFile=new File(CustomSkinLoader.DATA_DIR,fakeUrl);
            info.hash=FilenameUtils.getBaseName(fakeUrl);
            return info;
        }
        if(fakeUrl.startsWith(LEGACY_MARK)){
            fakeUrl=fakeUrl.replace(LEGACY_MARK, "");
            info.url=fakeUrl;
            info.hash=DigestUtils.sha1Hex(info.url);
            info.cacheFile = HttpRequestUtil.getCacheFile(info.hash);
            return info;
        }
        return info;
    }

    public static String getLegacyFakeUrl(String url){
        return LEGACY_MARK+url;
    }
    public static String getLocalFakeUrl(String path){
        return LOCAL_MARK+path;
    }
    public static String getLocalLegacyFakeUrl(String path,String hash){
        return LOCAL_LEGACY_MARK+hash+","+path;
    }
    public static String getHash(String url,long size,long lastModified){
        return DigestUtils.sha1Hex(size+url+lastModified);
    }
    public static File getCacheFile(String hash){
        return getCacheFile(defaultCacheDir,hash);
    }
    public static File getCacheFile(File cacheDir,String hash){
        return new File(new File(cacheDir,hash.length()>2?hash.substring(0,2):"xx"), hash);
    }    
}
