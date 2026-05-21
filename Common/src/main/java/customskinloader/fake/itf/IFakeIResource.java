package customskinloader.fake.itf;

import java.io.InputStream;

import net.minecraft.server.packs.resources.Resource;

/** {@link Resource} is no longer an interface since 22w14a */
public interface IFakeIResource {
    // 22w13a- (1.18.2-)
    interface V1 {
        InputStream getInputStream();
    }

    // 22w14a+ (1.19+)
    interface V2 {
        default InputStream open() {
            return ((IFakeIResource.V1) this).getInputStream();
        }
    }
}
