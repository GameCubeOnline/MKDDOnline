package com.kirbymimi.mmb.system;

import com.kirbymimi.mmb.actor.Actor;

public class KTask {
   WThread parent;
   boolean paused;
   int taskWeight;
   long updateDelay;
   long lastUpdateDelay;
   long lastUpdateTime;
   double delta;
   Actor actor;
   Runnable exec;
   double maxDelta;

   public KTask(Runnable exec) {
      this.maxDelta = 0.03D;
      this.exec = exec;
      this.lastUpdateTime = System.nanoTime();
   }

   public KTask(Runnable exec, long updateDelay) {
      this(exec);
      this.updateDelay = updateDelay;
   }

   public KTask(Actor actor) {
      this.maxDelta = 0.03D;
      this.actor = actor;
      actor.setKTask(this);
      this.lastUpdateTime = System.nanoTime();
   }

   public KTask(Actor actor, long updateDelay) {
      this(actor);
      this.updateDelay = updateDelay;
   }

   public void run() {
      long time = System.nanoTime();
      if (time >= this.lastUpdateTime + this.updateDelay) {
         this.lastUpdateDelay = time - this.lastUpdateTime;
         this.lastUpdateTime = time;
         this.delta = (double)this.lastUpdateDelay / 1.0E9D;
         if (this.delta > this.maxDelta) {
            this.delta = this.maxDelta;
         }

         if (!this.paused) {
            if (this.exec != null) {
               this.exec.run();
            }

            if (this.actor != null) {
               Actor parent = this.actor;

               while((parent = parent.getParentActor()) != null) {
                  if (parent.isPaused()) {
                     return;
                  }
               }

               this.actor.run();
            }
         }

      }
   }

   public void kill() {
      this.parent.removeTask(this);
      if (this.actor != null && !this.actor.killed()) {
         this.actor.kill();
      }

   }

   public double getDelta() {
      return this.delta;
   }

   public void setPaused(boolean paused) {
      this.paused = paused;
   }
}
