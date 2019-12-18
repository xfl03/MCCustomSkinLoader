package customskinloader.utils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

/**
 * @author Alexander Xia
 * @since 13.6
 */
public class MinecraftUtil {
    public static File getMinecraftDataDir() {
        return Minecraft.getMinecraft().gameDir;
    }

    public static TextureManager getTextureManager() {
        return Minecraft.getMinecraft().getTextureManager();
    }

    public static SkinManager getSkinManager() {
        return Minecraft.getMinecraft().getSkinManager();
    }

    private static boolean is_1_15_plus = false;
    private static Constructor<?> constructor = null;
    public static SimpleTexture createThreadDownloadImageData(File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocation, IImageBuffer imageBufferIn, MinecraftProfileTexture.Type textureType) {
        if (constructor == null) {
            Class<?> c;
            try {
                c = Class.forName("net.minecraft.client.renderer.texture.ThreadDownloadImageData", false, MinecraftUtil.class.getClassLoader()); // Forge 1.13
            } catch (ClassNotFoundException e1) {
                try {
                    c = Class.forName("net.minecraft.client.renderer.texture.DownloadingTexture", false, MinecraftUtil.class.getClassLoader()); // Forge 1.14+
                } catch (ClassNotFoundException e2) {
                    c = ThreadDownloadImageData.class; // Forge 1.12- or Fabric or Vanilla
                }
            }
            try {
                constructor = c.getConstructor(File.class, String.class, ResourceLocation.class, boolean.class, Runnable.class); // For 1.15+
                is_1_15_plus = true;
            } catch (NoSuchMethodException e1) {
                try {
                    constructor = c.getConstructor(File.class, String.class, ResourceLocation.class, IImageBuffer.class); // For 1.15-
                } catch (NoSuchMethodException e2) {
                    throw new RuntimeException(e2);
                }
            }
        }
        try {
            if (is_1_15_plus) {
                return (SimpleTexture) constructor.newInstance(cacheFileIn, imageUrlIn, textureResourceLocation, textureType == MinecraftProfileTexture.Type.SKIN, imageBufferIn);
            } else {
                return (SimpleTexture) constructor.newInstance(cacheFileIn, imageUrlIn, textureResourceLocation, imageBufferIn);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static ArrayList<String> minecraftVersion = new ArrayList<String>();
    private static String minecraftMainVersion = null;
    private final static Pattern MINECRAFT_VERSION_PATTERN = Pattern.compile(".*?(\\d+\\.\\d+[\\.]?\\d*).*?");

    public static ArrayList<String> getMinecraftVersions() {
        if (minecraftVersion != null && !minecraftVersion.isEmpty())
            return minecraftVersion;
        testProbe();
        return minecraftVersion;
    }

    public static String getMinecraftVersionText() {
        StringBuilder sb = new StringBuilder();
        for (String version : getMinecraftVersions())
            sb.append(version).append(" ");
        return StringUtils.trim(sb.toString());
    }

    public static String getMinecraftMainVersion() {
        if (minecraftMainVersion != null)
            return minecraftMainVersion;
        for (String version : getMinecraftVersions()) {
            Matcher m = null;
            try {
                m = MINECRAFT_VERSION_PATTERN.matcher(version);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (m == null || !m.matches())
                continue;
            minecraftMainVersion = m.group(m.groupCount());
            break;
        }
        return minecraftMainVersion;
    }

    // (domain|ip)(:port)
    public static String getServerAddress() {
        net.minecraft.client.multiplayer.ServerData data = Minecraft.getMinecraft().getCurrentServerData();
        if (data == null)//Single Player
            return null;
        return data.serverIP;
    }

    // ip:port
    public static String getStandardServerAddress() {
        return HttpUtil0.parseAddress(getServerAddress());
    }

    public static boolean isLanServer() {
        return HttpUtil0.isLanServer(getStandardServerAddress());
    }

    public static String getCurrentUsername() {
        return Minecraft.getMinecraft().getSession().getProfile().getName();
    }

    private final static Pattern MINECRAFT_CORE_FILE_PATTERN = Pattern.compile("^(.*?)/versions/([^\\/\\\\]*?)/([^\\/\\\\]*?).jar$");

    private static void testProbe() {
        minecraftVersion.clear();
        URL urls[] = JavaUtil.getClasspath();
        for (URL url : urls) {
            Matcher m = null;
            try {
                m = MINECRAFT_CORE_FILE_PATTERN.matcher(URLDecoder.decode(url.getPath(), "UTF-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (m == null || !m.matches())
                continue;
            minecraftVersion.add(m.group(2));
        }
    }

    public static boolean isCoreFile(URL url) {
        return regexMatch(url, MINECRAFT_CORE_FILE_PATTERN);
    }

    private final static Pattern LIBRARY_FILE_PATTERN = Pattern.compile("^(.*?)/libraries/(.*?)/([^\\/\\\\]*?).jar$");

    public static boolean isLibraryFile(URL url) {
        return regexMatch(url, LIBRARY_FILE_PATTERN);
    }

    private static boolean regexMatch(URL url, Pattern p) {
        Matcher m;
        try {
            m = p.matcher(URLDecoder.decode(url.getPath(), "UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return m.matches();
    }

    public static String getCredential(GameProfile profile) {
        return (profile == null || profile.hashCode() == 0) ? null :
                (profile.getId() == null ? profile.getName() : String.format("%s-%s", profile.getName(), profile.getId()));
    }
}
