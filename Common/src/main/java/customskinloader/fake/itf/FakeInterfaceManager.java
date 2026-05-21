package customskinloader.fake.itf;

import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.Optional;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public class FakeInterfaceManager {
    public static InputStream IResource_getInputStream(Object resource) {
        return ((IFakeIResource.V2) resource).open();
    }

    public static Optional<Resource> IResourceManager_getResource(Object resourceManager, Object location) {
        return ((IFakeIResourceManager.V2) resourceManager).getResource((Identifier) location);
    }

    public static int NativeImage_getPixel(Object image, int x, int y) {
        if (image instanceof IFakeNativeImage) {
            return ((IFakeNativeImage) image).getPixel(x, y);
        }
        return ((BufferedImage) image).getRGB(x, y);
    }

    public static void NativeImage_setPixel(Object image, int x, int y, int color) {
        if (image instanceof IFakeNativeImage) {
            ((IFakeNativeImage) image).setPixel(x, y, color);
        } else {
            ((BufferedImage) image).setRGB(x, y, color);
        }
    }
}
