package org.hostile.dumpbuddy;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;

public class ClassDumperAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation) {
        Path path = Paths.get("out/");

        if (!path.toFile().isDirectory()) {
            path.toFile().mkdir();
        }

        instrumentation.addTransformer(new ClassDumperTransformer(path));
    }

    private static class ClassDumperTransformer implements ClassFileTransformer {

        private final Path outputPath;

        public ClassDumperTransformer(Path path) {
            this.outputPath = path;
        }

        @Override
        public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                                ProtectionDomain protectionDomain, byte[] classfileBuffer) {
            File outputFile = new File(outputPath.toFile().getPath() + className + ".class");
            FileOutputStream outputStream;

            try {
                outputStream = new FileOutputStream(outputFile);
            } catch (IOException exc) {
                exc.printStackTrace();
                return classfileBuffer;
            }

            try {
                outputStream.write(classfileBuffer);
            } catch (IOException exc) {
                System.out.println("An unexpected error occurred while attempting to write class bytes for " + className + "!");
                exc.printStackTrace();

                return classfileBuffer;
            }
            try {
                outputStream.close();
            } catch (IOException exc) {
                exc.printStackTrace();
            }

            return classfileBuffer;
        }
    }

}
