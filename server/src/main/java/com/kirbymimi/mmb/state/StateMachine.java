package com.kirbymimi.mmb.state;

public class StateMachine<T, U extends State<T>> {
   T parent;
   U current;

   public StateMachine(T parent) {
      this.parent = parent;
   }

   public boolean transit(Object next) {
      return this.transit2((U) next);
   }

   public boolean transit2(U next) {
      next.parent = this.parent;
      if (!next.enter(this.current)) {
         return false;
      } else if (this.current != null && !this.current.exit(next)) {
         return false;
      } else {
         next.init(this.current);
         this.current = next;
         return true;
      }
   }

   public void run() {
      if (this.current != null) {
         this.current.run();
      }
   }

   public U state() {
      return this.current;
   }

   public Class<?> getCurrentStateClass() {
      return this.current.getClass();
   }
}
