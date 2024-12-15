package customskinloader.mixin;

import customskinloader.fake.itf.IFakeResourceLocation;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ResourceLocation.class)
public abstract class MixinResourceLocation implements IFakeResourceLocation {
    @Unique
    private Object customskinloader$texture;

    @Override
    public Object customskinloader$getTexture() {
        return this.customskinloader$texture;
    }

    @Override
    public void customskinloader$setTexture(Object texture) {
        this.customskinloader$texture = texture;
    }
}
