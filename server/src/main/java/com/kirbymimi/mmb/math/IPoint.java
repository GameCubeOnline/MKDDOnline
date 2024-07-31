package com.kirbymimi.mmb.math;

public class IPoint {
   public int x;
   public int y;
   public int z;
   public Expression xCalc;
   public Expression xCalc2;
   public Expression yCalc;
   public Expression yCalc2;
   public Expression zCalc;
   public Expression zCalc2;

   public IPoint() {
   }

   public IPoint(int x, int y) {
      this.set(x, y);
   }

   public IPoint(int x, int y, int z) {
      this.set(x, y, z);
   }

   public IPoint(double x, double y) {
      this.set(x, y);
   }

   public IPoint(double x, double y, double z) {
      this.set(x, y, z);
   }

   public IPoint(IPoint p) {
      this.x = p.x;
      this.y = p.y;
      this.z = p.z;
   }

   public IPoint clone() {
      return new IPoint(this);
   }

   public void set(IPoint p) {
      this.x = p.x;
      this.y = p.y;
      this.z = p.z;
   }

   public void set(int x, int y) {
      this.x = x;
      this.y = y;
   }

   public void set(int x, int y, int z) {
      this.x = x;
      this.y = y;
      this.z = z;
   }

   public void set(double x, double y) {
      this.x = (int)x;
      this.y = (int)y;
   }

   public void set(double x, double y, double z) {
      this.x = (int)x;
      this.y = (int)y;
      this.z = (int)z;
   }

   public void add(IPoint second) {
      this.add(this, second);
   }

   public void add(IPoint first, IPoint second) {
      this.x = first.x + second.x;
      this.y = first.y + second.y;
      this.z = first.z + second.z;
   }

   public void updateExpressions(Object arg) {
      if (this.xCalc != null) {
         try {
            this.x = this.xCalc.resolvei();
         } catch (Expression.ExpressionResolveException var5) {
            if (this.xCalc2 != null) {
               this.x = this.xCalc2.resolvei();
            }
         }
      }

      if (this.yCalc != null) {
         try {
            this.y = this.yCalc.resolvei();
         } catch (Expression.ExpressionResolveException var4) {
            if (this.yCalc2 != null) {
               this.y = this.yCalc2.resolvei();
            }
         }
      }

      if (this.zCalc != null) {
         try {
            this.z = this.zCalc.resolvei();
         } catch (Expression.ExpressionResolveException var3) {
            if (this.zCalc2 != null) {
               this.z = this.zCalc2.resolvei();
            }
         }
      }

   }
}
