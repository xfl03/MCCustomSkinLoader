package customskinloader.fake.itf;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;

public interface IFakeMinecraft {
    // 1.13.2+
    default IResourceManager func_195551_G() {
        return (IResourceManager) ((Minecraft) this).getResourceManager();
    }
}
