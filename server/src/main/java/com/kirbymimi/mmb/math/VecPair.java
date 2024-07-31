package com.kirbymimi.mmb.math;

import com.kirbymimi.mmb.ut.ArrayUT;
import com.kirbymimi.mmb.ut.ClassUT;

public class VecPair {
   double[][] pair = new double[2][];

   public VecPair(Object... poses) {
      this.set(poses);
   }

   public void set(Object... poses) {
      Object[][] passes = ArrayUT.halve(poses);
      this.pair[0] = (double[])ClassUT.construct(double[].class, passes[0]);
      this.pair[1] = (double[])ClassUT.construct(double[].class, passes[1]);
   }

   public void set(int x, int y, double val) {
      this.pair[x][y] = val;
   }
}
