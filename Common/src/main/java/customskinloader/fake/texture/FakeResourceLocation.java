package customskinloader.fake.texture;

import customskinloader.fake.FakeMinecraftProfileTexture;
import net.minecraft.util.ResourceLocation;

public class FakeResourceLocation extends ResourceLocation {
    public static FakeResourceLocation create(ResourceLocation location, FakeMinecraftProfileTexture texture) {
        String[] s = location.toString().split(":", 2);
        return new FakeResourceLocation(s[0], s[1], texture);
    }

    private final FakeMinecraftProfileTexture texture;

    public FakeResourceLocation(String domain, String path, FakeMinecraftProfileTexture texture) {
        super(domain, path);
        this.texture = texture;
    }

    public FakeMinecraftProfileTexture getTexture() {
        return this.texture;
    }
}
