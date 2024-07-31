package com.kirbymimi.mmb.graphics.animation;

import com.kirbymimi.mmb.math.IMath;
import com.kirbymimi.mmb.system.MMBSystem;

public abstract class Animator<T extends Animations<U>, U extends Animations.Animation<?>> implements MMBSystem.DeltaRunnable {
   T animations;
   U current;
   double time;
   int cframe;
   boolean loop;
   double speed = 1.0D;

   public Animator(T animations) {
      this.animations = animations;
   }

   public synchronized void setAnim(String name) {
      U next = this.animations.get(name);
      if (next != this.current) {
         if (next == null) {
            this.loop = true;
         } else {
            this.loop = next.loop;
         }

         this.current = next;
         this.time = 0.0D;
         this.cframe = 0;
      }
   }

   public boolean isDone() {
      return this.current != null && !this.loop && this.cframe == this.current.animations.length - 1;
   }

   public void setSpeed(double speed) {
      this.speed = speed;
   }

   public double getSpeed() {
      return this.speed;
   }

   public <R> R getFrame() {
      return this.current == null ? null : (R) this.current.get(this.cframe);
   }

   public synchronized void run(double delta) {
      if (this.current != null) {
         this.time += delta * this.current.rate * this.speed;

         while(this.time >= 1.0D) {
            --this.time;
            if (this.loop) {
               this.cframe = IMath.clinc(this.cframe, this.current.animations.length);
            } else {
               this.cframe = IMath.lminc(this.cframe, this.current.animations.length);
            }
         }

      }
   }
}
