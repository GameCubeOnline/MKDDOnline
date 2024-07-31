package com.kirbymimi.mmb.processor;

public class ForkValue extends Value {
   double min;
   double max;

   public double get() {
      if (this.value == Double.NaN) {
         this.value = Math.random() * (this.max - this.min) + this.min;
      }

      return this.value;
   }
}
