package customskinloader.tweaker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import customskinloader.utils.JavaUtil;
import org.apache.commons.io.IOUtils;

import customskinloader.utils.MinecraftUtil;
import net.minecraft.launchwrapper.IClassTransformer;

public class ClassTransformer implements IClassTransformer {

    private ZipFile zipFile = null;
    private ArrayList<String> classes=new ArrayList<String>();

    public ClassTransformer() {
        ModSystemTweaker.logger.info("ClassTransformer Begin");
        try {
            URL urls[] = JavaUtil.getClasspath();
            for (URL url : urls) {
                if(MinecraftUtil.isCoreFile(url)){
                    ModSystemTweaker.logger.debug(url.toString()+" : SKIP (core file).");
                    continue;
                }
                if(MinecraftUtil.isLibraryFile(url)){
                    ModSystemTweaker.logger.debug(url.toString()+" : SKIP (library file).");
                    continue;
                }
                File file = new File(url.toURI());
                if(!file.isFile()){
                    ModSystemTweaker.logger.info(url.toString()+" : EXCEPTION (file not found).");
                    continue;
                }
                ZipFile tempZipFile = getZipFile(file);
                if(tempZipFile==null){
                    ModSystemTweaker.logger.info(url.toString()+" : EXCEPTION (^ Message ^).");
                    continue;
                }
                if (tempZipFile.getEntry("customskinloader/tweaker/ClassTransformer.class") == null){
                    tempZipFile.close();
                    ModSystemTweaker.logger.debug(url.toString()+" : FINISH (not target).");
                    continue;
                }
                zipFile = tempZipFile;
                Enumeration<? extends ZipEntry> entries=zipFile.entries();
                StringBuilder sb=new StringBuilder();
                while(entries.hasMoreElements()){
                    ZipEntry entry=entries.nextElement();
                    String name=entry.getName();
                    if(name.endsWith(".class")&&(!name.contains("/")||name.startsWith("net"))){
                        classes.add(name);
                        sb.append(" ");
                        sb.append(name);
                    }
                }
                ModSystemTweaker.logger.info(url.toString()+" : CHOOSE.");
                ModSystemTweaker.logger.info("Classes:" + sb.toString());
                break;
            }
        } catch (Exception e) {
            ModSystemTweaker.logger.warning(e);
        }
        if (zipFile == null) {
            ModSystemTweaker.logger.info("Can not find JAR in the classpath.");
        }
    }

    private static ZipFile getZipFile(File file)
    {
        ZipFile zipFile0;
        try {
            zipFile0 = new ZipFile(file);
            return zipFile0;
        } catch (Exception e) {
            ModSystemTweaker.logger.warning(e);
        }
        return null;
    }

    public byte[] transform(String name, String transformedName, byte bytes[]) {
        if (zipFile == null)
            return bytes;
        
        String fullName = (name.startsWith("net") ? name.replace('.', '/') : name) + ".class";//Notch(Run)/MCP(Dev) Class Name
        if(!classes.contains(fullName)){//Check Name
            if(name.equals(transformedName))
                return bytes;
            fullName = transformedName.replace('.', '/') + ".class";//MCP Class Name
            if(!classes.contains(fullName))//check Again
                return bytes;
        }
        ZipEntry ze = zipFile.getEntry(fullName);
        if (ze == null)
            return bytes;
        byte diBytes[] = getClass(ze);
        if (diBytes != null) {
            ModSystemTweaker.logger.info("Class '" + name + "'("+transformedName+") transformed.");
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
            ModSystemTweaker.logger.info("Failed: " + ze.getName() + " " + bytes.length + " / " + ze.getSize());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
        
    }
}
