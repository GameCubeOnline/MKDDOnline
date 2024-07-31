package com.kirbymimi.mmb.ut;

import java.util.HashMap;
import java.util.Iterator;

public class ConcurrentVariable<T> {
   HashMap<Class<?>, ConcurrentVariable.Entry<T>> map = new HashMap();
   T defaultValue;
   ConcurrentVariable.Entry<T> best;

   public ConcurrentVariable(T defaultValue) {
      this.defaultValue = defaultValue;
   }

   public synchronized void entry(Class<?> key, int weight, T value) {
      ConcurrentVariable.Entry<T> entry = (ConcurrentVariable.Entry)this.map.get(key);
      if (entry == null) {
         entry = new ConcurrentVariable.Entry();
         this.map.put(key, entry);
      }

      entry.weight = weight;
      entry.value = value;
      if (this.best == entry) {
         this.recalcBest();
      } else if (this.best == null || this.best.weight < weight) {
         this.best = entry;
      }

   }

   public synchronized void remove(Class<?> key) {
      if (this.map.containsKey(key)) {
         this.map.remove(key);
         if (this.map.isEmpty()) {
            this.best = null;
         } else {
            this.recalcBest();
         }
      }
   }

   void recalcBest() {
      ConcurrentVariable.Entry<T> bestEntry = null;
      Iterator var3 = this.map.values().iterator();

      while(var3.hasNext()) {
         ConcurrentVariable.Entry<T> entry = (ConcurrentVariable.Entry)var3.next();
         if (bestEntry != null && entry.weight > bestEntry.weight) {
            bestEntry = entry;
         }
      }

      this.best = bestEntry;
   }

   public T get() {
      return this.best == null ? this.defaultValue : this.best.value;
   }

   static class Entry<T> {
      int weight;
      T value;
   }
}
