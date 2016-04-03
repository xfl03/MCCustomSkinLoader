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
        final ResourceLocation resourcelocation = new ResourceLocation("skins/" + profileTexture.getHash());
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
            File file1 = new File(this.skinCacheDir, profileTexture.getHash().length() > 2 ? profileTexture.getHash().substring(0, 2) : "xx");
            File file2 = new File(file1, profileTexture.getHash());
            final IImageBuffer iimagebuffer = textureType == Type.SKIN ? new ImageBufferDownload() : null;
            ThreadDownloadImageData threaddownloadimagedata = new ThreadDownloadImageData(file2, profileTexture.getUrl(), DefaultPlayerSkin.getDefaultSkinLegacy(), new IImageBuffer()
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

                try
                {
                    map.putAll(SkinManager.this.sessionService.getTextures(profile, requireSecure));
                }
                catch (InsecureTextureException var3)
                {
                    ;
                }

				/*
                if (map.isEmpty() && profile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId()))
                {
                    profile.getProperties().clear();
                    profile.getProperties().putAll(Minecraft.getMinecraft().getProfileProperties());
                    map.putAll(SkinManager.this.sessionService.getTextures(profile, false));
                }*/
                
                //CustomSkinLoader Begin (User Skin/Cape Part)
                if(customskinloader.CustomSkinLoader.config.enable){
                	Map newMap=customskinloader.CustomSkinLoader.loadProfile(profile.getName(), map);
                	if(!newMap.isEmpty()){
                		map.clear();
                		map.putAll(newMap);
                	}
                }
                //CustomSkinLoader End
                Minecraft.getMinecraft().addScheduledTask(new Runnable()
                {
                    public void run()
                    {
                        if (map.containsKey(Type.SKIN))
                        {
                            SkinManager.this.loadSkin((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN, skinAvailableCallback);
                        }

                        if (map.containsKey(Type.CAPE))
                        {
                            SkinManager.this.loadSkin((MinecraftProfileTexture)map.get(Type.CAPE), Type.CAPE, skinAvailableCallback);
                        }
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
    			customskinloader.CustomSkinLoader.loadProfileFromCache(profile.getName()):
    				this.skinCacheLoader.getUnchecked(profile);
    	//CustomSkinLoader End
    }

    public interface SkinAvailableCallback
    {
        void skinAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture);
    }
}
