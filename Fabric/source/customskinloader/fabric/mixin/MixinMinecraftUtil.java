package customskinloader.fabric.mixin;

import java.io.File;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import customskinloader.utils.MinecraftUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.ServerEntry;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(value = {MinecraftUtil.class})
public abstract class MixinMinecraftUtil {
    @Overwrite
    public static File getMinecraftDataDir() {
        return MinecraftClient.getInstance().runDirectory;
    }
    
    @Overwrite
    public static MinecraftSessionService getSessionService() {
        return MinecraftClient.getInstance().getSessionService();
    }
    
    @Overwrite
    public static String getServerAddress() {
        ServerEntry data = MinecraftClient.getInstance().getCurrentServerEntry();
        if (data == null) {
            return null;
        }
        return data.address;
    }
    
    @Overwrite
    public static String getCurrentUsername() {
        return MinecraftClient.getInstance().getSession().getProfile().getName();
    }
}
