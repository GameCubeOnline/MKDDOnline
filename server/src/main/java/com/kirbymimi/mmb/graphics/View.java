package com.kirbymimi.mmb.graphics;

import com.kirbymimi.mmb.actor.Actor;
import com.kirbymimi.mmb.math.Point;

public class View {
   Actor target;
   Matrix viewport;
   Matrix camera;
   Matrix viewportClone;
   Matrix cameraClone;

   public View(Matrix viewport, Matrix camera) {
      this.target = this.target;
      this.viewportClone = viewport;
      this.cameraClone = camera;
      this.viewport = this.viewportClone.clone();
      this.camera = this.cameraClone.clone();
   }

   public void setTarget(Actor target) {
      this.target = target;
   }

   public void render(Graphics graphics) {
      Point winSize = graphics.getDefaultSize();
      this.viewport.copy(this.viewportClone);
      this.camera.copy(this.cameraClone);
      graphics.pushMatrix(this.viewport);
      graphics.pushMatrix(this.camera);
      graphics.setScissors((int)(-this.camera.getX()), (int)(-this.camera.getY()), (int)(this.camera.getScaleX() * winSize.x), (int)(this.camera.getScaleY() * winSize.y));
      this.target.graphicsEntry(graphics);
      graphics.renderListes();
      graphics.popMatrix();
      graphics.popMatrix();
   }

   public void setOrthogonal(Graphics graphics) {
      graphics.setTransform(graphics.getBaseMatrix());
      graphics.transform(this.viewport);
   }

   public Matrix getCamera() {
      return this.cameraClone;
   }

   public Matrix getViewport() {
      return this.viewport;
   }
}
