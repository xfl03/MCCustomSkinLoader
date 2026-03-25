package customskinloader.forge;

import net.minecraftforge.fml.IExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;

@Mod("customskinloader")
public class ForgeMod {
    public ForgeMod() {
        this.setExtensionPoint();
    }

    private void setExtensionPoint() {
        ModLoadingContext.get().registerExtensionPoint(IExtensionPoint.DisplayTest.class, () -> new IExtensionPoint.DisplayTest(() -> "customskinloader", (remote, isServer) -> isServer));
    }
}
