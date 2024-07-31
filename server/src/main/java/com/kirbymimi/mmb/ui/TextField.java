package com.kirbymimi.mmb.ui;

import com.kirbymimi.mmb.graphics.FontData;
import com.kirbymimi.mmb.graphics.Graphics;
import com.kirbymimi.mmb.graphics.GraphicsEvent;
import com.kirbymimi.mmb.math.Point;
import com.kirbymimi.mmb.ut.BitUT;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;

public class TextField extends Component implements GraphicsEvent {
   TextField.InputFilter filter;
   int bgcolor;
   FontData font;
   public String desc;
   volatile int indicatorTimer;
   String[] onChange;
   boolean ctrlheld;
   char[] chars;
   int charPos;
   int charPosCheck;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$kirbymimi$mmb$ui$TextField$InputFilter;

   public TextField(Component parent) {
      super(parent);
      this.filter = TextField.InputFilter.ANY;
      this.desc = "";
      this.chars = new char[64];
   }

   public void render(Graphics graph) {
      super.render(graph);
      graph.setARGB(this.bgcolor);
      Point dim = this.getRealDimension();
      graph.fillRect(0.0D, 0.0D, dim.x, dim.y);
      graph.setFont(this.font);
      String s = this.makeString();
      graph.drawText(4.0D, (double)this.font.size + this.getRealHeight() / 2.0D - 8.0D, this.desc);
      int tw = graph.textWidth(this.font, this.desc);
      graph.drawText((double)(4 + tw), (double)this.font.size + this.getRealHeight() / 2.0D - 8.0D, s);
      tw += graph.textWidth(this.font, s.substring(0, this.charPos));
      if (this.focused) {
         if (this.indicatorTimer < graph.getUpdateRate()) {
            graph.drawLine((double)(tw + 5), 2.0D, (double)(tw + 5), this.getRealHeight() - 2.0D);
         }
      }
   }

   public void loseFocus() {
      super.loseFocus();
      this.reRender();
   }

   public void grabFocus() {
      this.indicatorTimer = 0;
      super.grabFocus();
      this.reRender();
   }

   public void run() {
      if (this.charPos != this.charPosCheck) {
         this.indicatorTimer = 0;
         this.charPosCheck = this.charPos;
         this.reRender();
      }

      if (this.focused) {
         ++this.indicatorTimer;
         if (this.indicatorTimer == this.graph.getUpdateRate()) {
            this.reRender();
         }

         this.indicatorTimer %= this.graph.getUpdateRate() * 2;
         if (this.indicatorTimer == 0) {
            this.reRender();
         }

      }
   }

   public void invoke(Component parent, GraphicsEvent.EventData data) {
      GraphicsEvent.GraphicsKeyEvent key;
      int var10;
      switch($SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events()[data.type.ordinal()]) {
      case 1:
         this.addTask(() -> {
            this.setCursor(2);
         });
      case 2:
      case 4:
      case 5:
      case 6:
      default:
         break;
      case 3:
         GraphicsEvent.GraphicsMouseEvent mouse = (GraphicsEvent.GraphicsMouseEvent)data;
         int descWidth = this.graph.textWidth(this.font, this.desc);
         int x = mouse.x - descWidth - 4;
         if (x < 0) {
            x = 0;
         }

         int testX = 0;
         int i = 0;
         char[] var12;
         int var22 = (var12 = this.makeString().toCharArray()).length;

         for(var10 = 0; var10 < var22; ++var10) {
            char c = var12[var10];
            int w = this.graph.charWidth(this.font, c);
            if (x <= testX + w / 2) {
               break;
            }

            ++i;
            testX += w;
         }

         this.charPos = i;
         break;
      case 7:
         if (!parent.focused) {
            return;
         }

         this.indicatorTimer = 0;
         key = (GraphicsEvent.GraphicsKeyEvent)data;
         if (key.key != 127 && key.key != 8) {
            char c = (char)key.key;
            if (!this.insertAndTest(c)) {
               return;
            }

            return;
         }

         this.remove();
         return;
      case 8:
         key = (GraphicsEvent.GraphicsKeyEvent)data;
         if (!parent.focused) {
            return;
         }

         if (key.key == 17) {
            this.ctrlheld = true;
         }

         if (key.key == 37 && this.charPos != 0) {
            --this.charPos;
         }

         if (key.key == 39) {
            if (this.charPos != this.makeString().length()) {
               ++this.charPos;
            }
         } else if (this.ctrlheld) {
            Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
            switch(key.key) {
            case 67:
               StringSelection selection = new StringSelection(this.makeString());
               clip.setContents(selection, selection);
               break;
            case 86:
               try {
                  String s = (String)clip.getData(DataFlavor.stringFlavor);
                  TextField.TextFieldSave save = new TextField.TextFieldSave(this);
                  char[] var11;
                  var10 = (var11 = s.toCharArray()).length;

                  for(int var9 = 0; var9 < var10; ++var9) {
                     char c = var11[var9];
                     if (!this.insertAndTest(c)) {
                        save.load(this);
                     }
                  }

                  this.reRender();
                  return;
               } catch (Exception var14) {
               }
            }

            return;
         }

         return;
      case 9:
         key = (GraphicsEvent.GraphicsKeyEvent)data;
         if (key.key == 17) {
            this.ctrlheld = false;
         }
      }

   }

   boolean insertAndTest(char c) {
      this.insert(c);
      String test = this.makeString();
      boolean ret = this.passFilter(new String(test), c);
      if (ret) {
         return true;
      } else {
         this.remove();
         return false;
      }
   }

   boolean passFilter(String s, char c) {
      switch($SWITCH_TABLE$com$kirbymimi$mmb$ui$TextField$InputFilter()[this.filter.ordinal()]) {
      case 1:
         return true;
      default:
         switch(c) {
         case '#':
            if (this.filter == TextField.InputFilter.FLOAT) {
               return false;
            } else if (this.filterHexTest() > 1) {
               return false;
            } else {
               if (this.charPos == (this.chars[0] == '-' ? 2 : 1)) {
                  return true;
               }

               return false;
            }
         case ',':
         case '.':
            return this.charPos != 1 && (this.contains(',') || this.contains('.'));
         case '-':
            if ((this.filter != TextField.InputFilter.UINT || this.filter != TextField.InputFilter.ULONG) && this.filter != TextField.InputFilter.UQUAD) {
               if (this.chars[1] == '-') {
                  return false;
               }

               if (this.charPos == 1) {
                  return true;
               }

               return false;
            }

            return false;
         case 'X':
         case 'x':
            if (this.filter == TextField.InputFilter.FLOAT) {
               return false;
            } else if (this.filterHexTest() > 1) {
               return false;
            } else {
               if (this.charPos == (this.chars[0] == '-' ? 3 : 2)) {
                  return true;
               }

               return false;
            }
         default:
            switch($SWITCH_TABLE$com$kirbymimi$mmb$ui$TextField$InputFilter()[this.filter.ordinal()]) {
            case 2:
            case 5:
               try {
                  Integer.decode(s);
                  return true;
               } catch (Exception var4) {
                  return false;
               }
            case 3:
            case 6:
               return BitUT.longCheck(s);
            case 4:
            case 7:
               return BitUT.quadCheck(s);
            default:
               return false;
            }
         }
      }
   }

   int filterHexTest() {
      int cnt = 0;
      char[] var5;
      int var4 = (var5 = this.chars).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         char c2 = var5[var3];
         if (c2 == 0) {
            break;
         }

         if (c2 == 'x' || c2 == 'X' || c2 == '#') {
            ++cnt;
         }
      }

      return cnt;
   }

   boolean contains(char c) {
      for(int i = 0; i != this.charPos; ++i) {
         if (this.chars[i] == c) {
            return true;
         }
      }

      return false;
   }

   char insert(char c) {
      int len = this.length();
      if (len == this.chars.length) {
         return '\u0000';
      } else {
         System.arraycopy(this.chars, this.charPos, this.chars, this.charPos + 1, len - this.charPos);
         char old = this.chars[this.charPos];
         this.chars[this.charPos++] = c;
         return old;
      }
   }

   void remove() {
      if (this.charPos != 0) {
         int len = this.length();
         this.chars[--this.charPos] = 0;
         System.arraycopy(this.chars, this.charPos + 1, this.chars, this.charPos, len - this.charPos - 1);
         this.chars[len - 1] = 0;
      }
   }

   void replace(char c) {
      if (this.charPos != 0) {
         --this.charPos;
         this.chars[this.charPos] = c;
      }
   }

   public int length() {
      int len;
      for(len = 0; len != this.chars.length && this.chars[len] != 0; ++len) {
      }

      return len;
   }

   public String makeString() {
      return new String(this.chars, 0, this.length());
   }

   public String makeStringLow() {
      return this.makeString().toLowerCase();
   }

   public void setString(String str) {
      char[] c = str.toCharArray();

      for(int i = 0; i != c.length; ++i) {
         this.chars[i] = c[i];
      }

      this.charPos = c.length;
      Arrays.fill(this.chars, this.charPos, this.chars.length, '\u0000');
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

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$kirbymimi$mmb$ui$TextField$InputFilter() {
      int[] var10000 = $SWITCH_TABLE$com$kirbymimi$mmb$ui$TextField$InputFilter;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[TextField.InputFilter.values().length];

         try {
            var0[TextField.InputFilter.ANY.ordinal()] = 1;
         } catch (NoSuchFieldError var8) {
         }

         try {
            var0[TextField.InputFilter.FLOAT.ordinal()] = 8;
         } catch (NoSuchFieldError var7) {
         }

         try {
            var0[TextField.InputFilter.INT.ordinal()] = 2;
         } catch (NoSuchFieldError var6) {
         }

         try {
            var0[TextField.InputFilter.LONG.ordinal()] = 3;
         } catch (NoSuchFieldError var5) {
         }

         try {
            var0[TextField.InputFilter.QUAD.ordinal()] = 4;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[TextField.InputFilter.UINT.ordinal()] = 5;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[TextField.InputFilter.ULONG.ordinal()] = 6;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[TextField.InputFilter.UQUAD.ordinal()] = 7;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$kirbymimi$mmb$ui$TextField$InputFilter = var0;
         return var0;
      }
   }

   public static enum InputFilter {
      ANY,
      INT,
      LONG,
      QUAD,
      UINT,
      ULONG,
      UQUAD,
      FLOAT;
   }

   static class TextFieldSave {
      char[] chars = new char[64];
      int charPos;

      TextFieldSave(TextField tf) {
         System.arraycopy(tf.chars, 0, this.chars, 0, this.chars.length);
         this.charPos = tf.charPos;
      }

      void load(TextField tf) {
         System.arraycopy(this.chars, 0, tf.chars, 0, this.chars.length);
         tf.charPos = this.charPos;
      }
   }
}
