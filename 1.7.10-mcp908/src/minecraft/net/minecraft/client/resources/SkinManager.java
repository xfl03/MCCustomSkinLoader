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
    public static final ResourceLocation field_152793_a = new ResourceLocation("textures/entity/steve.png");
    private static final ExecutorService field_152794_b = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue());
    private final TextureManager field_152795_c;
    private final File field_152796_d;
    private final MinecraftSessionService field_152797_e;
    private final LoadingCache field_152798_f;
    private static final String __OBFID = "CL_00001830";

    public SkinManager(TextureManager p_i1044_1_, File p_i1044_2_, MinecraftSessionService p_i1044_3_)
    {
        this.field_152795_c = p_i1044_1_;
        this.field_152796_d = p_i1044_2_;
        this.field_152797_e = p_i1044_3_;
        this.field_152798_f = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader()
        {
            private static final String __OBFID = "CL_00001829";
            public Map func_152786_a(GameProfile p_152786_1_)
            {
                return Minecraft.getMinecraft().func_152347_ac().getTextures(p_152786_1_, false);
            }
            public Object load(Object p_load_1_)
            {
                return this.func_152786_a((GameProfile)p_load_1_);
            }
        });
    }

    public ResourceLocation func_152792_a(MinecraftProfileTexture p_152792_1_, Type p_152792_2_)
    {
        return this.func_152789_a(p_152792_1_, p_152792_2_, (SkinManager.SkinAvailableCallback)null);
    }

    public ResourceLocation func_152789_a(MinecraftProfileTexture p_152789_1_, final Type p_152789_2_, final SkinManager.SkinAvailableCallback p_152789_3_)
    {
        final ResourceLocation var4 = new ResourceLocation("skins/" + p_152789_1_.getHash());
        ITextureObject var5 = this.field_152795_c.getTexture(var4);
        if (var5 != null)
        {
            if (p_152789_3_ != null)
            {
                p_152789_3_.func_152121_a(p_152789_2_, var4);
            }
        }
        else
        {
            File var6 = new File(this.field_152796_d, p_152789_1_.getHash().substring(0, 2));
            File var7 = new File(var6, p_152789_1_.getHash());
            final ImageBufferDownload var8 = p_152789_2_ == Type.SKIN ? new ImageBufferDownload() : null;
            ThreadDownloadImageData var9 = new ThreadDownloadImageData(var7, p_152789_1_.getUrl(), field_152793_a, new IImageBuffer()
            {
                private static final String __OBFID = "CL_00001828";
                public BufferedImage parseUserSkin(BufferedImage p_78432_1_)
                {
                    if (var8 != null)
                    {
                        p_78432_1_ = var8.parseUserSkin(p_78432_1_);
                    }

                    return p_78432_1_;
                }
                public void func_152634_a()
                {
                    if (var8 != null)
                    {
                        var8.func_152634_a();
                    }

                    if (p_152789_3_ != null)
                    {
                        p_152789_3_.func_152121_a(p_152789_2_, var4);
                    }
                }
            });
            this.field_152795_c.loadTexture(var4, var9);
        }

        return var4;
    }

    public void func_152790_a(final GameProfile p_152790_1_, final SkinManager.SkinAvailableCallback p_152790_2_, final boolean p_152790_3_)
    {
        field_152794_b.submit(new Runnable()
        {
            private static final String __OBFID = "CL_00001827";
            public void run()
            {
                final HashMap var1 = Maps.newHashMap();

                try
                {
                    var1.putAll(SkinManager.this.field_152797_e.getTextures(p_152790_1_, p_152790_3_));
                }
                catch (InsecureTextureException var3)
                {
                    ;
                }
                
                /*
                 * If keep this, code below will not be executed.
                 * p_152790_1_.getId() may be null, so NullPointerException may be thrown.
                 * Minecraft.getMinecraft().getSession().func_148256_e().getId() also may be null, which makes it meaningless.
                if (var1.isEmpty() && p_152790_1_.getId().equals(Minecraft.getMinecraft().getSession().func_148256_e().getId()))
                {
                	System.out.println("RUN3");
                    var1.putAll(SkinManager.this.field_152797_e.getTextures(SkinManager.this.field_152797_e.fillProfileProperties(p_152790_1_, false), false));
                }*/
                
                //CustomSkinLoader Begin (User Skin/Cape Part)
                if(customskinloader.CustomSkinLoader.config.enable){
                	Map newMap=customskinloader.CustomSkinLoader.loadProfile(p_152790_1_.getName(), var1);
                	if(!newMap.isEmpty()){
                		var1.clear();
                		var1.putAll(newMap);
                	}
                }
                //CustomSkinLoader End

                Minecraft.getMinecraft().func_152344_a(new Runnable()
                {
                    private static final String __OBFID = "CL_00001826";
                    public void run()
                    {
                        if (var1.containsKey(Type.SKIN))
                        {
                            SkinManager.this.func_152789_a((MinecraftProfileTexture)var1.get(Type.SKIN), Type.SKIN, p_152790_2_);
                        }

                        if (var1.containsKey(Type.CAPE))
                        {
                            SkinManager.this.func_152789_a((MinecraftProfileTexture)var1.get(Type.CAPE), Type.CAPE, p_152790_2_);
                        }
                    }
                });
            }
        });
    }

    public Map func_152788_a(GameProfile p_152788_1_)
    {
		//CustomSkinLoader Begin (Skull Part)
    	//return (Map)this.field_152798_f.getUnchecked(p_152788_1_);
    	return (customskinloader.CustomSkinLoader.config.enable && customskinloader.CustomSkinLoader.config.enableSkull)?
    			customskinloader.CustomSkinLoader.loadProfileFromCache(p_152788_1_.getName()):
    				(Map)this.field_152798_f.getUnchecked(p_152788_1_);
    	//CustomSkinLoader End
    }

    public interface SkinAvailableCallback
    {
        void func_152121_a(Type p_152121_1_, ResourceLocation p_152121_2_);
    }
}
