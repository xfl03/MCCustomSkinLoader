package net.minecraft.client.resources;

import net.minecraft.util.ResourceLocation;

// this class shouldn't be created by ASM because of modlauncher issue.
public interface IResourceManager {
    IResource getResource(ResourceLocation location);
}
