package customskinloader.fake.itf;

import java.io.InputStream;

import net.minecraft.client.resources.IResource;

/** {@link IResource} is no longer an interface since 22w14a */
public interface IFakeIResource {
    // 1.13.2 ~ 22w13a
    interface V1 {
        default InputStream func_199027_b() {
            return ((IResource) this).getInputStream();
        }
    }

    // 22w14a+
    interface V2 {
        default InputStream open() {
            return ((IFakeIResource.V1) this).func_199027_b();
        }
    }
}
