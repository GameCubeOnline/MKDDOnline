package com.kirbymimi.mmb.ut.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class SafeList<T> implements List<T> {
   public T[] arr = (T[]) new Object[2];
   public T[] iterArr = (T[]) new Object[2];
   public int cPos;
   boolean regenerateIterator;

   public T[] toArray() {
      return this.arr;
   }

   public synchronized boolean add(T obj) {
      this.regenerateIterator = true;
      if (this.cPos == this.arr.length) {
         this.reAlloc(this.arr.length * 2);
      }

      this.arr[this.cPos++] = obj;
      return true;
   }

   public synchronized void add(int pos, T obj) {
      this.regenerateIterator = true;
      if (pos >= this.arr.length) {
         this.reAllocFixSize(pos);
      }

      System.arraycopy(this.arr, pos, this.arr, pos + 1, this.cPos - pos);
      this.arr[pos] = obj;
   }

   public synchronized boolean addAll(Collection<? extends T> c) {
      if (c.size() == 0) {
         return false;
      } else {
         this.regenerateIterator = true;
         Iterator var3 = c.iterator();

         while(var3.hasNext()) {
            T obj = (T) var3.next();
            this.add(obj);
         }

         return true;
      }
   }

   public synchronized boolean addAll(int pos, Collection<? extends T> c) {
      if (c.size() == pos) {
         return false;
      } else {
         this.regenerateIterator = true;
         int idx = pos;
         Iterator var5 = c.iterator();

         while(var5.hasNext()) {
            T obj = (T) var5.next();
            if (idx != 0) {
               --idx;
            } else {
               this.add(obj);
            }
         }

         return true;
      }
   }

   public synchronized T set(int pos, T element) {
      this.regenerateIterator = true;
      if (pos >= this.arr.length) {
         this.reAllocFixSize(pos);
      }

      if (this.cPos < pos) {
         this.cPos = pos;
      }

      T ret = this.arr[pos];
      this.arr[pos] = element;
      return ret;
   }

   void reAlloc(int size) {
      Object[] nArr = new Object[size];
      System.arraycopy(this.arr, 0, nArr, 0, this.cPos);
      this.arr = (T[]) nArr;
   }

   void reAllocFixSize(int size) {
      int i;
      for(i = 2; i <= size; i *= 2) {
      }

      if (this.arr.length < i) {
         this.reAlloc(i);
      }
   }

   public synchronized boolean remove(Object obj) {
      for(int cIdx = 0; cIdx != this.cPos; ++cIdx) {
         if (this.arr[cIdx] == obj) {
            this.regenerateIterator = true;
            this.arr[cIdx] = null;
            if (cIdx != this.cPos) {
               System.arraycopy(this.arr, cIdx + 1, this.arr, cIdx, this.cPos - cIdx - 1);
            }

            --this.cPos;
            return true;
         }
      }

      return false;
   }

   public synchronized T remove(int pos) {
      this.regenerateIterator = true;
      T ret = this.arr[pos];
      this.arr[pos] = null;
      if (pos != this.cPos) {
         System.arraycopy(this.arr, pos + 1, this.arr, pos, this.cPos - pos - 1);
      }

      --this.cPos;
      return ret;
   }

   public synchronized SafeList<T> clone() {
      SafeList<T> list = new SafeList();
      list.arr = (T[]) this.arr.clone();
      list.cPos = this.cPos;
      list.regenerateIterator = true;
      return list;
   }

   public synchronized T getRealloc(int idx) {
      if (this.cPos <= idx) {
         this.reAllocFixSize(idx);
         this.cPos = idx + 1;
      }

      return this.arr[idx];
   }

   public synchronized T get(int idx) {
      if (this.cPos <= idx) {
         throw new ArrayIndexOutOfBoundsException(idx);
      } else {
         return this.arr[idx];
      }
   }

   public int size() {
      return this.cPos;
   }

   public boolean isEmpty() {
      return this.cPos == 0;
   }

   public int length() {
      return this.cPos;
   }

   public synchronized void clear() {
      this.cPos = 0;
      this.regenerateIterator = true;
   }

   public T first() {
      return this.arr[0];
   }

   public boolean contains(Object o) {
      return this.indexOf(o) != -1;
   }

   public synchronized boolean containsAll(Collection<?> c) {
      Iterator var3 = c.iterator();

      while(var3.hasNext()) {
         Object o = var3.next();
         if (!this.contains(o)) {
            return false;
         }
      }

      return true;
   }

   public synchronized int indexOf(Object o) {
      for(int i = 0; i != this.cPos; ++i) {
         if (this.arr[i] == o) {
            return i;
         }
      }

      return -1;
   }

   public synchronized int lastIndexOf(Object o) {
      for(int i = -1; i != this.cPos - 1; --i) {
         if (this.arr[i] == o) {
            return i;
         }
      }

      return -1;
   }

   public ListIterator<T> listIterator() {
      return null;
   }

   public ListIterator<T> listIterator(int index) {
      return null;
   }

   public boolean removeAll(Collection<?> c) {
      Iterator var3 = c.iterator();

      while(var3.hasNext()) {
         Object o = var3.next();
         this.remove(o);
      }

      return false;
   }

   public synchronized boolean retainAll(Collection<?> c) {
      boolean[] marks = new boolean[this.cPos];
      int cnt = 0;

      int i;
      for(i = 0; i != this.size(); ++i) {
         if (c.contains(this.get(i))) {
            marks[i] = true;
            ++cnt;
         }
      }

      if (cnt == this.cPos) {
         return false;
      } else {
         this.regenerateIterator = true;

         for(i = 0; i != this.cPos; ++i) {
            if (!marks[i]) {
               this.remove(i);
            }
         }

         return true;
      }
   }

   public synchronized SafeList<T> subList(int fromIndex, int toIndex) {
      SafeList<T> ret = new SafeList();

      for(int i = fromIndex; i != toIndex; ++i) {
         ret.arr[i - fromIndex] = this.arr[i];
      }

      ret.regenerateIterator = true;
      return null;
   }

   public synchronized <U> U[] toArray(U[] a) {
      Object[] ret = new Object[this.cPos];
      System.arraycopy(this.arr, 0, ret, 0, this.cPos);
      return (U[]) ret;
   }

   void regenerateIterator() {
      if (this.regenerateIterator) {
         if (this.iterArr.length != this.arr.length) {
            this.iterArr = (T[]) new Object[this.arr.length];
         }

         this.regenerateIterator = false;
         System.arraycopy(this.arr, 0, this.iterArr, 0, this.cPos);
      }
   }

   public synchronized Iterator<T> iterator() {
      this.regenerateIterator();
      return new SafeList.SafeListIterator(this);
   }

   public static class SafeListIterator<T> implements Iterator<T> {
      public T[] arr;
      public int len;
      public int cPos;

      SafeListIterator(SafeList<T> lst) {
         this.arr = lst.iterArr;
         this.len = lst.cPos;
      }

      public boolean hasNext() {
         return this.cPos != this.len;
      }

      public T next() {
         return this.arr[this.cPos++];
      }
   }
}
