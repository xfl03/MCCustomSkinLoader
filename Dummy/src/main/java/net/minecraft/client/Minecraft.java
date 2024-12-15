package net.minecraft.client;

import java.io.File;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SkinManager;

public class Minecraft {
    public File gameDir;
    public static Minecraft getMinecraft() { return null; }
    public ServerData getCurrentServerData() { return null; }
    public IResourceManager getResourceManager() { return null; }
    public SkinManager getSkinManager() { return null; }
    public TextureManager getTextureManager() { return null; }
}
