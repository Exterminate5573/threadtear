package me.nov.threadtear.execution.generic;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.util.asm.ASMHelper;

import java.util.Map;

public class UnHider extends Execution {
    public UnHider() {
        super(ExecutionCategory.GENERIC, "UnHider", "This seems important<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.RUNNABLE);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {

        map.values().stream().map(clazz -> clazz.node).forEach(classNode -> {
            if (ASMHelper.isAccess(classNode.access, ACC_SYNTHETIC)) {
                classNode.access &= ~ACC_SYNTHETIC;
            }

            classNode.fields.stream()
                    .filter(node -> ASMHelper.isAccess(node.access, ACC_SYNTHETIC))
                    .forEach(node -> node.access &= ~ACC_SYNTHETIC);

            classNode.methods.forEach(methodNode -> {
                if (ASMHelper.isAccess(methodNode.access, ACC_SYNTHETIC)) {
                    methodNode.access &= ~ACC_SYNTHETIC;
                }

                if (ASMHelper.isAccess(methodNode.access, ACC_BRIDGE)) {
                    methodNode.access &= ~ACC_BRIDGE;
                }
            });
        });

        return true;
    }
}
