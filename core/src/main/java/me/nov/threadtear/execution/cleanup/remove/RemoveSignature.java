package me.nov.threadtear.execution.cleanup.remove;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;

import java.util.Map;

public class RemoveSignature extends Execution {
    public RemoveSignature() {
        super(ExecutionCategory.CLEANING, "Remove Signature", "Remove class signatures.<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.RUNNABLE);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {

        map.values().forEach(clazz -> {
            clazz.node.signature = null;
            clazz.node.methods.forEach(methodNode -> methodNode.signature = null);
            clazz.node.fields.forEach(fieldNode -> fieldNode.signature = null);
        });

        return true;
    }
}
