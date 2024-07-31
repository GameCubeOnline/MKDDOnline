package com.kirbymimi.mmb.system;

import com.kirbymimi.mmb.actor.Actor;
import com.kirbymimi.mmb.res.Resource;
import com.kirbymimi.mmb.ut.ktml.KTMLEntry;
import com.kirbymimi.mmb.ut.list.FastList;
import com.kirbymimi.mmb.ut.list.SafeList;
import java.io.File;
import java.io.PrintStream;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Iterator;

public class MMBSystem {
   Class<?> mainCls;
   PrintStream fatalStream;
   PrintStream warningStream;
   int logicFrameRate;
   int renderFrameRate;
   Resource systemRes;
   KTMLEntry drawOrderTranslation;
   FastList<Resource.ResLookup> resLookup;
   static HashMap<Thread, MMBSystem> thread2Sys = new HashMap();
   SafeList<WThread> workers;

   public MMBSystem() {
      this.fatalStream = System.out;
      this.warningStream = System.out;
      this.logicFrameRate = 1;
      this.renderFrameRate = 1;
      this.resLookup = new FastList();
      this.workers = new SafeList();
   }

   public void init(Class<?> mainCls) {
      this.mainCls = mainCls;
      this.systemRes = Resource.load("MMBSystem");
      if (this.systemRes != null) {
         KTMLEntry resShorts = (KTMLEntry)this.systemRes.get("ResLookup");
         Iterator var4 = resShorts.iterator();

         while(var4.hasNext()) {
            KTMLEntry entry = (KTMLEntry)var4.next();

            try {
               Resource.ResLookup lookup = new Resource.ResLookup(entry.getInt("priority"), entry.getString("name"));
               this.resLookup.add(lookup);
            } catch (Exception var6) {
               this.fatal((Object)("Cannot find class : " + entry.getName() + " for resource shortcuts"));
            }
         }

         this.drawOrderTranslation = (KTMLEntry)this.systemRes.get("DrawOrders");
      }
   }

   public static final MMBSystem get() {
      KThread thread = KThread.get();
      if (thread != null) {
         return thread.getSystem();
      } else {
         MMBSystem ret = (MMBSystem)thread2Sys.get(Thread.currentThread());
         if (ret != null) {
            return ret;
         } else {
            System.out.println("Current thread isn't a KThread");
            System.exit(-1);
            return null;
         }
      }
   }

   public static final void addThread2Sys(Thread thread, MMBSystem system) {
      thread2Sys.put(thread, system);
   }

   public static void fatalS(Object msg) {
      get().fatal(msg);
   }

   public static void fatalS(Exception ex) {
      get().fatal(ex);
   }

   public void fatal(Object msg) {
      this.fatalStream.println(msg);
      System.exit(-1);
   }

   public void fatal(Exception ex) {
      ex.printStackTrace(this.fatalStream);
      System.exit(-1);
   }

   public static void warningS(Object msg) {
      get().fatal(msg);
   }

   public void warning(Object msg) {
      this.warningStream.println(msg);
   }

   public static void exitS(int code, String msg) {
      get().exit(code, msg);
   }

   public void exit(int code, String msg) {
      this.warning(msg);
      System.exit(code);
   }

   public void setLogicFrameRate(int logicFrameRate) {
      this.logicFrameRate = logicFrameRate;
   }

   public int getLogicFrameRate() {
      return this.logicFrameRate;
   }

   public void setRenderFrameRate(int RenderFrameRate) {
      this.renderFrameRate = RenderFrameRate;
   }

   public int getRenderFrameRate() {
      return this.renderFrameRate;
   }

   public int getWorkerCount() {
      return this.workers.length();
   }

   public void createWorkers(int count) {
      this.workers.clear();

      for(int i = 0; i != count; ++i) {
         WThread thread = new WThread(this, i);
         thread.start();
         this.workers.add(thread);
      }

   }

   public void createWorkers() {
      this.createWorkers(Runtime.getRuntime().availableProcessors());
   }

   public void killWorkers() {
      WThread thread;
      for(Iterator var2 = this.workers.iterator(); var2.hasNext(); thread.exit = true) {
         thread = (WThread)var2.next();
      }

   }

   public static void addWorkS(Actor actor) {
      get().addWork(actor);
   }

   public static void addWorkS(Runnable runnable) {
      get().addWork(runnable);
   }

   public static void addWorkS(KTask work) {
      get().addWork(work);
   }

   public static Resource getResS() {
      return get().systemRes;
   }

   public static FastList<Resource.ResLookup> getResLookup() {
      return get().resLookup;
   }

   public static int translateDrawOrderS(String name) {
      return get().drawOrderTranslation.get(name).getAsInt();
   }

   public void addWork(Actor actor) {
      this.addWork(new KTask(actor));
   }

   public void addWork(Runnable runnable) {
      this.addWork(new KTask(runnable));
   }

   public void addWork(KTask work) {
      if (this.workers.isEmpty()) {
         this.fatal((Object)"No worker available");
      }

      WThread best = (WThread)this.workers.first();
      Iterator var4 = this.workers.iterator();

      while(var4.hasNext()) {
         WThread thread = (WThread)var4.next();
         if (thread.taskWeight < best.taskWeight) {
            best = thread;
         }
      }

      best.addTask(work);
   }

   public static File openFileS(String name) {
      return get().openFile(name);
   }

   public File openFile(String name) {
      String str = null;

      try {
         str = URLDecoder.decode(this.mainCls.getProtectionDomain().getCodeSource().getLocation().toString(), "UTF-8").replace("target/classes/", "");
      } catch (Exception var14) {
      }

      str = str.replace("/bin/", "");
      str = str.replace("file:/", "");
      if (str.contains(".jar")) {
         str = str.substring(0, str.lastIndexOf(47));
      }

      if (str.charAt(str.length() - 1) == '/') {
         name = str + name;
      } else {
         name = str + "/" + name;
      }

      String[] path = name.split("/");
      String cfile = "";
      String[] var8 = path;
      int var7 = path.length;

      for(int var6 = 0; var6 < var7; ++var6) {
         String cpath = var8[var6];
         if (!cpath.isEmpty()) {
            boolean found = false;
            String[] var13;
            int var12 = (var13 = (new File(cfile + "/")).list()).length;

            for(int var11 = 0; var11 < var12; ++var11) {
               String search = var13[var11];
               if (search.compareToIgnoreCase(cpath) == 0) {
                  cfile = cfile + "/" + search;
                  found = true;
                  break;
               }
            }

            if (!found && !cfile.isEmpty()) {
               return new File(name);
            }
         }
      }

      return new File(cfile);
   }

   public interface DeltaRunnable {
      void run(double var1);
   }
}
