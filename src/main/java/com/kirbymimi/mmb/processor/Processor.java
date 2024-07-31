package com.kirbymimi.mmb.processor;

import com.kirbymimi.mmb.ut.ktml.KTMLEntry;
import com.kirbymimi.mmb.ut.list.FastList;
import java.util.HashMap;

public class Processor {
   Class<?> processorClass;
   FastList<String> commands = new FastList();
   int handle;

   public void setClass(Class<?> cls) {
      this.processorClass = cls;
   }

   public void ktmlEntry(KTMLEntry entry) {
      this.commands.add(entry.getAsString());
   }

   public void addCommand(String cmd) {
      this.commands.add(cmd);
   }

   public void exec(Object target, HashMap<String, Double> variables) {
      ProcessorCompiler proc = new ProcessorCompiler();
      proc.addProcessor(this);
      proc.compile();
   }
}
