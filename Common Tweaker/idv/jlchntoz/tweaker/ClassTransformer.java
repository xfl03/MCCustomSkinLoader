package idv.jlchntoz.tweaker;

import idv.jlchntoz.CustomSkinLoader;

import java.io.*;
import java.net.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import net.minecraft.launchwrapper.IClassTransformer;

public class ClassTransformer implements IClassTransformer {

	private ZipFile zipFile = null;

	public ClassTransformer() {
		ForgeTweaker.logger.info("ClassTransformer Begin");
		try {
			URLClassLoader ucl = (URLClassLoader)idv.jlchntoz.tweaker.ClassTransformer.class.getClassLoader();
			URL urls[] = ucl.getURLs();
			for (int i = 0; i < urls.length; i++) {
				URL url = urls[i];
				ZipFile tempZipFile = getZipFile(url);
				if (tempZipFile == null)
					continue;
				zipFile = tempZipFile;
				ForgeTweaker.logger.info("URL: " + url);
				ForgeTweaker.logger.info("File: " + zipFile);
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (zipFile == null) {
			ForgeTweaker.logger.info("Can not find needed JAR in the classpath.");
		}
	}

	private static ZipFile getZipFile(URL url)
	{
		ZipFile zipFile0;
		try {
			URI uri = url.toURI();
			File file = new File(uri);
			zipFile0 = new ZipFile(file);
			if (zipFile0.getEntry("idv/jlchntoz/tweaker/ClassTransformer.class") != null){
				ForgeTweaker.logger.info("JAR Found!");
				return zipFile0;
			}
			zipFile0.close();
		} catch (Exception e) {
		}
		return null;
	}

	public byte[] transform(String name, String transformedName, byte bytes[]) {
		byte diBytes[] = getClass(name);
		if (diBytes != null) {
			ForgeTweaker.logger.info("Class '" + name + "' transformed.");
			return diBytes;
		}
		else
			return bytes;
	}

	private byte[] getClass(String name) {
		if (zipFile == null)
			return null;
		String fullName = name + ".class";
		ZipEntry ze = zipFile.getEntry(fullName);
		if (ze == null)
			return null;
		try {
			InputStream in = zipFile.getInputStream(ze);
			byte[] bytes = readAll(in);
			if ((long)bytes.length == ze.getSize())
				return bytes;
			ForgeTweaker.logger.info("Failed: " + fullName + " " + bytes.length + " / " + ze.getSize());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}

	public static byte[] readAll(InputStream is) throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte buffer[] = new byte[1024];
		do {
			int len = is.read(buffer);
			if (len >= 0) {
				baos.write(buffer, 0, len);
			} else {
				is.close();
				byte bytes[] = baos.toByteArray();
				return bytes;
			}
		} while (true);
	}
}
