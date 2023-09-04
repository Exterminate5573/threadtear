package me.nov.threadtear.execution.cleanup.remove;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class RemoveInvalidPackage extends Execution {

    public RemoveInvalidPackage() {
        super(ExecutionCategory.CLEANING, "Remove Invalid Package", "\"KurwaNaChujTaKlasaRemove\"<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.BETTER_DECOMPILE, ExecutionTag.SHRINK, ExecutionTag.RUNNABLE);
    }

    @Override
    public boolean execute(Map<String, Clazz> map, boolean verbose) {
        AtomicInteger count = new AtomicInteger();

        //TODO: Add a way to properly remove this package
        map.keySet().removeIf(name -> {
            boolean remove = name.startsWith("/////////////////////");
            if (remove) {
                count.getAndIncrement();
            }
            return remove;
        });

        logger.info("Removed {} invalid packages", count.get());

        return true;
    }
}
