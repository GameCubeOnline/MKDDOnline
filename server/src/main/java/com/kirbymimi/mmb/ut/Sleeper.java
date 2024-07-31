package com.kirbymimi.mmb.ut;

public class Sleeper {
   long time;
   long lastTime;

   public Sleeper() {
   }

   public Sleeper(long time) {
      this.lastTime = System.nanoTime();
      this.setTime(time);
   }

   public void setTime(long time) {
      this.time = time;
   }

   public void update() {
      long timeSpent = System.nanoTime() - this.lastTime;
      long sleepTime = (this.time - timeSpent) / 1000000L;

      try {
         if (sleepTime > 0L && sleepTime < 1000L) {
            Thread.sleep(sleepTime);
         }
      } catch (Exception var6) {
      }

      this.lastTime = System.nanoTime();
   }
}
