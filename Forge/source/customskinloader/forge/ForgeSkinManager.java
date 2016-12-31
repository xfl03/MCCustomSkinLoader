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
import customskinloader.fake.FakeSkinManager;
import customskinloader.utils.HttpTextureUtil;
import customskinloader.utils.HttpTextureUtil.HttpTextureInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.ResourceLocation;

public class FakeSkinManager extends SkinManager {
    private FakeSkinManager fakeManager;//Fake Skin Manager

	public FakeSkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) {
		super(textureManagerInstance, skinCacheDirectory, sessionService);
        this.fakeManager=new FakeSkinManager(textureManagerInstance,skinCacheDirectory,sessionService);
	}
	
	@Override
	public ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, Type textureType){
		return this.loadSkin(profileTexture, textureType, null);
	}
	@Override
	public ResourceLocation loadSkin(final MinecraftProfileTexture profileTexture, final Type textureType, final SkinAvailableCallback skinAvailableCallback){
		return this.fakeManager.loadSkin(profileTexture, textureType, skinAvailableCallback);
	}
	@Override
	public void loadProfileTextures(final GameProfile profile, final SkinAvailableCallback skinAvailableCallback, final boolean requireSecure){
		this.fakeManager.loadProfileTextures(profile, skinAvailableCallback, requireSecure);
	}
	@Override
	public Map<Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile){
		return this.fakeManager.loadSkinFromCache(profile);
	}
}
