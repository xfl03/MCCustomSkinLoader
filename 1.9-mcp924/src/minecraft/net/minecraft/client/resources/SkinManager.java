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
    private final LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> skinCacheLoader;

    public SkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService)
    {
        this.textureManager = textureManagerInstance;
        this.skinCacheDir = skinCacheDirectory;
        this.sessionService = sessionService;
        this.skinCacheLoader = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).<GameProfile, Map<Type, MinecraftProfileTexture>>build(new CacheLoader<GameProfile, Map<Type, MinecraftProfileTexture>>()
        {
            public Map<Type, MinecraftProfileTexture> load(GameProfile p_load_1_) throws Exception
            {
                return Minecraft.getMinecraft().getSessionService().getTextures(p_load_1_, false);
            }
        });
        //CustomSkinLoader Begin (Init)
        customskinloader.loader.MojangAPILoader.defaultSessionService=sessionService;
        customskinloader.utils.HttpTextureUtil.defaultCacheDir=skinCacheDirectory;
        //CustomSkinLoader End
    }

    /**
     * Used in the Skull renderer to fetch a skin. May download the skin if it's not in the cache
     */
    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, Type textureType)
    {
        return this.loadSkin(profileTexture, textureType, (SkinManager.SkinAvailableCallback)null);
    }

    /**
     * May download the skin if its not in the cache, can be passed a SkinManager#SkinAvailableCallback for handling
     */
    public ResourceLocation loadSkin(final MinecraftProfileTexture profileTexture, final Type textureType, final SkinManager.SkinAvailableCallback skinAvailableCallback)
    {
        //CustomSkinLoader Begin (Parse HttpTextureInfo)
    	customskinloader.utils.HttpTextureUtil.HttpTextureInfo info=customskinloader.utils.HttpTextureUtil.toHttpTextureInfo(profileTexture.getUrl());
    	//CustomSkinLoader End
        final ResourceLocation resourcelocation = new ResourceLocation("skins/" + info.hash);//Modified
        ITextureObject itextureobject = this.textureManager.getTexture(resourcelocation);

        if (itextureobject != null)
        {
            if (skinAvailableCallback != null)
            {
                skinAvailableCallback.skinAvailable(textureType, resourcelocation, profileTexture);
            }
        }
        else
        {
            final IImageBuffer iimagebuffer = textureType == Type.SKIN ? new customskinloader.renderer.SkinBuffer() : null;//Modified
            ThreadDownloadImageData threaddownloadimagedata = new ThreadDownloadImageData(info.cacheFile, info.url, DefaultPlayerSkin.getDefaultSkinLegacy(), new IImageBuffer()//Modified
            {
                public BufferedImage parseUserSkin(BufferedImage image)
                {
                    if (iimagebuffer != null)
                    {
                        image = iimagebuffer.parseUserSkin(image);
                    }

                    return image;
                }
                public void skinAvailable()
                {
                    if (iimagebuffer != null)
                    {
                        iimagebuffer.skinAvailable();
                    }

                    if (skinAvailableCallback != null)
                    {
                        skinAvailableCallback.skinAvailable(textureType, resourcelocation, profileTexture);
                    }
                }
            });
            this.textureManager.loadTexture(resourcelocation, threaddownloadimagedata);
        }

        return resourcelocation;
    }

    public void loadProfileTextures(final GameProfile profile, final SkinManager.SkinAvailableCallback skinAvailableCallback, final boolean requireSecure)
    {
        THREAD_POOL.submit(new Runnable()
        {
            public void run()
            {
                final Map<Type, MinecraftProfileTexture> map = Maps.<Type, MinecraftProfileTexture>newHashMap();
                
                //CustomSkinLoader Begin (User Profile Part)
                if(customskinloader.CustomSkinLoader.config.enable){
                    map.putAll(customskinloader.CustomSkinLoader.loadProfile(profile));
                }else{
                    try{
                        map.putAll(SkinManager.this.sessionService.getTextures(profile, requireSecure));
                    }catch(InsecureTextureException var3){}
                }
                //CustomSkinLoader End
                
                Minecraft.getMinecraft().addScheduledTask(new Runnable()
                {
                    public void run()
                    {
                        //CustomSkinLoader Begin (Loading Task)
                        for(Type type:Type.values()){
                            if(map.containsKey(type))
                                SkinManager.this.loadSkin((MinecraftProfileTexture)map.get(type), type, skinAvailableCallback);
                        }
                        //CustomSkinLoader End
                    }
                });
            }
        });
    }

    public Map<Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile)
    {
        //CustomSkinLoader Begin (Skull Part)
        //return (Map)this.skinCacheLoader.getUnchecked(profile);
        return (customskinloader.CustomSkinLoader.config.enable && customskinloader.CustomSkinLoader.config.enableSkull)?
                customskinloader.CustomSkinLoader.loadProfileFromCache(profile):
                    this.skinCacheLoader.getUnchecked(profile);
        //CustomSkinLoader End
    }

    public interface SkinAvailableCallback
    {
        void skinAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture);
    }
}
