package net.minecraftforge.fml;

import java.util.function.Supplier;

public class ModLoadingContext {
    public static ModLoadingContext get() {
        return null;
    }

    public <T> void registerExtensionPoint(ExtensionPoint<T> point, Supplier<T> extension) {

    }

    public <T> void registerExtensionPoint(Class<? extends IExtensionPoint<T>> point, Supplier<T> extension) {

    }
}
