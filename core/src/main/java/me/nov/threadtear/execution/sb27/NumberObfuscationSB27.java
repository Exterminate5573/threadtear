package me.nov.threadtear.execution.sb27;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.vm.IVMReferenceHandler;
import org.objectweb.asm.tree.ClassNode;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

public class NumberObfuscationSB27 extends Execution implements IVMReferenceHandler {

  private Map<String, Clazz> classes;
  private int encrypted;
  private int decrypted;

  public NumberObfuscationSB27() {
    super(ExecutionCategory.SB27, "Number Obfuscation Removal", "Nothing here yet.", ExecutionTag.RUNNABLE, ExecutionTag.POSSIBLY_MALICIOUS);
  }

  @Override
  public boolean execute(Map<String, Clazz> map, boolean verbose) {
    return false;
  }

  @Override
  public String getAuthor() {
    return "Exterminate";
  }

  @Override
  public ClassNode tryClassLoad(String name) {
    return null;
  }



}
