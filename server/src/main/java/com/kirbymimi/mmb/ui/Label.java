package com.kirbymimi.mmb.ui;

import com.kirbymimi.mmb.graphics.FontData;
import com.kirbymimi.mmb.graphics.Graphics;
import com.kirbymimi.mmb.math.Point;

public class Label extends Component {
   String text;
   FontData font;
   int bgcolor;

   public Label(Component parent) {
      super(parent);
   }

   public void render(Graphics graph) {
      super.render(graph);
      graph.setARGB(this.bgcolor);
      Point pos = this.getPosition();
      Point dim = this.getRealDimension();
      graph.fillRect(pos.x, pos.y, dim.x, dim.y);
      graph.setFont(this.font);
      graph.drawText(pos.x + 4.0D, pos.y + (double)this.font.size + this.getRealHeight() / 2.0D - 8.0D, this.text);
   }
}
