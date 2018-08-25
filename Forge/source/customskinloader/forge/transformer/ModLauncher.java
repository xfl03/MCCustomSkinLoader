package customskinloader.forge.transformer;

import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.ITransformerVotingContext;
import cpw.mods.modlauncher.api.TransformerVoteResult;
import customskinloader.forge.TransformerManager;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.tree.ClassNode;

import java.util.HashSet;
import java.util.Set;

public class ModLauncher implements ITransformer<ClassNode> {
    private TransformerManager transformerManager = new TransformerManager();

    //ModLauncher
    public ClassNode transform(ClassNode input, ITransformerVotingContext context) {
        return transformerManager.transform(input);
    }

    public TransformerVoteResult castVote(ITransformerVotingContext context) {
        return TransformerVoteResult.YES;
    }

    public Set<Target> targets() {
        Set<Target> sets = new HashSet<Target>();
        for (String className : transformerManager.map.keySet()) {
            String obfName = FMLDeobfuscatingRemapper.INSTANCE.unmap(className);
            sets.add(Target.targetClass(obfName));
        }
        return sets;
    }
}
