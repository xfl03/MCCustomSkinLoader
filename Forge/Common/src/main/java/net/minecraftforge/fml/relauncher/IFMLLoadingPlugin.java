package net.minecraftforge.fml.relauncher;

import java.util.Map;

public interface IFMLLoadingPlugin {
    String[] getASMTransformerClass();
    String getModContainerClass();
    String getSetupClass();
    void injectData(Map<String, Object> data);
    String getAccessTransformerClass();

    @interface Name {
        String value();
    }

    @interface SortingIndex {
        int value();
    }
}
