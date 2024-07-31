package com.kirbymimi.mmb.math;

public class IMath {
   public static final int selBig(int i1, int i2) {
      return i1 < i2 ? i2 : i1;
   }

   public static final int selSmall(int i1, int i2) {
      return i1 > i2 ? i2 : i1;
   }

   public static final int cladd(int val, int add, int limit) {
      for(val += add; val >= limit; val -= limit) {
      }

      return val;
   }

   public static final int clinc(int val, int limit) {
      return val == limit - 1 ? 0 : val + 1;
   }

   public static final int lminc(int val, int limit) {
      return val == limit - 1 ? val : val + 1;
   }

   public static final int truncate(double val, int trunc) {
      return truncate((int)val, trunc);
   }

   public static final int truncate(int val, int trunc) {
      return val - val % trunc;
   }
}
