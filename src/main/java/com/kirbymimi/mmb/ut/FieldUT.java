package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.system.MMBSystem;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldUT {
   public static final Field getField(Class<?> cls, String field) {
      while(true) {
         try {
            return cls.getDeclaredField(field);
         } catch (Exception var3) {
            cls = cls.getSuperclass();
            if (cls == null) {
               return null;
            }
         }
      }
   }

   public static final Method getMethod(Class<?> cls, String method) {
      while(true) {
         try {
            return cls.getDeclaredMethod(method, (Class[])null);
         } catch (Exception var3) {
            cls = cls.getSuperclass();
            if (cls == null) {
               return null;
            }
         }
      }
   }

   public static final Method getMethod(Class<?> cls, String method, Class<?>... args) {
      while(true) {
         try {
            return cls.getDeclaredMethod(method, args);
         } catch (Exception var4) {
            cls = cls.getSuperclass();
            if (cls == null) {
               return null;
            }
         }
      }
   }

   public static <T> T newInstance(Class<T> cls) {
      try {
         return cls.newInstance();
      } catch (Exception var2) {
         return null;
      }
   }

   public static <T> T newInstance(Class<T> cls, Object[] args) {
      try {
         if (args == null) {
            return cls.newInstance();
         } else {
            Class[] classes = new Class[args.length];

            for(int i = 0; i != args.length; ++i) {
               classes[i] = args[i].getClass();
            }

            return cls.getConstructor(classes).newInstance(args);
         }
      } catch (Exception var4) {
         return null;
      }
   }
}
