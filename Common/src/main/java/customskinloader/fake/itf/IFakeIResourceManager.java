package customskinloader.fake.itf;

import java.util.Optional;

import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public interface IFakeIResourceManager {
    interface V1 {
        // 22w13a-
        Resource getResource(Identifier location);
    }

    interface V2 {
        // 22w14a+
        default Optional getResource(Identifier location) {
            return Optional.ofNullable(((IFakeIResourceManager.V1) this).getResource(location));
        }
    }
}
