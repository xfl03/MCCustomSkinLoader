package net.minecraft.client.resources;

import java.io.InputStream;

// this class shouldn't be created by ASM because of modlauncher issue.
public interface IResource {
    InputStream getInputStream();
}
