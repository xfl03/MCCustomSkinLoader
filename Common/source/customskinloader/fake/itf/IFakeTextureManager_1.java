package customskinloader.fake.itf;

import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public interface IFakeTextureManager_1 {
    default boolean func_229263_a_(ResourceLocation textureLocation, Texture textureObj) {
        return ((TextureManager) this).loadTexture(textureLocation, (ITextureObject) textureObj);
    }

    default Texture func_229267_b_(ResourceLocation textureLocation) {
        return (Texture) ((TextureManager) this).getTexture(textureLocation);
    }
}
