package customskinloader.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import customskinloader.CustomSkinLoader;

public class HttpRequestUtil {
    public static class HttpRequest {
        public String url;
        public String userAgent = null;
        public String payload = null;
        public boolean loadContent = true;
        public boolean checkPNG = false;

        /**
         * Cache Time
         * -1=No Cache  0=Always Cache  t=Default Cache Time(second)
         */
        public int cacheTime = 600;
        public File cacheFile = null;//Default Cache File

        public HttpRequest(String url) {
            this.url = url;
        }

        public HttpRequest setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        public HttpRequest setPayload(String payload) {
            this.payload = payload;
            return this;
        }

        public HttpRequest setLoadContent(boolean loadContent) {
            this.loadContent = loadContent;
            return this;
        }

        public HttpRequest setCheckPNG(boolean checkPNG) {
            this.checkPNG = checkPNG;
            return this;
        }

        public HttpRequest setCacheTime(int cacheTime) {
            this.cacheTime = cacheTime;
            return this;
        }

        public HttpRequest setCacheFile(File cacheFile) {
            this.cacheFile = cacheFile;
            return this;
        }
    }

    public static class HttpResponce {
        public String content = null;
        public int responceCode = -1;
        public boolean success = false;
        public boolean fromCache = false;
    }

    public static class CacheInfo {
        public String url;
        public String etag = null;

        public long lastModified = -1;//ms
        public long expire = -1;//UNIX timestamp
    }

    public static final File CACHE_DIR = new File(CustomSkinLoader.DATA_DIR, "caches");

    public static HttpResponce makeHttpRequest(HttpRequest request) {
        return makeHttpRequest(request, 0);
    }

    public static HttpResponce makeHttpRequest(HttpRequest request, int redirectTime) {
        try {
            if(request.url.contains("{ABORT}")){
                CustomSkinLoader.logger.info("ABORT tag found in url, request has been aborted.");
                return new HttpResponce();
            }
            CustomSkinLoader.logger.debug("Try to request '" + request.url + (request.userAgent == null ? "'." : "' with user agent '" + request.userAgent + "'."));
            //Check Cache
            if (StringUtils.isNotEmpty(request.payload) || CustomSkinLoader.config.forceDisableCache){
                request.cacheTime = -1;//No Cache
            }
            File cacheInfoFile = null;
            CacheInfo cacheInfo = new CacheInfo();
            if (request.cacheFile == null && request.cacheTime >= 0) {
                String hash = DigestUtils.sha1Hex(request.url);
                request.cacheFile = new File(CACHE_DIR, hash);
                cacheInfoFile = new File(CACHE_DIR, hash + ".json");
            }
            if (request.cacheTime == 0 && request.cacheFile.isFile())
                return loadFromCache(request, new HttpResponce());
            if (cacheInfoFile != null && cacheInfoFile.isFile()) {
                String json = FileUtils.readFileToString(cacheInfoFile, "UTF-8");
                if (StringUtils.isNotEmpty(json))
                    cacheInfo = CustomSkinLoader.GSON.fromJson(json, CacheInfo.class);
                if (cacheInfo == null)
                    cacheInfo = new CacheInfo();
                if (cacheInfo.expire >= TimeUtil.getCurrentUnixTimestamp())
                    return loadFromCache(request, new HttpResponce(), cacheInfo.expire);
            }

            //Init Connection
            URL rawUrl = new URL(request.url);
            URI uri = new URI(
                rawUrl.getProtocol(),
                rawUrl.getUserInfo(),
                rawUrl.getHost(),
                rawUrl.getPort(),
                rawUrl.getPath(),
                rawUrl.getQuery(),
                rawUrl.getRef()
            );
            String url = uri.toASCIIString();
            if (!url.equalsIgnoreCase(request.url))
                CustomSkinLoader.logger.debug("Encoded URL: " + url);
            HttpURLConnection c = (HttpURLConnection) (new URL(url)).openConnection();
            c.setReadTimeout(1000 * 10);
            c.setConnectTimeout(1000 * 10);
            c.setDoInput(true);
            c.setUseCaches(false);
            c.setInstanceFollowRedirects(true);

            //Make Connection
            if (cacheInfo.lastModified >= 0)
                c.setIfModifiedSince(cacheInfo.lastModified);
            if (cacheInfo.etag != null)
                c.setRequestProperty("If-None-Match", cacheInfo.etag);
            c.setRequestProperty("Accept-Encoding", "gzip");
            if (request.userAgent != null)
                c.setRequestProperty("User-Agent", request.userAgent);
            if (StringUtils.isNotEmpty(request.payload)) {
                CustomSkinLoader.logger.info("Payload: " + request.payload);
                c.setRequestProperty("Content-Type", "application/json");
                c.setDoOutput(true);
                OutputStream os = c.getOutputStream();
                IOUtils.write(request.payload, os, "UTF-8");
                IOUtils.closeQuietly(os);
            }
            c.connect();

            //Check Connection
            HttpResponce responce = new HttpResponce();
            responce.responceCode = c.getResponseCode();
            int res = c.getResponseCode();
            if (res / 100 == 4 || res / 100 == 5) {//Failed
                CustomSkinLoader.logger.debug("Failed to request (Response Code: " + res + ")");
                return responce;
            }
            if (res == HttpURLConnection.HTTP_MOVED_PERM || res == HttpURLConnection.HTTP_MOVED_TEMP) {
                //Redirect
                if (redirectTime >= 4) {
                    CustomSkinLoader.logger.debug("Failed to request (Too many redirection)");
                    return responce;
                }
                request.url = c.getHeaderField("Location");//Get redirecting location
                if (request.url == null) {
                    CustomSkinLoader.logger.debug("Failed to request (Redirecting location not found)");
                    return responce;
                }
                CustomSkinLoader.logger.debug("Redirect to: " + request.url);
                return makeHttpRequest(request, redirectTime + 1);//Recursion
            }
            responce.success = true;
            CustomSkinLoader.logger.debug("Successfully request (Response Code: " + res + " , Content Length: " + c.getContentLength() + ")");
            if (responce.responceCode == HttpURLConnection.HTTP_NOT_MODIFIED)
                return loadFromCache(request, responce);
            if (responce.responceCode == HttpURLConnection.HTTP_NO_CONTENT)
                request.cacheTime = 3600;

            //Load Content
            InputStream is = "gzip".equals(c.getContentEncoding()) ? new GZIPInputStream(c.getInputStream()) : c.getInputStream();
            byte[] bytes = IOUtils.toByteArray(is);
            if (request.checkPNG && (bytes.length <= 4 || bytes[1] != (byte) 'P' || bytes[2] != (byte) 'N' || bytes[3] != (byte) 'G')) {
                CustomSkinLoader.logger.debug("Failed to request (Not Standard PNG)");
                responce.success = false;
                return responce;
            }
            if (request.cacheFile != null) {
                FileUtils.writeByteArrayToFile(request.cacheFile, bytes);
                if (cacheInfoFile != null) {
                    cacheInfo.url = request.url;
                    cacheInfo.etag = c.getHeaderField("ETag");
                    cacheInfo.lastModified = c.getLastModified();
                    cacheInfo.expire = getExpire(c, request.cacheTime);
                    FileUtils.write(cacheInfoFile, CustomSkinLoader.GSON.toJson(cacheInfo), "UTF-8");
                }
                CustomSkinLoader.logger.debug("Saved to cache (Length: " + request.cacheFile.length() + " , Path: '" + request.cacheFile.getAbsolutePath() + "' , Expire: " + cacheInfo.expire + ")");
            }
            if (!request.loadContent)
                return responce;
            responce.content = new String(bytes, StandardCharsets.UTF_8);
            CustomSkinLoader.logger.debug("Content: " + responce.content);
            return responce;

        } catch (Exception e) {
            CustomSkinLoader.logger.debug("Failed to request " + request.url + " (Exception: " + e.toString() + ")");
            return loadFromCache(request, new HttpResponce());
        }
    }

    public static File getCacheFile(String hash) {
        return new File(CACHE_DIR, hash);
    }

    private static HttpResponce loadFromCache(HttpRequest request, HttpResponce responce) {
        return loadFromCache(request, responce, 0);
    }

    private static HttpResponce loadFromCache(HttpRequest request, HttpResponce responce, long expireTime) {
        if (request.cacheFile == null || !request.cacheFile.isFile())
            return responce;
        CustomSkinLoader.logger.debug("Cache file found (Length: " + request.cacheFile.length() + " , Path: '" + request.cacheFile.getAbsolutePath() + "' , Expire: " + expireTime + ")");
        responce.fromCache = true;
        responce.success = true;
        if (!request.loadContent)
            return responce;

        CustomSkinLoader.logger.info("Try to load from cache '" + request.cacheFile.getAbsolutePath() + "'.");
        try {
            responce.content = FileUtils.readFileToString(request.cacheFile, "UTF-8");
            CustomSkinLoader.logger.debug("Successfully load from cache");
            CustomSkinLoader.logger.debug("Content: " + responce.content);
        } catch (IOException e) {
            CustomSkinLoader.logger.debug("Failed to load from cache (Exception: " + e.toString() + ")");
            responce.success = false;
        }
        return responce;
    }

    private final static Pattern MAX_AGE_PATTERN = Pattern.compile(".*?max-age=(\\d+).*?");

    private static long getExpire(HttpURLConnection connection, int cacheTime) {
        String cacheControl = connection.getHeaderField("Cache-Control");
        if (StringUtils.isNotEmpty(cacheControl)) {
            Matcher m = MAX_AGE_PATTERN.matcher(cacheControl);
            if (m.matches())
                return TimeUtil.getUnixTimestamp(Long.parseLong(m.group(m.groupCount())));
        }
        long expires = connection.getExpiration();
        if (expires > 0)
            return expires / 1000;
        return TimeUtil.getUnixTimestampRandomDelay(cacheTime == 0 ? 2592000 : cacheTime);
    }
}
