package customskinloader.fake.itf;

import java.util.Optional;

import net.minecraft.client.resources.IResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.util.ResourceLocation;

public interface IFakeIResourceManager {
    // 1.13.2 ~ 22w13a
    default IResource func_199002_a(ResourceLocation location) {
        return (IResource) ((IResourceManager) this).getResource(location);
    }

    // 22w14a+
    default Optional getResource(ResourceLocation location) {
        return Optional.ofNullable(this.func_199002_a(location));
    }
}
