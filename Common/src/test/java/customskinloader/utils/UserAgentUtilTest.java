package customskinloader.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class UserAgentUtilTest {
    @Test
    public void buildsUserAgentWithRuntimeVersions() {
        assertEquals(
                "CustomSkinLoader/15.0.1 (Minecraft/26.2; NeoForge/26.2.0.35-beta; Java/25.0.1)",
                UserAgentUtil.buildUserAgent("15.0.1", "26.2", "NeoForge", "26.2.0.35-beta", "25.0.1")
        );
    }

    @Test
    public void normalizesInvalidHeaderCharactersAndMissingValues() {
        assertEquals(
                "CustomSkinLoader/15.0.1_SNAPSHOT (Minecraft/26.2_Pre-Release; unknown/unknown; Java/25.0.1)",
                UserAgentUtil.buildUserAgent("15.0.1 SNAPSHOT", "26.2 Pre-Release", null, "", "25.0.1")
        );
    }

    @Test
    public void expandsCustomUserAgentPlaceholders() {
        assertEquals(
                "SkinServer/2.0 (CustomSkinLoader/15.0.1; Minecraft/26.2; NeoForge/26.2.0.35-beta; Java/25.0.1)",
                UserAgentUtil.expandUserAgent(
                        "SkinServer/2.0 (CustomSkinLoader/{CSL_VERSION}; Minecraft/{MINECRAFT_VERSION}; {MOD_LOADER_NAME}/{MOD_LOADER_VERSION}; Java/{JAVA_VERSION})",
                        "15.0.1",
                        "26.2",
                        "NeoForge",
                        "26.2.0.35-beta",
                        "25.0.1"
                )
        );
    }

    @Test
    public void expandsDefaultUserAgentPlaceholderAndPreservesUnknownPlaceholders() {
        assertEquals(
                "Proxy/1.0 CustomSkinLoader/15.0.1 (Minecraft/26.2; Fabric/0.19.3; Java/25.0.1) {UNKNOWN} {minecraft_version}",
                UserAgentUtil.expandUserAgent(
                        "Proxy/1.0 {DEFAULT_USER_AGENT} {UNKNOWN} {minecraft_version}",
                        "15.0.1",
                        "26.2",
                        "Fabric",
                        "0.19.3",
                        "25.0.1"
                )
        );
    }

    @Test
    public void usesDefaultUserAgentWhenCustomValueIsNull() {
        assertEquals(
                "CustomSkinLoader/15.0.1 (Minecraft/26.2; Forge/14.23.5.2864; Java/1.8.0_442)",
                UserAgentUtil.expandUserAgent(null, "15.0.1", "26.2", "Forge", "14.23.5.2864", "1.8.0_442")
        );
    }

    @Test
    public void readsFabricImplementationVersionFallback() {
        assertEquals("0.19.2", UserAgentUtil.readStaticString(FabricLoaderVersionFixture.class, "VERSION"));
    }

    public static final class FabricLoaderVersionFixture {
        public static final String VERSION = "0.19.2";
    }
}
