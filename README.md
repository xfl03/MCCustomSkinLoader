# CustomSkinLoader

[![Version](https://img.shields.io/github/v/release/xfl03/MCCustomSkinLoader?label=&logo=V&labelColor=E1F5FE&color=5D87BF&style=for-the-badge)](https://github.com/xfl03/MCCustomSkinLoader/tags)
[![CurseForge](https://cf.way2muchnoise.eu/short_CustomSkinLoader.svg?badge_style=for_the_badge)](https://www.curseforge.com/minecraft/mc-mods/customskinloader)
[![Modrinth](https://img.shields.io/modrinth/dt/idMHQ4n2?label=&logo=Modrinth&labelColor=white&color=00AF5C&style=for-the-badge)](https://modrinth.com/mod/customskinloader)
[![License](https://img.shields.io/github/license/xfl03/MCCustomSkinLoader?label=&logo=c&style=for-the-badge&color=A8B9CC&labelColor=455A64)](https://github.com/xfl03/MCCustomSkinLoader/blob/master/LICENSE)
[![GitHub Workflow Status](https://img.shields.io/github/actions/workflow/status/xfl03/MCCustomSkinLoader/beta.yml?style=for-the-badge&label=&logo=Gradle&labelColor=388E3C)](https://github.com/xfl03/MCCustomSkinLoader/actions)
[![Star](https://img.shields.io/github/stars/xfl03/MCCustomSkinLoader?label=&logo=GitHub&labelColor=black&color=FAFAFA&style=for-the-badge)](https://github.com/xfl03/MCCustomSkinLoader/stargazers)

[![Minecraft Version](https://img.shields.io/badge/Minecraft-1.21%20|%201.20%20|%201.19%20|%201.18%20|%201.17%20|%201.16%20|%201.15%20|%201.14%20|%201.13%20|%201.12%20|%201.11%20|%201.10%20|%201.9%20|%201.8-green?style=for-the-badge&labelColor=388E3C&color=8BC34A)](https://github.com/xfl03/MCCustomSkinLoader)

## What's this?

CustomSkinLoader is a Minecraft mod that loads skins, capes, and elytra textures from online skin APIs or local files.

This branch is the Universal generation of MCCustomSkinLoader. It replaces the old edition-specific jars with one bootstrap artifact. The bootstrap jar prepares a runtime `CustomSkinLoader-Common.jar`, remaps it for the active Minecraft mapping namespace, and applies the required loader-specific class patches at runtime.

## Download

### Release Build

- [GitHub Releases](https://github.com/xfl03/MCCustomSkinLoader/releases)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/customskinloader)
- [Modrinth](https://modrinth.com/mod/customskinloader)

### Develop Build

- [GitHub Actions](https://github.com/xfl03/MCCustomSkinLoader/actions)
- [33 Kit（Chinese/中文）](https://3-3.dev/csl-download)

## Contact Us

- [Telegram @customskinloader](https://t.me/customskinloader)
- [QQ Group（Chinese/中文） 651287593](https://jq.qq.com/?_wv=1027&k=vF16R5tg)

## Supported Loaders

- Forge Legacy and Forge ModLauncher
- NeoForge
- Fabric
- Quilt-compatible Fabric Loader environments

## Feature

### Universal Runtime

One user-facing jar is used across supported loaders. The generated installable artifact is:

```text
Bootstrap/build/libs/CustomSkinLoader_Universal-<version>.jar
```

`Common/build/libs/Common-<version>.jar` is the nested runtime payload and should not be installed directly.

### Support Plenty of Skin Load APIs and Customizable Skin Load List

Supported skin loading APIs:

- [MojangAPI](https://minecraft.wiki/w/Mojang_API)
- [CustomSkinAPI](https://github.com/xfl03/CustomSkinLoaderAPI/tree/master/CustomSkinAPI)
- CustomSkinAPIPlus
- [UniSkinAPI](https://github.com/RecursiveG/UniSkinServer/tree/master/doc)
- [ElyByAPI](https://docs.ely.by/en/api.html)
- Legacy

Supported special skin sites and profiles:

- [Glitchless](https://games.glitchless.ru/games/minecraft/)
- [MinecraftCapes](https://minecraftcapes.net/)
- [OptiFineCape](https://optifine.net/home)
- [Cloaks+](https://cloaksplus.com/)
- [Cosmetica](https://cosmetica.cc/)
- [Wynntils](https://wynntils.com/) compatible profile support

You can customize the skin load list and load textures from any compatible skin server. Skin server owners can also use ExtraList files to help users add their server.

### HD Skins and Capes Support

CustomSkinLoader can load and process HD skins and capes. OptiFine cape textures are converted to the standard cape format when needed.

### Profile Cache

- Decreases repeated network requests.
- Allows cached profiles to load when the network is unavailable.

### Local Skin

Load skins without a skin server. With the default Legacy paths, place textures under:

```text
.minecraft/CustomSkinLoader/LocalSkin/skins/<USERNAME>.png
.minecraft/CustomSkinLoader/LocalSkin/capes/<USERNAME>.png
.minecraft/CustomSkinLoader/LocalSkin/elytras/<USERNAME>.png
```

### Extra List

Skin servers can provide ExtraList JSON files. Users can put them into `.minecraft/CustomSkinLoader/ExtraList` to add servers to the load list.

### Transparent Texture Support

The render patches are designed to keep skin and cape transparency correct on supported versions.

## Build

The project should compile on Windows, Linux, and macOS with a suitable Java 25 installation.

Windows:

```powershell
.\gradlew.bat clean build --stacktrace
```

Linux/macOS:

```bash
./gradlew clean build --stacktrace
```

GitHub Actions intentionally runs on Windows with PowerShell, but local project builds are not Windows-only.

## Default Load List

- [Mojang](https://minecraft.wiki/w/Mojang_API) (MojangAPI)
- [LittleSkin](https://littleskin.cn/) (CustomSkinAPI)
- [BlessingSkin](http://skin.prinzeugen.net/) (CustomSkinAPI)
- [ElyBy](https://docs.ely.by/en/api.html) (ElyByAPI)
- [TLauncher](https://tlauncher.org/) (ElyByAPI)
- [Glitchless](https://games.glitchless.ru/games/minecraft/) (ElyBy-compatible profile)
- LocalSkin (Legacy)
- [MinecraftCapes](https://minecraftcapes.net/)
- [OptiFineCape](https://optifine.net/home)
- [Cloaks+](https://cloaksplus.com/)
- [Cosmetica](https://cosmetica.cc/)

If you want to add another skin server to the default list, please open an [issue](https://github.com/xfl03/MCCustomSkinLoader/issues).

## To Skin Server Owner

CustomSkinLoader is designed for loading textures from many server implementations. It is usually better to support one of CustomSkinLoader's APIs than to copy this mod's internal implementation. If you maintain a skin server, you can provide an ExtraList file or request inclusion in the default load list.

## Development and Contribution

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Copyright & LICENSE

### Major Contributor

- 2013-2014 Jeremy Lam ([JLChnToZ](https://github.com/JLChnToZ))
- 2014-2024 Alexander Xia ([xfl03](https://github.com/xfl03))
- 2020-2026 [ZekerZhayard](https://github.com/ZekerZhayard)

### Binary File

You may use and share the unmodified official binary in modpacks. When used in a modpack, CustomSkinLoader must be listed in the mod list. Do not repost the mod to other websites without permission.

### Source Code

The source code is licensed under GPL-3.0-only. See [LICENSE](LICENSE).

Package `customskinloader` includes code or ideas from:

- [AsteriskTeam/TabIconHackForge](https://gitee.com/AsteriskTeam/TabIconHackForge) (GPLv3)
- [RecursiveG/UniSkinMod](https://github.com/RecursiveG/UniSkinMod) (GPLv3)
- [NekoCaffeine/Alchemy](https://github.com/NekoCaffeine/Alchemy) (GPLv3)

If you redistribute a modified build, change the package name and clearly mark the build as modified to avoid confusion with official CustomSkinLoader releases.
