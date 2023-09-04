package me.nov.threadtear.execution.sb27;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.util.asm.ASMHelper;
import org.objectweb.asm.tree.*;

import java.util.Map;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/*
    Should i move this to SuperblaubeereStringPool?
    TODO: Don't use FieldNode from ASM
    TODO: Array length checks
 */
public class SourceInfoStringObfuscationSB27 extends Execution {
    public SourceInfoStringObfuscationSB27() {
        super(ExecutionCategory.SB27, "Source Info String Obfuscation Removal", "Deobfuscates the source info.<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.RUNNABLE);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {

        map.values().stream().map(clazz -> clazz.node)
                .filter(classNode -> classNode.sourceFile != null)
                .filter(classNode -> classNode.sourceFile.contains("ä"))
                .filter(classNode -> classNode.sourceFile.contains("ü"))
                .filter(classNode -> classNode.sourceFile.contains("ö"))
                .forEach(classNode -> {
                    List<MethodNode> toRemove = new ArrayList<>();

                    classNode.methods.stream()
                            .filter(methodNode -> ASMHelper.isAccess(methodNode.access, ACC_PRIVATE))
                            .filter(methodNode -> ASMHelper.isAccess(methodNode.access, ACC_STATIC))
                            .filter(methodNode -> methodNode.desc.equals("()V"))
                            .forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                                    .filter(node -> node.getOpcode() == PUTSTATIC)
                                    .filter(node -> node.getPrevious().getOpcode() == INVOKEVIRTUAL)
                                    .filter(node -> ASMHelper.isString(node.getPrevious().getPrevious()))
                                    .filter(node -> ASMHelper.getString(node.getPrevious().getPrevious()).equals("ö"))
                                    .map(FieldInsnNode.class::cast)
                                    .filter(node -> node.desc.equals("[Ljava/lang/String;"))
                                    .findFirst().flatMap(node -> classNode.fields.stream().filter(field -> field.desc.equals(node.desc)).filter(field -> field.name.equals(node.name)).findFirst()).ifPresent(field -> {
                                        field.value = classNode.sourceFile.substring(classNode.sourceFile.indexOf("ä") + 1, classNode.sourceFile.lastIndexOf("ü")).split("ö");
                                        toRemove.add(methodNode);
                                    }));

                    classNode.methods.forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                            .filter(node -> node.getOpcode() == GETSTATIC)
                            .filter(node -> ASMHelper.isInteger(node.getNext()))
                            .filter(node -> node.getNext().getNext().getOpcode() == AALOAD)
                            .map(FieldInsnNode.class::cast)
                            .forEach(node -> classNode.fields.stream()
                                    .filter(field -> field.value instanceof String[])
                                    .filter(field -> field.desc.equals(node.desc))
                                    .filter(field -> field.name.equals(node.name))
                                    .findFirst().ifPresent(field -> {
                                        int position = ASMHelper.getInteger(node.getNext());
                                        methodNode.instructions.remove(node.getNext().getNext());
                                        methodNode.instructions.remove(node.getNext());
                                        methodNode.instructions.set(node, new LdcInsnNode(((String[]) field.value)[position]));
                                    })));

                    ASMHelper.findClInit(classNode).ifPresent(clinit -> Arrays.stream(clinit.instructions.toArray())
                            .filter(node -> node.getOpcode() == INVOKESTATIC)
                            .map(MethodInsnNode.class::cast)
                            .filter(node -> node.owner.equals(classNode.name))
                            .filter(node -> toRemove.stream().anyMatch(method -> method.name.equals(node.name) && method.desc.equals(node.desc)))
                            .forEach(node -> clinit.instructions.remove(node)));

                    classNode.methods.removeAll(toRemove);
                    classNode.fields.removeIf(fieldNode -> fieldNode.desc.equals("[Ljava/lang/String;") && fieldNode.value instanceof String[]);
                    toRemove.clear();
                });

        return true;
    }
}
