package me.nov.threadtear.execution.skidfuscator;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.util.asm.ASMHelper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.*;

public class StringObfuscationSkidfuscator extends Execution {
    public StringObfuscationSkidfuscator() {
        super(ExecutionCategory.SKIDFUSCATOR, "String Obfuscation Removal", "Deobfuscates strings.<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.POSSIBLE_DAMAGE);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {

        map.values().forEach(this::decrypt);

        return true;
    }

    private void decrypt(Clazz c) {
        logger.collectErrors(c);
        ClassNode classNode = c.node;

        HashMap<ClassNode, byte[]> classToKeys = new HashMap<>();

        new ArrayList<>(classNode.methods).forEach(mn -> Arrays.stream(mn.instructions.toArray())
                .filter(node -> node.getOpcode() == INVOKESTATIC)
                .map(MethodInsnNode.class::cast)
                .filter(node -> node.desc.equals("(Ljava/lang/String;I)Ljava/lang/String;"))
                .filter(node -> ASMHelper.isInteger(node.getPrevious()))
                .filter(node -> ASMHelper.isString(node.getPrevious().getPrevious()))
                .forEach(node -> {
                    if (!classToKeys.containsKey(classNode)) {
                        MethodNode decryptMethod = classNode.methods.stream().filter(methodNode -> methodNode.name.equals(node.name)).findFirst().get();
                        classNode.methods.remove(decryptMethod);

                        Arrays.stream(decryptMethod.instructions.toArray())
                                .filter(n -> n.getOpcode() == NEWARRAY)
                                .forEach(n -> {
                                    int length = ASMHelper.getInteger(n.getPrevious());
                                    byte[] keys = new byte[length];

                                    ASMHelper.getInstructionsBetween(n, decryptMethod.instructions.get(decryptMethod.instructions.indexOf(n) + (length * 4))).stream()
                                            .filter(insn -> insn.getOpcode() == BASTORE)
                                            .forEach(insn -> keys[ASMHelper.getInteger(insn.getPrevious().getPrevious())] = (byte) ASMHelper.getInteger(insn.getPrevious()));
                                    classToKeys.put(classNode, keys);
                                });
                    }

                    String string = ASMHelper.getString(node.getPrevious().getPrevious());
                    int xor = ASMHelper.getInteger(node.getPrevious());
                    byte[] keys = classToKeys.get(classNode);

                    mn.instructions.remove(node.getPrevious().getPrevious());
                    mn.instructions.remove(node.getPrevious());
                    mn.instructions.set(node, new LdcInsnNode(decrypt(string, xor, keys)));
                })
        );
    }

    private String decrypt(String string, int xor, byte[] keys) {
        byte[] encrypted = Base64.getDecoder().decode(string.getBytes());
        byte[] key = Integer.toString(xor).getBytes();

        for (int i = 0; i < encrypted.length; i++) {
            encrypted[i] ^= key[i % key.length];
            encrypted[i] ^= keys[i % keys.length];
        }
        return new String(encrypted);
    }
}
