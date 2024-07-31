package com.kirbymimi.mmb.ut.ktml;

import com.kirbymimi.mmb.ut.ClassUT;
import com.kirbymimi.mmb.ut.FieldUT;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;

public class KTML2Obj {
   String[] classPaths;
   Object root;

   public static void loadS(Object obj, KTMLEntry ktml) {
      (new KTML2Obj(obj)).load(obj, ktml);
   }

   public KTML2Obj(Object root) {
      this.root = root;
   }

   public void setClassSearch(String[] classPaths) {
      this.classPaths = classPaths;
   }

   public Object load(Object obj, KTMLEntry ktml) {
      return this.load(obj, (Class)null, (Class)null, ktml);
   }

   public Object rootLoad(KTMLEntry ktml) {
      return this.load(this.root, ktml);
   }

   public Object load(KTMLEntry ktml) {
      return this.load((Object)null, (Class)null, (Class)null, ktml);
   }

   public Object load(Object obj, Class<?> type, Class<?> parameterizedType, KTMLEntry ktml) {
      try {
         if (obj == null) {
            if (ktml.type != null) {
               Class<?> searchCls = ClassUT.search(ktml.type, this.classPaths);
               if (searchCls != null) {
                  type = searchCls;
               }
            }

            if (type == null) {
               return null;
            }

            if (type.isPrimitive()) {
               return ktml.getValue();
            }

            obj = this.resolveSimple(type, ktml);
            if (obj != null) {
               return obj;
            }

            obj = this.construct(ktml, type);
         } else {
            type = obj.getClass();
         }

         KTMLEntry entry;
         Iterator var7;
         if (type.isArray()) {
            int i = 0;
            var7 = ktml.iterator();

            while(var7.hasNext()) {
               entry = (KTMLEntry)var7.next();
               Array.set(obj, i++, this.load((Object)null, type.getComponentType(), (Class)null, entry));
            }

            return obj;
         } else if (!List.class.isAssignableFrom(type)) {
            Iterator var20 = ktml.iterator();

            while(var20.hasNext()) {
               KTMLEntry v = (KTMLEntry)var20.next();
               Field f = null;
               if (v.name != null) {
                  f = FieldUT.getField(type, v.name);
               }

               Object child = null;
               if (f == null) {
                  Method m = FieldUT.getMethod(type, "ktmlEntry", KTMLEntry.class);
                  if (m != null) {
                     child = m.invoke(obj, v);
                     if (child == null) {
                     }
                  }
               } else {
                  boolean acc = f.isAccessible();
                  f.setAccessible(true);
                  Class<?> genericCls = null;
                  Type generic = f.getGenericType();
                  if (generic != null && generic instanceof ParameterizedType) {
                     genericCls = (Class)((ParameterizedType)generic).getActualTypeArguments()[0];
                  }

                  child = this.load(child, f.getType(), genericCls, v);

                  try {
                     if (child != null) {
                        f.set(obj, child);
                     }
                  } catch (Exception var14) {
                     var14.printStackTrace();
                  }

                  f.setAccessible(acc);
               }
            }

            try {
               Method m = type.getMethod("loadDone", (Class[])null);
               m.invoke(obj, (Object[])null);
            } catch (Exception var13) {
            }

            return obj;
         } else {
            List lst = (List)obj;
            var7 = ktml.iterator();

            while(var7.hasNext()) {
               entry = (KTMLEntry)var7.next();
               lst.add(this.load((Object)null, parameterizedType, (Class)null, entry));
            }

            return obj;
         }
      } catch (Exception var15) {
         var15.printStackTrace();
         return null;
      }
   }

   Object resolveSimple(Class<?> type, KTMLEntry v) {
      if (type == Byte.class) {
         return v.getAsByte();
      } else if (type == Short.class) {
         return v.getAsShort();
      } else if (type == Integer.class) {
         return v.getAsInt();
      } else if (type == Long.class) {
         return v.getAsLong();
      } else if (type == Boolean.class) {
         return v.getAsBoolean();
      } else if (type == Character.class) {
         return v.getAsChar();
      } else if (type == Float.class) {
         return v.getAsFloat();
      } else if (type == Double.class) {
         return v.getAsDouble();
      } else if (type.isEnum()) {
         return v.getAsEnum(type);
      } else {
         return type == String.class ? v.getAsString() : null;
      }
   }

   Object construct(KTMLEntry entry, Class<?> cls) {
      if (cls.isArray()) {
         return Array.newInstance(cls.getComponentType(), entry.getElementCount());
      } else {
         try {
            return cls.getConstructor(this.root.getClass()).newInstance(this.root);
         } catch (Exception var7) {
            try {
               return cls.getConstructor(entry.getValue().getClass()).newInstance(entry.getValue());
            } catch (Exception var6) {
               try {
                  Method m = FieldUT.getMethod(this.root.getClass(), "construct", KTMLEntry.class);
                  return m.invoke(this.root, entry);
               } catch (Exception var5) {
                  try {
                     return cls.newInstance();
                  } catch (Exception var4) {
                     return null;
                  }
               }
            }
         }
      }
   }
}
