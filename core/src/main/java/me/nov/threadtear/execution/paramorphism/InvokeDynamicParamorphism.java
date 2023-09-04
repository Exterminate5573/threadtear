package me.nov.threadtear.execution.paramorphism;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.util.asm.ASMHelper;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class InvokeDynamicParamorphism extends Execution {

    private final Map<Integer, String[][]> lookups = new HashMap<>();

    public InvokeDynamicParamorphism() {
        super(ExecutionCategory.PARAMORPHISM, "Invoke Dynamic Obfuscation Removal.", "Fixes InvokeDynamic Calls.<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.POSSIBLE_DAMAGE);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {

        ClassNode bootstrapClass = searchForBootstrapClass(map);

        StringBuilder key = new StringBuilder();
        ASMHelper.findMethod(bootstrapClass, methodNode -> methodNode.name.equals("<init>")).ifPresent(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                .filter(ASMHelper::isString)
                .map(ASMHelper::getString)
                .forEach(key::append));

        if (key.toString().isBlank()){
            logger.error("Could not find key for InvokeDynamic Obfuscation!");
            return false;
        }

        decode(Base64.getDecoder().decode(key.toString()));

        map.values().stream().map(clazz -> clazz.node).forEach(classNode -> classNode.methods.forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
                .filter(node -> node instanceof InvokeDynamicInsnNode)
                .map(InvokeDynamicInsnNode.class::cast)
                .filter(node -> node.name.equals("call"))
                .forEach(node -> {
                    int hash = classNode.name.replace('/', '.').hashCode() * 31 + methodNode.name.hashCode();
                    int position = (int) ((long) node.bsmArgs[0] & 4294967295L);
                    int type = (int) node.bsmArgs[1];

                    switch (type) {
                        case 1:
                            type = INVOKESTATIC;
                            break;
                        case 2:
                            type = INVOKEVIRTUAL;
                            break;
                        case 3:
                            type = INVOKESPECIAL;
                            break;
                    }

                    if (!lookups.containsKey(hash))
                        return;

                    /*
                    Interface check for runnable deobf? maybe
                     */
                    String[] info = lookups.get(hash)[position];
                    methodNode.instructions.set(node,
                            new MethodInsnNode(
                                    type,
                                    info[0].replace('.', '/'),
                                    info[1],
                                    node.desc,
                                    false
                            ));
                })));

        lookups.clear();

        //TODO: Proper way to remove classes
        map.keySet().remove(bootstrapClass.name);
        map.keySet().remove(bootstrapClass.name.substring(0, bootstrapClass.name.indexOf('⛔') + 1));
        map.keySet().remove(bootstrapClass.name.substring(0, bootstrapClass.name.lastIndexOf('/') + 1) + "Dispatcher️");

        return true;
    }

    private void decode(byte[] byArray) {
        int n = 0;
        int n2 = (byArray[n++] & 0xFF) << 24 | ((byArray[n++] & 0xFF) << 16 | ((byArray[n++] & 0xFF) << 8 | (byArray[n++] & 0xFF)));
        for (int i = 0; i < n2; ++i) {
            int n3 = (byArray[n++] & 0xFF) << 24 | ((byArray[n++] & 0xFF) << 16 | ((byArray[n++] & 0xFF) << 8 | (byArray[n++] & 0xFF)));
            int n4 = (byArray[n++] & 0xFF) << 24 | ((byArray[n++] & 0xFF) << 16 | ((byArray[n++] & 0xFF) << 8 | (byArray[n++] & 0xFF)));
            String[][] stringArrayArray = new String[n4][];
            this.lookups.put(n3, stringArrayArray);
            for (int j = 0; j < n4; ++j) {
                int n5 = (byArray[n++] & 0xFF) << 8 | byArray[n++] & 0xFF;
                int n6 = 0;
                do {
                    byArray[n + n6] = (byte) (byArray[n + n6] ^ 0xAA);
                } while (++n6 < n5);
                String string = new String(byArray, n, n5);
                n += n5;
                int n7 = (byArray[n++] & 0xFF) << 8 | byArray[n++] & 0xFF;
                n6 = 0;
                do {
                    byArray[n + n6] = (byte) (byArray[n + n6] ^ 0xAA);
                } while (++n6 < n7);
                String string2 = new String(byArray, n, n7);
                n += n7;
                stringArrayArray[j] = new String[]{string, string2};
            }
        }
    }

    private ClassNode searchForBootstrapClass(Map<String, Clazz> map) {
        return map.values().stream().map(clazz -> clazz.node)
                .filter(classNode -> classNode.name.endsWith("⛔$0"))
                .findFirst()
                .orElseThrow();
    }

}
