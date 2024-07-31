package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.system.MMBSystem;

public class ClassUT {
   public static Class<?> search(String name, String[] paths) {
      try {
         return Class.forName(name);
      } catch (Exception var8) {
         name = '.' + name;
         String[] var5 = paths;
         int var4 = paths.length;
         int var3 = 0;

         while(var3 < var4) {
            String s = var5[var3];

            try {
               return Class.forName(s + name);
            } catch (Exception var7) {
               ++var3;
            }
         }

         return null;
      }
   }

   public static Class<?>[] makeClasses(Object[] objs) {
      Class[] ret = new Class[objs.length];

      for(int i = 0; i != ret.length; ++i) {
         ret[i] = objs[i].getClass();
      }

      return ret;
   }

   public static <T> T construct(Class<T> cls, Object... args) {
      try {
         return cls.getConstructor(makeClasses(args)).newInstance(args);
      } catch (Exception var3) {
         MMBSystem.fatalS((Object)"Cannot instantiate class");
         return null;
      }
   }
}
