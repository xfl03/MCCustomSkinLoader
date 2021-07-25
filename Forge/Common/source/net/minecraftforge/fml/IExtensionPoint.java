package net.minecraftforge.fml;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

public interface IExtensionPoint<T> {
    class DisplayTest implements IExtensionPoint<DisplayTest> {
        public DisplayTest(Supplier<String> suppliedVersion, BiPredicate<String, Boolean> remoteVersionTest) {}
    }
}
