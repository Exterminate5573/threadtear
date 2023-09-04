package me.nov.threadtear.execution.sb27;

import me.nov.threadtear.execution.Clazz;
import me.nov.threadtear.execution.Execution;
import me.nov.threadtear.execution.ExecutionCategory;
import me.nov.threadtear.execution.ExecutionTag;
import me.nov.threadtear.util.asm.ASMHelper;
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

    private int countSuccess;
    private int countFailure;

  public StringObfuscationSB27() {
    super(ExecutionCategory.SB27, "String Obfuscation Removal", "Deobfuscates strings.<br>Taken from <a href=\"https://github.com/narumii/Deobfuscator/\">Narumi Deobf</a>", ExecutionTag.RUNNABLE);
  }

  @Override
  public boolean execute(Map<String, Clazz> map, boolean verbose) {
      this.countFailure = 0;
      this.countSuccess = 0;

      map.values().forEach(this::decrypt);

      logger.info("Successfully decrypted {} strings and failed to decrypt {} strings!", countSuccess, countFailure);

      return true;
  }

  private void decrypt(Clazz c) {
      logger.collectErrors(c);
      ClassNode classNode = c.node;

      List<MethodNode> toRemove = new ArrayList<>();

      classNode.methods.forEach(methodNode -> Arrays.stream(methodNode.instructions.toArray())
              .filter(node -> node.getOpcode() == INVOKESTATIC)
              .filter(node -> ASMHelper.isString(node.getPrevious())) //key
              .filter(node -> ASMHelper.isString(node.getPrevious().getPrevious())) //encrypted
              .map(MethodInsnNode.class::cast)
              .filter(node -> node.owner.equals(classNode.name))
              .filter(node -> node.desc.equals("(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
              //TODO: Maybe some cache system?
              .forEach(node -> ASMHelper.findMethod(classNode, method -> method.name.equals(node.name) && method.desc.equals(node.desc)).ifPresent(method -> {
                  String string = ASMHelper.getString(node.getPrevious().getPrevious());
                  String key = ASMHelper.getString(node.getPrevious());

                  switch (getStringObfType(method)) {
                      case AES:
                          string = decryptAes(string, key);
                          break;
                      case DES:
                          string = decryptDes(string, key);
                          break;
                      case BLOWFISH:
                          string = decryptBlowfish(string, key);
                          break;
                      case XOR:
                          string = decryptXor(string, key);
                          break;
                  }

                  if (string == null) {
                      this.countFailure++;
                      return;
                  }

                  methodNode.instructions.remove(node.getPrevious().getPrevious());
                  methodNode.instructions.remove(node.getPrevious());
                  methodNode.instructions.set(node, new LdcInsnNode(string));
                  toRemove.add(method);
                  this.countSuccess++;
              })));

      classNode.methods.removeAll(toRemove);
      toRemove.clear();
  }

  @Override
  public String getAuthor() {
    return "Exterminate";
  }

    /*
      TODO: Better type checking lol
       */
    private Type getStringObfType(MethodNode methodNode) {
        Optional<String> string = Arrays.stream(methodNode.instructions.toArray())
                .filter(ASMHelper::isString)
                .map(ASMHelper::getString)
                .filter(node -> node.equals("AES") || node.equals("Blowfish") || node.equals("DES"))
                .findFirst();

        if (string.isPresent()) {
            switch (string.get()) {
                case "AES":
                    return Type.AES;
                case "Blowfish":
                    return Type.BLOWFISH;
                case "DES":
                    return Type.DES;
            }
        }

        return Type.XOR;
    }


    private String decryptAes(String obj, String key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(MessageDigest.getInstance("SHA-256").digest(key.getBytes(StandardCharsets.UTF_8)), "AES");

            Cipher des = Cipher.getInstance("AES");
            des.init(Cipher.DECRYPT_MODE, keySpec);

            return new String(des.doFinal(Base64.getDecoder().decode(obj.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);

        } catch (Exception e) {
            logger.warning("Can't decrypt AES string. [string: {}, key: {}] | {}", obj, key, e);
        }
        return null;
    }

    private String decryptBlowfish(String obj, String key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(MessageDigest.getInstance("MD5").digest(key.getBytes(StandardCharsets.UTF_8)), "Blowfish");

            Cipher des = Cipher.getInstance("Blowfish");
            des.init(Cipher.DECRYPT_MODE, keySpec);

            return new String(des.doFinal(Base64.getDecoder().decode(obj.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);

        } catch (Exception e) {
            logger.warning("Can't decrypt Blowfish string. [string: {}, key: {}] | {}", obj, key, e);
        }
        return null;
    }

    private String decryptDes(String obj, String key) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(Arrays.copyOf(MessageDigest.getInstance("MD5").digest(key.getBytes(StandardCharsets.UTF_8)), 8), "DES");

            Cipher des = Cipher.getInstance("DES");
            des.init(Cipher.DECRYPT_MODE, keySpec);

            return new String(des.doFinal(Base64.getDecoder().decode(obj.getBytes(StandardCharsets.UTF_8))), StandardCharsets.UTF_8);
        } catch (Exception e) {
            logger.warning("Can't decrypt DES string. [string: {}, key: {}] | {}", obj, key, e);
        }
        return null;
    }

    //TODO: Support for older versions
    private String decryptXor(String obj, String key) {
        try {
            obj = new String(Base64.getDecoder().decode(obj.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8);
        } catch (Exception ignored) {
        } //I know i know stupid but idc
        StringBuilder sb = new StringBuilder();
        char[] keyChars = key.toCharArray();
        int i = 0;
        for (char c : obj.toCharArray()) {
            sb.append((char) (c ^ keyChars[i % keyChars.length]));
            i++;
        }
        return sb.toString();
    }

    private enum Type {
        XOR, DES, BLOWFISH, AES
    }

}
