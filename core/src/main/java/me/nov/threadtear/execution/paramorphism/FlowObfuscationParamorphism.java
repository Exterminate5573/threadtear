package me.nov.threadtear.execution.paramorphism;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.VarInsnNode;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class FlowObfuscationParamorphism extends Execution {
    public FlowObfuscationParamorphism() {
        super(ExecutionCategory.PARAMORPHISM, "Flow Obfuscation Removal", "Removes Paramorphism Flow Obfuscation.<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.POSSIBLE_DAMAGE, ExecutionTag.SHRINK);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {
        AtomicInteger jumpsRemoved = new AtomicInteger();
        AtomicInteger tryCatchRemoved = new AtomicInteger();

        map.values().stream().map(clazz -> clazz.node)
                .flatMap(classNode -> classNode.methods.stream())
                .forEach(methodNode -> {
                    Arrays.stream(methodNode.instructions.toArray())
                            .filter(node -> node.getOpcode() == GOTO)
                            .map(JumpInsnNode.class::cast)
                            .forEach(node -> {
                                LabelNode labelNode = node.label;
                                if (labelNode.getNext().getOpcode() == GOTO
                                        && methodNode.instructions.indexOf(labelNode.getNext()) > methodNode.instructions.indexOf(node)) {
                                    methodNode.instructions.set(node, new JumpInsnNode(GOTO, ((JumpInsnNode) labelNode.getNext()).label));
                                } else if (labelNode.getNext() instanceof JumpInsnNode
                                        && labelNode.getNext().getNext() instanceof VarInsnNode
                                        && labelNode.getNext().getNext().getNext() instanceof JumpInsnNode) {
                                    methodNode.instructions.set(node, new JumpInsnNode(labelNode.getNext().getOpcode(), ((JumpInsnNode) labelNode.getNext()).label));
                                }
                                jumpsRemoved.incrementAndGet();
                            });

                    methodNode.tryCatchBlocks.removeIf(tbce -> {
                        boolean remove = tbce.type == null || tbce.type.isEmpty() || tbce.type.isBlank();
                        if (remove) {
                            tryCatchRemoved.incrementAndGet();
                        }
                        return remove;
                    });
                });

        logger.info("Removed {} jumps and {} try-catch blocks!", jumpsRemoved.get(), tryCatchRemoved.get());

        return true;
    }
}
