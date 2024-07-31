package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.system.KThread;

public class Chronometer {
   long startTime;

   public static void start(Class<?> source, String name) {
      Chronometer chrono = (Chronometer)KThread.getResourceS(source, Chronometer.class, name);
      chrono.startTime = System.nanoTime();
   }

   public static long msTime(Class<?> source, String name) {
      return (System.nanoTime() - ((Chronometer)KThread.getResourceS(source, Chronometer.class, name)).startTime) / 1000000L;
   }
}
