package com.kirbymimi.mmb.system;

import com.kirbymimi.mmb.res.ResourceContainer;

public class KThread extends Thread {
   ResourceContainer resourceContainer = new ResourceContainer();
   MMBSystem system;

   public KThread(MMBSystem system) {
      this.system = system;
   }

   public KThread(MMBSystem system, Runnable runnable) {
      super(runnable);
      this.system = system;
   }

   public KThread(Runnable runnable) {
      super(runnable);
      this.system = MMBSystem.get();
   }

   public KThread() {
      this.system = MMBSystem.get();
   }

   public MMBSystem getSystem() {
      return this.system;
   }

   public <T> T getResource(Class<?> parent, Class<T> resCls, String name) {
      return this.resourceContainer.getResource(parent, resCls, name);
   }

   public static <T> T getResourceS(Class<?> parent, Class<T> resCls, String name) {
      return get().resourceContainer.getResource(parent, resCls, name);
   }

   public static final KThread get() {
      try {
         return (KThread)Thread.currentThread();
      } catch (Exception var1) {
         return null;
      }
   }
}
