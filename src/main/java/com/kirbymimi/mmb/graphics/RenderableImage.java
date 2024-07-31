package com.kirbymimi.mmb.graphics;

import java.awt.Image;

public class RenderableImage implements Renderable {
   Image image;

   public RenderableImage(Image image) {
      this.image = image;
   }

   public Image getImage() {
      return this.image;
   }

   public void render(Graphics graphics) {
      graphics.drawImage(this.image);
   }

   public void render(Graphics graphics, double x, double y) {
      graphics.drawImage(this.image, x, y);
   }
}
