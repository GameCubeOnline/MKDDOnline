package com.kirbymimi.mmb.ut.list;

import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.IteratorWrap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class HashMapList<K, V> implements List<V> {
   HashMap<K, V> map;
   List<V> values;

   public HashMapList(int len) {
      this.map = new HashMap();
      this.values = new ArrayList();
   }

   public HashMapList() {
      this(false);
   }

   public HashMapList(boolean safe) {
      this.map = new HashMap();
      if (safe) {
         this.values = new SafeList();
      } else {
         this.values = new FastList();
      }

   }

   public V mapGet(K key) {
      return this.map.get(key);
   }

   public boolean containsKey(K key) {
      return this.map.containsKey(key);
   }

   public V put(K key, V val) {
      if (key == null) {
         this.values.add(val);
         return null;
      } else {
         V ret = this.map.put(key, val);
         if (ret != null) {
            int idx = this.values.indexOf(ret);
            this.values.remove(ret);
            this.values.add(idx, val);
         } else {
            this.values.add(val);
         }

         return ret;
      }
   }

   public Iterable<V> reverseIterator() {
      return new IteratorWrap(new IteratorWrap.ReverseIterator(this.values));
   }

   public boolean remove(Object o) {
      this.map.remove(o);
      return this.values.remove(o);
   }

   public boolean add(V o) {
      return this.values.add(o);
   }

   public void add(int idx, V o) {
      this.values.add(idx, o);
   }

   public boolean addAll(Collection<? extends V> lst) {
      return this.values.addAll(lst);
   }

   public boolean addAll(int idx, Collection<? extends V> lst) {
      return this.values.addAll(idx, lst);
   }

   public boolean contains(Object o) {
      return this.map.containsKey(o);
   }

   public boolean containsAll(Collection<?> o) {
      return this.values.contains(o);
   }

   public V get(int idx) {
      return this.values.get(idx);
   }

   public int indexOf(Object o) {
      return this.values.indexOf(o);
   }

   public int lastIndexOf(Object o) {
      return this.values.lastIndexOf(o);
   }

   public ListIterator<V> listIterator() {
      return this.values.listIterator();
   }

   public ListIterator<V> listIterator(int idx) {
      return this.values.listIterator(idx);
   }

   public V remove(int idx) {
      V val = this.values.get(idx);
      this.map.remove(val);
      this.values.remove(idx);
      return val;
   }

   public boolean removeAll(Collection<?> lst) {
      Iterator var3 = lst.iterator();

      while(var3.hasNext()) {
         Object o = var3.next();
         this.map.remove(o);
      }

      return this.values.remove(lst);
   }

   public boolean retainAll(Collection<?> lst) {
      boolean[] marks = new boolean[this.values.size()];
      int cnt = 0;

      int i;
      for(i = 0; i != this.size(); ++i) {
         if (lst.contains(this.values.get(i))) {
            marks[i] = true;
            ++cnt;
         }
      }

      if (cnt == this.values.size()) {
         return false;
      } else {
         for(i = 0; i != this.values.size(); ++i) {
            if (!marks[i]) {
               this.map.remove(this.get(i));
               this.values.remove(i);
            }
         }

         return true;
      }
   }

   public V set(int arg0, V arg1) {
      MMBSystem.fatalS((Object)"Illegal operation");
      return null;
   }

   public List<V> subList(int start, int end) {
      return this.values.subList(start, end);
   }

   public Object[] toArray() {
      return this.values.toArray();
   }

   public <T> T[] toArray(T[] idx) {
      return this.values.toArray(idx);
   }

   public void clear() {
      this.map.clear();
      this.values.clear();
   }

   public boolean isEmpty() {
      return this.values.isEmpty();
   }

   public Iterator<V> iterator() {
      return this.values.iterator();
   }

   public int size() {
      return this.values.size();
   }
}
