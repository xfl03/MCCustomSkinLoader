package net.minecraftforge.fml.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

//https://github.com/MinecraftForge/MinecraftForge/blob/1.13.x/src/main/java/net/minecraftforge/fml/common/Mod.java
//https://github.com/MinecraftForge/MinecraftForge/blob/1.12.x/src/main/java/net/minecraftforge/fml/common/Mod.java
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mod {
    String value();

    String modid();

    String name() default "";

    String version() default "";

    boolean clientSideOnly() default false;

    String acceptedMinecraftVersions() default "";

    String acceptableRemoteVersions() default "";

    String certificateFingerprint() default "";

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    @interface EventHandler {
    }

}
