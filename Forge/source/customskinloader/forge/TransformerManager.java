package customskinloader.forge;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import net.minecraftforge.fml.relauncher.FMLRelaunchLog;

public class TransformerManager implements IClassTransformer {
	public static class TransformTarget{
		public String className;
		public String srgName;
		public String mcpName;
		public String desc;
		public TransformTarget(String className,String srgName,String mcpName,String desc){
			this.className=className;
			this.srgName=srgName;
			this.mcpName=mcpName;
			this.desc=desc;
		}
	}
	public interface IMethodTransformer{
		public TransformTarget getTarget();
		public void transform(MethodNode mn);
	}
	private static final IMethodTransformer[] TRANFORMERS={new Transformers.SkinManagerTransformer()};
	private Map<String, Map<String, IMethodTransformer>> map;
	public TransformerManager(){
		map = new HashMap<String, Map<String, IMethodTransformer>>();
		for(IMethodTransformer t:TRANFORMERS)
			addMethodTransformer(t.getTarget(),t);
	}
	private void addMethodTransformer(TransformTarget target, IMethodTransformer transformer) {
        if (!map.containsKey(target.className))
            map.put(target.className, new HashMap<String, IMethodTransformer>());
        map.get(target.className).put(target.srgName + target.desc, transformer);
        map.get(target.className).put(target.mcpName + target.desc, transformer);
    }
	
	//From: https://github.com/RecursiveG/UniSkinMod/blob/1.9.4/src/main/java/org/devinprogress/uniskinmod/coremod/BaseAsmTransformer.java
	@Override
	public byte[] transform(String obfClassName, String className, byte[] bytes) {
		if (!map.containsKey(className)) return bytes;
        Map<String, IMethodTransformer> transMap = map.get(className);
        
        ClassReader cr = new ClassReader(bytes);
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        // NOTE: `map` = convert obfuscated name to srgName;
        List<MethodNode> ml = new ArrayList<MethodNode>();
        ml.addAll(cn.methods);
        for (MethodNode mn : ml) {
            String methodName = FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(obfClassName, mn.name, mn.desc);
            String methodDesc = FMLDeobfuscatingRemapper.INSTANCE.mapMethodDesc(mn.desc);
            if (transMap.containsKey(methodName + methodDesc)) {
                try {
                    FMLRelaunchLog.info("Transforming method %s in class %s(%s)", methodName + methodDesc, obfClassName, className);
                    transMap.get(methodName + methodDesc).transform(mn);
                    FMLRelaunchLog.info("Successfully transformed method %s in class %s(%s)", methodName + methodDesc, obfClassName, className);
                } catch (Exception e) {
                    FMLRelaunchLog.warning("An error happened when transforming method %s in class %s(%s). The whole class was not modified.", methodName + methodDesc, obfClassName, className);
                    e.printStackTrace();
                    return bytes;
                }
            }
        }

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cn.accept(cw);
        return cw.toByteArray();
	}
}
