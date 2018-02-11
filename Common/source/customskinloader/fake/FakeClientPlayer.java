package customskinloader.fake;

import java.util.UUID;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import customskinloader.CustomSkinLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.client.resources.SkinManager.SkinAvailableCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;

public class FakeClientPlayer {
		//For Legacy Skin
		public static ThreadDownloadImageData getDownloadImageSkin(ResourceLocation resourceLocationIn, String username)
	    {
			CustomSkinLoader.logger.debug("FakeClientPlayer/getDownloadImageSkin "+username);
	        TextureManager textman = Minecraft.getMinecraft().getTextureManager();
	        ITextureObject ito = textman.getTexture(resourceLocationIn);

	        if (ito == null)
	        {
	        	//if Legacy Skin for username not loaded yet
	        	SkinManager skinman = Minecraft.getMinecraft().getSkinManager();
	        	UUID offlineUUID=EntityPlayer.getOfflineUUID(username);
	            GameProfile offlineProfile=new GameProfile(offlineUUID,username);
	            
	            skinman.loadProfileTextures(offlineProfile, new LegacyBuffer(resourceLocationIn), false);
	        }

	        if(ito instanceof ThreadDownloadImageData)
	        	return (ThreadDownloadImageData)ito;
	        else
	        	return null;
	    }

	    public static ResourceLocation getLocationSkin(String username)
	    {
	    	CustomSkinLoader.logger.debug("FakeClientPlayer/getLocationSkin "+username);
	        return new ResourceLocation("skins/" + username);
	    }
	    
	    private static class LegacyBuffer implements SkinAvailableCallback{
	    	ResourceLocation resourceLocationIn;
	    	
	    	public LegacyBuffer(ResourceLocation resourceLocationIn) {
	    		CustomSkinLoader.logger.debug("FakeClientPlayer/LegacyBuffer/<init> "+resourceLocationIn);
	    		this.resourceLocationIn=resourceLocationIn;
	    	}

			@Override
			public void skinAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture) {
				if(typeIn!=Type.SKIN)
					return;
				CustomSkinLoader.logger.debug("FakeClientPlayer/LegacyBuffer/skinAvailable "+resourceLocationIn);
				TextureManager textman = Minecraft.getMinecraft().getTextureManager();
				ITextureObject ito = textman.getTexture(location);
				textman.loadTexture(resourceLocationIn, ito);
			}
	    	
	    }
}
