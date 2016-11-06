package customskinloader.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
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
	
	public static boolean isLanServer(String address){
		if(StringUtils.isEmpty(address))
			return true;
		String[] addresses=address.split(":");
		InetAddress add;
		try {
			add = InetAddress.getByName(addresses[0]);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return true;
		}
		int numIp=getNumIp(add.getHostAddress());
		return numIp==0||numIp==getNumIp("127.0.0.1")||
				(numIp>=getNumIp("192.168.0.0")&&numIp<=getNumIp("192.168.255.255"))||
				(numIp>=getNumIp("10.0.0.0")&&numIp<=getNumIp("10.255.255.255"))||
				(numIp>=getNumIp("172.16.0.0")&&numIp<=getNumIp("172.31.255.255"));
	}
	public static int getNumIp(String ip){
		int num=0;
		String[] ips=ip.split("\\.");
		if(ips.length!=4)
			return 0;
		for(int i=0;i<4;i++)
			num+=Integer.parseInt(ips[i])*(256^(3-i));
		return num;
	}
}