package customskinloader.fake.itf;

import java.io.InputStream;
import java.util.Optional;

import customskinloader.fake.FakeMinecraftProfileTexture;
import customskinloader.fake.texture.FakeNativeImage;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class FakeInterfaceManager {
    public static InputStream IResource_getInputStream(Object resource) {
        return ((IFakeIResource.V2) resource).open();
    }

    public static Optional<IResource> IResourceManager_getResource(Object resourceManager, Object location) {
        if (resourceManager instanceof IFakeIResourceManager.V2) {
            return ((IFakeIResourceManager.V2) resourceManager).getResource((IFakeResourceLocation) location);
        }
        return ((IFakeIResourceManager.V1) resourceManager).getResource((ResourceLocation) location);
    }

    public static IResourceManager Minecraft_getResourceManager(Object minecraft) {
        return (IResourceManager) ((IFakeMinecraft) minecraft).func_195551_G();
    }

    public static int NativeImage_getPixel(Object nativeImage, int x, int y) {
        return ((IFakeNativeImage) nativeImage).getPixel(x, y);
    }

    public static void NativeImage_setPixel(Object nativeImage, int x, int y, int color) {
        ((IFakeNativeImage) nativeImage).setPixel(x, y, color);
    }

    public static FakeNativeImage NativeImage_getFakeImage(Object nativeImage) {
        return ((IFakeNativeImage) nativeImage).getFakeImage();
    }

    public static void NativeImage_setFakeImage(Object nativeImage, Object fakeImage) {
        ((IFakeNativeImage) nativeImage).setFakeImage((FakeNativeImage) fakeImage);
    }

    public static FakeMinecraftProfileTexture ResourceLocation_getTexture(Object location) {
        return ((IFakeResourceLocation) location).getTexture();
    }

    public static void ResourceLocation_setTexture(Object location, FakeMinecraftProfileTexture texture) {
        ((IFakeResourceLocation) location).setTexture(texture);
    }
}
