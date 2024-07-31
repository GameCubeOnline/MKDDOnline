package com.kirbymimi.mmb.math;

public class Point {
   public double x;
   public double y;
   public double z;
   public Expression xCalc;
   public Expression xCalc2;
   public Expression yCalc;
   public Expression yCalc2;
   public Expression zCalc;
   public Expression zCalc2;

   public Point() {
   }

   public Point(int x, int y) {
      this.set(x, y);
   }

   public Point(int x, int y, int z) {
      this.set(x, y, z);
   }

   public Point(double x, double y) {
      this.set(x, y);
   }

   public Point(double x, double y, double z) {
      this.set(x, y, z);
   }

   public Point(Point p) {
      this.x = p.x;
      this.y = p.y;
      this.z = p.z;
   }

   public Point clone() {
      return new Point(this);
   }

   public void set(Point p) {
      this.x = p.x;
      this.y = p.y;
      this.z = p.z;
   }

   public void set(int x, int y) {
      this.x = (double)x;
      this.y = (double)y;
   }

   public void set(int x, int y, int z) {
      this.x = (double)x;
      this.y = (double)y;
      this.z = (double)z;
   }

   public void set(double x, double y) {
      this.x = x;
      this.y = y;
   }

   public void set(double x, double y, double z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void add(Point second) {
      this.add(this, second);
   }

   public void add(Point first, Point second) {
      this.x = first.x + second.x;
      this.y = first.y + second.y;
      this.z = first.z + second.z;
   }

   public void updateExpressions(Object arg) {
      if (this.xCalc != null) {
         try {
            this.x = this.xCalc.resolve();
         } catch (Expression.ExpressionResolveException var5) {
            if (this.xCalc2 != null) {
               this.x = this.xCalc2.resolve();
            }
         }
      }

      if (this.yCalc != null) {
         try {
            this.y = this.yCalc.resolve();
         } catch (Expression.ExpressionResolveException var4) {
            if (this.yCalc2 != null) {
               this.y = this.yCalc2.resolve();
            }
         }
      }

      if (this.zCalc != null) {
         try {
            this.z = this.zCalc.resolve();
         } catch (Expression.ExpressionResolveException var3) {
            if (this.zCalc2 != null) {
               this.z = this.zCalc2.resolve();
            }
         }
      }

   }
}
