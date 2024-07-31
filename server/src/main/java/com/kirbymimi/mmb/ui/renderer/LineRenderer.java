package com.kirbymimi.mmb.ui.renderer;

import com.kirbymimi.mmb.graphics.FontData;
import com.kirbymimi.mmb.graphics.Graphics;
import java.util.function.Consumer;

public class LineRenderer {
   boolean full;
   int totalWidth;
   int maxWidth;
   public Graphics graph;
   FontData font;
   int x;
   public int y;
   public int cx;
   Object[] elements = new Object[128];
   int currElement;
   int[] colors = new int[128];

   public LineRenderer(int maxWidth, Graphics graph, FontData font) {
      this.maxWidth = maxWidth;
      this.graph = graph;
      this.font = font;
   }

   public void reset(int x, int y) {
      this.x = x;
      this.y = y;
      this.totalWidth = x;
      this.currElement = 0;
      this.full = false;
   }

   public void addString(String str) {
      this.addString(str, -1);
   }

   public void addString(String str, int color) {
      if (!this.full) {
         this.elements[this.currElement] = str;
         this.colors[this.currElement] = color;
         this.totalWidth += this.graph.textWidth(this.font, str);
         this.addCmn();
      }
   }

   public void addFunction(Consumer<LineRenderer> function, int size) {
      this.elements[this.currElement] = function;
      this.totalWidth += size;
      this.addCmn();
   }

   public void setX(int align) {
      if (!this.full) {
         int i = align - this.totalWidth;
         if (i < 0) {
            i = 0;
         }

         this.elements[this.currElement] = i;
         this.totalWidth += i;
         this.addCmn();
      }
   }

   public void addSpace(int length) {
      if (!this.full) {
         this.elements[this.currElement] = length;
         this.totalWidth += length;
         this.addCmn();
      }
   }

   void addCmn() {
      if (this.maxWidth > this.totalWidth) {
         ++this.currElement;
      } else {
         int remWidth = this.graph.charWidth(this.font, '.') * 3 + 8;

         while(this.totalWidth > this.maxWidth - remWidth && this.currElement >= 0) {
            if (this.elements[this.currElement] instanceof String) {
               String s = (String)this.elements[this.currElement];
               this.totalWidth -= this.graph.charWidth(this.font, s.charAt(s.length() - 1));
               s = s.substring(0, s.length() - 1);
               if (s.length() == 0) {
                  --this.currElement;
               } else {
                  this.elements[this.currElement] = s;
               }
            } else {
               this.totalWidth -= (Integer)this.elements[this.currElement];
               --this.currElement;
            }
         }

         this.colors[this.currElement + 1] = -1;
         this.elements[this.currElement + 1] = "...";
         this.currElement += 2;
         this.full = true;
      }
   }

   public void draw() {
      this.cx = this.x;

      for(int i = 0; i != this.currElement; ++i) {
         if (this.elements[i] instanceof String) {
            this.graph.setARGB(this.colors[i]);
            this.graph.drawText(this.cx, this.y, (String)this.elements[i]);
            this.cx += this.graph.textWidth(this.font, (String)this.elements[i]);
         } else if (this.elements[i] instanceof Consumer) {
            ((Consumer)this.elements[i]).accept(this);
         } else {
            this.cx += (Integer)this.elements[i];
         }
      }

   }
}
