package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.system.KThread;
import java.awt.event.ActionListener;
import javax.swing.Timer;

public class ThreadUT {
   public static ThreadUT.Runner createRunner(Runnable func, long nanoPeriod) {
      ThreadUT.Runner ret = new ThreadUT.Runner(func, nanoPeriod);
      return ret;
   }

   public static Timer createSwingRunner(ActionListener func, long nanoPeriod) {
      Timer t = new Timer((int)(nanoPeriod / 1000000L), func);
      t.setRepeats(true);
      return t;
   }

   public static class Runner extends KThread {
      Runnable func;
      long nanoPeriod;
      volatile boolean stopped;

      public void kill() {
         this.stopped = true;
         this.interrupt();
      }

      public Runner(Runnable func, long nanoPeriod) {
         this.func = func;
         this.nanoPeriod = nanoPeriod;
      }

      public void run() {
         while(!this.stopped) {
            long last = System.nanoTime();
            this.func.run();
            long sleep = last + this.nanoPeriod - System.nanoTime();
            if (sleep >= 0L) {
               if (sleep > this.nanoPeriod) {
                  sleep = this.nanoPeriod;
               }

               try {
                  Thread.sleep((long)((int)(this.nanoPeriod / 1000000L)), (int)(this.nanoPeriod % 1000000L));
               } catch (Exception var6) {
               }
            }
         }

      }
   }
}
