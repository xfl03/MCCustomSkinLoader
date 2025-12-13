package customskinloader.mixin;

import java.util.Optional;

import customskinloader.fake.itf.IFakeIResourceManager;
import customskinloader.fake.itf.IFakeResourceLocation;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;

public abstract class MixinIResourceManager {
    @Mixin(IResourceManager.class)
    public interface V1 extends IFakeIResourceManager.V1 {

    }

    // 25w45a+
    @Mixin(IResourceManager.class)
    public interface V2 extends IFakeIResourceManager.V2 {
        @Override
        default Optional getResource(IFakeResourceLocation location) {
            return ((IResourceManager) this).getResource((Identifier) location);
        }
    }
}
