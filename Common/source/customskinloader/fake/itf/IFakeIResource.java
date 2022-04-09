package customskinloader.fake.itf;

import java.io.InputStream;

import net.minecraft.client.resources.IResource;

/** {@link net.minecraft.client.resources.IResource} is no longer an interface since 22w14a */
public interface IFakeIResource {
    // 1.13.2+
    default InputStream func_199027_b() {
        return ((IResource) this).getInputStream();
    }
}
