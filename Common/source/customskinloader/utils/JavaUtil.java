package customskinloader.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;

public class JavaUtil {
    public static URL[] getClasspath() {
        if (JavaUtil.class.getClassLoader() instanceof URLClassLoader) {
            //Java 8-
            //Use URLClassLoader Directly
            URLClassLoader ucl = (URLClassLoader) JavaUtil.class.getClassLoader();
            return ucl.getURLs();
        } else {
            //Java 9+
            //It uses APPClassLoader instead of URLClassLoader
            //Get Classpath from properties and parse it to URL array
            String classpath = System.getProperty("java.class.path");
            String[] elements = classpath.split(File.pathSeparator);
            if (elements.length == 0)
                return new URL[0];
            LinkedList<URL> urls = new LinkedList<URL>();
            for (String ele : elements) {
                try {
                    urls.add(new File(ele).toURI().toURL());
                } catch (Exception ignored) {
                }
            }
            return urls.toArray(new URL[0]);
        }
    }
}
