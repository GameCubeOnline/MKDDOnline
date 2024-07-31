package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.system.MMBSystem;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

public class ArrayUT {
   public static <T> T[] newArray(Class<T> cls, int len) {
      Object[] ret = (Object[])Array.newInstance(cls, len);

      try {
         for(int i = 0; i != len; ++i) {
            ret[i] = cls.newInstance();
         }
      } catch (Exception var4) {
         MMBSystem.fatalS((Object)("Cannot create array of " + cls));
      }

      return (T[]) ret;
   }

   public static <T> T[] newArray(Class<T> cls, int len, Class<?>[] classArgs, Object... args) {
      Object[] ret = (Object[])Array.newInstance(cls, len);

      try {
         Constructor<T> ct = cls.getConstructor(classArgs);

         for(int i = 0; i != len; ++i) {
            ret[i] = ct.newInstance(args);
         }
      } catch (Exception var7) {
         MMBSystem.fatalS((Object)("Cannot create array of " + cls));
      }

      return (T[]) ret;
   }

   public static <T> T[] newArray(Class<T> cls, int len, Object... args) {
      Object[] ret = (Object[])Array.newInstance(cls, len);

      try {
         Class[] ctCls = new Class[args.length];

         for(int i = 0; i != args.length; ++i) {
            ctCls[i] = args[i].getClass();
         }

         Constructor<T> ct = cls.getConstructor(ctCls);

         for(int i = 0; i != len; ++i) {
            ret[i] = ct.newInstance(args);
         }
      } catch (Exception var7) {
         MMBSystem.fatalS((Object)("Cannot create array of " + cls));
      }

      return (T[]) ret;
   }

   public static byte[] arrFrom(byte[] data, int off, int len) {
      byte[] ret = new byte[len];
      System.arraycopy(data, off, ret, 0, len);
      return ret;
   }

   public static String zeroTerminatedNewString(byte[] data, int off) {
      int len = 0;

      for(int i = off; i < data.length && data[i] != 0; ++i) {
         ++len;
      }

      return new String(data, off, len);
   }

   public static String arrToString(Object[] arr) {
      String str = "";
      if (arr.length == 0) {
         return str;
      } else {
         int i;
         for(i = 0; i != arr.length - 1; ++i) {
            str = str + arr[i] + " ";
         }

         return str + arr[i];
      }
   }

   public static <T> T[][] halve(T[] arr) {
      if ((arr.length & 1) != 0) {
         MMBSystem.fatalS((Object)"Don't halve an array with an odd size.");
      }

      return split(arr, arr.length >> 1);
   }

   public static <T> T[][] split(T[] arr, int idx) {
      if (idx >= arr.length) {
         throw new ArrayIndexOutOfBoundsException(idx);
      } else {
         Object[] part1 = (Object[])Array.newInstance(arr.getClass().getComponentType(), idx);
         Object[] part2 = (Object[])Array.newInstance(arr.getClass().getComponentType(), arr.length - idx);
         System.arraycopy(arr, 0, part1, 0, part1.length);
         System.arraycopy(arr, idx, part2, 0, part2.length);
         Object[][] ret = (Object[][])Array.newInstance(arr.getClass(), 2);
         ret[0] = part1;
         ret[1] = part2;
         return (T[][]) ret;
      }
   }

   public static <T> T[] merge(T[] first, T[] second) {
      Object[] nArr = (Object[])Array.newInstance(first.getClass().getComponentType(), first.length + second.length);
      System.arraycopy(first, 0, nArr, 0, first.length);
      System.arraycopy(second, 0, nArr, first.length, second.length);
      return (T[]) nArr;
   }

   public static int elementIdx(Object[] array, Object search) {
      for(int i = 0; i != array.length; ++i) {
         if (array[i] == search) {
            return i;
         }
      }

      return -1;
   }

   public static void arrAdd(double[] res, double[] op1, double[] op2) {
      for(int i = 0; i != res.length; ++i) {
         res[i] = op1[i] + op2[i];
      }

   }
}
