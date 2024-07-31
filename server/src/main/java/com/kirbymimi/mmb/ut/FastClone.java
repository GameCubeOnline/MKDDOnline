package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.system.MMBSystem;

public class FastClone {
   public static <T> T clone(T obj) {
      Object ret = null;

      try {
         ret = obj.getClass().newInstance();
      } catch (Exception var3) {
         MMBSystem.fatalS((Object)("Cannot instantiate object to be cloned : " + obj.getClass()));
      }

      set(ret, obj);
      return (T) ret;
   }

   public static void set(Object dst, Object src) {
      Class<?> cls = null;
      if (dst.getClass().isInstance(src)) {
         cls = src.getClass();
      } else if (src.getClass().isInstance(dst)) {
         cls = dst.getClass();
      } else {
         MMBSystem.fatalS((Object)("Missmatching classes for set : " + dst.getClass() + " and " + src.getClass()));
      }

   }
}
