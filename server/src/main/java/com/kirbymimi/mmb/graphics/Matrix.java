package com.kirbymimi.mmb.graphics;

public interface Matrix {
   void setIdentity();

   void setScale(double var1, double var3);

   void scale(double var1, double var3);

   void setTranslate(double var1, double var3);

   void translate(double var1, double var3);

   double getScaleX();

   double getScaleY();

   double getX();

   double getY();

   Matrix clone();

   void copy(Matrix var1);
}
