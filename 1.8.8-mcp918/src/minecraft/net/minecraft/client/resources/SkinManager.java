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
    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, Type p_152792_2_)
    {
        return this.loadSkin(profileTexture, p_152792_2_, (SkinManager.SkinAvailableCallback)null);
    }

    /**
     * May download the skin if its not in the cache, can be passed a SkinManager#SkinAvailableCallback for handling
     */
    public ResourceLocation loadSkin(final MinecraftProfileTexture profileTexture, final Type p_152789_2_, final SkinManager.SkinAvailableCallback skinAvailableCallback)
    {
        final ResourceLocation resourcelocation = new ResourceLocation(p_152789_2_.name() + "/" + profileTexture.getHash());
        /*Remove cache
        ITextureObject itextureobject = this.textureManager.getTexture(resourcelocation);

        if (itextureobject != null)
        {
            if (skinAvailableCallback != null)
            {
                skinAvailableCallback.skinAvailable(p_152789_2_, resourcelocation, profileTexture);
            }
        }
        else
        {*/
            File file1 = new File(this.skinCacheDir, profileTexture.getHash().length() > 2 ? profileTexture.getHash().substring(0, 2) : "xx");
            File file2 = new File(file1, profileTexture.getHash());
            final IImageBuffer iimagebuffer = p_152789_2_ == Type.SKIN ? new ImageBufferDownload() : null;
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
                        skinAvailableCallback.skinAvailable(p_152789_2_, resourcelocation, profileTexture);
                    }
                }
            });
            this.textureManager.loadTexture(resourcelocation, threaddownloadimagedata);
        //}

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

                if (map.isEmpty() && profile.getId().equals(Minecraft.getMinecraft().getSession().getProfile().getId()))
                {
                    profile.getProperties().clear();
                    profile.getProperties().putAll(Minecraft.getMinecraft().func_181037_M());
                    map.putAll(SkinManager.this.sessionService.getTextures(profile, false));
                }
              //CustomSkinLoader Begin
                Map<Type, MinecraftProfileTexture> var2 = Maps.<Type, MinecraftProfileTexture>newHashMap();
                if (map.containsKey(Type.SKIN)){
                    MinecraftProfileTexture var3=((MinecraftProfileTexture)map.get(Type.SKIN));
                    String var4=var3.getMetadata("model");
                    Map var5 = null;
                    if(var4!=null){
                        var5 = Maps.newHashMap();
                        var5.put("model", var4);
                     }
                    var2.put(Type.SKIN, new MinecraftProfileTexture(var3.getUrl()+"?Skin="+profile.getName(),var5));
                }else{
                    Map var5 = Maps.newHashMap();
                    //var5.put("model", "slim");
                    var2.put(Type.SKIN,new MinecraftProfileTexture( "http://skins.minecraft.net/MinecraftSkins/"+profile.getName()+".png",var5));
                }
                if (map.containsKey(Type.CAPE)){
                    var2.put(Type.CAPE, new MinecraftProfileTexture(((MinecraftProfileTexture)map.get(Type.CAPE)).getUrl()+"?Cloak="+profile.getName(),null));
                }else{
                    var2.put(Type.CAPE,new MinecraftProfileTexture( "http://skins.minecraft.net/MinecraftCloaks/"+profile.getName()+".png",null));
                }
                map.clear();
                map.putAll(var2);
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
        return (Map)this.skinCacheLoader.getUnchecked(profile);
    }

    public interface SkinAvailableCallback
    {
        void skinAvailable(Type p_180521_1_, ResourceLocation location, MinecraftProfileTexture profileTexture);
    }
}
