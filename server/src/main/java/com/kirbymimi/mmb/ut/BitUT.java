package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.system.MMBSystem;
import java.math.BigInteger;

public class BitUT {
   static final BigInteger smallestLong = new BigInteger("-FFFFFFFFFFFFFFFF", 16);
   static final BigInteger biggestLong = new BigInteger("FFFFFFFFFFFFFFFF", 16);
   static final BigInteger smallestQuad = new BigInteger("-FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
   static final BigInteger biggestQuad = new BigInteger("FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF", 16);
   public static final int[] intMasks = new int[33];
   public static final int[] intMasks2 = new int[33];
   public static final long[] longMasks = new long[65];
   public static final long[] longMasks2 = new long[65];

   static {

      long cmask = 0L;

      for(int i = 0; i != 65; ++i) {
         longMasks[i] = cmask;
         cmask = cmask << 1 | 1L;
         longMasks2[i] = cmask;
      }

   }

   public static long alignHi(long val, long align) {
      --align;
      return val + align & ~align;
   }

   public static int alignHi(int val, int align) {
      --align;
      return val + align & ~align;
   }

   public static long ror(long val, long rot) {
      return val >>> (int)rot | val << (int)(64L - rot);
   }

   public static long ror(long val, long rot, long esize) {
      return (val >>> (int)rot | val << (int)(esize - rot)) & longMasks[(int)esize];
   }

   public static long ror32(long val, long rot) {
      return val >>> (int)rot | val << (int)(32L - rot);
   }

   public static byte lowestSetBit(int bits) {
      int cmask = 1;

      for(byte i = 0; i != 32; ++i) {
         if ((bits & cmask) != 0) {
            return i;
         }

         cmask <<= 1;
      }

      MMBSystem.fatalS((Object)"trying to call lowestSetBit on a value that has not bit set ! :(");
      return 0;
   }

   public static byte lowestSetBit(long bits) {
      int cmask = 1;

      for(byte i = 0; i != 64; ++i) {
         if ((bits & (long)cmask) != 0L) {
            return i;
         }

         cmask <<= 1;
      }

      MMBSystem.fatalS((Object)"trying to call lowestSetBit on a value that has not bit set ! :(");
      return 0;
   }

   public static int highestSetBit(long bits, int size) {
      int cmask = 1 << size - 1;

      for(int i = size - 1; i != -1; --i) {
         if ((bits & (long)cmask) != 0L) {
            return i;
         }

         cmask >>= 1;
      }

      MMBSystem.fatalS((Object)"trying to call highestSetBit on a value that has not bit set ! :(");
      return 0;
   }

   public static boolean bitAt(int bits, int off) {
      return (bits >> off & 1) == 1;
   }

   public static boolean longCheck(String s) {
      return bigCheck(s, smallestLong, biggestLong);
   }

   public static boolean quadCheck(String s) {
      return bigCheck(s, smallestQuad, biggestQuad);
   }

   public static boolean bigCheck(String s, BigInteger smallest, BigInteger biggest) {
      try {
         boolean neg = false;
         if (s.charAt(0) == '-') {
            s = s.substring(1);
            neg = true;
         }

         BigInteger i;
         if (!s.startsWith("0x") && !s.startsWith("0X")) {
            if (s.startsWith("#")) {
               i = new BigInteger(s.substring(1), 16);
            } else {
               i = new BigInteger(s);
            }
         } else {
            i = new BigInteger(s.substring(2), 16);
         }

         if (neg) {
            i.negate();
         }

         if (i.compareTo(new BigInteger("0")) < 0) {
            if (i.compareTo(smallest) < 0) {
               return false;
            }
         } else if (i.compareTo(biggest) > 0) {
            return false;
         }

         return true;
      } catch (Exception var5) {
         return false;
      }
   }

   public static BigInteger longDecode(String s) {
      return bigDecode(s, smallestLong, biggestLong);
   }

   public static BigInteger quadDecode(String s) {
      return bigDecode(s, smallestQuad, biggestQuad);
   }

   public static BigInteger bigDecode(String s, BigInteger smallest, BigInteger biggest) {
      try {
         boolean neg = false;
         if (s.charAt(0) == '-') {
            s = s.substring(1);
            neg = true;
         }

         BigInteger i;
         if (s.startsWith("0x")) {
            i = new BigInteger(s.substring(2), 16);
         } else if (s.startsWith("#")) {
            i = new BigInteger(s.substring(1), 16);
         } else {
            i = new BigInteger(s);
         }

         if (neg) {
            i.negate();
         }

         if (i.compareTo(new BigInteger("0")) < 0) {
            if (i.compareTo(smallest) < 0) {
               return null;
            }
         } else if (i.compareTo(biggest) > 0) {
            return null;
         }

         return i;
      } catch (Exception var5) {
         return null;
      }
   }
}
