package com.kirbymimi.mmb.ut;

public abstract class ObjComponent<T> {
   protected T parent;

   public ObjComponent(T parent) {
      this.parent = parent;
   }

   public abstract boolean run();
}
