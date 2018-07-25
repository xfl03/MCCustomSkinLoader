package customskinloader.fake;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;

import customskinloader.CustomSkinLoader;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpTextureUtil.HttpTextureInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager.SkinAvailableCallback;
import net.minecraft.util.ResourceLocation;

public class FakeSkinManager{
    private static final ExecutorService THREAD_POOL = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
    private final TextureManager textureManager;

    public FakeSkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) {
        this.textureManager=textureManagerInstance;
        HttpTextureUtil.defaultCacheDir=skinCacheDirectory;
    }
    
    public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, Type textureType){
        return this.loadSkin(profileTexture, textureType, null);
    }
    public ResourceLocation loadSkin(final MinecraftProfileTexture profileTexture, final Type textureType, final SkinAvailableCallback skinAvailableCallback){
        HttpTextureInfo info=HttpTextureUtil.toHttpTextureInfo(profileTexture.getUrl());
        
        final ResourceLocation resourcelocation = new ResourceLocation("skins/" + info.hash);
        ITextureObject itextureobject = this.textureManager.getTexture(resourcelocation);

        if (itextureobject != null)//Have already loaded
            makeCallback(skinAvailableCallback,textureType, resourcelocation, profileTexture);
        else{
            ThreadDownloadImageData threaddownloadimagedata = new ThreadDownloadImageData(info.cacheFile, info.url, DefaultPlayerSkin.getDefaultSkinLegacy(),
                new BaseBuffer(skinAvailableCallback, textureType, resourcelocation, profileTexture));
            if(skinAvailableCallback instanceof FakeClientPlayer.LegacyBuffer)//Cache for client player
                FakeClientPlayer.textureCache.put(resourcelocation,threaddownloadimagedata);
            this.textureManager.loadTexture(resourcelocation, threaddownloadimagedata);
        }
        return resourcelocation;
    }
    public void loadProfileTextures(final GameProfile profile, final SkinAvailableCallback skinAvailableCallback, final boolean requireSecure){
        THREAD_POOL.submit(new Runnable(){
            public void run(){
                final Map<Type, MinecraftProfileTexture> map = Maps.newHashMap();
                map.putAll(customskinloader.CustomSkinLoader.loadProfile(profile));

                Minecraft.getMinecraft().addScheduledTask(new Runnable(){
                    public void run(){
                        for(Type type:Type.values())
                            if(map.containsKey(type))
                                FakeSkinManager.this.loadSkin(map.get(type), type, skinAvailableCallback);
                    }
                });
            }
        });
    }
    public Map<Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile){
        return CustomSkinLoader.loadProfileFromCache(profile);
    }
    
    private static void makeCallback(SkinAvailableCallback callback,Type type, ResourceLocation location, MinecraftProfileTexture texture){
        if(callback!=null)
            callback.skinAvailable(type, location, texture);
    }
    private class BaseBuffer implements IImageBuffer{
        private IImageBuffer buffer;
        
        private SkinAvailableCallback callback;
        private Type type;
        private ResourceLocation location;
        private MinecraftProfileTexture texture;
        public BaseBuffer(SkinAvailableCallback callback,Type type,ResourceLocation location,MinecraftProfileTexture texture){
            this.buffer=(type==Type.SKIN?new FakeSkinBuffer():null);
            
            this.callback=callback;
            this.type=type;
            this.location=location;
            this.texture=texture;
        }

        public net.minecraft.client.renderer.texture.NativeImage func_195786_a(net.minecraft.client.renderer.texture.NativeImage image){
            return buffer instanceof FakeSkinBuffer ? ((FakeSkinBuffer) buffer).func_195786_a(image) : image;
        }
        public BufferedImage parseUserSkin(BufferedImage image){
            return buffer==null?image:buffer.parseUserSkin(image);
        }
        public void skinAvailable(){
            if (buffer != null){
                buffer.skinAvailable();
                if ("auto".equals(texture.getMetadata("model")) && buffer instanceof FakeSkinBuffer){
                    //Auto judge skin type
                    Map<String, String> metadata = Maps.newHashMap();
                    metadata.put("model", ((FakeSkinBuffer) buffer).judgeType());
                    texture = new MinecraftProfileTexture(texture.getUrl(), metadata);
                }
            }

            FakeSkinManager.makeCallback(callback,type, location, texture);
        }
    }
}
