package com.kirbymimi.mmb.math;

import com.kirbymimi.mmb.ut.ArrayUT;
import com.kirbymimi.mmb.ut.list.SafeList;
import java.util.Iterator;

public class Shape {
   int pointCount;
   SafeList<double[]> positions;
   SafeList<VecPair> sortedPositions;
   SafeList<double[]> transformedPositions;
   VecPair boundingBox;
   double[] transform;
   boolean autoRecalc;

   public Shape(int pointCount, boolean autoRecalc) {
      this.positions = new SafeList();
      this.sortedPositions = new SafeList();
      this.transformedPositions = new SafeList();
      this.pointCount = pointCount;
      this.autoRecalc = autoRecalc;
      this.boundingBox = new VecPair(new Object[0]);
   }

   public Shape(int pointCount) {
      this(pointCount, false);
   }

   public Shape setTransform(double[] transform) {
      this.transform = transform;
      return this;
   }

   public Shape setAutoRecalc(boolean autoRecalc) {
      this.autoRecalc = autoRecalc;
      return this;
   }

   public void recalc() {
      if (!this.positions.isEmpty()) {
         if (this.transform != null) {
            while(this.transformedPositions.length() != this.positions.length()) {
               if (this.positions.length() > this.transformedPositions.length()) {
                  this.positions.add(new double[this.pointCount]);
                  this.transformedPositions.add(new double[this.pointCount]);
               } else {
                  this.positions.remove(this.positions.first());
                  this.transformedPositions.remove(this.transformedPositions.first());
               }
            }

            Iterator<double[]> transformIterator = this.transformedPositions.iterator();
            Iterator var3 = this.positions.iterator();

            while(var3.hasNext()) {
               double[] pos = (double[])var3.next();
               ArrayUT.arrAdd((double[])transformIterator.next(), pos, this.transform);
            }
         }

         int compCnt = ((double[])this.positions.first()).length;

         for(int i = 0; i != compCnt; ++i) {
            double min = Double.MAX_VALUE;
            double max = Double.MIN_VALUE;
            Iterator var8 = (this.transform == null ? this.positions : this.transformedPositions).iterator();

            while(var8.hasNext()) {
               double[] d = (double[])var8.next();
               double val = Double.valueOf(d[i]);
               if (min > val) {
                  min = val;
               }

               if (max < val) {
                  max = val;
               }
            }

            this.boundingBox.set(0, i, min);
            this.boundingBox.set(1, i, max);
         }

         if (this.positions.length() > 1) {
            ;
         }
      }
   }

   public boolean intersect(Shape that) {
      return false;
   }

   public void entry(double[] point) {
      this.positions.add(point);
   }

   public void entry(Object... values) {
      double[] add = new double[this.pointCount];

      for(int i = 0; i != this.pointCount; ++i) {
         add[i] = (Double)values[i];
      }

      this.entry(add);
   }

   public void addLoop() {
      this.positions.add((double[])this.positions.first());
   }
}
