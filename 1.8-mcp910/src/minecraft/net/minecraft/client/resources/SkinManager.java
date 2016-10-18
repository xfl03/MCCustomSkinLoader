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
        //CustomSkinLoader Begin (Init)
        customskinloader.loader.MojangAPILoader.defaultSessionService=sessionService;
        customskinloader.utils.HttpTextureUtil.defaultCacheDir=skinCacheDirectory;
        customskinloader.utils.HttpTextureUtil.initDefaultCacheDir();
        //CustomSkinLoader End
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
        //CustomSkinLoader Begin (Parse HttpTextureInfo)
    	customskinloader.utils.HttpTextureUtil.HttpTextureInfo info=customskinloader.utils.HttpTextureUtil.toHttpTextureInfo(profileTexture.getUrl());
    	//CustomSkinLoader End
        final ResourceLocation var4 = new ResourceLocation("skins/" + info.hash);//Modified
        ITextureObject var5 = this.textureManager.getTexture(var4);

        if (var5 != null)
        {
            if (skinAvailableCallback != null)
            {
                skinAvailableCallback.skinAvailable(p_152789_2_, var4, profileTexture);
            }
        }
        else
        {
            final IImageBuffer var8 = p_152789_2_ == Type.SKIN ? new customskinloader.renderer.SkinBuffer() : null;//Modified
            ThreadDownloadImageData var9 = new ThreadDownloadImageData(info.cacheFile, info.url, DefaultPlayerSkin.getDefaultSkinLegacy(), new IImageBuffer()//Modified
            {
                private static final String __OBFID = "CL_00001828";
                public BufferedImage parseUserSkin(BufferedImage image)
                {
                    if (var8 != null)
                    {
                        image = var8.parseUserSkin(image);
                    }

                    return image;
                }
                public void skinAvailable()
                {
                    if (var8 != null)
                    {
                        var8.skinAvailable();
                    }

                    if (skinAvailableCallback != null)
                    {
                        skinAvailableCallback.skinAvailable(p_152789_2_, var4, profileTexture);
                    }
                }
            });
            this.textureManager.loadTexture(var4, var9);
        }

        return var4;
    }

    public void loadProfileTextures(final GameProfile profile, final SkinManager.SkinAvailableCallback skinAvailableCallback, final boolean requireSecure)
    {
        THREAD_POOL.submit(new Runnable()
        {
            private static final String __OBFID = "CL_00001827";
            public void run()
            {
                final HashMap var1 = Maps.newHashMap();
                
                //CustomSkinLoader Begin (User Skin/Cape Part)
                if(customskinloader.CustomSkinLoader.config.enable){
                    var1.putAll(customskinloader.CustomSkinLoader.loadProfile(profile));
                }else{
                    try{
                        var1.putAll(SkinManager.this.sessionService.getTextures(profile, requireSecure));
                    }catch(InsecureTextureException var3){}
                }
                //CustomSkinLoader End

                Minecraft.getMinecraft().addScheduledTask(new Runnable()
                {
                    private static final String __OBFID = "CL_00001826";
                    public void run()
                    {
                        if (var1.containsKey(Type.SKIN))
                        {
                            SkinManager.this.loadSkin((MinecraftProfileTexture)var1.get(Type.SKIN), Type.SKIN, skinAvailableCallback);
                        }

                        if (var1.containsKey(Type.CAPE))
                        {
                            SkinManager.this.loadSkin((MinecraftProfileTexture)var1.get(Type.CAPE), Type.CAPE, skinAvailableCallback);
                        }
                    }
                });
            }
        });
    }

    public Map loadSkinFromCache(GameProfile profile)
    {
        //CustomSkinLoader Begin (Skull Part)
        //return (Map)this.skinCacheLoader.getUnchecked(profile);
        return (customskinloader.CustomSkinLoader.config.enable && customskinloader.CustomSkinLoader.config.enableSkull)?
                customskinloader.CustomSkinLoader.loadProfileFromCache(profile):
                    (Map)this.skinCacheLoader.getUnchecked(profile);
        //CustomSkinLoader End
    }

    public interface SkinAvailableCallback
    {
        void skinAvailable(Type p_180521_1_, ResourceLocation location, MinecraftProfileTexture profileTexture);
    }
}
