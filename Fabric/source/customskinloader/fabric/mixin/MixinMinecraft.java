package customskinloader.fabric.mixin;

import java.util.concurrent.Executor;

import com.google.common.util.concurrent.ListenableFuture;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Minecraft.class)
public abstract class MixinMinecraft implements Executor {
    public ListenableFuture<Object> func_152344_a(Runnable runnable) {
        this.execute(runnable);
        return null;
    }
}
