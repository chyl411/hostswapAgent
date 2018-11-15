package com.ztessc.core.instrumentation;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class LoadAgent {
    public LoadAgent() {
    }

    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        String[] args = agentArgs.split("\\|");
        reDefineClass(inst, args[0], args[1], args[2]);
    }

    private static void reDefineClass(Instrumentation instrumentation, String path, String classFile, String args) throws IOException {
        byte[] reporterClassFile = getBytes(classFile, path);

        try {
            //直接Class.forName加载类时由于此时classloader是系统级别的classloader，由于要修改的class基本上是由tomcat 的classloader加载的，
            //导致ClassnotfoundException，需要通过getAllLoadedClasses获取已经加载的class则正常
//            ClassDefinition reporterDef = new ClassDefinition(Class.forName(args), reporterClassFile);
//            instrumentation.redefineClasses(new ClassDefinition[]{reporterDef});

            Class[] classes = instrumentation.getAllLoadedClasses();
            for (Class cls : classes) {
                if (cls.getName().equals(args)) {
                    ClassDefinition reporterDef = new ClassDefinition(cls, reporterClassFile);
                    instrumentation.redefineClasses(new ClassDefinition[]{reporterDef});
                    break;
                }
            }
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    private static byte[] getBytes(String javaFileName, String jarPath) throws IOException {
        JarFile jarFile = new JarFile(jarPath);
        Enumeration entries = jarFile.entries();

        JarEntry entry;
        do {
            if (!entries.hasMoreElements()) {
                return null;
            }

            entry = (JarEntry) entries.nextElement();
        }
        while (!entry.getName().endsWith(".class") || !entry.getName().contains(javaFileName.substring(0, javaFileName.lastIndexOf("."))));

        byte[] var6;
        try {
            InputStream inputStream = jarFile.getInputStream(entry);
            var6 = getBytes(inputStream);
        } catch (IOException var10) {
            System.out.println("Could not obtain class entry for " + entry.getName());
            throw var10;
        } finally {
            jarFile.close();
        }

        return var6;
    }

    private static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        byte[] buffer = new byte['\uffff'];

        int len;
        while ((len = is.read(buffer)) != -1) {
            os.write(buffer, 0, len);
        }

        os.flush();
        return os.toByteArray();
    }
}
