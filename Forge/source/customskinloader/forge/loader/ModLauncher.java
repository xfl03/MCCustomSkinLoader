package customskinloader.forge.loader;

import customskinloader.forge.TransformerManager;
import customskinloader.forge.transformer.FakeSkinManagerTransformer;
import customskinloader.forge.transformer.PlayerTabTransformer;
import customskinloader.forge.transformer.SkinManagerTransformer;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

/**
 * ModLauncher for 1.13+
 * @deprecated Use JavaScript Instead
 */
@Deprecated
public class ModLauncher {

    private static final TransformerManager.IMethodTransformer[] TRANFORMERS = {
            new SkinManagerTransformer.InitTransformer(),
            new SkinManagerTransformer.LoadSkinTransformer(),
            new SkinManagerTransformer.LoadProfileTexturesTransformer(),
            new SkinManagerTransformer.LoadSkinFromCacheTransformer(),
            new PlayerTabTransformer.ScoreObjectiveTransformer(),
            new FakeSkinManagerTransformer.InitTransformer()
    };
    private TransformerManager transformerManager = new TransformerManager(TRANFORMERS);

    public ClassNode transform(ClassNode input) {
        for (MethodNode mn : input.methods) {
            transformerManager.transform(input, mn, input.name, mn.name, mn.desc);
        }
        return input;
    }

    //Singleton
    private volatile static ModLauncher INSTANCE = null;

    public static ModLauncher instance() {
        if (INSTANCE == null) {
            synchronized (ModLauncher.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ModLauncher();
                }
            }
        }
        return INSTANCE;
    }

}
