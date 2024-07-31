package com.kirbymimi.mmb.math;

import com.kirbymimi.mmb.system.KThread;
import java.util.Random;

public class DMath {
   public static double clip(double val, double min, double max) {
      if (val < min) {
         return min;
      } else {
         return val > max ? max : val;
      }
   }

   public static double clip(double val, double max) {
      return clip(val, 0.0D, max);
   }

   public static double rand() {
      return getRand().nextDouble();
   }

   public static double rand(double max) {
      return getRand().nextDouble() * max;
   }

   public static double rand(double min, double max) {
      return min + getRand().nextDouble() * (max - min);
   }

   public static final double selBig(double i1, double i2) {
      return i1 < i2 ? i2 : i1;
   }

   public static final double selSmall(double i1, double i2) {
      return i1 > i2 ? i2 : i1;
   }

   public static final double cladd(double val, double add, double limit) {
      for(val += add; val >= limit; val -= limit) {
      }

      return val;
   }

   public static final double clinc(double val, double limit) {
      return val == limit - 1.0D ? 0.0D : val + 1.0D;
   }

   public static final double substractToZero(double val, double sub) {
      double startSign = Math.signum(val);
      val -= val > 0.0D ? sub : -sub;
      return startSign == Math.signum(val) ? val : 0.0D;
   }

   static Random getRand() {
      return (Random)KThread.getResourceS(DMath.class, Random.class, "rand");
   }

   public static boolean rectIntersect(double x1, double y1, double w1, double h1, double x2, double y2, double w2, double h2) {
      if (!(x1 > x2 + w2) && !(x2 > x1 + w1)) {
         return !(y1 > y2 + h2) && !(y2 > y1 + h1);
      } else {
         return false;
      }
   }
}
