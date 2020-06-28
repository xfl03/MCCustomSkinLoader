package customskinloader.fake.itf;

import net.minecraft.client.Minecraft;

public interface IFakeMinecraft {
    default void execute(Runnable runnable) {
        ((Minecraft) this).addScheduledTask(runnable);
    }
}
