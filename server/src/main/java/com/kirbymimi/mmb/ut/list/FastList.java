package com.kirbymimi.mmb.ut.list;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

public class FastList<T> implements List<T> {
   public T[] arr = (T[]) new Object[2];
   public int cPos;

   public T[] toArray() {
      return this.arr;
   }

   public boolean add(T obj) {
      if (this.cPos == this.arr.length) {
         this.reAlloc(this.arr.length * 2);
      }

      this.arr[this.cPos++] = obj;
      return true;
   }

   public void add(int pos, T obj) {
      if (pos >= this.arr.length) {
         this.reAllocFixSize(pos);
      }

      System.arraycopy(this.arr, pos, this.arr, pos + 1, this.cPos - pos);
      this.arr[pos] = obj;
   }

   public boolean addAll(Collection<? extends T> c) {
      if (c.size() == 0) {
         return false;
      } else {
         Iterator var3 = c.iterator();

         while(var3.hasNext()) {
            T obj = (T) var3.next();
            this.add(obj);
         }

         return true;
      }
   }

   public boolean addAll(int pos, Collection<? extends T> c) {
      if (c.size() == pos) {
         return false;
      } else {
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

   public T set(int pos, T element) {
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

   public boolean remove(Object obj) {
      for(int cIdx = 0; cIdx != this.cPos; ++cIdx) {
         if (this.arr[cIdx] == obj) {
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

   public T remove(int pos) {
      T ret = this.arr[pos];
      this.arr[pos] = null;
      if (pos != this.cPos) {
         System.arraycopy(this.arr, pos + 1, this.arr, pos, this.cPos - pos - 1);
      }

      --this.cPos;
      return ret;
   }

   public FastList<T> clone() {
      FastList<T> list = new FastList();
      list.arr = (T[]) this.arr.clone();
      list.cPos = this.cPos;
      return list;
   }

   public T getRealloc(int idx) {
      if (this.cPos <= idx) {
         this.reAllocFixSize(idx);
         this.cPos = idx + 1;
      }

      return this.arr[idx];
   }

   public T get(int idx) {
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

   public void clear() {
      this.cPos = 0;
   }

   public T first() {
      return this.arr[0];
   }

   public boolean contains(Object o) {
      return this.indexOf(o) != -1;
   }

   public boolean containsAll(Collection<?> c) {
      Iterator var3 = c.iterator();

      while(var3.hasNext()) {
         Object o = var3.next();
         if (!this.contains(o)) {
            return false;
         }
      }

      return true;
   }

   public int indexOf(Object o) {
      for(int i = 0; i != this.cPos; ++i) {
         if (this.arr[i] == o) {
            return i;
         }
      }

      return -1;
   }

   public int lastIndexOf(Object o) {
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

   public boolean retainAll(Collection<?> c) {
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
         for(i = 0; i != this.cPos; ++i) {
            if (!marks[i]) {
               this.remove(i);
            }
         }

         return true;
      }
   }

   public FastList<T> subList(int fromIndex, int toIndex) {
      FastList<T> ret = new FastList();

      for(int i = fromIndex; i != toIndex; ++i) {
         ret.arr[i - fromIndex] = this.arr[i];
      }

      return null;
   }

   public <U> U[] toArray(U[] a) {
      Object[] ret = new Object[this.cPos];
      System.arraycopy(this.arr, 0, ret, 0, this.cPos);
      return (U[]) ret;
   }

   public Iterator<T> iterator() {
      return new FastList.FastListIterator(this);
   }

   public static class FastListIterator<T> implements Iterator<T> {
      public T[] arr;
      public int len;
      public int cPos;

      FastListIterator(FastList<T> Lst) {
         this.arr = Lst.arr;
         this.len = Lst.cPos;
      }

      public boolean hasNext() {
         return this.cPos != this.len;
      }

      public T next() {
         return this.arr[this.cPos++];
      }
   }
}
