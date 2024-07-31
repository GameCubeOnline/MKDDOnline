package com.kirbymimi.mmb.processor;

import com.kirbymimi.mmb.ut.list.FastList;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import javax.lang.model.SourceVersion;
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject.Kind;

public class ProcessorCompiler {
   FastList<Processor> processors = new FastList();
   FastList<String> imports = new FastList();
   FastList<String> staticImports = new FastList();
   static long uidAllocator;
   static final boolean[] ENDMARKERS = new boolean[256];
   static final int[] ENDMARKERSVALS = new int[]{32, 123, 125, 59, 40, 41, 60, 62, 58, 61};

   static {
      for(int i = 0; i != ENDMARKERSVALS.length; ++i) {
         ENDMARKERS[ENDMARKERSVALS[i]] = true;
      }

   }

   public ProcessorCompiler() {
      this.staticImports.add("java.lang.Math.*");
   }

   public void addProcessor(Processor processor) {
      this.processors.add(processor);
   }

   public void addImport(String importName) {
      this.imports.add(importName);
   }

   public void addStaticImport(String staticImport) {
      this.staticImports.add(staticImport);
   }

   public byte[] compile() {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      PrintWriter printer = new PrintWriter(out);
      printer.println("package com.kirbymimi.mme.cache;");
      Iterator var4 = this.imports.iterator();

      String className;
      while(var4.hasNext()) {
         className = (String)var4.next();
         printer.println("import " + className + ";");
      }

      var4 = this.staticImports.iterator();

      while(var4.hasNext()) {
         className = (String)var4.next();
         printer.println("import static " + className + ";");
      }

      className = "cache" + this.allocateUID();
      printer.println("public class " + className + " {");
      int cfuncID = 0;
      byte[] workBuf = new byte[1024];
      FastList<String> variables = new FastList();
      FastList<String> objectFields = new FastList();
      Iterator var9 = this.processors.iterator();

      while(var9.hasNext()) {
         Processor processor = (Processor)var9.next();
         variables.clear();
         objectFields.clear();
         printer.println("public static void func" + cfuncID + "(" + processor.processorClass.getName() + " obj) {");
         ++cfuncID;
         int cStrIdx = 0;
         Iterator var12 = processor.commands.iterator();

         String str;
         while(var12.hasNext()) {
            str = (String)var12.next();
            byte[] bytes = (str + ";").getBytes();
            byte[] var17 = bytes;
            int var16 = bytes.length;

            for(int var15 = 0; var15 < var16; ++var15) {
               byte b = var17[var15];
               if (ENDMARKERS[b & 255]) {
                  if (cStrIdx != 0) {
                     String var = new String(workBuf, 0, cStrIdx);
                     cStrIdx = 0;
                     if (!SourceVersion.isKeyword(var)) {
                        try {
                           Double.parseDouble(var);
                        } catch (Exception var21) {
                           try {
                              processor.processorClass.getDeclaredField(var);
                              objectFields.add(var);
                           } catch (Exception var20) {
                              variables.add(var);
                           }
                        }
                     }
                  }
               } else {
                  workBuf[cStrIdx++] = b;
               }
            }
         }

         for(var12 = variables.iterator(); var12.hasNext(); str = (String)var12.next()) {
         }

         var12 = processor.commands.iterator();

         while(var12.hasNext()) {
            str = (String)var12.next();

            String str2;
            for(Iterator var24 = objectFields.iterator(); var24.hasNext(); str = str.replace(str2, "obj." + str2)) {
               str2 = (String)var24.next();
            }
         }

         for(var12 = variables.iterator(); var12.hasNext(); str = (String)var12.next()) {
         }

         printer.println("}");
      }

      printer.println("}");
      printer.close();
      new File("debug/compiler.bin");
      return null;
   }

   synchronized long allocateUID() {
      return (long)(uidAllocator++);
   }

   class ByteArrayJavaFileObject extends SimpleJavaFileObject {
      private final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

      protected ByteArrayJavaFileObject(String name, Kind kind) {
         super(URI.create("byte:///" + name.replace('.', '/') + kind.extension), kind);
      }

      public OutputStream openOutputStream() throws IOException {
         return this.outputStream;
      }

      public byte[] getBytes() {
         return this.outputStream.toByteArray();
      }
   }

   class InMemoryClassFileManager extends ForwardingJavaFileManager<StandardJavaFileManager> {
      private final Map<String, ProcessorCompiler.ByteArrayJavaFileObject> classFiles = new HashMap();

      protected InMemoryClassFileManager(StandardJavaFileManager fileManager) {
         super(fileManager);
      }

      public JavaFileObject getJavaFileForOutput(Location location, String className, Kind kind, FileObject sibling) throws IOException {
         ProcessorCompiler.ByteArrayJavaFileObject fileObject = ProcessorCompiler.this.new ByteArrayJavaFileObject(className, kind);
         this.classFiles.put(className, fileObject);
         return fileObject;
      }

      public Map<String, ProcessorCompiler.ByteArrayJavaFileObject> getClassFiles() {
         return this.classFiles;
      }
   }

   class InMemoryClassLoader extends ClassLoader {
      private final Map<String, ProcessorCompiler.ByteArrayJavaFileObject> classFiles;

      public InMemoryClassLoader(Map<String, ProcessorCompiler.ByteArrayJavaFileObject> classFiles) {
         this.classFiles = classFiles;
      }

      protected Class<?> findClass(String name) throws ClassNotFoundException {
         ProcessorCompiler.ByteArrayJavaFileObject fileObject = (ProcessorCompiler.ByteArrayJavaFileObject)this.classFiles.get(name);
         if (fileObject != null) {
            byte[] bytes = fileObject.getBytes();
            return this.defineClass(name, bytes, 0, bytes.length);
         } else {
            return super.findClass(name);
         }
      }
   }

   class InMemoryJavaFileObject extends SimpleJavaFileObject {
      private final String code;

      protected InMemoryJavaFileObject(String name, String code) {
         super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension), Kind.SOURCE);
         this.code = code;
      }

      public CharSequence getCharContent(boolean ignoreEncodingErrors) {
         return this.code;
      }
   }
}
