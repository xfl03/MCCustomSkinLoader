package customskinloader.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;

import customskinloader.CustomSkinLoader;

public class HttpUtil0 {
	/**
	 * Read all text from http.
	 */
	public static String readHttp(String url){
		try {
        	CustomSkinLoader.logger.info("Try to read '"+url+"'");
        	HttpURLConnection c=createConnection(url);
            int res = c.getResponseCode()/100;
            if (res==4||res==5||c.getResponseCode()==HttpURLConnection.HTTP_NO_CONTENT) {
            	CustomSkinLoader.logger.info("Failed to read (Response Code: "+c.getResponseCode()+")");
                return null;
            }
            CustomSkinLoader.logger.info("Successfully read (Response Code: "+c.getResponseCode()+" , Content Length: "+c.getContentLength()+")");
            InputStream is = c.getInputStream();
            return IOUtils.toString(is, Charsets.UTF_8);
        } catch (Exception e) {
        	CustomSkinLoader.logger.info("Failed to read (Exception: "+e.getMessage()+")");
            return null;
        }
	}
	
	/**
	 * Get url after redirect.
	 * Using this method, can also check if it is exist.
	 * @param url - before redirect
	 * @return url - after redirect (null for not exist)
	 */
	public static String getRealUrl(String url){
		try {
        	CustomSkinLoader.logger.info("Try to get real url of '"+url+"'.");
        	HttpURLConnection c=createConnection(url);
            int res = c.getResponseCode()/100;
            if (res==4||res==5) {
            	CustomSkinLoader.logger.info("Failed to get real url (Response Code: "+c.getResponseCode()+")");
                return null;
            }
            CustomSkinLoader.logger.info("Successfully get real url ("
            		+ "Response Code: "+c.getResponseCode()+" , "
            		+ "Content Length: "+c.getContentLength()+" , "
            		+ "Real URL: "+c.getURL().toString()+")");
            return c.getURL().toString();
        } catch (Exception e) {
        	CustomSkinLoader.logger.info("Failed to get real url (Exception: "+e.getMessage()+")");
            return null;
        }
	}
	
	private static HttpURLConnection createConnection(String url) throws MalformedURLException, IOException{
		HttpURLConnection c = (HttpURLConnection) (new URL(url)).openConnection();
        c.setReadTimeout(1000 * 10);
        c.setConnectTimeout(1000 * 10);
        c.setUseCaches(false);
        c.setInstanceFollowRedirects(true);
        c.connect();
        return c;
	}
}
