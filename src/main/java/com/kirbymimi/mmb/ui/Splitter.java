package com.kirbymimi.mmb.ui;

import com.kirbymimi.mmb.graphics.Graphics;
import com.kirbymimi.mmb.graphics.GraphicsEvent;

public class Splitter extends Component implements GraphicsEvent.Mouse {
   Component.Expand direction;
   boolean pressed;
   int color = -1;
   public double ratio;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events;

   public Splitter(Component parent) {
      super(parent);
      this.addEventListener(this);
      this.phantom = true;
      this.priority = 1;
   }

   public void render(Graphics graph) {
      super.render(graph);
      if (this.direction == Component.Expand.WIDTH) {
         this.position.y = 0.0D;
         this.realDimension.y = this.parent.getRealHeight();
      } else {
         this.position.x = 0.0D;
         this.realDimension.x = this.parent.getRealWidth();
      }

   }

   public void run() {
      if (this.root.mouseHeld && this.pressed) {
         this.setCursor(10);
         this.setPos((double)(this.direction == Component.Expand.WIDTH ? this.root.mouseX : this.root.mouseY));
         this.reRender();
      } else {
         this.pressed = false;
      }
   }

   public void reLayout() {
      super.reLayout();
      this.setPos(this.direction == Component.Expand.WIDTH ? this.position.x + this.realDimension.x / 2.0D : this.position.y + this.realDimension.y / 2.0D);
   }

   public void setPos(double p) {
      double x;
      double w;
      if (this.direction == Component.Expand.WIDTH) {
         x = p - this.realDimension.x / 2.0D;
         if (x < 0.0D) {
            x = 0.0D;
         } else {
            w = this.parent.getRealWidth() - this.realDimension.x;
            if (x > w) {
               x = w;
            }
         }

         this.position.x = x;
         this.ratio = (x + this.realDimension.x / 2.0D) / this.parent.getRealWidth();
      } else {
         x = p - this.realDimension.y / 2.0D;
         if (x < 0.0D) {
            x = 0.0D;
         } else {
            w = this.parent.getRealHeight() - this.realDimension.y;
            if (x > w) {
               x = w;
            }
         }

         this.position.y = x;
         this.ratio = (x + this.realDimension.y / 2.0D) / this.parent.getRealHeight();
      }

   }

   public void invoke(Component src, GraphicsEvent.GraphicsMouseEvent data) {
      switch($SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events()[data.type.ordinal()]) {
      case 1:
         this.setCursor(10);
      case 2:
      default:
         break;
      case 3:
         this.pressed = true;
      }

   }

   public double getRatio() {
      return this.direction == Component.Expand.WIDTH ? (this.position.x + this.realDimension.x / 2.0D) / this.parent.getRealWidth() : (this.position.y + this.realDimension.y / 2.0D) / this.parent.getRealHeight();
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events() {
      int[] var10000 = $SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[GraphicsEvent.Events.values().length];

         try {
            var0[GraphicsEvent.Events.KEYPRESS.ordinal()] = 8;
         } catch (NoSuchFieldError var10) {
         }

         try {
            var0[GraphicsEvent.Events.KEYRELEASE.ordinal()] = 9;
         } catch (NoSuchFieldError var9) {
         }

         try {
            var0[GraphicsEvent.Events.KEYTYPE.ordinal()] = 7;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[GraphicsEvent.Events.MOUSECLICK.ordinal()] = 2;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[GraphicsEvent.Events.MOUSEENTER.ordinal()] = 5;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[GraphicsEvent.Events.MOUSEEXIT.ordinal()] = 6;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[GraphicsEvent.Events.MOUSEOVER.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[GraphicsEvent.Events.MOUSEPRESS.ordinal()] = 3;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[GraphicsEvent.Events.MOUSERELEASE.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[GraphicsEvent.Events.MOUSEWHEEL.ordinal()] = 10;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events = var0;
         return var0;
      }
   }
}
