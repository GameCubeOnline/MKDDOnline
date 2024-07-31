package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.system.MMBSystem;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public class IteratorWrap<T> implements Iterable<T> {
   Object it;

   public IteratorWrap(Object it) {
      this.it = it;
   }

   public Iterator<T> iterator() {
      if (this.it instanceof Iterable) {
         return ((Iterable)this.it).iterator();
      } else if (this.it instanceof Iterator) {
         return (Iterator)this.it;
      } else if (this.it instanceof Enumeration) {
         return new IteratorWrap.EnumIterator((Enumeration)this.it);
      } else {
         MMBSystem.fatalS((Object)("cannot iterator over " + this.it.getClass()));
         return null;
      }
   }

   public static <T> Iterable<T> supplierIterator(List<?> lst, Class<T> cls) {
      return new IteratorWrap.SupplierIterator(lst);
   }

   public static class ArrayIterator<T> implements Iterator<T>, Iterable<T> {
      T[] src;
      int idx;

      public ArrayIterator(T[] src) {
         this.src = src;
      }

      public boolean hasNext() {
         return this.idx != this.src.length;
      }

      public T next() {
         return this.src[this.idx++];
      }

      public Iterator<T> iterator() {
         return this;
      }
   }

   public static class CastIterator<T> implements Iterator<T>, Iterable<T> {
      Iterator<?> src;
      Class<T> cls;
      T next;

      public CastIterator(List<?> src, Class<T> cls) {
         this.src = src.iterator();
         this.cls = cls;
      }

      public Iterator<T> iterator() {
         return this;
      }

      public boolean hasNext() {
         do {
            if (!this.src.hasNext()) {
               return false;
            }

            this.next = (T) this.src.next();
         } while(!this.cls.isInstance(this.next));

         return true;
      }

      public T next() {
         return this.next;
      }
   }

   public static class EnumIterator<T> implements Iterator<T>, Iterable<T> {
      Enumeration<T> en;

      public EnumIterator(Enumeration<T> en) {
         this.en = en;
      }

      public boolean hasNext() {
         return this.en.hasMoreElements();
      }

      public T next() {
         return this.en.nextElement();
      }

      public Iterator<T> iterator() {
         return this;
      }
   }

   public static class ReverseIterator<T> implements Iterator<T>, Iterable<T> {
      List<T> src;
      int idx;

      public ReverseIterator(List<T> src) {
         this.src = src;
         this.idx = src.size() - 1;
      }

      public boolean hasNext() {
         return this.idx != -1;
      }

      public T next() {
         return this.src.get(this.idx--);
      }

      public Iterator<T> iterator() {
         return this;
      }
   }

   public static class SupplierIterator<T> implements Iterator<T>, Iterable<T> {
      List<?> src;
      int idx;
      T next;

      public SupplierIterator(List<?> src) {
         this.src = src;
      }

      public boolean hasNext() {
         Object o = null;

         do {
            if (this.idx == this.src.size()) {
               return false;
            }

            o = this.src.get(this.idx++);
            if (!(o instanceof Supplier)) {
               break;
            }

            o = ((Supplier)o).get();
         } while(o == null);

         this.next = (T) o;
         return true;
      }

      public T next() {
         return this.next;
      }

      public Iterator<T> iterator() {
         return this;
      }
   }
}
