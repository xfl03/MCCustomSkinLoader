package net.minecraft.client;

import com.mojang.authlib.minecraft.MinecraftSessionService;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.SkinManager;
import net.minecraft.util.Session;

import java.io.File;

public class Minecraft {
    public File gameDir = null;

    public static Minecraft getMinecraft() {
        return null;
    }

    public SkinManager getSkinManager() {
        return null;
    }

    public TextureManager getTextureManager() {
        return null;
    }

    public MinecraftSessionService getSessionService() {
        return null;
    }

    public Session getSession() {
        return null;
    }

    public ServerData getCurrentServerData() {
        return null;
    }

    public void addScheduledTask(Runnable r) {
    }
}
