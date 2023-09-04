package me.nov.threadtear.execution.sb27;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.util.asm.ASMHelper;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class FlowObfuscationSB27 extends Execution {
    public FlowObfuscationSB27() {
        super(ExecutionCategory.SB27, "Flow Obfuscation Removal", "Removes Flow Obfuscation.<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.POSSIBLE_DAMAGE, ExecutionTag.BETTER_DECOMPILE);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {

        map.values().forEach(this::decrypt);

        return true;
    }

    private void decrypt(Clazz c) {
        logger.collectErrors(c);
        ClassNode classNode = c.node;

        List<MethodNode> toRemove = new ArrayList<>();

        classNode.methods.forEach(methodNode -> {
            boolean modified;
            do {
                modified = false;

                methodNode.instructions.resetLabels();
                for (AbstractInsnNode node : methodNode.instructions.toArray()) {
                    if (ASMHelper.isString(node)
                            && node.getNext() instanceof MethodInsnNode
                            && ((MethodInsnNode) node.getNext()).name.equals("length")
                            && ((MethodInsnNode) node.getNext()).desc.equals("()I")
                            && ((MethodInsnNode) node.getNext()).owner.equals("java/lang/String")
                            && node.getNext().getNext().getOpcode() == POP) {

                        methodNode.instructions.remove(node.getNext().getNext());
                        methodNode.instructions.remove(node.getNext());
                        methodNode.instructions.remove(node);
                        modified = true;
                    } else if (ASMHelper.isString(node)
                            && node.getNext() instanceof MethodInsnNode
                            && ((MethodInsnNode) node.getNext()).name.equals("length")
                            && ((MethodInsnNode) node.getNext()).desc.equals("()I")
                            && ((MethodInsnNode) node.getNext()).owner.equals("java/lang/String")
                            && node.getNext().getNext().getOpcode() == POP2) {

                        methodNode.instructions.remove(node.getNext().getNext());
                        methodNode.instructions.remove(node.getNext());
                        methodNode.instructions.set(node, new InsnNode(POP));
                        modified = true;
                    } else if (node.getOpcode() == POP && ASMHelper.isInteger(node.getPrevious())) {
                        methodNode.instructions.remove(node.getPrevious());
                        methodNode.instructions.remove(node);
                        modified = true;
                    } else if (node.getOpcode() == POP2 && ASMHelper.isInteger(node.getPrevious())) {
                        methodNode.instructions.remove(node.getPrevious());
                        methodNode.instructions.set(node, new InsnNode(POP));
                        modified = true;
                    } else if (node instanceof MethodInsnNode
                            && ((MethodInsnNode) node).owner.equals("java/lang/String")
                            && ((MethodInsnNode) node).name.equals("valueOf")
                            && ((MethodInsnNode) node).desc.equals("(Ljava/lang/Object;)Ljava/lang/String;")
                            && node.getPrevious() instanceof MethodInsnNode
                            && ((MethodInsnNode) node.getPrevious()).owner.equals("java/lang/StringBuilder")) {

                        methodNode.instructions.set(node, new MethodInsnNode(INVOKEVIRTUAL, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
                        modified = true;

                    } else if (node instanceof JumpInsnNode && node.getOpcode() != GOTO) {
                        JumpInsnNode jump = (JumpInsnNode) node;
                        LabelNode labelNode = jump.label;
                        Boolean result = resolveJump(jump);
                        if (result != null) {
                            if (result) {
                                if (ASMHelper.isDoubleIf(jump)) {
                                    methodNode.instructions.remove(node.getPrevious().getPrevious());
                                }

                                methodNode.instructions.remove(node.getPrevious());
                                methodNode.instructions.remove(node);
                            } else {
                                if (ASMHelper.isDoubleIf(jump)) {
                                    methodNode.instructions.remove(node.getPrevious().getPrevious());
                                    methodNode.instructions.remove(node.getPrevious());
                                } else {
                                    methodNode.instructions.remove(node.getPrevious());
                                }

                                if (methodNode.instructions.indexOf(jump) > methodNode.instructions.indexOf(jump.label)) {
                                    methodNode.instructions.remove(jump);
                                } else {
                                    if (jump.getNext().getOpcode() == ACONST_NULL) {
                                        methodNode.instructions.remove(node.getNext().getNext());
                                        methodNode.instructions.remove(node.getNext());
                                    } else if (jump.getNext().getOpcode() == RETURN) {
                                        methodNode.instructions.remove(node.getNext());
                                    } else if (jump.getNext().getNext().getOpcode() >= IRETURN && jump.getNext().getNext().getOpcode() <= ARETURN) {
                                        methodNode.instructions.remove(node.getNext().getNext());
                                        methodNode.instructions.remove(node.getNext());
                                    }

                                    methodNode.instructions.set(node, new JumpInsnNode(GOTO, labelNode));
                                }
                                modified = true;
                            }
                        }

                        if (node.getPrevious() instanceof MethodInsnNode && ((MethodInsnNode) node.getPrevious()).owner.equals(classNode.name)) {
                            AtomicBoolean bruhModified = new AtomicBoolean();
                            MethodInsnNode methodInsnNode = (MethodInsnNode) node.getPrevious();

                            //TODO: Fix boiler plate
                            if (methodInsnNode.desc.endsWith("Z")) {
                                ASMHelper.findMethod(classNode, method -> method.name.equals(methodInsnNode.name) && method.desc.equals(methodInsnNode.desc)).ifPresent(method -> Arrays.stream(method.instructions.toArray())
                                        .filter(insn -> insn instanceof JumpInsnNode)
                                        .filter(insn -> insn.getOpcode() != GOTO)
                                        .findFirst().ifPresent(insn -> {
                                            methodNode.instructions.remove(methodInsnNode);
                                            methodNode.instructions.set(node, new JumpInsnNode(insn.getOpcode(), ((JumpInsnNode) node).label));

                                            toRemove.add(method);
                                            bruhModified.set(true);
                                        }));
                            } else if (methodInsnNode.desc.endsWith("I")) {
                                ASMHelper.findMethod(classNode, method -> method.name.equals(methodInsnNode.name) && method.desc.equals(methodInsnNode.desc)).ifPresent(method -> Arrays.stream(method.instructions.toArray())
                                        .filter(insn -> insn.getOpcode() >= LCMP)
                                        .filter(insn -> insn.getOpcode() <= DCMPG)
                                        .findFirst().ifPresent(insn -> {
                                            methodNode.instructions.set(node.getPrevious(), new InsnNode(insn.getOpcode()));

                                            toRemove.add(method);
                                            bruhModified.set(true);
                                        }));
                            }
                            modified = bruhModified.get();
                        }
                    }
                }
            } while (modified);
        });

        classNode.methods.removeAll(toRemove);
        toRemove.clear();
    }

    private Boolean resolveJump(JumpInsnNode jump) {
        if (jump.getOpcode() == IFNULL && jump.getPrevious().getOpcode() == ACONST_NULL) {
            return false;
        } else if (ASMHelper.isDoubleIf(jump) && ASMHelper.isInteger(jump.getPrevious()) && ASMHelper.isInteger(jump.getPrevious().getPrevious())) {
            int first = ASMHelper.getInteger(jump.getPrevious().getPrevious());
            int second = ASMHelper.getInteger(jump.getPrevious());

            switch (jump.getOpcode()) {
                case IF_ICMPNE: {
                    return first == second;
                }
                case IF_ICMPEQ: {
                    return first != second;
                }
                case IF_ICMPLT: {
                    return first >= second;
                }
                case IF_ICMPGE: {
                    return first < second;
                }
                case IF_ICMPGT: {
                    return first <= second;
                }
                case IF_ICMPLE: {
                    return first > second;
                }
            }
        } else if (ASMHelper.isSingleIf(jump) && ASMHelper.isInteger(jump.getPrevious())) {
            int value = ASMHelper.getInteger(jump.getPrevious());

            switch (jump.getOpcode()) {
                case IFNE: {
                    return value == 0;
                }
                case IFEQ: {
                    return value == 1;
                }
                case IFGE: {
                    return value < 0;
                }
                case IFGT: {
                    return value <= 0;
                }
                case IFLE: {
                    return value > 0;
                }
                case IFLT: {
                    return value >= 0;
                }
            }
        }

        return null;
    }
}
