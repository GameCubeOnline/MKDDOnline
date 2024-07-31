package com.kirbymimi.mmb.ut.list;

import java.util.concurrent.locks.ReentrantLock;

public class FastStack<T> {
   boolean synced;
   ReentrantLock lock;
   T[] arr = (T[]) new Object[8];
   int cpos;

   public FastStack() {
   }

   public FastStack(boolean synced) {
      if (synced) {
         this.synced = true;
         this.lock = new ReentrantLock();
      }

   }

   public void push(T obj) {
      if (this.synced) {
         this.lock.lock();
      }

      if (this.cpos - 1 >= this.arr.length) {
         Object[] narr = new Object[this.arr.length << 1];
         System.arraycopy(this.arr, 0, narr, 0, this.arr.length);
         this.arr = (T[]) narr;
      }

      this.arr[this.cpos++] = obj;
      if (this.synced) {
         this.lock.unlock();
      }

   }

   public T pop() {
      if (this.cpos == 0) {
         return null;
      } else {
         if (this.synced) {
            this.lock.lock();
         }

         --this.cpos;
         T ret = this.arr[this.cpos];
         if (this.synced) {
            this.lock.unlock();
         }

         return ret;
      }
   }

   public T peek() {
      return this.cpos == 0 ? null : this.arr[this.cpos - 1];
   }

   public boolean isEmpty() {
      return this.cpos == 0;
   }
}
