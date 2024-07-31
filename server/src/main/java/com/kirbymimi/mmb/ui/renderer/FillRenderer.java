package com.kirbymimi.mmb.ui.renderer;

import com.kirbymimi.mmb.graphics.Graphics;
import com.kirbymimi.mmb.math.Point;

public class FillRenderer extends ComponentRenderer {
   int color;

   public void render(Graphics graph) {
      super.render(graph);
      graph.setARGB(this.color);
      Point dim = this.comp.getRealDimension();
      graph.fillRect(this.pos.x, this.pos.y, dim.x, dim.y);
   }

   public double getWidth(Graphics graph) {
      return this.comp.getRealDimension().x;
   }

   public double getHeight(Graphics graph) {
      return this.comp.getRealDimension().y;
   }
}
