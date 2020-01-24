package customskinloader.fake.itf;

import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.ResourceLocation;

public interface IFakeTextureManager_2 {
    default void func_229263_a_(ResourceLocation textureLocation, Texture textureObj) {
        ((IFakeTextureManager_1) this).func_229263_a_(textureLocation, textureObj);
    }
}
