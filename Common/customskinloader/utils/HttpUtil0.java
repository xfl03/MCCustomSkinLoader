package customskinloader.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import customskinloader.CustomSkinLoader;

public class HttpUtil0 {
	/**
	 * Read all text from http.
	 * @since 13.11
	 */
	public static String readHttp(String url,String userAgent){
		return readHttp(url,userAgent,null);
	}
	public static String readHttp(String url,String userAgent,String payload){
		HttpURLConnection c=null;
		try {
			CustomSkinLoader.logger.info("Try to read '"+url+(userAgent==null?"' without user agent.":"' with user agent '"+userAgent+"'."));
			c=createConnection(url,userAgent,payload);
			int res = c.getResponseCode()/100;
			if (res==4||res==5||c.getResponseCode()==HttpURLConnection.HTTP_NO_CONTENT) {
				CustomSkinLoader.logger.info("Failed to read (Response Code: "+c.getResponseCode()+")");
				return null;
			}
			CustomSkinLoader.logger.info("Successfully read (Response Code: "+c.getResponseCode()+" , Content Length: "+c.getContentLength()+")");
			InputStream is = getStream(c);
			String text=IOUtils.toString(is, Charsets.UTF_8);
			CustomSkinLoader.logger.info("Content: "+text);
			return text;
		} catch (Exception e) {
			CustomSkinLoader.logger.info("Failed to read (Exception: "+e.toString()+")");
			return null;
		} finally {
			if(c!=null)
				c.disconnect();
		}
	}
	
	public static boolean saveHttp(String url,File target){
		HttpURLConnection c=null;
		try {
			CustomSkinLoader.logger.info("Try to save '"+url+"' to '"+target.getAbsolutePath()+"'.");
			if(target.isFile()){
				CustomSkinLoader.logger.info("Cache file found (Length: "+target.length()+")");
				return true;
			}
			c=createConnection(url,null);
			int res = c.getResponseCode()/100;
			if (res==4||res==5||c.getResponseCode()==HttpURLConnection.HTTP_NO_CONTENT) {
				CustomSkinLoader.logger.info("Failed to save (Response Code: "+c.getResponseCode()+")");
				return false;
			}
			CustomSkinLoader.logger.info("Successfully save (Response Code: "+c.getResponseCode()+" , Content Length: "+c.getContentLength()+")");
			InputStream is = getStream(c);
			byte [] bytes = IOUtils.toByteArray(is);
			FileUtils.writeByteArrayToFile(target,bytes);
			return target.exists();
		} catch (Exception e) {
			CustomSkinLoader.logger.info("Failed to save (Exception: "+e.toString()+")");
			return false;
		} finally {
			if(c!=null)
				c.disconnect();
		}
	}
	
	/**
	 * Get faked url after redirect.
	 * Using this method, can also check if it is exist.
	 * @param url - before redirect
	 * @return url - faked url after redirect (null for not exist)
	 * @since 14.1
	 */
	public static String getFakeUrl(String url,String userAgent){
		HttpURLConnection c=null;
		try {
			CustomSkinLoader.logger.info("Try to get fake url of '"+url+"' with user agent '"+userAgent+"'.");
			c=createConnection(url,userAgent);
			int res = c.getResponseCode()/100;
			if (res==4||res==5) {
				CustomSkinLoader.logger.info("Failed to get fake url (Response Code: "+c.getResponseCode()+")");
				return null;
			}
			String url1=c.getURL().toString();
			CustomSkinLoader.logger.info("Successfully get fake url ("
					+ "Response Code: "+c.getResponseCode()+" , "
					+ "Content Length: "+c.getContentLength()+" , "
					+ "Real URL: "+url1+")");
			return HttpTextureUtil.getLegacyFakeUrl(url1, HttpTextureUtil.getHash(url1, c.getContentLength(), c.getLastModified()));
		} catch (Exception e) {
			CustomSkinLoader.logger.info("Failed to get fake url (Exception: "+e.toString()+")");
			return null;
		} finally{
			if(c!=null)
				c.disconnect();
		}
	}
	private static HttpURLConnection createConnection(String url,String userAgent) throws MalformedURLException, IOException{
		return createConnection(url,userAgent,null);
	}
	private static HttpURLConnection createConnection(String url,String userAgent,String payload) throws MalformedURLException, IOException{
		HttpURLConnection c = (HttpURLConnection) (new URL(url)).openConnection();
		c.setReadTimeout(1000 * 10);
		c.setConnectTimeout(1000 * 10);
		c.setDoInput(true);
		c.setUseCaches(false);
		c.setInstanceFollowRedirects(true);
		
		c.setRequestProperty("Accept-Encoding", "gzip");
		if(userAgent!=null)
			c.setRequestProperty("User-Agent",userAgent);
		if(StringUtils.isNotEmpty(payload)){
			CustomSkinLoader.logger.info("Payload: "+payload);
			c.setDoOutput(true);
			OutputStream os=c.getOutputStream();
			IOUtils.write(payload, os);
			IOUtils.closeQuietly(os);
		}
		c.connect();
		return c;
	}
	private static InputStream getStream(HttpURLConnection connection) throws IOException{
		if ("gzip".equals(connection.getContentEncoding()))
			return new GZIPInputStream(connection.getInputStream());
		else
			return connection.getInputStream();
	}
	
	public static boolean isLocal(String url){
		return url==null? false : !url.startsWith("http");
	}
	
	//From: http://blog.csdn.net/xiyushiyi/article/details/46685387
	public static void ignoreHttpsCertificate(){
		HostnameVerifier doNotVerify = new HostnameVerifier() {

			public boolean verify(String hostname, SSLSession session) {
				return true;
			}
		};
		TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
			public X509Certificate[] getAcceptedIssuers() {
				return new X509Certificate[] {};
			}
			public void checkClientTrusted(X509Certificate[] chain, String authType){}
			public void checkServerTrusted(X509Certificate[] chain, String authType){}
		}};

		try {
			SSLContext sc = SSLContext.getInstance("TLS");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());
			HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(doNotVerify);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
