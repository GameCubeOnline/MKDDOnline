package com.kirbymimi.mmb.ut;

public class TaskChain<T> {
   public T src;
   public TaskChain.Function<T> func;

   public TaskChain(T src, TaskChain.Function<T> func) {
      this.src = src;
      this.func = func;
   }

   public boolean exec() {
      if (this.func == null) {
         return true;
      } else {
         TaskChain.Function<T> ret = this.func.exec(this.src, this);
         if (ret != null) {
            this.func = ret;
         }

         return false;
      }
   }

   public void close() {
      this.func = null;
   }

   public interface Function<T> {
      TaskChain.Function<T> exec(T var1, TaskChain<T> var2);
   }
}
