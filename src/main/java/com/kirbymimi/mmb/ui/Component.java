package com.kirbymimi.mmb.ui;

import com.kirbymimi.mmb.actor.Actor;
import com.kirbymimi.mmb.graphics.Graphics;
import com.kirbymimi.mmb.graphics.GraphicsEvent;
import com.kirbymimi.mmb.math.DMath;
import com.kirbymimi.mmb.math.Point;
import com.kirbymimi.mmb.ui.renderer.ComponentRenderer;
import com.kirbymimi.mmb.ut.ArrayUT;
import com.kirbymimi.mmb.ut.Controller;
import com.kirbymimi.mmb.ut.FieldUT;
import com.kirbymimi.mmb.ut.IntPtr;
import com.kirbymimi.mmb.ut.IteratorWrap;
import com.kirbymimi.mmb.ut.Observer;
import com.kirbymimi.mmb.ut.ktml.KTML2Obj;
import com.kirbymimi.mmb.ut.ktml.KTMLDecoder;
import com.kirbymimi.mmb.ut.ktml.KTMLEntry;
import com.kirbymimi.mmb.ut.list.SafeList;
import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

public class Component extends Actor {
   ReentrantLock lock;
   protected Graphics graph;
   String name;
   Component parent;
   protected Component root;
   int priority;
   Component.DimensionMode dimensionMode;
   Component.StartPosition layoutStart;
   Component.Expand layoutMove;
   Component.Expand layoutExpand;
   SafeList<ComponentRenderer> renderers;
   SafeList<GraphicsEvent> eventListeners;
   SafeList<Component.TaskInterface> tasks;
   Point position;
   Point targetDimension;
   Point realDimension;
   public boolean focused;
   boolean phantom;
   Component focusedComp;
   Observer observer;
   String[] onGrabFocus;
   String[] onLoseFocus;
   boolean mouseHeld;
   int mouseX;
   int mouseY;
   int mouseButton;
   int nextCursor;
   Controller controller;
   static final String[] commonClsSearch = new String[]{"com.kirbymimi.mmb.ui.renderer", "com.kirbymimi.mmb.ui"};
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$DimensionMode;
   // $FF: synthetic field
   private static int[] $SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$StartPosition;

   public Component(String ktml, Graphics graph) {
      this(ktml, new String[0], graph);
   }

   public Component(String ktml, String[] clsSearch, Graphics graph) {
      this.lock = new ReentrantLock();
      this.name = "";
      this.priority = 0;
      this.dimensionMode = Component.DimensionMode.MANUAL;
      this.layoutStart = Component.StartPosition.TOPLEFT;
      this.layoutMove = Component.Expand.WIDTH;
      this.layoutExpand = Component.Expand.WIDTH;
      this.renderers = new SafeList();
      this.eventListeners = new SafeList();
      this.tasks = new SafeList();
      this.position = new Point();
      this.targetDimension = new Point();
      this.realDimension = new Point();
      this.observer = new Observer();
      this.graph = graph;
      KTMLEntry gui = null;
      gui = KTMLDecoder.decode(new File(ktml));
      KTML2Obj loader = new KTML2Obj(this);
      clsSearch = clsSearch.length == 0 ? commonClsSearch : (String[])ArrayUT.merge(commonClsSearch, clsSearch);
      loader.setClassSearch(clsSearch);
      loader.rootLoad(gui);
      this.controller = new Controller();
      graph.addController(this.controller);
   }

   public Component() {
      this.lock = new ReentrantLock();
      this.name = "";
      this.priority = 0;
      this.dimensionMode = Component.DimensionMode.MANUAL;
      this.layoutStart = Component.StartPosition.TOPLEFT;
      this.layoutMove = Component.Expand.WIDTH;
      this.layoutExpand = Component.Expand.WIDTH;
      this.renderers = new SafeList();
      this.eventListeners = new SafeList();
      this.tasks = new SafeList();
      this.position = new Point();
      this.targetDimension = new Point();
      this.realDimension = new Point();
      this.observer = new Observer();
   }

   public Component(Component parent) {
      this.lock = new ReentrantLock();
      this.name = "";
      this.priority = 0;
      this.dimensionMode = Component.DimensionMode.MANUAL;
      this.layoutStart = Component.StartPosition.TOPLEFT;
      this.layoutMove = Component.Expand.WIDTH;
      this.layoutExpand = Component.Expand.WIDTH;
      this.renderers = new SafeList();
      this.eventListeners = new SafeList();
      this.tasks = new SafeList();
      this.position = new Point();
      this.targetDimension = new Point();
      this.realDimension = new Point();
      this.observer = new Observer();

      for(this.parent = parent; parent.parent != null; parent = parent.parent) {
      }

      this.root = parent;
      this.graph = this.root.graph;
   }

   public void addEvent(KTMLEntry ktml) {
      this.getObserver().register(this, (Method)FieldUT.getMethod(this.getClass(), ktml.getAsString(), Observer.Event.class), (String)ktml.getType());
   }

   public void parentAddEvent(KTMLEntry ktml) {
      this.getObserver().register(this.parent, (Method)FieldUT.getMethod(this.parent.getClass(), ktml.getAsString(), Observer.Event.class), (String)ktml.getType());
   }

   public void loadDone() {
      this.position.updateExpressions(this);
   }

   public String toString() {
      return this.name;
   }

   public Point getPosition() {
      return this.position.clone();
   }

   public Point getTargetDimension() {
      return this.targetDimension.clone();
   }

   public Point getRealDimension() {
      return this.realDimension.clone();
   }

   public double getRealWidth() {
      return this.realDimension.x;
   }

   public double getRealHeight() {
      return this.realDimension.y;
   }

   public int getMouseX() {
      return this.mouseX;
   }

   public int getMouseY() {
      return this.mouseY;
   }

   public int getMouseButton() {
      return this.mouseButton;
   }

   public void setPosition(int x, int y) {
      this.position.set(x, y);
   }

   public void setPosition(Point p) {
      this.position.set(p);
   }

   public void setTargetDimension(int w, int h) {
      this.targetDimension.set(w, h);
   }

   public void setDimensionMode(Component.DimensionMode dimensionMode) {
      this.dimensionMode = dimensionMode;
   }

   public void setlayoutStart(Component.StartPosition start) {
      this.layoutStart = start;
   }

   public void setlayoutMove(Component.Expand move) {
      this.layoutMove = move;
   }

   public void setlayoutExpand(Component.Expand expand) {
      this.layoutExpand = expand;
   }

   public void add(Component c) {
      this.lock.lock();
      int cidx = -1;
      Iterator var4 = this.children().iterator();

      while(var4.hasNext()) {
         Component c2 = (Component)var4.next();
         ++cidx;
         if (c2.priority > c.priority) {
            this.children.add(cidx, c);
            cidx = -1;
            break;
         }
      }

      if (cidx != -1) {
         this.children.add(c);
      }

      c.parent = this;
      this.lock.unlock();
   }

   public void delete(Component c) {
      this.lock.lock();
      c.parent = null;
      c.root = null;
      this.children.remove(c);
      this.lock.unlock();
   }

   public void extricate() {
      if (this.root != null) {
         this.root.reRender();
         if (this.root.focusedComp != null) {
            this.root.focusedComp.loseFocus();
         }

         this.parent.delete(this);
         Iterator var2 = this.children().iterator();

         Component c;
         while(var2.hasNext()) {
            c = (Component)var2.next();
            c.setRoot(this);
         }

         this.graph = this.graph.createSame();
         var2 = this.children().iterator();

         while(var2.hasNext()) {
            c = (Component)var2.next();
            c.setGraph(this.graph);
         }

         this.graph.createView().setTarget(this);
         this.graph.setWindowPosition((int)this.position.x, (int)this.position.y);
         this.position.set(0, 0);
         this.graph.start();
      }
   }

   public void reinvolve(Component comp) {
      if (this.root == null) {
         if (this.focusedComp != null) {
            this.focusedComp.loseFocus();
         }

         comp.add(this);
         Iterator var3 = this.children().iterator();

         Component c;
         while(var3.hasNext()) {
            c = (Component)var3.next();
            c.setRoot(comp.getRoot());
         }

         var3 = this.children().iterator();

         while(var3.hasNext()) {
            c = (Component)var3.next();
            c.setGraph(comp.graph);
         }

         this.root = comp;
         this.graph.deleteWindow();
         this.graph = comp.graph;
         this.root.reRender();
      }
   }

   public Component getRoot() {
      return this.root == null ? this : this.root;
   }

   public boolean isRoot() {
      return this.root == null;
   }

   void setRoot(Component root) {
      this.root = root;
      Iterator var3 = this.children().iterator();

      while(var3.hasNext()) {
         Component c = (Component)var3.next();
         c.setRoot(root);
      }

   }

   void setGraph(Graphics graph) {
      this.graph = graph;
      Iterator var3 = this.children().iterator();

      while(var3.hasNext()) {
         Component c = (Component)var3.next();
         c.setGraph(graph);
      }

   }

   public Graphics getGraphics() {
      return this.graph;
   }

   public Component search(String name) {
      if (this.name.compareTo(name) == 0) {
         return this;
      } else {
         Iterator var4 = this.children().iterator();

         while(var4.hasNext()) {
            Component c = (Component)var4.next();
            Component result = c.search(name);
            if (result != null) {
               return result;
            }
         }

         return null;
      }
   }

   public Component getBrother(String name) {
      if (this.parent == null) {
         return null;
      } else {
         Iterator var3 = this.parent.children().iterator();

         while(var3.hasNext()) {
            Component c = (Component)var3.next();
            if (c.name.compareTo(name) == 0) {
               return c;
            }
         }

         return null;
      }
   }

   public ComponentRenderer searchRenderer(String name) {
      Iterator var3 = this.renderers.iterator();

      while(var3.hasNext()) {
         ComponentRenderer r = (ComponentRenderer)var3.next();
         if (r.getName().compareTo(name) == 0) {
            return r;
         }
      }

      return null;
   }

   public Observer getObserver() {
      return this.observer;
   }

   public void addTask(Component.TaskInterface t) {
      this.lock.lock();
      this.getRoot().tasks.add(t);
      this.lock.unlock();
   }

   void execTasks() {
      while(!this.tasks.isEmpty()) {
         this.lock.lock();
         Object[] arr = this.tasks.toArray();
         this.tasks.clear();
         this.lock.unlock();
         Object[] var5 = arr;
         int var4 = arr.length;

         for(int var3 = 0; var3 < var4; ++var3) {
            Object t = var5[var3];
            ((Component.TaskInterface)t).exec();
         }
      }

      this.graph.setCursor(this.nextCursor);
      this.nextCursor = 0;
   }

   public void setCursor(int cursor) {
      this.root.nextCursor = cursor;
   }

   public void childRender(Graphics graph) {
      graph.setScissors((int)this.getRealWidth(), (int)this.getRealHeight());
      Iterator var3 = this.renderers.iterator();

      while(var3.hasNext()) {
         ComponentRenderer compRender = (ComponentRenderer)var3.next();
         compRender.setComponent(this);
         compRender.render(graph);
      }

      var3 = this.children().iterator();

      while(var3.hasNext()) {
         Component c = (Component)var3.next();
         graph.pushTranslate(c.position);
         c.childRender(graph);
         c.render(graph);
         graph.popTranslate();
         graph.setScissors((int)this.getRealWidth(), (int)this.getRealHeight());
      }

   }

   public void reRender() {
      Iterator var2 = this.children().iterator();

      while(var2.hasNext()) {
         Component c = (Component)var2.next();
         c.reRender();
      }

      this.graph.repaintWindow();
   }

   public void run() {
      this.controller.update(this.getDelta());
      Iterator var2 = this.children().iterator();

      while(var2.hasNext()) {
         Component c = (Component)var2.next();
         c.run();
      }

      if (this.root == null) {
         this.execTasks();
      }

   }

   public Component childAt(int idx) {
      return (Component)this.children.get(idx);
   }

   public void propagateEvent(GraphicsEvent.EventData data) {
      Iterator var3 = this.children().iterator();

      while(var3.hasNext()) {
         Component c = (Component)var3.next();
         GraphicsEvent.EventData next = data;
         if (data instanceof GraphicsEvent.GraphicsMouseEvent) {
            GraphicsEvent.GraphicsMouseEvent dataMouse = (GraphicsEvent.GraphicsMouseEvent)data;
            GraphicsEvent.GraphicsMouseEvent nextMouse = null;
            if (!(c.getPosition().x > (double)dataMouse.x) && !(c.getPosition().y > (double)dataMouse.y) && !(c.position.x + c.realDimension.x < (double)dataMouse.x) && !(c.getPosition().y + c.realDimension.y < (double)dataMouse.y)) {
               nextMouse = dataMouse.clone();
               nextMouse.x = (int)((double)dataMouse.x - c.position.x);
               nextMouse.y = (int)((double)dataMouse.y - c.position.y);
               c.mouseX = nextMouse.x;
               c.mouseY = nextMouse.y;
               c.mouseButton = nextMouse.button;
               switch($SWITCH_TABLE$com$kirbymimi$mmb$graphics$GraphicsEvent$Events()[dataMouse.type.ordinal()]) {
               case 1:
               case 2:
               case 5:
               case 6:
               default:
                  break;
               case 3:
                  c.mouseHeld = true;
                  c.grabFocus();
                  break;
               case 4:
                  c.mouseHeld = false;
               }

               next = nextMouse;
            } else {
               next = null;
            }
         }

         if (next != null) {
            c.propagateEvent((GraphicsEvent.EventData)next);
         }
      }

      synchronized(this.eventListeners) {
         Iterator var9 = this.eventListeners.iterator();

         while(true) {
            if (!var9.hasNext()) {
               break;
            }

            GraphicsEvent e = (GraphicsEvent)var9.next();
            e.invoke(this, data);
         }
      }

      if (this instanceof GraphicsEvent) {
         ((GraphicsEvent)this).invoke(this, data);
      }

   }

   public void addEventListener(GraphicsEvent listener) {
      synchronized(this.eventListeners) {
         this.eventListeners.add(listener);
      }
   }

   public void addEventListener(GraphicsEvent.Mouse listener) {
      synchronized(this.eventListeners) {
         this.eventListeners.add(new GraphicsEvent.MouseBridge(listener));
      }
   }

   public void addEventListener(GraphicsEvent.Key listener) {
      synchronized(this.eventListeners) {
         this.eventListeners.add(new GraphicsEvent.KeyBridge(listener));
      }
   }

   public void addEventListener(GraphicsEvent.MouseWheel listener) {
      synchronized(this.eventListeners) {
         this.eventListeners.add(new GraphicsEvent.MouseWheelBridge(listener));
      }
   }

   public boolean intersect(Component op) {
      if (!this.phantom && !op.phantom) {
         return !(this.position.x + this.realDimension.x <= op.position.x) && !(op.position.x + op.realDimension.x <= this.position.x) && !(this.position.y + this.realDimension.y <= op.position.y) && !(op.position.y + op.realDimension.y <= this.position.y);
      } else {
         return false;
      }
   }

   public void grabFocus() {
      this.observer.sendEvent((String)"onGrabFocus", this);
      if (this.root == null) {
         this.focused = true;
      } else {
         if (this.root.focusedComp != null) {
            this.root.focusedComp.loseFocus();
         }

         this.root.focusedComp = this;
         this.focused = true;
      }
   }

   public void loseFocus() {
      this.focused = false;
      this.getRoot().focusedComp = null;
      this.observer.sendEvent((String)"onLoseFocus", this);
   }

   public void reLayout() {
      if (this.parent != null) {
         switch($SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$DimensionMode()[this.dimensionMode.ordinal()]) {
         case 1:
         default:
            break;
         case 2:
            this.targetDimension.set(this.parent.targetDimension);
            break;
         case 3:
            this.targetDimension.set(this.parent.targetDimension.x, this.targetDimension.y);
            break;
         case 4:
            this.targetDimension.set(this.targetDimension.x, this.parent.targetDimension.y);
         }
      }

      this.targetDimension.updateExpressions(this);
      this.realDimension.set(this.targetDimension);
      Iterator var2 = this.children().iterator();

      Component c;
      while(var2.hasNext()) {
         c = (Component)var2.next();
         c.reLayout();
      }

      label29:
      while(true) {
         var2 = this.children().iterator();

         while(var2.hasNext()) {
            c = (Component)var2.next();
            if (!this.reLayout(c)) {
               continue label29;
            }
         }

         return;
      }
   }

   boolean reLayout(Component c) {
      if (c.phantom) {
         return true;
      } else {
         if (c.realDimension.x > this.realDimension.x) {
            this.realDimension.x = c.realDimension.x;
         }

         if (c.realDimension.y > this.realDimension.y) {
            this.realDimension.y = c.realDimension.y;
         }

         IntPtr moveNext = new IntPtr(0);
         Point resetPos = new Point();
         this.clearResetPos(c, resetPos);

         label46:
         while(true) {
            c.position.set(resetPos);
            moveNext.v = Integer.MAX_VALUE;

            label44:
            while(true) {
               Iterator var5 = this.children().iterator();

               while(var5.hasNext()) {
                  Component c2 = (Component)var5.next();
                  if (c == c2) {
                     break;
                  }

                  switch(this.setLayout(c, c2, moveNext, resetPos)) {
                  case -3:
                     continue label44;
                  case -2:
                     return false;
                  case -1:
                     continue label46;
                  }
               }

               return true;
            }
         }
      }
   }

   public int setLayout(Component c, Component c2, IntPtr moveNext, Point resetPos) {
      if (!c.intersect(c2)) {
         return 0;
      } else {
         Point var10000;
         if (c.layoutMove == Component.Expand.WIDTH) {
            moveNext.v = (int)DMath.selSmall(c2.realDimension.y - (c.position.y - c2.position.y), (double)moveNext.v);
            switch($SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$StartPosition()[c.layoutStart.ordinal()]) {
            case 1:
            case 3:
               var10000 = c.position;
               var10000.x += c2.realDimension.x;
               if (c.position.x + c.realDimension.x > this.realDimension.x) {
                  if (this.layoutExpand == Component.Expand.HEIGHT) {
                     if (c.layoutStart == Component.StartPosition.TOPLEFT) {
                        resetPos.y += (double)moveNext.v;
                     } else {
                        resetPos.y -= (double)moveNext.v;
                     }

                     if (!(resetPos.y < 0.0D) && !(resetPos.y + c.realDimension.y > this.realDimension.y)) {
                        return -1;
                     }

                     var10000 = this.realDimension;
                     var10000.y += c.realDimension.y;
                     this.clearResetPos(c, resetPos);
                     return -2;
                  }

                  var10000 = this.realDimension;
                  var10000.x += c.position.x + c.realDimension.x - this.realDimension.x;
                  return -1;
               }

               return -3;
            case 2:
            case 4:
               c.position.x = c2.position.x - c.realDimension.x;
               if (c.position.x < 0.0D) {
                  if (this.layoutExpand == Component.Expand.HEIGHT) {
                     if (c.layoutStart == Component.StartPosition.TOPRIGHT) {
                        resetPos.y += (double)moveNext.v;
                     } else {
                        resetPos.y -= (double)moveNext.v;
                     }

                     if (!(resetPos.y < 0.0D) && !(resetPos.y + c.realDimension.y > this.realDimension.y)) {
                        return -1;
                     }

                     var10000 = this.realDimension;
                     var10000.y += c.realDimension.y;
                     this.clearResetPos(c, resetPos);
                     return -2;
                  }

                  var10000 = this.realDimension;
                  var10000.x -= c.position.x;
                  return -2;
               }

               return -3;
            }
         } else {
            moveNext.v = (int)DMath.selSmall(c2.realDimension.x - (c.position.x - c2.position.x), (double)moveNext.v);
            switch($SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$StartPosition()[c.layoutStart.ordinal()]) {
            case 1:
            case 2:
               var10000 = c.position;
               var10000.y += c2.realDimension.y;
               if (c.position.y + c.realDimension.y > this.realDimension.y) {
                  if (this.layoutExpand == Component.Expand.WIDTH) {
                     if (c.layoutStart == Component.StartPosition.TOPLEFT) {
                        resetPos.x += (double)moveNext.v;
                     } else {
                        resetPos.x -= (double)moveNext.v;
                     }

                     if (!(resetPos.x < 0.0D) && !(resetPos.x + c.realDimension.x > this.realDimension.x)) {
                        return -1;
                     }

                     var10000 = this.realDimension;
                     var10000.x += c.realDimension.x;
                     this.clearResetPos(c, resetPos);
                     return -2;
                  }

                  var10000 = this.realDimension;
                  var10000.y += c.position.y + c.realDimension.y - this.realDimension.y;
                  return -1;
               }

               return -3;
            case 3:
            case 4:
               c.position.y = c2.position.y - c.realDimension.y;
               if (c.position.y < 0.0D) {
                  if (this.layoutExpand == Component.Expand.WIDTH) {
                     if (c.layoutStart == Component.StartPosition.BOTTOMLEFT) {
                        resetPos.x += (double)moveNext.v;
                     } else {
                        resetPos.x -= (double)moveNext.v;
                     }

                     if (!(resetPos.x < 0.0D) && !(resetPos.x + c.realDimension.x > this.realDimension.x)) {
                        return -1;
                     }

                     var10000 = this.realDimension;
                     var10000.x += c.realDimension.x;
                     this.clearResetPos(c, resetPos);
                     return -2;
                  }

                  var10000 = this.realDimension;
                  var10000.y -= c.position.y;
                  return -2;
               }

               return -3;
            }
         }

         return 0;
      }
   }

   public void clearResetPos(Component c, Point p) {
      switch($SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$StartPosition()[c.layoutStart.ordinal()]) {
      case 1:
         p.set(0, 0);
         break;
      case 2:
         p.set(this.realDimension.x - c.realDimension.x, 0.0D);
         break;
      case 3:
         p.set(0.0D, this.realDimension.y - c.realDimension.y);
         break;
      case 4:
         p.set(this.realDimension.x - c.realDimension.x, this.realDimension.y - c.realDimension.y);
      }

      c.position.set(p);
   }

   public void render(Graphics graph) {
      if (this.root == null) {
         this.setTargetDimension((int)graph.getWindowSize().x, (int)graph.getWindowSize().y);
         this.reLayout();
      }

      this.childRender(graph);
   }

   public Iterable<Component> children() {
      return new IteratorWrap(this.children.iterator());
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
   static int[] $SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$DimensionMode() {
      int[] var10000 = $SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$DimensionMode;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[Component.DimensionMode.values().length];

         try {
            var0[Component.DimensionMode.MANUAL.ordinal()] = 1;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[Component.DimensionMode.PARENT_FILL.ordinal()] = 2;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[Component.DimensionMode.PARENT_FILLH.ordinal()] = 4;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[Component.DimensionMode.PARENT_FILLW.ordinal()] = 3;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$DimensionMode = var0;
         return var0;
      }
   }

   // $FF: synthetic method
   static int[] $SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$StartPosition() {
      int[] var10000 = $SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$StartPosition;
      if (var10000 != null) {
         return var10000;
      } else {
         int[] var0 = new int[Component.StartPosition.values().length];

         try {
            var0[Component.StartPosition.BOTTOMLEFT.ordinal()] = 3;
         } catch (NoSuchFieldError var4) {
         }

         try {
            var0[Component.StartPosition.BOTTOMRIGHT.ordinal()] = 4;
         } catch (NoSuchFieldError var3) {
         }

         try {
            var0[Component.StartPosition.TOPLEFT.ordinal()] = 1;
         } catch (NoSuchFieldError var2) {
         }

         try {
            var0[Component.StartPosition.TOPRIGHT.ordinal()] = 2;
         } catch (NoSuchFieldError var1) {
         }

         $SWITCH_TABLE$com$kirbymimi$mmb$ui$Component$StartPosition = var0;
         return var0;
      }
   }

   public static enum DimensionMode {
      MANUAL,
      PARENT_FILL,
      PARENT_FILLW,
      PARENT_FILLH;
   }

   public static enum Expand {
      WIDTH,
      HEIGHT;
   }

   public static enum Layout {
      MANUAL,
      AUTOMATIC_DIRECTION;
   }

   public static enum StartPosition {
      TOPLEFT,
      TOPRIGHT,
      BOTTOMLEFT,
      BOTTOMRIGHT;
   }

   public interface TaskInterface {
      void exec();
   }
}
