package customskinloader.forge;

import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Forge Mod Container
 */
@Mod(
        value = "customskinloader",
        modid = "customskinloader",
        name = "CustomSkinLoader",
        version = "@MOD_FULL_VERSION@",
        clientSideOnly = true,
        acceptedMinecraftVersions = "[1.8,1.13)",
        acceptableRemoteVersions = "*"
)//1.13-
public class ForgeMod {
    public ForgeMod() {
        try {
            this.setExtensionPoint();
        } catch (Throwable ignored) {
            // before forge-1.13.2-25.0.103
        }
    }

    // Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible after forge-1.13.2-25.0.107.
    private void setExtensionPoint() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> "customskinloader", (remote, isServer) -> isServer));
    }
}
