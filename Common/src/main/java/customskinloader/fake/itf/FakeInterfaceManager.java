package customskinloader.fake.itf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class FakeInterfaceManager {
    public static InputStream IResource_getInputStream(Object resource) {
        return ((IFakeIResource.V2) resource).open();
    }

    public static Optional<IResource> IResourceManager_getResource(Object resourceManager, ResourceLocation location) throws IOException {
        return ((IFakeIResourceManager) resourceManager).getResource(location);
    }

    public static IResourceManager Minecraft_getResourceManager(Object minecraft) {
        return (IResourceManager) ((IFakeMinecraft) minecraft).func_195551_G();
    }
}
