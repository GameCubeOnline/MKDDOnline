package com.kirbymimi.mmb.graphics;

import java.awt.geom.AffineTransform;

public class Matrix2D implements Matrix {
   AffineTransform transform;

   public Matrix2D() {
      this.transform = new AffineTransform();
   }

   public Matrix2D(AffineTransform src) {
      this.transform = src;
   }

   public Matrix2D(double[] values) {
      this.transform = new AffineTransform(values);
   }

   public AffineTransform getAffineTransform() {
      return this.transform;
   }

   public void setIdentity() {
      this.transform.setToIdentity();
   }

   public void setScale(double x, double y) {
      this.transform.setToScale(x, y);
   }

   public void scale(double x, double y) {
      this.transform.scale(x, y);
   }

   public void setTranslate(double x, double y) {
      this.transform.setToTranslation(x, y);
   }

   public void translate(double x, double y) {
      this.transform.translate(x, y);
   }

   public double getScaleX() {
      return this.transform.getScaleX();
   }

   public double getScaleY() {
      return this.transform.getScaleY();
   }

   public double getX() {
      return this.transform.getTranslateX();
   }

   public double getY() {
      return this.transform.getTranslateY();
   }

   public Matrix2D clone() {
      double[] retMtx = new double[6];
      this.transform.getMatrix(retMtx);
      return new Matrix2D(retMtx);
   }

   public void copy(Matrix src) {
      this.transform.setTransform(((Matrix2D)src).transform);
   }
}
