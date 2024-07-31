package com.kirbymimi.mmb.graphics;

import com.kirbymimi.mmb.ui.Component;

public interface GraphicsEvent {
   void invoke(Component var1, GraphicsEvent.EventData var2);

   public static class EventData {
      public GraphicsEvent.Events type;
      public GraphicsEvent.Types eventType;
      // $FF: synthetic field
      private static int[] $SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events;

      EventData(GraphicsEvent.Events type) {
         this.type = type;
         switch($SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events()[type.ordinal()]) {
         case 1:
         case 2:
         case 3:
         case 4:
         case 5:
         case 6:
            this.eventType = GraphicsEvent.Types.MOUSE;
            break;
         case 7:
         case 8:
         case 9:
            this.eventType = GraphicsEvent.Types.KEY;
            break;
         case 10:
            this.eventType = GraphicsEvent.Types.MOUSEWHEEL;
         }

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

   public static enum Events {
      MOUSEOVER,
      MOUSECLICK,
      MOUSEPRESS,
      MOUSERELEASE,
      MOUSEENTER,
      MOUSEEXIT,
      KEYTYPE,
      KEYPRESS,
      KEYRELEASE,
      MOUSEWHEEL;
   }

   public static class GraphicsKeyEvent extends GraphicsEvent.EventData {
      public int key;

      public GraphicsKeyEvent(GraphicsEvent.Events type, int key) {
         super(type);
         this.key = key;
      }
   }

   public static class GraphicsMouseEvent extends GraphicsEvent.EventData {
      public int x;
      public int y;
      public int button;

      public GraphicsMouseEvent(GraphicsEvent.Events type, int x, int y, int button) {
         super(type);
         this.x = x;
         this.y = y;
         this.button = button;
      }

      public GraphicsEvent.GraphicsMouseEvent clone() {
         return new GraphicsEvent.GraphicsMouseEvent(this.type, this.x, this.y, this.button);
      }
   }

   public static class GraphicsMouseWheelEvent extends GraphicsEvent.EventData {
      public int move;

      public GraphicsMouseWheelEvent(GraphicsEvent.Events type, int move) {
         super(type);
         this.move = move;
      }
   }

   public interface Key {
      void invoke(Component var1, GraphicsEvent.GraphicsKeyEvent var2);
   }

   public static class KeyBridge implements GraphicsEvent {
      GraphicsEvent.Key cb;

      public KeyBridge(GraphicsEvent.Key cb) {
         this.cb = cb;
      }

      public void invoke(Component parent, GraphicsEvent.EventData data) {
         if (data.eventType == GraphicsEvent.Types.KEY) {
            this.cb.invoke(parent, (GraphicsEvent.GraphicsKeyEvent)data);
         }
      }
   }

   public interface Mouse {
      void invoke(Component var1, GraphicsEvent.GraphicsMouseEvent var2);
   }

   public static class MouseBridge implements GraphicsEvent {
      GraphicsEvent.Mouse cb;

      public MouseBridge(GraphicsEvent.Mouse cb) {
         this.cb = cb;
      }

      public void invoke(Component parent, GraphicsEvent.EventData data) {
         if (data.eventType == GraphicsEvent.Types.MOUSE) {
            this.cb.invoke(parent, (GraphicsEvent.GraphicsMouseEvent)data);
         }
      }
   }

   public interface MouseWheel {
      void invoke(Component var1, GraphicsEvent.GraphicsMouseWheelEvent var2);
   }

   public static class MouseWheelBridge implements GraphicsEvent {
      GraphicsEvent.MouseWheel cb;

      public MouseWheelBridge(GraphicsEvent.MouseWheel cb) {
         this.cb = cb;
      }

      public void invoke(Component parent, GraphicsEvent.EventData data) {
         if (data.eventType == GraphicsEvent.Types.MOUSEWHEEL) {
            this.cb.invoke(parent, (GraphicsEvent.GraphicsMouseWheelEvent)data);
         }
      }
   }

   public static enum Types {
      MOUSE,
      KEY,
      MOUSEWHEEL;
   }
}
