package customskinloader.forge;

import customskinloader.CustomSkinLoader;
import net.minecraft.launchwrapper.Launch;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;
import net.minecraftforge.fml.network.FMLNetworkConstants;
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
        acceptedMinecraftVersions = "[1.8,)",
        acceptableRemoteVersions = "*",
        certificateFingerprint = "52885f395e68f42e9b3b629ba56ecf606f7d4269"
)//1.13-
public class ForgeMod {
    public ForgeMod() {
        try {
            this.setExtensionPoint();
        } catch (Throwable ignored) {
            // before forge-1.13.2-25.0.103
        }
    }

    @Mod.EventHandler
    public void fingerprintError(FMLFingerprintViolationEvent event) {
        if ((boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment") || event.isDirectory()) return;//Development Environment

        CustomSkinLoader.logger.warning("!!!Fingerprint ERROR!!!");
        CustomSkinLoader.logger.warning("Failed to check fingerprint in file '" + event.getSource().getAbsolutePath() + "'.");
        CustomSkinLoader.logger.warning("Excepted Fingerprint: " + event.getExpectedFingerprint());
        if (event.getFingerprints().isEmpty()) {
            CustomSkinLoader.logger.warning("No Fingerprint Founded.");
        } else {
            CustomSkinLoader.logger.warning("Founded Fingerprint: ");
            for (String s : event.getFingerprints())
                CustomSkinLoader.logger.warning(s);
        }

        throw new RuntimeException("Fingerprint ERROR, please **DO NOT MODIFY** any mod.");
    }

    // Make sure the mod being absent on the other network side does not cause the client to display the server as incompatible after forge-1.13.2-25.0.107.
    private void setExtensionPoint() {
        ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (remote, isServer) -> true));
    }
}