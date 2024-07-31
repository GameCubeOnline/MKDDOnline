package com.kirbymimi.mmb.graphics.javaGraphics;

import com.kirbymimi.mmb.graphics.FontData;
import com.kirbymimi.mmb.graphics.Graphics;
import com.kirbymimi.mmb.graphics.GraphicsEvent;
import com.kirbymimi.mmb.graphics.Matrix;
import com.kirbymimi.mmb.graphics.Matrix2D;
import com.kirbymimi.mmb.math.Point;
import com.kirbymimi.mmb.ut.ThreadUT;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.ImageObserver;
import java.io.ByteArrayInputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class JavaGraphics extends Graphics {
   Graphics2D graph;
   JavaGraphics.FrameClass pan = new JavaGraphics.FrameClass(this);
   JFrame frm = new JFrame();
   Timer runner;
   int currCursor;
   volatile boolean repainted;
   Matrix baseMatrix;

   public JavaGraphics(int updateRate, String name) {
      super(updateRate);
      this.frm.setName(name);
      JavaGraphics.EventListener events = new JavaGraphics.EventListener(this);
      this.pan.addMouseListener(events);
      this.pan.addKeyListener(events);
      this.pan.addMouseWheelListener(events);
      this.pan.setFocusable(true);
      this.pan.grabFocus();
      this.frm.add(this.pan);
      this.frm.setDefaultCloseOperation(3);
   }

   public Graphics createSame() {
      return new JavaGraphics(this.updateRate, this.frm.getName());
   }

   public Point getWindowSize() {
      Point ret = new Point();
      ret.x = (double)this.pan.getWidth();
      ret.y = (double)this.pan.getHeight();
      return ret;
   }

   public void start() {
      this.runner = ThreadUT.createSwingRunner(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            JavaGraphics.this.runWindow();
         }
      }, (long)(1000000000 / this.updateRate));
      this.runner.start();
      this.frm.setVisible(true);
   }

   public void deleteWindow() {
      super.deleteWindow();
      this.frm.setVisible(false);
      this.frm.dispose();
      this.runner.stop();
   }

   public void runWindow() {
      this.frm.repaint();
      this.baseMatrix = new Matrix2D(this.graph.getTransform());
      java.awt.Point mousePos = this.pan.getMousePosition();
      Point defaultSize = this.getDefaultSize();
      Point currentSize = this.getWindowSize();
      if (mousePos == null) {
         this.sendRemoveToControllers("mouse:position");
      } else {
         mousePos.x = (int)((double)mousePos.x * (defaultSize.x / currentSize.x));
         mousePos.y = (int)((double)mousePos.y * (defaultSize.y / currentSize.y));
         this.sendToControllers("mouse:position", new Point(mousePos.x, mousePos.y));
      }

      this.propagateEventMouse(GraphicsEvent.Events.MOUSEOVER, -1);
   }

   public void repaintWindow() {
      if (!this.repainted) {
         this.repainted = true;
         this.pan.repaint();
      }
   }

   public void setWindowPosition(int x, int y) {
      this.frm.setLocation(x, y);
   }

   public void setWindowSize(int x, int y) {
      super.setWindowSize(x, y);
      this.pan.setPreferredSize(new Dimension(x, y));
      this.frm.pack();
   }

   public void setCursor(int cursor) {
      if (this.currCursor != cursor) {
         this.currCursor = cursor;
         this.pan.setCursor(new Cursor(cursor));
      }
   }

   void propagateEventMouse(GraphicsEvent.Events type, int button) {
      java.awt.Point p = this.pan.getMousePosition();
      if (p != null) {
         ;
      }
   }

   void propagateEventKey(GraphicsEvent.Events type, int key) {
   }

   void propagateEventMouseWheel(GraphicsEvent.Events type, int move) {
   }

   public Matrix getBaseMatrix() {
      return this.baseMatrix;
   }

   public void setARGB(int color) {
      this.graph.setColor(new Color(color, true));
   }

   public void setTranslate(int x, int y, int z) {
      this.graph.translate(x, y);
   }

   public void setScale(int x, int y, int z) {
      this.graph.scale((double)x, (double)y);
   }

   public void setScissors(int x, int y, int w, int h) {
      this.graph.setClip(x, y, w, h);
   }

   public void transform(Matrix matrix) {
      if (matrix instanceof Matrix2D) {
         this.graph.transform(((Matrix2D)matrix).getAffineTransform());
      }

   }

   public void setTransform(Matrix matrix) {
      if (matrix instanceof Matrix2D) {
         this.graph.setTransform(((Matrix2D)matrix).getAffineTransform());
      }

   }

   public Matrix getCurrentMatrix() {
      return new Matrix2D(this.graph.getTransform());
   }

   public void setTip(String tip) {
      this.pan.setToolTipText(tip);
   }

   public void drawLine(int x0, int y0, int x1, int y1) {
      this.graph.drawLine(x0, y0, x1, y1);
   }

   public void drawRect(int x, int y, int w, int h) {
      this.graph.drawRect(x, y, w, h);
   }

   public void fillRect(int x, int y, int w, int h) {
      this.graph.fillRect(x, y, w, h);
   }

   public void fillCircle(int x, int y, int w, int h) {
      this.graph.fillOval(x, y, w, h);
   }

   public void loadFont(FontData font) {
      font.loaded = true;
      Font f = null;
      if (font.graphImplementation == null) {
         if (font.data != null) {
            try {
               f = Font.createFont(0, new ByteArrayInputStream(font.data));
            } catch (Exception var4) {
            }
         } else if (font.name != null) {
            f = new Font(font.name, font.style, font.size);
         } else {
            f = this.graph.getFont();
         }

         font.graphImplementation = f;
      } else {
         f = (Font)font.graphImplementation;
      }

   }

   public void setFont(FontData font) {
      if (!font.loaded) {
         this.loadFont(font);
      }

      this.setARGB(font.color);
      Font f = (Font)font.graphImplementation;
      this.graph.setFont(f);
   }

   public int textWidth(FontData font, String text) {
      Font f = (Font)font.graphImplementation;
      FontMetrics metrics = this.graph.getFontMetrics(f);
      char[] chars = text.toCharArray();
      return metrics.charsWidth(chars, 0, chars.length);
   }

   public int charWidth(FontData font, char c) {
      return this.graph.getFontMetrics((Font)font.graphImplementation).charWidth(c);
   }

   public void drawText(int x, int y, String str) {
      this.graph.drawString(str, x, y);
   }

   public void drawChar(int x, int y, String str, int charPos) {
      this.graph.drawChars(str.toCharArray(), charPos, 1, x, y);
   }

   public void drawImage(Image image, int x, int y) {
      this.graph.drawImage(image, x, y, (ImageObserver)null);
   }

   public void run() {
      Point defSize = this.getDefaultSize();
      Point winSize = this.getWindowSize();
      this.graph.scale(winSize.x / defSize.x, winSize.y / defSize.y);
   }

   static class EventListener implements MouseListener, KeyListener, MouseWheelListener {
      JavaGraphics graph;

      EventListener(JavaGraphics graph) {
         this.graph = graph;
      }

      public void keyTyped(KeyEvent e) {
         this.graph.propagateEventKey(GraphicsEvent.Events.KEYTYPE, e.getKeyChar());
      }

      public void keyPressed(KeyEvent e) {
         this.graph.sendToControllers("keyboard:" + e.getKeyCode());
         this.graph.propagateEventKey(GraphicsEvent.Events.KEYPRESS, e.getKeyCode());
      }

      public void keyReleased(KeyEvent e) {
         this.graph.sendRemoveToControllers("keyboard:" + e.getKeyCode());
         this.graph.propagateEventKey(GraphicsEvent.Events.KEYRELEASE, e.getKeyCode());
      }

      public void mouseClicked(MouseEvent e) {
         this.graph.sendToControllers("mouse:" + e.getButton());
         this.graph.propagateEventMouse(GraphicsEvent.Events.MOUSECLICK, e.getButton());
      }

      public void mousePressed(MouseEvent e) {
         this.graph.sendToControllers("mouse:" + e.getButton());
         this.graph.propagateEventMouse(GraphicsEvent.Events.MOUSEPRESS, e.getButton());
      }

      public void mouseReleased(MouseEvent e) {
         this.graph.sendRemoveToControllers("mouse:" + e.getButton());
         this.graph.propagateEventMouse(GraphicsEvent.Events.MOUSERELEASE, e.getButton());
      }

      public void mouseEntered(MouseEvent e) {
         this.graph.propagateEventMouse(GraphicsEvent.Events.MOUSEENTER, e.getButton());
      }

      public void mouseExited(MouseEvent e) {
         this.graph.propagateEventMouse(GraphicsEvent.Events.MOUSEEXIT, e.getButton());
      }

      public void mouseWheelMoved(MouseWheelEvent e) {
         this.graph.sendRemoveToControllers("mousewheel:" + e.getPreciseWheelRotation());
         this.graph.propagateEventMouseWheel(GraphicsEvent.Events.MOUSEWHEEL, e.getUnitsToScroll());
      }
   }

   static class FrameClass extends JPanel {
      JavaGraphics jgraph;
      private static final long serialVersionUID = 1L;

      FrameClass(JavaGraphics jgraph) {
         this.jgraph = jgraph;
      }

      public void paint(java.awt.Graphics graph) {
         this.jgraph.repainted = false;
         this.jgraph.graph = (Graphics2D)graph;
         super.paint(graph);
         this.jgraph.run();
         this.jgraph.render();
      }
   }
}
