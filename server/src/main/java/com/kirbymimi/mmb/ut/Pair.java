package com.kirbymimi.mmb.ut;

public class Pair<T, U> {
   T first;
   U second;

   public Pair(T first, U second) {
      this.first = first;
      this.second = second;
   }

   public T getFirst() {
      return this.first;
   }

   public U getSecond() {
      return this.second;
   }
}
