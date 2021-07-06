package customskinloader.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mojang.authlib.GameProfile;
import customskinloader.fake.itf.IFakeMinecraft;
import net.minecraft.client.Minecraft;
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

    public static InputStream getResourceFromResourceLocation(ResourceLocation location) throws IOException {
        return ((IFakeMinecraft) Minecraft.getMinecraft()).getResourceFromResourceLocation(location);
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

    public static String getCredential(GameProfile profile) {
        return (profile == null || profile.hashCode() == 0) ? null :
                (profile.getId() == null ? profile.getName() : String.format("%s-%s", profile.getName(), profile.getId()));
    }
}
