package me.nov.threadtear.execution.sb27;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.util.asm.ASMHelper;
import org.objectweb.asm.tree.*;

import java.util.*;

/*
    TODO: Cast/Size checks etc
 */
public class StringPoolObfuscationSB27 extends Execution {

    private int changeCount = 0;

    public StringPoolObfuscationSB27() {
        super(ExecutionCategory.SB27, "String Pool Obfuscation Removal", "Removes the string list and puts strings in their proper places.<br>Use this after String Deobfuscation and Number Pool Obfuscation!<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.RUNNABLE);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {
        this.changeCount = 0;

        map.values().forEach(this::decrypt);

        logger.info("Moved {} Strings!", changeCount);

        return true;
    }

    private void decrypt(Clazz c) {
        logger.collectErrors(c);
        ClassNode classNode = c.node;

        List<MethodNode> toRemove = new ArrayList<>();
        Map<Integer, String> strings = new HashMap<>();

        classNode.methods.stream()
                .filter(methodNode -> ASMHelper.isAccess(methodNode.access, ACC_PRIVATE))
                .filter(methodNode -> ASMHelper.isAccess(methodNode.access, ACC_STATIC))
                .filter(methodNode -> methodNode.desc.equals("()V"))
                .forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                        .filter(node -> node.getOpcode() == PUTSTATIC)
                        //.filter(node -> node.getNext().getOpcode() == GETSTATIC)
                        //.filter(node -> node.getPrevious().getOpcode() == ANEWARRAY)
                        .map(FieldInsnNode.class::cast)
                        .filter(node -> node.desc.equals("[Ljava/lang/String;"))
                        .findFirst().flatMap(node -> classNode.fields.stream().filter(field -> field.desc.equals(node.desc)).filter(field -> field.name.equals(node.name)).findFirst()).ifPresent(field -> {
                            int size = strings.size();
                            //TODO: Optimize this as fuck
                            Arrays.stream(methodNode.instructions.toArray())
                                    .filter(insn -> insn.getOpcode() == GETSTATIC)
                                    .filter(insn -> ASMHelper.isInteger(insn.getNext()))
                                    .filter(insn -> ASMHelper.isString(insn.getNext().getNext()))
                                    .filter(insn -> insn.getNext().getNext().getNext().getOpcode() == AASTORE)
                                    .map(FieldInsnNode.class::cast)
                                    .filter(insn -> insn.name.equals(field.name) && insn.desc.equals(field.desc))
                                    .forEach(insn -> strings.put(ASMHelper.getInteger(insn.getNext()), ASMHelper.getString(insn.getNext().getNext())));

                            if (strings.size() != size) {
                                toRemove.add(methodNode);
                                field.value = REMOVEABLE;
                            }
                        }));


        classNode.methods.forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                .filter(node -> node.getOpcode() == GETSTATIC)
                .filter(node -> ASMHelper.isInteger(node.getNext()))
                .filter(node -> node.getNext().getNext().getOpcode() == AALOAD)
                .map(FieldInsnNode.class::cast)
                .forEach(node -> classNode.fields.stream()
                        .filter(field -> field.value instanceof Removeable)
                        .filter(field -> field.desc.equals(node.desc))
                        .findFirst().ifPresent(field -> {
                            int position = ASMHelper.getInteger(node.getNext());
                            methodNode.instructions.remove(node.getNext().getNext());
                            methodNode.instructions.remove(node.getNext());
                            methodNode.instructions.set(node, new LdcInsnNode(strings.get(position)));
                        })));

        ASMHelper.findClInit(classNode).ifPresent(clinit -> Arrays.stream(clinit.instructions.toArray())
                .filter(node -> node.getOpcode() == INVOKESTATIC)
                .map(MethodInsnNode.class::cast)
                .filter(node -> node.owner.equals(classNode.name))
                .filter(node -> toRemove.stream().anyMatch(method -> method.name.equals(node.name) && method.desc.equals(node.desc)))
                .forEach(node -> clinit.instructions.remove(node)));

        classNode.methods.removeAll(toRemove);
        classNode.fields.removeIf(fieldNode -> fieldNode.desc.equals("[Ljava/lang/String;") && fieldNode.value instanceof Removeable);
        changeCount += strings.size();
        toRemove.clear();
        strings.clear();
    }

    @Override
    public String getAuthor() {
        return "Exterminate";
    }
}
