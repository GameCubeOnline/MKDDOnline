package com.kirbymimi.mmb.ui.renderer;

import com.kirbymimi.mmb.graphics.FontData;
import com.kirbymimi.mmb.graphics.Graphics;

public class TextRenderer extends ComponentRenderer {
   String text;
   FontData font;
   int color;

   public void render(Graphics graph) {
      super.render(graph);
      graph.setFont(this.font);
      graph.setARGB(this.color);
      graph.drawText(this.pos.x, this.pos.y + (double)this.font.size - 2.0D, this.text);
   }

   public double getWidth(Graphics graph) {
      return (double)graph.textWidth(this.font, this.text);
   }

   public double getHeight(Graphics graph) {
      return (double)this.font.size;
   }
}
