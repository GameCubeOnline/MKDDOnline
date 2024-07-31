package com.kirbymimi.mmb.ui;

import com.kirbymimi.mmb.graphics.Graphics;
import com.kirbymimi.mmb.math.Point;

public class Button extends Component {
   int color;

   public void render(Graphics graph) {
      super.render(graph);
      graph.setARGB(this.color);
      Point pos = this.getPosition();
      Point dim = this.getRealDimension();
      graph.fillRect(pos.x, pos.y, dim.x, dim.y);
   }
}
