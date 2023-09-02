package me.nov.threadtear.decompiler;

import com.strobel.assembler.InputTypeLoader;
import com.strobel.assembler.metadata.*;
import com.strobel.decompiler.DecompilationOptions;
import com.strobel.decompiler.Decompiler;
import com.strobel.decompiler.DecompilerSettings;
import com.strobel.decompiler.PlainTextOutput;
import me.nov.threadtear.ThreadtearCore;
import me.nov.threadtear.io.JarIO;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProcyonBridge implements IDecompilerBridge {

    @Override
    public void setAggressive(boolean aggressive) {}

    @Override
    public String decompile(File archive, String name, byte[] bytes) {
        try {
            File temp = File.createTempFile(name, ".class");

            FileOutputStream fos = new FileOutputStream(temp);
            fos.write(bytes);
            fos.close();

            ITypeLoader typeLoader = new InputTypeLoader();
            MetadataSystem metadataSystem = new MetadataSystem(typeLoader);
            TypeReference type = metadataSystem.lookupType(temp.getCanonicalPath());

            DecompilerSettings settings = DecompilerSettings.javaDefaults();
            settings.setForceExplicitImports(true);

            DecompilationOptions options = new DecompilationOptions();
            options.setSettings(settings);
            options.setFullDecompilation(true);

            TypeDefinition resolvedType;
            if (type == null || ((resolvedType = type.resolve()) == null))
                throw new Exception("Unable to resolve type.");

            StringWriter stringwriter = new StringWriter();
            settings.getLanguage().decompileType(resolvedType, new PlainTextOutput(stringwriter), options);

            //Convert from Unicode to string
            return unicodeToString(stringwriter.toString());
        } catch (Exception e) {
            e.printStackTrace();

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);

            return sw.toString();
        }
    }

    public String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            String group = matcher.group(2);
            ch = (char) Integer.parseInt(group, 16);
            String group1 = matcher.group(1);
            str = str.replace(group1, ch + "");
        }
        return str;
    }

    public static class ProcyonDecompilerInfo extends DecompilerInfo<ProcyonBridge> {
        @Override
        public String getName() {
            return "Procyon";
        }

        @Override
        public String getVersionInfo() {
            return "0.6.0";
        }

        @Override
        public ProcyonBridge createDecompilerBridge() {
            return new ProcyonBridge();
        }
    }
}
