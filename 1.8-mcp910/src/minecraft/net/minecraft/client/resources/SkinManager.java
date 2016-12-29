package net.minecraft.client.resources;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ImageBufferDownload;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

public class SkinManager
{
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue());
    private final TextureManager textureManager;
    private final File skinCacheDir;
    private final MinecraftSessionService sessionService;
    private final LoadingCache skinCacheLoader;
    private static final String __OBFID = "CL_00001830";
    
    private customskinloader.fake.FakeSkinManager fakeManager;//Fake Skin Manager

    public SkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService)
    {
        this.textureManager = textureManagerInstance;
        this.skinCacheDir = skinCacheDirectory;
        this.sessionService = sessionService;
        this.skinCacheLoader = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader()
        {
            private static final String __OBFID = "CL_00001829";
            public Map func_152786_a(GameProfile profile)
            {
                return Minecraft.getMinecraft().getSessionService().getTextures(profile, false);
            }
            public Object load(Object p_load_1_)
            {
                return this.func_152786_a((GameProfile)p_load_1_);
            }
        });
        fakeManager=new customskinloader.fake.FakeSkinManager(textureManagerInstance,skinCacheDirectory,sessionService);
    }

    /**
     * Used in the Skull renderer to fetch a skin. May download the skin if it's not in the cache
     */
    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, Type p_152792_2_)
    {
        return this.loadSkin(profileTexture, p_152792_2_, (SkinManager.SkinAvailableCallback)null);
    }

    /**
     * May download the skin if its not in the cache, can be passed a SkinManager#SkinAvailableCallback for handling
     */
    public ResourceLocation loadSkin(final MinecraftProfileTexture profileTexture, final Type p_152789_2_, final SkinManager.SkinAvailableCallback skinAvailableCallback)
    {
        return this.fakeManager.loadSkin(profileTexture, p_152789_2_, skinAvailableCallback);
    }

    public void loadProfileTextures(final GameProfile profile, final SkinManager.SkinAvailableCallback skinAvailableCallback, final boolean requireSecure)
    {
        this.fakeManager.loadProfileTextures(profile, skinAvailableCallback, requireSecure);
    }

    public Map loadSkinFromCache(GameProfile profile)
    {
        return this.fakeManager.loadSkinFromCache(profile);
    }

    public interface SkinAvailableCallback
    {
        void skinAvailable(Type p_180521_1_, ResourceLocation location, MinecraftProfileTexture profileTexture);
    }
}
