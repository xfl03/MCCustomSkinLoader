package com.mojang.authlib.minecraft;

import com.mojang.authlib.properties.Property;

public interface MinecraftSessionService {
    MinecraftProfileTextures unpackTextures(Property packedTextures);
}
