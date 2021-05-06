package customskinloader.fake;

import java.io.File;
import java.util.EnumMap;
import java.util.function.Supplier;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import customskinloader.CustomSkinLoader;
import customskinloader.utils.HttpRequestUtil;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.DownloadingTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.util.ResourceLocation;

public class FakeThreadDownloadImageData {
    private static IThreadDownloadImageDataBuilder builder;

    public static SimpleTexture createThreadDownloadImageData(File cacheFileIn, String imageUrlIn, ResourceLocation textureResourceLocationIn, IImageBuffer imageBufferIn, MinecraftProfileTexture.Type textureTypeIn) {
        SimpleTexture texture = null;
        if (FakeThreadDownloadImageData.builder == null) {
            EnumMap<ThreadDownloadImageDataBuilder, Throwable> throwables = new EnumMap<>(ThreadDownloadImageDataBuilder.class);
            for (ThreadDownloadImageDataBuilder builder : ThreadDownloadImageDataBuilder.values()) {
                try {
                    FakeThreadDownloadImageData.builder = builder.get().get();
                    texture = FakeThreadDownloadImageData.builder.build(cacheFileIn, imageUrlIn, textureResourceLocationIn, imageBufferIn, textureTypeIn);
                    CustomSkinLoader.logger.info("ThreadDownloadImageData Class: %s", texture.getClass().getName());
                    break;
                } catch (Throwable t) {
                    throwables.put(builder, t);
                }
            }

            if (texture == null) {
                CustomSkinLoader.logger.warning("Unable to get ThreadDownloadImageData Class: ");
                throwables.forEach((k, v) -> {
                    CustomSkinLoader.logger.warning("Caused by: (%s)", k.name());
                    CustomSkinLoader.logger.warning(v);
                });
                throw new RuntimeException("Unable to get ThreadDownloadImageData Class!");
            }
        } else {
            texture = builder.build(cacheFileIn, imageUrlIn, textureResourceLocationIn, imageBufferIn, textureTypeIn);
        }
        return texture;
    }

    public static void downloadTexture(File cacheFile, String imageUrl) {
        HttpRequestUtil.HttpRequest request = new HttpRequestUtil.HttpRequest(imageUrl).setLoadContent(false).setCacheTime(0).setCacheFile(cacheFile);
        for (int i = 0; i <= CustomSkinLoader.config.retryTime; i++) {
            if (i != 0) {
                CustomSkinLoader.logger.debug("Retry to download texture %s (%s)", imageUrl, i);
            }
            if (HttpRequestUtil.makeHttpRequest(request).success) {
                break;
            }
        }
    }

    private interface IThreadDownloadImageDataBuilder {
        SimpleTexture build(File cacheFile, String imageUrl, ResourceLocation textureResourceLocation, IImageBuffer imageBuffer, MinecraftProfileTexture.Type textureType);
    }

    private enum ThreadDownloadImageDataBuilder {
        // DO NOT replace new IThreadDownloadImageDataBuilder() with lambda.
        V1(() -> new IThreadDownloadImageDataBuilder() { // Forge 1.8.x~1.12.x
            @Override
            public SimpleTexture build(File cacheFile, String imageUrl, ResourceLocation textureResourceLocation, IImageBuffer imageBuffer, MinecraftProfileTexture.Type textureType) {
                return new ThreadDownloadImageData(cacheFile, imageUrl, textureResourceLocation, imageBuffer);
            }
        }),
        V2(() -> new IThreadDownloadImageDataBuilder() { // Forge 1.13.x
            @Override
            public SimpleTexture build(File cacheFile, String imageUrl, ResourceLocation textureResourceLocation, IImageBuffer imageBuffer, MinecraftProfileTexture.Type textureType) {
                return new net.minecraft.client.renderer.texture.ThreadDownloadImageData(cacheFile, imageUrl, textureResourceLocation, imageBuffer);
            }
        }),
        V3(() -> new IThreadDownloadImageDataBuilder() { // Forge 1.14.x
            @Override
            public SimpleTexture build(File cacheFile, String imageUrl, ResourceLocation textureResourceLocation, IImageBuffer imageBuffer, MinecraftProfileTexture.Type textureType) {
                return new DownloadingTexture(cacheFile, imageUrl, textureResourceLocation, imageBuffer);
            }
        }),
        V4(() -> new IThreadDownloadImageDataBuilder() { // Forge 1.15.x~1.16.x
            @Override
            public SimpleTexture build(File cacheFile, String imageUrl, ResourceLocation textureResourceLocation, IImageBuffer imageBuffer, MinecraftProfileTexture.Type textureType) {
                return new DownloadingTexture(cacheFile, imageUrl, textureResourceLocation, true, imageBuffer);
            }
        });

        private final Supplier<IThreadDownloadImageDataBuilder> builder;

        ThreadDownloadImageDataBuilder(Supplier<IThreadDownloadImageDataBuilder> builder) {
            this.builder = builder;
        }

        public Supplier<IThreadDownloadImageDataBuilder> get() {
            return this.builder;
        }
    }
}
