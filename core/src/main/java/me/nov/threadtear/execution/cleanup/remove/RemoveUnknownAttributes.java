package me.nov.threadtear.execution.cleanup.remove;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import org.objectweb.asm.Attribute;

import java.util.Map;

public class RemoveUnknownAttributes extends Execution {
    public RemoveUnknownAttributes() {
        super(ExecutionCategory.CLEANING, "Remove Unkown Attributes", "Remove unknown attributes.<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.BETTER_DECOMPILE, ExecutionTag.SHRINK);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {

        map.values().stream().map(clazz -> clazz.node).forEach(classNode -> {

            if (classNode.attrs != null)
                classNode.attrs.removeIf(Attribute::isUnknown);

            classNode.methods.forEach(methodNode -> {
                if (methodNode.attrs != null)
                    methodNode.attrs.removeIf(Attribute::isUnknown);
            });

            classNode.fields.forEach(fieldNode -> {
                if (fieldNode.attrs != null)
                    fieldNode.attrs.removeIf(Attribute::isUnknown);
            });

        });

        return true;
    }
}
