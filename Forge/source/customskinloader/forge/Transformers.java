package customskinloader.forge;

import org.objectweb.asm.tree.MethodNode;

import customskinloader.forge.TransformerManager.IMethodTransformer;
import customskinloader.forge.TransformerManager.TransformTarget;

public class Transformers {
	public static class SkinManagerTransformer implements IMethodTransformer{
		TransformTarget target=new TransformTarget("net.minecraft.client.Minecraft","init","func_71384_a","()V");
		@Override
		public TransformTarget getTarget() {
			return target;
		}

		@Override
		public void transform(MethodNode mn) {
			//TODO
		}
		
	}

}
