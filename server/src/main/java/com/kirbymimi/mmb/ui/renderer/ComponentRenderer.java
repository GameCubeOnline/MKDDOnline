package com.kirbymimi.mmb.ui.renderer;

import com.kirbymimi.mmb.graphics.Graphics;
import com.kirbymimi.mmb.math.Point;
import com.kirbymimi.mmb.ui.Component;

public abstract class ComponentRenderer {
   protected Component comp;
   String name = "";
   ComponentRenderer.RendererPos posCalc;
   Point pos;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$kirbymimi$mmb$ui$renderer$ComponentRenderer$RendererPos;

   public ComponentRenderer() {
      this.posCalc = ComponentRenderer.RendererPos.MANUAL;
      this.pos = new Point();
   }

   public void setComponent(Component comp) {
      this.comp = comp;
   }

   public void render(Graphics graph) {
      switch($SWITCH_TABLE$com$kirbymimi$mmb$ui$renderer$ComponentRenderer$RendererPos()[this.posCalc.ordinal()]) {
      case 1:
         this.pos.updateExpressions(this);
         break;
      case 2:
         this.pos.y = this.comp.getRealHeight() / 2.0D - this.getHeight(graph) / 2.0D;
         this.pos.x = this.comp.getRealWidth() / 2.0D - this.getWidth(graph) / 2.0D;
      }

   }

   public Component getComponent() {
      return this.comp;
   }

   public String getName() {
      return this.name;
   }

   public double getWidth(Graphics graph) {
      return this.comp.getRealWidth();
   }

   public double getHeight(Graphics graph) {
      return this.comp.getRealHeight();
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$kirbymimi$mmb$ui$renderer$ComponentRenderer$RendererPos() {
      int[] var10000 = $SWITCH_TABLE$com$kirbymimi$mmb$ui$renderer$ComponentRenderer$RendererPos;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[ComponentRenderer.RendererPos.values().length];

         try {
            var0[ComponentRenderer.RendererPos.CENTER.ordinal()] = 2;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[ComponentRenderer.RendererPos.MANUAL.ordinal()] = 1;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$kirbymimi$mmb$ui$renderer$ComponentRenderer$RendererPos = var0;
         return var0;
      }
   }

   public static enum RendererPos {
      MANUAL,
      CENTER;
   }
}
