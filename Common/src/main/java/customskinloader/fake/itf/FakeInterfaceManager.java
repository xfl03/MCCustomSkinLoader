package customskinloader.fake.itf;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

public class FakeInterfaceManager {
    public static InputStream IResource_getInputStream(Object resource) {
        return ((IFakeIResource.V2) resource).open();
    }

    public static Optional<IResource> IResourceManager_getResource(Object resourceManager, ResourceLocation location) {
        return ((IFakeIResourceManager) resourceManager).getResource(location);
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

    public static Object ResourceLocation_getTexture(Object location) {
        return ((IFakeResourceLocation) location).customskinloader$getTexture();
    }

    public static void ResourceLocation_setTexture(Object location, Object texture) {
        ((IFakeResourceLocation) location).customskinloader$setTexture(texture);
    }

    public static GameProfile SkinManagerCacheKey_profile(Object skinManagerCacheKey) {
        return ((IFakeSkinManagerCacheKey) skinManagerCacheKey).profile();
    }

    public static Property SkinManagerCacheKey_packedTextures(Object skinManagerCacheKey) {
        return ((IFakeSkinManagerCacheKey) skinManagerCacheKey).packedTextures();
    }
}
