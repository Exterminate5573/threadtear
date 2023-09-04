package me.nov.threadtear.execution.generic;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.util.asm.ASMHelper;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.Arrays;
import java.util.Map;

//idfk
public class StackOperationFixer extends Execution {


    public StackOperationFixer() {
        super(ExecutionCategory.GENERIC, "Stack Operation Fixer", "Fixes the stack operations (idk).<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.RUNNABLE);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {

        map.values().stream().map(clazz -> clazz.node).forEach(classNode -> classNode.methods.forEach(methodNode -> {
            transformNormally(methodNode);

            if (Arrays.stream(methodNode.instructions.toArray()).anyMatch(node -> node.getOpcode() >= POP && node.getOpcode() <= SWAP))
                transformUsingAnalyzer(classNode, methodNode);
        }));
        return true;
    }

    //Idk if this works XD
    private void transformNormally(MethodNode methodNode) {
        boolean modified;
        do {
            modified = false;
            for (AbstractInsnNode node : methodNode.instructions.toArray()) {
                switch (node.getOpcode()) {
                    case POP -> {
                        int type = check(node.getPrevious());
                        if (type == 1) {
                            methodNode.instructions.remove(node.getPrevious());
                            methodNode.instructions.remove(node);
                            modified = true;
                        }
                    }
                    case POP2 -> {
                        int type = check(node.getPrevious());
                        if (type == 1 && check(node.getPrevious().getPrevious()) == 1) {
                            methodNode.instructions.remove(node.getPrevious().getPrevious());
                            methodNode.instructions.remove(node.getPrevious());
                            methodNode.instructions.remove(node);
                            modified = true;
                        } else if (type == 2) {
                            methodNode.instructions.remove(node.getPrevious());
                            methodNode.instructions.remove(node);
                            modified = true;
                        }
                    }
                    case DUP -> {
                        int type = check(node.getPrevious());
                        if (type == 1) {
                            methodNode.instructions.insert(node.getPrevious(), node.getPrevious().clone(null));
                            methodNode.instructions.remove(node);
                            modified = true;
                        }
                    }
                    case DUP_X1, DUP2_X1, DUP2_X2, DUP_X2 -> {
                    }
                    case DUP2 -> {
                        int type = check(node.getPrevious());
                        if (type == 2) {
                            methodNode.instructions.insert(node.getPrevious(), node.getPrevious().clone(null));
                            methodNode.instructions.remove(node);
                            modified = true;
                        }
                    }
                    case SWAP -> {
                        int firstType = check(node.getPrevious().getPrevious());
                        int secondType = check(node.getPrevious());

                        if (secondType == 1 && firstType == 1) {
                            AbstractInsnNode cloned = node.getPrevious().getPrevious();

                            methodNode.instructions.remove(node.getPrevious().getPrevious());
                            methodNode.instructions.set(node, cloned.clone(null));
                            modified = true;
                        }
                    }
                }
            }
        } while (modified);
    }

    private void transformUsingAnalyzer(ClassNode classNode, MethodNode methodNode) {
        /*Map<AbstractInsnNode, Frame<SourceValue>> frames = analyzeSource(classNode, methodNode);

        if (frames == null)
            return;

        boolean modified;
        do {
            modified = false;
            for (AbstractInsnNode node : methodNode.instructions.toArray()) {
                Frame<SourceValue> frame = frames.get(node);

                if (frame == null)
                    continue;

                switch (node.getOpcode()) {
                    case POP: {
                        break;
                    }
                    case POP2: {
                    }
                    case DUP: {
                        break;
                    }
                    case DUP_X1: {
                        break;
                    }
                    case DUP_X2: {
                        break;
                    }
                    case DUP2: {
                        break;
                    }
                    case DUP2_X1: {
                        break;
                    }
                    case DUP2_X2: {
                        break;
                    }
                    case SWAP: {
                        break;
                    }
                }
            }
        } while (modified);*/
    }

    private int check(AbstractInsnNode node) {
        if (ASMHelper.isLong(node) || ASMHelper.isDouble(node)) {
            return 2;
        } else if (ASMHelper.isInteger(node) || ASMHelper.isFloat(node) || node instanceof LdcInsnNode) {
            return 1;
        }

        return 0;
    }
}
