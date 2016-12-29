package customskinloader.forge;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import customskinloader.forge.TransformerManager.IMethodTransformer;
import customskinloader.forge.TransformerManager.TransformTarget;

public class Transformers {
	@TransformTarget(className="net.minecraft.client.Minecraft",
			methodNames={"func_71384_a","init","startGame"},
			desc="()V")
	public static class SkinManagerTransformer implements IMethodTransformer{
		@Override
		public void transform(MethodNode mn) {
			//TODO transform here
			AbstractInsnNode node=mn.instructions.getFirst();
			while(node!=null){
				if(node.getOpcode()==Opcodes.NEW){
					TypeInsnNode typeNode=(TypeInsnNode) node;
					if(typeNode.desc.equalsIgnoreCase("net/minecraft/client/resources/SkinManager")){
						typeNode.desc="customskinloader/fake/FakeSkinManager";
					}
				}
				if(node.getOpcode()==Opcodes.INVOKESPECIAL){
					MethodInsnNode min=(MethodInsnNode) node;
					if(min.owner.equalsIgnoreCase("net/minecraft/client/resources/SkinManager")){
						min.owner="customskinloader/fake/FakeSkinManager";
					}
				}
				node=node.getNext();
			}
		}
		
	}

}
