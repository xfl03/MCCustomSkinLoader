package customskinloader.forge;

import customskinloader.CustomSkinLoader;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLFingerprintViolationEvent;

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

    @EventHandler
    public void fingerprintError(FMLFingerprintViolationEvent event) {
        if (event.isDirectory()) return;//Development Environment

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
}