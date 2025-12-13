package customskinloader.mixin;

import customskinloader.fake.FakeMinecraftProfileTexture;
import customskinloader.fake.itf.IFakeResourceLocation;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;

public abstract class MixinResourceLocation {
    @Mixin(ResourceLocation.class)
    public abstract static class V1 implements IFakeResourceLocation {
        private FakeMinecraftProfileTexture texture;

        @Override
        public FakeMinecraftProfileTexture getTexture() {
            return this.texture;
        }

        @Override
        public void setTexture(FakeMinecraftProfileTexture texture) {
            this.texture = texture;
        }
    }

    @Mixin(Identifier.class)
    public abstract static class V2 implements IFakeResourceLocation {
        private FakeMinecraftProfileTexture texture;

        @Override
        public FakeMinecraftProfileTexture getTexture() {
            return this.texture;
        }

        @Override
        public void setTexture(FakeMinecraftProfileTexture texture) {
            this.texture = texture;
        }
    }
}
