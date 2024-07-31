package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.consumers.TriConsumer;
import com.kirbymimi.mmb.ut.consumers.TriFunction;
import com.kirbymimi.mmb.ut.list.SafeList;
import java.lang.annotation.Annotation;
import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClassLinker {
   static HashMap<Class<?>, ClassLinker.Links> links = new HashMap();

   public static void entry(Class<?> cls, ClassLinker.Prop prop) {
      ClassLinker.Links method = registerClass(cls);
      if (!method.propertiesSet.contains(prop)) {
         addProp(cls, method, prop);
      }
   }

   static ClassLinker.Links registerClass(Class<?> cls) {
      ClassLinker.Links method = (ClassLinker.Links)links.get(cls);
      if (method == null) {
         method = new ClassLinker.Links();
         links.put(cls, method);
      }

      return method;
   }

   public static void updateLinks(Class<?> cls) {
      ClassLinker.Links method = registerClass(cls);
      Class<?> parent = cls.getSuperclass();
      if (parent != null) {
         updateLinks(parent);
         ClassLinker.Links parentMethod = (ClassLinker.Links)links.get(parent);
         Iterator var5 = parentMethod.properties.iterator();

         while(var5.hasNext()) {
            ClassLinker.Prop prop = (ClassLinker.Prop)var5.next();
            if (!method.propertiesSet.contains(prop)) {
               addProp(cls, method, prop);
            }
         }

      }
   }

   private static void addProp(Class<?> cls, ClassLinker.Links links, ClassLinker.Prop prop) {
      links.properties.add(prop);
      links.propertiesSet.add(prop);

      while(cls != null) {
         Method[] var6;
         int var5 = (var6 = cls.getDeclaredMethods()).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            Method method = var6[var4];
            if (prop.filter(method)) {
               Object lambda = createLambda(method);
               links.methods.put(prop.key(method), lambda);
            }
         }

         cls = cls.getSuperclass();
      }

   }

   private static Object getMethod(Class<?> cls, Object key) {
      for(; cls != null; cls = cls.getSuperclass()) {
         ClassLinker.Links link = (ClassLinker.Links)links.get(cls);
         if (link != null) {
            Object ret = link.methods.get(key);
            if (ret != null) {
               return ret;
            }
         }
      }

      return null;
   }

   public static <T> Consumer<T> getConsumer(Class<?> cls, Object key) {
      return (Consumer)getMethod(cls, key);
   }

   public static <T, U> BiConsumer<T, U> getBiConsumer(Class<?> cls, Object key) {
      return (BiConsumer)getMethod(cls, key);
   }

   public static <T, U, V> TriConsumer<T, U, V> getTriConsumer(Class<?> cls, Object key) {
      return (TriConsumer)getMethod(cls, key);
   }

   public static <T, R> Function<T, R> getFunction(Class<?> cls, Object key) {
      return (Function)getMethod(cls, key);
   }

   public static <T, U, R> BiFunction<T, U, R> getBiFunction(Class<?> cls, Object key) {
      return (BiFunction)getMethod(cls, key);
   }

   public static <T, U, V, R> TriFunction<T, U, V, R> getTriFunction(Class<?> cls, Object key) {
      return (TriFunction)getMethod(cls, key);
   }

   public static Object createLambda(Method m) {
      Class<?> cls = null;
      String funcName;
      if (m.getReturnType() == Void.TYPE) {
         funcName = "accept";
         switch(m.getParameterCount() + 1) {
         case 1:
            cls = Consumer.class;
            break;
         case 2:
            cls = BiConsumer.class;
            break;
         case 3:
            cls = TriConsumer.class;
            break;
         default:
            MMBSystem.fatalS((Object)"Bad parameter count for lambda creation");
         }
      } else {
         funcName = "apply";
         switch(m.getParameterCount() + 1) {
         case 1:
            cls = Function.class;
            break;
         case 2:
            cls = BiFunction.class;
            break;
         case 3:
            cls = TriFunction.class;
            break;
         default:
            MMBSystem.fatalS((Object)"Bad parameter count for lambda creation");
         }
      }

      return createLambda(m, funcName, cls);
   }

   public static <T> T createLambda(Method m, String funcName, Class<T> functionClass) {
      try {
         Class[] parms = new Class[m.getParameterCount()];
         Arrays.fill(parms, Object.class);
         Lookup caller = MethodHandles.lookup();
         CallSite site = LambdaMetafactory.metafactory(caller, funcName, MethodType.methodType(functionClass), MethodType.methodType(m.getReturnType(), Object.class, parms), caller.findVirtual(m.getDeclaringClass(), m.getName(), MethodType.methodType(m.getReturnType(), m.getParameterTypes())), MethodType.methodType(m.getReturnType(), m.getDeclaringClass(), m.getParameterTypes()));
         MethodHandle factory = site.getTarget();
         return (T) factory.invoke();
      } catch (Throwable var7) {
         var7.printStackTrace();
         return null;
      }
   }

   public static class Links {
      HashMap<Object, Object> methods = new HashMap();
      SafeList<ClassLinker.Prop> properties = new SafeList();
      HashSet<ClassLinker.Prop> propertiesSet = new HashSet();
   }

   public abstract static class Prop {
      public abstract boolean filter(Method var1);

      public abstract Object key(Method var1);
   }

   public abstract static class PropAnnotation extends ClassLinker.Prop {
      Class<? extends Annotation> annotation;

      public PropAnnotation(Class<? extends Annotation> annotation) {
         this.annotation = annotation;
      }

      public boolean filter(Method method) {
         return method.getAnnotation(this.annotation) != null;
      }
   }

   public static class PropStringStart extends ClassLinker.Prop {
      String start;

      public PropStringStart(String start) {
         this.start = start;
      }

      public boolean filter(Method method) {
         return method.getName().startsWith(this.start);
      }

      public Object key(Method method) {
         return method.getName();
      }
   }
}
