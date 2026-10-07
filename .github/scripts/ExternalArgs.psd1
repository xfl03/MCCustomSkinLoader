# This configuration file contains workarounds for various issues unrelated to this mod
# that may occur when running CI tests on certain Minecraft versions.
@{
    Args = @(
        @{
            Matrix = @(
                @{
                    Loaders = @('forge')
                    VersionRange = @('1.13.2', '1.14.3')
                }
            )
            JvmArgs = '-javaagent:CustomSkinLoader-Test-1.0.0.jar=ForgeNetworkFix'
        },
        @{
            Matrix = @(
                @{
                    Loaders = @('fabric', 'quilt')
                    VersionRange = @('1.14', '1.19')
                },
                @{
                    Loaders = @('forge')
                    VersionRange = @('1.14.2', '1.14.3', '1.15', '1.15.1')
                }
            )
            JvmArgs = '-javaagent:CustomSkinLoader-Test-1.0.0.jar=MC145102Fix'
        },
        @{
            Matrix = @(
                @{
                    Loaders = @('fabric', 'forge', 'quilt')
                    VersionRange = @('1.16.4', '1.16.5')
                }
            )
            JvmArgs = '-javaagent:CustomSkinLoader-Test-1.0.0.jar=AuthlibPrivilegesFix'
        },
        @{
            Matrix = @(
                @{
                    Loaders = @('quilt')
                    VersionRange = @('1.17', '1.17.1')
                }
            )
            JvmArgs = '-Dloader.systemLibraries=${library_directory}/com/mojang/blocklist/1.0.5/blocklist-1.0.5.jar'
        },
        @{
            Matrix = @(
                @{
                    Loaders = @('quilt')
                    VersionRange = @('1.18', '1.18.1')
                }
            )
            JvmArgs = '-Dloader.systemLibraries=${library_directory}/com/mojang/blocklist/1.0.6/blocklist-1.0.6.jar'
        },
        @{
            Matrix = @(
                @{
                    Loaders = @('neoforge')
                    VersionRange = @('1.20.5', '26.1.2')
                }
            )
            JvmArgs = '-javaagent:CustomSkinLoader-Test-1.0.0.jar=NeoForgeVanillaFilterFix'
        }
    )
}
