package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.ut.list.FastList;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;

public class Judge {
   Object origin;
   FastList<Object> objects = new FastList();
   static Judge.JudgePropAnnotation props = new Judge.JudgePropAnnotation();

   public Judge() {
   }

   public Judge(Object origin) {
      this.origin = origin;
      this.entry(origin);
   }

   public void entry(Object obj) {
      this.objects.add(obj);
      ClassLinker.entry(obj.getClass(), props);
   }

   public void remove(Object obj) {
      this.objects.remove(obj);
   }

   public <T> int judge(T argument, String name) {
      int cval = 0;
      Iterator var5 = this.objects.iterator();

      while(var5.hasNext()) {
         Object obj = var5.next();
         BiFunction<Object, T, Integer> func = ClassLinker.getBiFunction(obj.getClass(), name);
         if (func != null) {
            int newVal = (Integer)func.apply(obj, argument);
            if (Math.abs(newVal) > Math.abs(cval)) {
               cval = newVal;
            }
         }
      }

      return cval;
   }

   public int judge(String name) {
      int cval = 0;
      Iterator var4 = this.objects.iterator();

      while(var4.hasNext()) {
         Object obj = var4.next();
         Function<Object, Integer> func = ClassLinker.getFunction(obj.getClass(), name);
         if (func != null) {
            int newVal = (Integer)func.apply(obj);
            if (Math.abs(newVal) > Math.abs(cval)) {
               cval = newVal;
            }
         }
      }

      return cval;
   }

   public boolean judgeB(String name) {
      return this.judge(name) >= 0;
   }

   public <T> boolean judgeB(T argument, String name) {
      return this.judge(argument, name) >= 0;
   }

   public static class JudgePropAnnotation extends ClassLinker.PropAnnotation {
      public JudgePropAnnotation() {
         super(Jury.class);
      }

      public Object key(Method method) {
         return ((Jury)method.getAnnotation(Jury.class)).value();
      }
   }
}
