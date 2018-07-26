package customskinloader.forge.remapper;

import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

// https://github.com/NekoCaffeine/Alchemy/blob/rebirth/src/main/java/index/alchemy/util/ASMHelper.java#L652-L672
public class MethodNameRemapper extends Remapper {

    private final String owner, srcName, newName;

    public MethodNameRemapper(String owner, String srcName, String newName) {
        this.owner = owner;
        this.srcName = srcName;
        this.newName = newName;
    }

    @Override
    public String mapMethodName(String owner, String name, String desc) {
        return this.owner.equals(owner) && srcName.equals(name) ? newName : super.map(name);
    }

    public static void remapMethodName(ClassNode cn, String owner, String srcName, String newName) {
        cn.accept(new ClassRemapper(null, new MethodNameRemapper(owner, srcName, newName)));
    }
}