package customskinloader.fake.itf;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
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

    public static IResourceManager Minecraft_getResourceManager(Minecraft minecraft) {
        return (IResourceManager) ((IFakeMinecraft) minecraft).func_195551_G();
    }

    public static void Minecraft_addScheduledTask(Minecraft minecraft, Runnable runnable) {
        ((IFakeMinecraft) minecraft).execute(runnable);
    }

    public static void TextureManager_loadTexture(TextureManager textureManager, ResourceLocation textureLocation, Object textureObj) {
        ((IFakeTextureManager.V2) textureManager).func_229263_a_(textureLocation, (Texture) textureObj);
    }

    public static Texture TextureManager_getTexture(TextureManager textureManager, ResourceLocation textureLocation, Object textureObj) {
        return ((IFakeTextureManager.V1) textureManager).getTexture(textureLocation, (Texture) textureObj);
    }

    public static void ThreadDownloadImageData_resetNewBufferedImage(Object threadDownloadImageData, BufferedImage image) {
        ((IFakeThreadDownloadImageData) threadDownloadImageData).resetNewBufferedImage(image);
    }
}
