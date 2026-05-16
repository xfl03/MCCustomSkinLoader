package customskinloader.bootstrap.forge.v1;

import java.util.Map;

import net.minecraftforge.fml.relauncher.IFMLCallHook;

public class CallHook implements IFMLCallHook {
    @Override
    public void injectData(Map<String, Object> data) {
        TransformerBootstrap.injectData(data);
    }

    @Override
    public Void call() {
        return null;
    }
}
