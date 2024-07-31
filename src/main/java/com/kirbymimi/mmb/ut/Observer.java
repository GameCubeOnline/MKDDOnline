package com.kirbymimi.mmb.ut;

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.BiConsumer;

public class Observer {
   static Observer globalObserver = new Observer();
   HashMap<String, Observer.Child> children = new HashMap();

   public static void registerGlobal(Object observer, BiConsumer<Object, Observer.Event> callBack, String[] type) {
      globalObserver.register(observer, callBack, type);
   }

   public void register(Object observer, BiConsumer<Object, Observer.Event> callBack, String[] type) {
      if (type.length != 0) {
         HashMap<String, Observer.Child> cl = this.children;
         int i = 0;

         while(true) {
            String s = type[i];
            Observer.Child c = (Observer.Child)cl.get(s);
            if (c == null) {
               c = new Observer.Child((Observer.Child)null);
               cl.put(s, c);
            }

            if (i == type.length - 1) {
               ((Observer.Child)cl.get(type[type.length - 1])).listeners.add(new Observer.Listener(observer, callBack));
               return;
            }

            cl = c.childListeners;
            ++i;
         }
      }
   }

   public static void registerGlobal(Object observer, BiConsumer<Object, Observer.Event> callBack, String type) {
      globalObserver.register(observer, callBack, new String[]{type});
   }

   public void register(Object observer, BiConsumer<Object, Observer.Event> callBack, String type) {
      this.register(observer, callBack, new String[]{type});
   }

   public static void registerGlobal(Object observer, BiConsumer<Object, Observer.Event> callBack, String type, String name) {
      globalObserver.register(observer, callBack, new String[]{type, name});
   }

   public void register(Object observer, BiConsumer<Object, Observer.Event> callBack, String type, String name) {
      this.register(observer, callBack, new String[]{type, name});
   }

   public static void registerGlobal(Object observer, Method callBack, String type) {
      globalObserver.register(observer, createEventLambda(callBack, observer.getClass()), new String[]{type});
   }

   public void register(Object observer, Method callBack, String type) {
      this.register(observer, createEventLambda(callBack, observer.getClass()), new String[]{type});
   }

   public static void registerGlobal(Object observer, Method callBack, String type, String name) {
      globalObserver.register(observer, createEventLambda(callBack, observer.getClass()), new String[]{type, name});
   }

   public void register(Object observer, Method callBack, String type, String name) {
      this.register(observer, createEventLambda(callBack, observer.getClass()), new String[]{type, name});
   }

   static BiConsumer<Object, Observer.Event> createEventLambda(Method m, Class<?> c) {
      try {
         Lookup caller = MethodHandles.lookup();
         CallSite site = LambdaMetafactory.metafactory(caller, "accept", MethodType.methodType(BiConsumer.class), MethodType.methodType(Void.TYPE, Object.class, Object.class), caller.findVirtual(c, m.getName(), MethodType.methodType(Void.TYPE, m.getParameterTypes()[0])), MethodType.methodType(Void.TYPE, c, m.getParameterTypes()[0]));
         MethodHandle factory = site.getTarget();
         return (BiConsumer<Object, Event>) factory.invoke();
      } catch (Throwable var5) {
         var5.printStackTrace();
         return null;
      }
   }

   public static void sendEventGlobal(String[] type, Object data) {
      (new Observer.Event(type, data)).send(globalObserver);
   }

   public void sendEvent(String[] type, Object data) {
      (new Observer.Event(type, data)).send(this);
   }

   public static void sendEventGlobal(String type, Object data) {
      (new Observer.Event(type, data)).send(globalObserver);
   }

   public void sendEvent(String type, Object data) {
      (new Observer.Event(type, data)).send(this);
   }

   public static void sendEventGlobal(String type, String name, Object data) {
      (new Observer.Event(type, name, data)).send(globalObserver);
   }

   public void sendEvent(String type, String name, Object data) {
      (new Observer.Event(type, name, data)).send(this);
   }

   private static class Child {
      Vector<Observer.Listener> listeners;
      HashMap<String, Observer.Child> childListeners;

      private Child() {
         this.listeners = new Vector();
         this.childListeners = new HashMap();
      }

      // $FF: synthetic method
      Child(Observer.Child var1) {
         this();
      }
   }

   public static class Event {
      public String[] type;
      public Object data;

      public Event(String[] type, Object data) {
         this.type = type;
         this.data = data;
      }

      public Event(String type, Object data) {
         this.type = new String[]{type};
         this.data = data;
      }

      public Event(String type, String name, Object data) {
         this.type = new String[]{type, name};
         this.data = data;
      }

      public void send(Observer obs) {
         HashMap<String, Observer.Child> cl = obs.children;
         String[] var6;
         int var5 = (var6 = this.type).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            String s = var6[var4];
            Observer.Child c = (Observer.Child)cl.get(s);
            if (c == null) {
               return;
            }

            Iterator var9 = c.listeners.iterator();

            while(var9.hasNext()) {
               Observer.Listener l = (Observer.Listener)var9.next();
               l.callBack.accept(l.parent, this);
            }

            cl = c.childListeners;
         }

      }
   }

   private static class Listener {
      Object parent;
      BiConsumer<Object, Observer.Event> callBack;

      Listener(Object parent, BiConsumer<Object, Observer.Event> callBack) {
         this.parent = parent;
         this.callBack = callBack;
      }
   }
}
