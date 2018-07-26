package customskinloader.forge.remapper;

import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

// https://github.com/NekoCaffeine/Alchemy/blob/rebirth/src/main/java/index/alchemy/util/ASMHelper.java#L652-L672
public class ClassNameRemapper extends Remapper {

    private final String srcName, newName;

    public ClassNameRemapper(String srcName, String newName) {
        this.srcName = srcName;
        this.newName = newName;
    }

    @Override
    public String map(String typeName) {
        return srcName.equals(typeName) ? newName : super.map(typeName);
    }

    public static void remapClassName(ClassNode cn, String srcName, String newName) {
        cn.accept(new ClassRemapper(null, new ClassNameRemapper(srcName, newName)));
    }
}