package customskinloader.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import javax.net.ssl.SSLSocketFactory;

import org.apache.commons.lang3.StringUtils;

public class HttpUtil0 {
    public static boolean isLocal(String url){
        return url != null && !url.startsWith("http://") && !url.startsWith("https://");
    }
    
    public static String parseAddress(String address) {
        if(StringUtils.isEmpty(address))
            return null;
        String[] addresses=address.split(":");
        InetAddress add;
        try {
            add = InetAddress.getByName(addresses[0]);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
        return add.getHostAddress()+(addresses.length==2 ? addresses[1] : "25565");
    }
    public static boolean isLanServer(String standardAddress){
        if(StringUtils.isEmpty(standardAddress))
            return true;
        String[] addresses=standardAddress.split(":");
        int numIp=getNumIp(addresses[0]);
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

    //https://stackoverflow.com/questions/30817934/extended-server-name-sni-extension-not-sent-with-jdk1-8-0-but-send-with-jdk1-7
    public static class SSLSocketFactoryFacade extends SSLSocketFactory {

        SSLSocketFactory sslsf;

        public SSLSocketFactoryFacade() {
            sslsf = (SSLSocketFactory) SSLSocketFactory.getDefault();;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return sslsf.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return sslsf.getSupportedCipherSuites();
        }

        @Override
        public Socket createSocket(Socket socket, String s, int i, boolean b) throws IOException {
            return sslsf.createSocket(socket, s, i, b);
        }

        @Override
        public Socket createSocket(String s, int i) throws IOException, UnknownHostException {
            return sslsf.createSocket(s, i);
        }

        @Override
        public Socket createSocket(String s, int i, InetAddress inetAddress, int i1) throws IOException, UnknownHostException {
            return sslsf.createSocket(s, i, inetAddress, i1);
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i) throws IOException {
            return createSocket(inetAddress, i);
        }

        @Override
        public Socket createSocket(InetAddress inetAddress, int i, InetAddress inetAddress1, int i1) throws IOException {
            return createSocket(inetAddress, i, inetAddress1, i1);
        }
    }
}
