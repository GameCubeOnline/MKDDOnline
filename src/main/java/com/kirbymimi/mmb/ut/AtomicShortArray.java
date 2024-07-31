package com.kirbymimi.mmb.ut;

import java.io.Serializable;
import java.lang.reflect.Field;
import sun.misc.Unsafe;

public class AtomicShortArray implements Serializable {
   private static final long serialVersionUID = 2862133569453604235L;
   private static Unsafe unsafe;
   private static final int base;
   private static final int shift;
   private final short[] array;

   static {
      try {
         Field f = Unsafe.class.getDeclaredField("theUnsafe");
         f.setAccessible(true);
         unsafe = (Unsafe)f.get((Object)null);
      } catch (Exception var1) {
         var1.printStackTrace();
      }

      base = unsafe.arrayBaseOffset(int[].class);
      int scale = unsafe.arrayIndexScale(short[].class);
      if ((scale & scale - 1) != 0) {
         throw new Error("data type scale not a power of two");
      } else {
         shift = 31 - Integer.numberOfLeadingZeros(scale);
      }
   }

   private long checkedByteOffset(int i) {
      if (i >= 0 && i < this.array.length) {
         return byteOffset(i);
      } else {
         throw new IndexOutOfBoundsException("index " + i);
      }
   }

   private static long byteOffset(int i) {
      return ((long)i << shift) + (long)base;
   }

   public AtomicShortArray(int length) {
      this.array = new short[length];
   }

   public AtomicShortArray(short[] array) {
      this.array = (short[])array.clone();
   }

   public final int length() {
      return this.array.length;
   }

   public final short get(int i) {
      return this.getRaw(this.checkedByteOffset(i));
   }

   private short getRaw(long offset) {
      return unsafe.getShortVolatile(this.array, offset);
   }

   public final void set(int i, short newValue) {
      unsafe.putShortVolatile(this.array, this.checkedByteOffset(i), newValue);
   }

   public final short getAndSet(int i, short newValue) {
      short ret = unsafe.getShort(this.array, this.checkedByteOffset(i));
      unsafe.putShort(this.array, this.checkedByteOffset(i), newValue);
      return ret;
   }

   public String toString() {
      int iMax = this.array.length - 1;
      if (iMax == -1) {
         return "[]";
      } else {
         StringBuilder b = new StringBuilder();
         b.append('[');
         int i = 0;

         while(true) {
            b.append(this.getRaw(byteOffset(i)));
            if (i == iMax) {
               return b.append(']').toString();
            }

            b.append(',').append(' ');
            ++i;
         }
      }
   }
}
