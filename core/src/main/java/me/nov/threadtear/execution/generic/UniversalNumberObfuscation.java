package me.nov.threadtear.execution.generic;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.util.MathHelper;
import me.nov.threadtear.util.asm.ASMHelper;
import org.objectweb.asm.tree.*;

import java.util.Map;

public class UniversalNumberObfuscation extends Execution {

    //TODO: extended?
    private final boolean extended = true;

    public UniversalNumberObfuscation() {
        super(ExecutionCategory.GENERIC, "Number Obfuscation Removal", "Universal Number Obfuscation Removal<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.RUNNABLE);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {

        map.values().forEach(this::decrypt);

        return true;
    }

    private void decrypt(Clazz c) {
        logger.collectErrors(c);
        ClassNode classNode = c.node;

        classNode.methods.forEach(methodNode -> transform(classNode, methodNode));
    }

    public void transform(ClassNode classNode, MethodNode methodNode) {
        boolean modified;
        do {
            modified = false;

            for (AbstractInsnNode node : methodNode.instructions.toArray()) {
                if (ASMHelper.isString(node)
                        && node.getNext() instanceof MethodInsnNode
                        && ((MethodInsnNode) node.getNext()).name.equals("length")
                        && ((MethodInsnNode) node.getNext()).owner.equals("java/lang/String")) {

                    methodNode.instructions.remove(node.getNext());
                    methodNode.instructions.set(node, ASMHelper.getNumber(ASMHelper.getString(node).length()));
                    modified = true;
                } else if (node.getOpcode() == INEG || node.getOpcode() == LNEG) {
                    if (ASMHelper.isInteger(node.getPrevious())) {
                        int number = -ASMHelper.getInteger(node.getPrevious());

                        methodNode.instructions.remove(node.getPrevious());
                        methodNode.instructions.set(node, ASMHelper.getNumber(number));
                        modified = true;
                    } else if (ASMHelper.isLong(node.getPrevious())) {
                        long number = -ASMHelper.getLong(node.getPrevious());

                        methodNode.instructions.remove(node.getPrevious());
                        methodNode.instructions.set(node, ASMHelper.getNumber(number));
                        modified = true;
                    }
                } else if ((node.getOpcode() >= IADD && node.getOpcode() <= LXOR)) {
                    if (ASMHelper.isInteger(node.getPrevious().getPrevious()) && ASMHelper.isInteger(node.getPrevious())) {
                        int first = ASMHelper.getInteger(node.getPrevious().getPrevious());
                        int second = ASMHelper.getInteger(node.getPrevious());

                        Integer product = MathHelper.doMath(node.getOpcode(), first, second);
                        if (product != null) {
                            methodNode.instructions.remove(node.getPrevious().getPrevious());
                            methodNode.instructions.remove(node.getPrevious());
                            methodNode.instructions.set(node, ASMHelper.getNumber(product));
                            modified = true;
                        }
                    } else if (ASMHelper.isLong(node.getPrevious().getPrevious()) && ASMHelper.isLong(node.getPrevious())) {
                        long first = ASMHelper.getLong(node.getPrevious().getPrevious());
                        long second = ASMHelper.getLong(node.getPrevious());

                        Long product = MathHelper.doMath(node.getOpcode(), first, second);
                        if (product != null) {
                            methodNode.instructions.remove(node.getPrevious().getPrevious());
                            methodNode.instructions.remove(node.getPrevious());
                            methodNode.instructions.set(node, ASMHelper.getNumber(product));
                            modified = true;
                        }
                    } else if ((ASMHelper.isLong(node.getPrevious().getPrevious()) && ASMHelper.isInteger(node.getPrevious()))) {
                        long first = ASMHelper.getLong(node.getPrevious().getPrevious());
                        long second = ASMHelper.getInteger(node.getPrevious());

                        Long product = MathHelper.doMath(node.getOpcode(), first, second);
                        if (product != null) {
                            methodNode.instructions.remove(node.getPrevious().getPrevious());
                            methodNode.instructions.remove(node.getPrevious());
                            methodNode.instructions.set(node, ASMHelper.getNumber(product));
                            modified = true;
                        }
                    } else if ((ASMHelper.isInteger(node.getPrevious().getPrevious()) && ASMHelper.isLong(node.getPrevious()))) {
                        long first = ASMHelper.getInteger(node.getPrevious().getPrevious());
                        long second = ASMHelper.getLong(node.getPrevious());

                        Long product = MathHelper.doMath(node.getOpcode(), first, second);
                        if (product != null) {
                            methodNode.instructions.remove(node.getPrevious().getPrevious());
                            methodNode.instructions.remove(node.getPrevious());
                            methodNode.instructions.set(node, ASMHelper.getNumber(product));
                            modified = true;
                        }
                    }
                } else if (extended && (ASMHelper.isLong(node) && ASMHelper.isLong(node.getNext()) && node.getNext().getNext().getOpcode() == LCMP)) {
                    int result = Long.compare(ASMHelper.getLong(node), ASMHelper.getLong(node.getNext()));

                    methodNode.instructions.remove(node.getNext().getNext());
                    methodNode.instructions.remove(node.getNext());

                    methodNode.instructions.set(node, ASMHelper.getNumber(result));
                } else if (extended && (node instanceof FieldInsnNode && ((FieldInsnNode) node).desc.equals("J") && node.getOpcode() == GETSTATIC
                        && ASMHelper.isLong(node.getNext()) && node.getNext().getNext().getOpcode() == LCMP)) {

                    int result = Long.compare(getFieldValue(classNode, ((FieldInsnNode) node).name), ASMHelper.getLong(node.getNext()));

                    methodNode.instructions.remove(node.getNext().getNext());
                    methodNode.instructions.remove(node.getNext());

                    methodNode.instructions.set(node, ASMHelper.getNumber(result));
                } else if (ASMHelper.isString(node) && ASMHelper.isInteger(node.getNext()) && ASMHelper.isMethod(node.getNext().getNext(), "java/lang/Integer", "parseInt", "(Ljava/lang/String;I)I")) {
                    int value = Integer.parseInt((String) ((LdcInsnNode) node).cst, ((IntInsnNode) node.getNext()).operand);
                    methodNode.instructions.remove(node.getNext().getNext());
                    methodNode.instructions.remove(node.getNext());
                    methodNode.instructions.set(node, new LdcInsnNode(value));
                    modified = true;
                } else if (ASMHelper.isString(node) && ASMHelper.isMethod(node.getNext(), "java/lang/String", "hashCode", "()I")) {
                    int val = ((LdcInsnNode) node).cst.hashCode();
                    methodNode.instructions.remove(node.getNext());
                    methodNode.instructions.set(node, new LdcInsnNode(val));
                    modified = true;
                }
            }
        } while (modified);
    }

    private long getFieldValue(ClassNode classNode, String name) {
        return classNode.fields.stream()
                .filter(fieldNode -> fieldNode.name.equals(name))
                .filter(fieldNode -> fieldNode.value != null)
                .map(fieldNode -> (long) fieldNode.value)
                .findFirst()
                .orElse(0L);
    }
}
