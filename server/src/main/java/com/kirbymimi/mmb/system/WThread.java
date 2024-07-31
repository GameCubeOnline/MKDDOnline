package com.kirbymimi.mmb.system;

import com.kirbymimi.mmb.ut.Sleeper;
import com.kirbymimi.mmb.ut.list.SafeList;
import java.util.Iterator;

public class WThread extends KThread {
   SafeList<KTask> workPile = new SafeList();
   int taskWeight;
   volatile boolean active;
   Sleeper sleeper = new Sleeper();
   int id;
   volatile boolean exit;

   public WThread(MMBSystem sys, int id) {
      super(sys);
      this.id = id;
      this.setName("Worker thread " + id);
   }

   public void addTask(KTask task) {
      ++this.taskWeight;
      task.parent = this;
      this.workPile.add(task);
   }

   public void removeTask(KTask task) {
      --this.taskWeight;
      this.workPile.remove(task);
   }

   public void run() {
      while(!this.exit) {
         Iterator var2 = this.workPile.iterator();

         while(var2.hasNext()) {
            KTask task = (KTask)var2.next();

            try {
               task.run();
            } catch (Exception var4) {
               var4.printStackTrace();
               task.kill();
            }
         }

         this.sleeper.setTime((long)(1000000000 / MMBSystem.get().getLogicFrameRate()));
         this.sleeper.update();
      }

   }

   public static int myID() {
      KThread kthread = KThread.get();
      return !(kthread instanceof WThread) ? 0 : ((WThread)kthread).id;
   }
}
