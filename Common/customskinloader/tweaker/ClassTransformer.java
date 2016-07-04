package customskinloader.tweaker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.IOUtils;

import net.minecraft.launchwrapper.IClassTransformer;

public class ClassTransformer implements IClassTransformer {

	private ZipFile zipFile = null;
	private ArrayList<String> classes=new ArrayList<String>();

	public ClassTransformer() {
		ForgeTweaker.logger.info("ClassTransformer Begin");
		try {
			URLClassLoader ucl = (URLClassLoader)this.getClass().getClassLoader();
			URL urls[] = ucl.getURLs();
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];
				ZipFile tempZipFile = getZipFile(url);
				if (tempZipFile.getEntry("customskinloader/tweaker/ClassTransformer.class") == null){
					tempZipFile.close();
					continue;
				}
				zipFile = tempZipFile;
				Enumeration<ZipEntry> entries=(Enumeration<ZipEntry>) zipFile.entries();
				StringBuilder sb=new StringBuilder();
				while(entries.hasMoreElements()){
					ZipEntry entry=entries.nextElement();
					String name=entry.getName();
					if(name.endsWith(".class")&&!name.contains("/")){
						classes.add(name);
						sb.append(" ");
						sb.append(name);
					}
				}
				ForgeTweaker.logger.info("Jar File URL: " + url);
				ForgeTweaker.logger.info("Classes:" + sb.toString());
				break;
			}
		} catch (Exception e) {
			ForgeTweaker.logger.warning(e);
		}
		if (zipFile == null) {
			ForgeTweaker.logger.info("Can not find JAR in the classpath.");
		}
	}

	private static ZipFile getZipFile(URL url)
	{
		ZipFile zipFile0;
		try {
			File file = new File(url.toURI());
			zipFile0 = new ZipFile(file);
			return zipFile0;
		} catch (Exception e) {
			ForgeTweaker.logger.warning(e);
		}
		return null;
	}

	public byte[] transform(String name, String transformedName, byte bytes[]) {
		if (zipFile == null)
			return bytes;
		
		String fullName = name + ".class";
		if(!classes.contains(fullName))
			return bytes;
		ZipEntry ze = zipFile.getEntry(fullName);
		if (ze == null)
			return bytes;
		byte diBytes[] = getClass(ze);
		if (diBytes != null) {
			ForgeTweaker.logger.info("Class '" + name + "'("+transformedName+") transformed.");
			return diBytes;
		}
		else
			return bytes;
	}

	private byte[] getClass(ZipEntry ze) {
		try {
			InputStream is = zipFile.getInputStream(ze);
			byte[] bytes = IOUtils.toByteArray(is);
			if ((long)bytes.length == ze.getSize())
				return bytes;
			ForgeTweaker.logger.info("Failed: " + ze.getName() + " " + bytes.length + " / " + ze.getSize());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
}
