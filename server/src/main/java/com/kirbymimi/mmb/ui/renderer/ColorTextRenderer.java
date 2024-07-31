package com.kirbymimi.mmb.ui.renderer;

import com.kirbymimi.mmb.graphics.FontData;
import com.kirbymimi.mmb.graphics.Graphics;

public class ColorTextRenderer extends ComponentRenderer {
   String text;
   FontData font;
   int[] colors;

   public void render(Graphics graph) {
      super.render(graph);
      graph.setFont(this.font);
      double cx = this.pos.x;

      for(int i = 0; i != this.text.length(); ++i) {
         graph.setARGB(this.colors[i]);
         graph.drawChar(cx, this.pos.y, this.text, i);
         cx += (double)graph.charWidth(this.font, this.text.charAt(i));
      }

   }

   public double getWidth(Graphics graph) {
      return (double)graph.textWidth(this.font, this.text);
   }

   public double getHeight(Graphics graph) {
      return (double)this.font.size;
   }
}
