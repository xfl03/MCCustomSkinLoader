package customskinloader.mixin;

import customskinloader.fake.itf.IFakeMinecraft;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements IFakeMinecraft {

}
