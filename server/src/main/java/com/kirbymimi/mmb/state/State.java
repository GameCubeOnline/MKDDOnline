package com.kirbymimi.mmb.state;

public abstract class State<T> {
   protected T parent;

   public boolean enter(State<T> prev) {
      return true;
   }

   public boolean exit(State<T> next) {
      return true;
   }

   public void init(State<T> prev) {
   }

   public void run() {
   }
}
