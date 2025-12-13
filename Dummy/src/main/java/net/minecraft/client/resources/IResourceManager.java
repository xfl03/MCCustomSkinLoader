package net.minecraft.client.resources;

import java.util.Optional;

import net.minecraft.resources.Identifier;
import net.minecraft.util.ResourceLocation;

public interface IResourceManager {
    IResource getResource(ResourceLocation location);
    Optional getResource(Identifier Identifier);
}
