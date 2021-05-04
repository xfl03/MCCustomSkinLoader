package customskinloader.fake.itf;

import java.io.IOException;
import java.io.InputStream;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

public interface IFakeMinecraft {
    default InputStream getResourceFromResourceLocation(ResourceLocation location) throws IOException {
        return ((Minecraft) this).getResourceManager().getResource(location).getInputStream();
    }

    // 1.14+
    default void execute(Runnable runnable) {
        ((Minecraft) this).addScheduledTask(runnable);
    }
}
