import com.google.common.cache.*;
import com.google.common.collect.Maps;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.*;
import com.mojang.authlib.properties.PropertyMap;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.concurrent.*;

public class bwn
{
	public static interface a
	{

		public abstract void a(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type type, kk kk1, MinecraftProfileTexture minecraftprofiletexture);
	}


	private static final ExecutorService a;
	private final bvi b;
	private final File c;
	private final MinecraftSessionService d;
	private final LoadingCache e;

	public bwn(bvi textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService)
	{
		b = textureManagerInstance;
		c = skinCacheDirectory;
		d = sessionService;
		e = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader() {

			public Map a(GameProfile gameProfile)
				throws Exception
			{
				return bcf.z().X().getTextures(gameProfile, false);
			}

			public Object load(Object obj)
				throws Exception
			{
				return a((GameProfile)obj);
			}
		});
	}

	public kk a(MinecraftProfileTexture profileTexture, com.mojang.authlib.minecraft.MinecraftProfileTexture.Type textureType)
	{
		return a(profileTexture, textureType, ((a) (null)));
	}

	public kk a(final MinecraftProfileTexture profileTexture,final com.mojang.authlib.minecraft.MinecraftProfileTexture.Type textureType, final a skinAvailableCallback)
	{
		final kk resourceLocation = new kk((new StringBuilder()).append("skins/").append(profileTexture.getHash()).toString());
		bvj textureObject = b.b(resourceLocation);
		if (textureObject != null)
		{
			if (skinAvailableCallback != null)
				skinAvailableCallback.a(textureType, resourceLocation, profileTexture);
		} else
		{
			File cacheDir = new File(c, profileTexture.getHash().length() <= 2 ? "xx" : profileTexture.getHash().substring(0, 2));
			File cacheFile = new File(cacheDir, profileTexture.getHash());
			final bnj imageBuffer = textureType != com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN ? null : ((bnj) (new bnp()));
			buy threadDownloadImageData = new buy(cacheFile, profileTexture.getUrl(), bvw.a(), new bnj() {

				public BufferedImage a(BufferedImage image)
				{
					if (imageBuffer != null)
						image = imageBuffer.a(image);
					return image;
				}

				public void a()
				{
					if (imageBuffer != null)
						imageBuffer.a();
					if (b != null)
						skinAvailableCallback.a(textureType, resourceLocation, profileTexture);
				}

			});
			b.a(resourceLocation, threadDownloadImageData);
		}
		return resourceLocation;
	}

	public void a(final GameProfile profile, final a skinAvailableCallback, final boolean requireSecure)
	{
		a.submit(new Runnable() {

			public void run()
			{
				final Map map = Maps.newHashMap();
				try
				{
					map.putAll(bwn.this.d.getTextures(profile, requireSecure));
				}
				catch (InsecureTextureException insecureTextureException) { }
				/*
				if (map.isEmpty() && profile.getId().equals(bcf.z().K().e().getId()))
				{
					profile.getProperties().clear();
					profile.getProperties().putAll(bcf.z().L());
					map.putAll(bwn.this.d.getTextures(profile, false));
				}*/
				//CustomSkinLoader Begin
                if(customskinloader.CustomSkinLoader.config.enable){
                	Map newMap=customskinloader.CustomSkinLoader.loadProfile(profile.getName(), map);
                	if(!newMap.isEmpty()){
                		map.clear();
                		map.putAll(newMap);
                	}
                }
                //CustomSkinLoader End
				bcf.z().a(new Runnable() {

					public void run()
					{
						if (map.containsKey(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN))
							bwn.this.a((MinecraftProfileTexture)map.get(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN), com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.SKIN, skinAvailableCallback);
						if (map.containsKey(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.CAPE))
							bwn.this.a((MinecraftProfileTexture)map.get(com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.CAPE), com.mojang.authlib.minecraft.MinecraftProfileTexture.Type.CAPE, skinAvailableCallback);
					}
				});
			}
		});
	}

	public Map a(GameProfile profile)
	{
		return (Map)e.getUnchecked(profile);
	}

	static MinecraftSessionService a(bwn skinManagerInstance)
	{
		return skinManagerInstance.d;
	}

	static 
	{
		a = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue());
	}
}
