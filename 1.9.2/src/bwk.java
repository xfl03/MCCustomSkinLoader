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

public class bwk {
    private static final ExecutorService a = new ThreadPoolExecutor(0, 2, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue());//THREAD_POOL
    private final bvf b;//textureManager
    private final File c;//skinCacheDir
    private final MinecraftSessionService d;//sessionServive
    private final LoadingCache<GameProfile, Map<Type, MinecraftProfileTexture>> e;//skinCacheLoader

    public bwk(bvf textureManagerInstance, File skinCacheDirectory, MinecraftSessionService sessionService) {
        this.b = textureManagerInstance;
        this.c = skinCacheDirectory;
        this.d = sessionService;
        this.e = CacheBuilder.newBuilder().expireAfterAccess(15L, TimeUnit.SECONDS).build(new CacheLoader<GameProfile, Map<Type, MinecraftProfileTexture>>() {
             //roolback
            public Map<Type, MinecraftProfileTexture> a(GameProfile p_load_1_) throws Exception {
                return bcc.z().X().getTextures(p_load_1_, false);
            }
            public Map<Type, MinecraftProfileTexture> load(GameProfile p_load_1_) throws Exception {
                return bcc.z().X().getTextures(p_load_1_, false);
            }
        });
    }

    public kk a(MinecraftProfileTexture profileTexture, Type textureType) {
        return this.a(profileTexture, textureType, (bwk.a)null);
    }

    public kk a(final MinecraftProfileTexture profileTexture, final Type textureType, final bwk.a skinAvailableCallback) {
        final kk kk = new kk("skins/" + profileTexture.getHash());
        bvg bvg = this.b.b(kk);
        if(bvg != null) {
            if(skinAvailableCallback != null) {
                skinAvailableCallback.a(textureType, kk, profileTexture);
            }
        } else {
            File file1 = new File(this.c, profileTexture.getHash().length() > 2?profileTexture.getHash().substring(0, 2):"xx");
            File file2 = new File(file1, profileTexture.getHash());
            final bng bng = textureType == Type.SKIN?new bnm():null;
            buv buv = new buv(file2, profileTexture.getUrl(), bvt.a(), new bng() {
                public BufferedImage a(BufferedImage image) {
                    if(bng != null) {
                        image = bng.a(image);
                    }

                    return image;
                }

                public void a() {
                    if(bng != null) {
                        bng.a();
                    }

                    if(skinAvailableCallback != null) {
                        skinAvailableCallback.a(textureType, kk, profileTexture);
                    }

                }
            });
            this.b.a((kk)kk, (bvg)buv);
        }

        return kk;
    }

    public void a(final GameProfile profile, final bwk.a skinAvailableCallback, final boolean requireSecure) {
        a.submit(new Runnable() {
            public void run() {
                final Map<Type, MinecraftProfileTexture> map = Maps.newHashMap();

                try {
                    map.putAll(bwk.this.d.getTextures(profile, requireSecure));
                } catch (InsecureTextureException var3) {
                    ;
                }
                /*
                if(map.isEmpty() && profile.getId().equals(bcc.z().K().e().getId())) {
                    profile.getProperties().clear();
                    profile.getProperties().putAll(bcc.z().L());
                    map.putAll(bwk.this.d.getTextures(profile, false));
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

                bcc.z().a(new Runnable() {
                    public void run() {
                        if(map.containsKey(Type.SKIN)) {
                            bwk.this.a((MinecraftProfileTexture)map.get(Type.SKIN), Type.SKIN, skinAvailableCallback);
                        }

                        if(map.containsKey(Type.CAPE)) {
                            bwk.this.a((MinecraftProfileTexture)map.get(Type.CAPE), Type.CAPE, skinAvailableCallback);
                        }

                    }
                });
            }
        });
    }

    public Map<Type, MinecraftProfileTexture> a(GameProfile profile) {
        //CustomSkinLoader Begin (Skull Part)
        //return (Map)this.e.getUnchecked(profile);
        return (customskinloader.CustomSkinLoader.config.enable && customskinloader.CustomSkinLoader.config.enableSkull)?
                customskinloader.CustomSkinLoader.loadProfileFromCache(profile.getName()):
                    this.e.getUnchecked(profile);
        //CustomSkinLoader End
    }

    public interface a {
        void a(Type var1, kk var2, MinecraftProfileTexture var3);
    }
}
