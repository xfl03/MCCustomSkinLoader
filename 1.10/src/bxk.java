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
import javax.annotation.Nullable;

//SkinManager
public class bxk {
   private static final ExecutorService a = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue());//THREAD_POOL
   private final bwf b;//textureManager
   private final File c;//skinCacheDir
   private final MinecraftSessionService d;//sessionService
   private final LoadingCache e;//skinCacheLoader

   //SkinManager(TextureManager textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService)
   public bxk(bwf textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) {
      this.b = textureManagerInstance;
      this.c = skinCacheDirectory;
      this.d = sessionService;
      this.e = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader() {
         public Map a(GameProfile p_load_1_) throws Exception {
            return bcx.z().X().getTextures(p_load_1_, false);
         }

         // $FF: synthetic method
         public Object load(Object p_load_1_) throws Exception {
            return this.a((GameProfile)p_load_1_);
         }
      });
   }

   //ResourceLocation loadSkin(MinecraftProfileTexture profileTexture, Type textureType)
   public kn a(MinecraftProfileTexture profileTexture, Type textureType) {
      return this.a(profileTexture, textureType, (bxk.a)null);
   }

   //ResourceLocation loadSkin(final MinecraftProfileTexture profileTexture, final Type textureType, @Nullable final SkinManager.SkinAvailableCallback skinAvailableCallback)
   public kn a(final MinecraftProfileTexture profileTexture, final Type textureType, @Nullable final bxk.a skinAvailableCallback) {
      final kn resourcelocation = new kn("skins/" + profileTexture.getHash());
      bwg itextureobject = this.b.b(resourcelocation);
      if(itextureobject != null) {
         if(skinAvailableCallback != null) {
            skinAvailableCallback.a(textureType, resourcelocation, profileTexture);
         }
      } else {
         File file1 = new File(this.c, profileTexture.getHash().length() > 2?profileTexture.getHash().substring(0, 2):"xx");
         File file2 = new File(file1, profileTexture.getHash());
         final boc iimagebuffer = textureType == Type.SKIN?new boi():null;
         bvv threaddownloadimagedata = new bvv(file2, profileTexture.getUrl(), bwt.a(), new boc() {
            public BufferedImage a(BufferedImage image) {
               if(iimagebuffer != null) {
                  image = iimagebuffer.a(image);
               }

               return image;
            }

            public void a() {
               if(iimagebuffer != null) {
                  iimagebuffer.a();
               }

               if(skinAvailableCallback != null) {
                  skinAvailableCallback.a(textureType, resourcelocation, profileTexture);
               }

            }
         });
         this.b.a((kn)resourcelocation, (bwg)threaddownloadimagedata);
      }

      return resourcelocation;
   }

   //loadProfileTextures(final GameProfile profile, final SkinManager.SkinAvailableCallback skinAvailableCallback, final boolean requireSecure)
   public void a(final GameProfile profile, final bxk.a skinAvailableCallback, final boolean requireSecure) {
      a.submit(new Runnable() {
         public void run() {
            final Map<Type, MinecraftProfileTexture> map = Maps.newHashMap();

            try {
               map.putAll(bxk.this.d.getTextures(profile, requireSecure));
            } catch (InsecureTextureException var3) {
               ;
            }

            /*
            if(map.isEmpty() && profile.getId().equals(bcx.z().K().e().getId())) {
               profile.getProperties().clear();
               profile.getProperties().putAll(bcx.z().L());
               map.putAll(bxk.this.d.getTextures(profile, false));
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

            bcx.z().a(new Runnable() {
               public void run() {
                  if(map.containsKey(Type.SKIN)) {
                     bxk.this.a((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN, skinAvailableCallback);
                  }

                  if(map.containsKey(Type.CAPE)) {
                     bxk.this.a((MinecraftProfileTexture)map.get(Type.CAPE), Type.CAPE, skinAvailableCallback);
                  }

               }
            });
         }
      });
   }

   //Map<Type, MinecraftProfileTexture> loadSkinFromCache(GameProfile profile)
   public Map a(GameProfile profile) {
      //CustomSkinLoader Begin (Skull Part)
      //return (Map)this.e.getUnchecked(profile);
      return (customskinloader.CustomSkinLoader.config.enable && customskinloader.CustomSkinLoader.config.enableSkull)?
            customskinloader.CustomSkinLoader.loadProfileFromCache(profile.getName(),(Map)this.e.getUnchecked(profile)):
            (Map)this.e.getUnchecked(profile);
      //CustomSkinLoader End
   }

   //SkinAvailableCallback
   public interface a {
	  //skinAvailable(Type typeIn, ResourceLocation location, MinecraftProfileTexture profileTexture)
      void a(Type var1, kn var2, MinecraftProfileTexture var3);
   }
}
