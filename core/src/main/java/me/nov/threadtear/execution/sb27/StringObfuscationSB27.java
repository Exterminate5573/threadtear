package me.nov.threadtear.execution.sb27;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.vm.VM;
import org.benf.cfr.reader.bytecode.analysis.parse.expression.AbstractNewArray;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

public class StringObfuscationSB27 extends Execution {

  private Map<String, Clazz> classes;
  private int countSuccess;
  private int countFailure;

  public StringObfuscationSB27() {
    super(ExecutionCategory.SB27, "String Obfuscation Removal", "Deobfuscates strings.<br>This is still a work in progress and might not work 100%!", ExecutionTag.RUNNABLE, ExecutionTag.POSSIBLY_MALICIOUS);
  }

  @Override
  public boolean execute(Map<String, Clazz> map, boolean verbose) {
    classes = map;
    this.countFailure = 0;
    this.countSuccess = 0;
    classes.values().forEach(this::decrypt);

    logger.info("Successfully decrypted {} strings and failed to decrypt {} strings!", countSuccess, countFailure);

    return true;
  }

  private void decrypt(Clazz c) {
    logger.collectErrors(c);
    ClassNode cn = c.node;

    logger.info("Class: " + cn.name);

    MethodNode clinit = getClinit(cn);

    if (clinit == null) {
      logger.error("Clinit not found, SB27 string deobfuscation failed!");
      return;
    }

    MethodNode handleStrings = null;

    //Find all invokestatics
    for (AbstractInsnNode ain : clinit.instructions) {
      if (ain.getOpcode() != Opcodes.INVOKESTATIC) continue;

      //Detect which one handles strings
      //We should do this by finding if it creates a new string array at the start of the method
      MethodInsnNode newMethod = (MethodInsnNode) ain;
      MethodNode method = getMethod(cn, newMethod.name);
      if (method == null) {
          logger.error("Method not found, SB27 string deobfuscation failed!");
          return;
      }

      for (AbstractInsnNode ain2 : method.instructions) {
          if (ain2.getOpcode() == Opcodes.ANEWARRAY) {
              //TODO: There has to be a better method right?
              if (((TypeInsnNode) ain2).desc.equals("java/lang/String")) {
                  handleStrings = method;
                  logger.info("Found handleStrings method: " + handleStrings.name);
                  break;
              }
          }
      }

    }

    if (handleStrings == null) {
        logger.info("handleStrings method not found, skipping class.");
        return;
    }

    Map<String, MethodNode> decryptMethods = new HashMap<>();

    for (MethodNode m : cn.methods) {
        label:
        for (AbstractInsnNode ain : m.instructions) {
          if (ain.getOpcode() == Opcodes.LDC) {
            LdcInsnNode ldc = (LdcInsnNode) ain;
            if (ldc.cst instanceof String string) {
                switch (string) {
                    case "AES":
                        decryptMethods.put("AES", m);
                        break label;
                    case "DES":
                        decryptMethods.put("DES", m);
                        break label;
                    case "Blowfish":
                        decryptMethods.put("Blowfish", m);
                        break label;
                }
                //TODO: XOR
            }
          }
        }
    }

    logger.info("Found decrypt methods: " + decryptMethods.size());

      for (AbstractInsnNode ain : handleStrings.instructions) {
          if (ain.getOpcode() == Opcodes.INVOKESTATIC) {
              MethodInsnNode min = (MethodInsnNode) ain;
              if (min.desc.equals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")) {

                  String input;
                  String key;
                  String method;

                  input = (String) ((LdcInsnNode)min.getPrevious().getPrevious()).cst;
                  key = (String) ((LdcInsnNode)min.getPrevious()).cst;
                  method = min.name;

                  String methodType = null;

                  //Find decrypt method
                  for (Map.Entry<String, MethodNode> entry : decryptMethods.entrySet()) {
                      if (Objects.equals(entry.getValue().name, method)) {
                          methodType = entry.getKey();
                          break;
                      }
                  }

                  //TODO: Better way to do XOR

                  if (methodType == null) {
                      methodType = "XOR";
                  }

                  String decryptedString = null;
                  switch (methodType) {
                      case "AES" -> decryptedString = decryptAES(input, key);
                      case "DES" -> decryptedString = decryptDES(input, key);
                      case "Blowfish" -> decryptedString = decryptBlowfish(input, key);
                      case "XOR" -> decryptedString = decryptXOR(input, key);
                  }

                  //Replace with decrypted string, We need to replace the invokestatic, ldc, ldc, IALOAD with a single ldc
                  if (decryptedString != null) {
                      MethodNode node = getMethod(cn, min.name);
                      if (node == null) {
                          logger.error("Method not found, SB27 string deobfuscation failed!");
                          return;
                      }

                      //Remove instructions
                      //node.instructions.remove(min.getPrevious().getPrevious().getPrevious());
                      node.instructions.remove(min.getPrevious().getPrevious());
                      node.instructions.remove(min.getPrevious());

                      node.instructions.insert(min, new LdcInsnNode(decryptedString));

                      node.instructions.remove(min);

                      this.countSuccess++;
                  } else {
                      logger.info("Failed to decrypt string: " + input);
                      this.countFailure++;
                  }

              } else {
                    logger.info("Unknown decrypt method: " + min.desc);
                    this.countFailure++;
              }
          }
      }


  }

  @Override
  public String getAuthor() {
    return "Exterminate";
  }

  private MethodNode getClinit(ClassNode klass) {
    for (MethodNode method : klass.methods) {
      if (method.name.equals("<clinit>")) return method;
    }

    return null;
  }

  private MethodNode getMethod(ClassNode klass, String name) {
    for (MethodNode method : klass.methods) {
      if (method.name.equals(name)) return method;
    }

    return null;
  }

  private static String decryptBlowfish(String input, String key) {
    try {
      SecretKeySpec keySpec = new SecretKeySpec(MessageDigest.getInstance("MD5").digest(key.getBytes(StandardCharsets.UTF_8)), "Blowfish");
      Cipher cipher = Cipher.getInstance("Blowfish");
      cipher.init(Cipher.DECRYPT_MODE, keySpec);
      return new String(cipher.doFinal(Base64.getDecoder().decode(input.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
    }
    catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private static String decryptDES(String input, String key) {
    try {
      SecretKeySpec keySpec = new SecretKeySpec(Arrays.copyOf(MessageDigest.getInstance("MD5").digest(key.getBytes(StandardCharsets.UTF_8)), 8), "DES");
      Cipher cipher = Cipher.getInstance("DES");
      cipher.init(Cipher.DECRYPT_MODE, keySpec);
      return new String(cipher.doFinal(Base64.getDecoder().decode(input.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
    }
    catch (Exception exception) {
      exception.printStackTrace();
      return null;
    }
  }

  private static String decryptXOR(String input, String key) {
    input = new String(Base64.getDecoder().decode(input.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
    StringBuilder sb = new StringBuilder();
    char[] keyChars = key.toCharArray();
    int i = 0;
    for (char c : input.toCharArray()) {
      sb.append((char) (c ^ keyChars[i % keyChars.length]));
      i++;
    }
    return sb.toString();
  }

  public static String decryptAES(String input, String key) {
    try {
      SecretKeySpec keySpec = new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8)), "AES");

      Cipher des = Cipher.getInstance("AES");
      des.init(Cipher.DECRYPT_MODE, keySpec);

      return new String(des.doFinal(Base64.getDecoder().decode(input.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);

    } catch (Exception e) {
      e.printStackTrace();
    }
    return null;
  }

}
