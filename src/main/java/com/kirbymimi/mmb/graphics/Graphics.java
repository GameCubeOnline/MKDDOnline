package com.kirbymimi.mmb.graphics;

import com.kirbymimi.mmb.math.IPoint;
import com.kirbymimi.mmb.math.Point;
import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.Controller;
import com.kirbymimi.mmb.ut.list.FastList;
import com.kirbymimi.mmb.ut.list.FastStack;
import com.kirbymimi.mmb.ut.list.SafeList;
import java.awt.Image;
import java.util.Iterator;

public abstract class Graphics {
   MMBSystem system;
   FastStack<IPoint> translateStack = new FastStack();
   protected FastStack<Matrix> matrixStack = new FastStack();
   protected int updateRate;
   Point defaultSize = new Point();
   SafeList<Controller> controllerList = new SafeList();
   SafeList<View> views = new SafeList();
   View currentView;
   FastList<FastList<Renderable>> renderList = new FastList();

   public Graphics(int updateRate) {
      this.updateRate = updateRate;
      this.system = MMBSystem.get();
   }

   public Iterable<View> getViews() {
      return this.views;
   }

   public View createView(Matrix viewPort, Matrix camera) {
      View view = new View(viewPort, camera);
      this.views.add(view);
      return view;
   }

   public View createView() {
      Matrix2D vp = new Matrix2D();
      Matrix2D cam = new Matrix2D();
      return this.createView(vp, cam);
   }

   public void removeView(View view) {
      this.views.remove(view);
   }

   public void removeAllViews() {
      this.views.clear();
   }

   public View getCurrentView() {
      return this.currentView;
   }

   public void render() {
      MMBSystem.addThread2Sys(Thread.currentThread(), this.system);
      this.setARGB(-16777216);
      Point p = this.getWindowSize();
      this.fillRect(0.0D, 0.0D, p.x, p.y);
      Iterator var3 = this.views.iterator();

      while(var3.hasNext()) {
         View view = (View)var3.next();
         this.currentView = view;
         view.render(this);
      }

   }

   public void renderListes() {
      Iterator var2 = this.renderList.iterator();

      while(true) {
         FastList list;
         do {
            if (!var2.hasNext()) {
               return;
            }

            list = (FastList)var2.next();
         } while(list == null);

         Iterator var4 = list.iterator();

         while(var4.hasNext()) {
            Renderable renderable = (Renderable)var4.next();
            renderable.render(this);
         }

         list.clear();
      }
   }

   public void addToDrawList(Renderable renderable, int order) {
      FastList<Renderable> list = (FastList)this.renderList.getRealloc(order);
      if (list == null) {
         list = new FastList();
         this.renderList.add(order, list);
      }

      list.add(renderable);
   }

   public void addController(Controller controller) {
      this.controllerList.add(controller);
   }

   public void removeController(Controller controller) {
      this.controllerList.remove(controller);
   }

   public void sendToControllers(String name, Object data) {
      Iterator var4 = this.controllerList.iterator();

      while(var4.hasNext()) {
         Controller controller = (Controller)var4.next();
         controller.entry(name, data);
      }

   }

   public void sendToControllers(String name) {
      this.sendToControllers(name, 0);
   }

   public void sendRemoveToControllers(String name) {
      Iterator var3 = this.controllerList.iterator();

      while(var3.hasNext()) {
         Controller controller = (Controller)var3.next();
         controller.remove(name);
      }

   }

   public abstract Graphics createSame();

   public abstract void start();

   public int getUpdateRate() {
      return this.updateRate;
   }

   public void deleteWindow() {
   }

   public abstract void runWindow();

   public abstract void repaintWindow();

   public abstract void setWindowPosition(int var1, int var2);

   public void setWindowSize(int w, int h) {
      this.defaultSize = new Point(w, h);
   }

   public void setWindowSize(double w, double h) {
      this.defaultSize = new Point(w, h);
   }

   public Point getDefaultSize() {
      return this.defaultSize.clone();
   }

   public abstract Point getWindowSize();

   public abstract void setCursor(int var1);

   public abstract Matrix getBaseMatrix();

   public abstract void setARGB(int var1);

   public abstract void setTranslate(int var1, int var2, int var3);

   public abstract void setScale(int var1, int var2, int var3);

   public abstract void setTransform(Matrix var1);

   public abstract void transform(Matrix var1);

   public abstract Matrix getCurrentMatrix();

   public void setScissors(double w, double h) {
      this.setScissors(0, 0, (int)w, (int)h);
   }

   public void setScissors(int w, int h) {
      this.setScissors(0, 0, w, h);
   }

   public abstract void setScissors(int var1, int var2, int var3, int var4);

   public void setTip(String tip) throws Exception {
      throw new Exception("NotImplementedException");
   }

   public void drawLine(double x0, double y0, double x1, double y1) {
      this.drawLine((int)x0, (int)y0, (int)x1, (int)y1);
   }

   public abstract void drawLine(int var1, int var2, int var3, int var4);

   public void drawRect(double x, double y, double w, double h) {
      this.drawRect((int)x, (int)y, (int)w, (int)h);
   }

   public abstract void drawRect(int var1, int var2, int var3, int var4);

   public void fillRect(double x, double y, double w, double h) {
      this.fillRect((int)x, (int)y, (int)w, (int)h);
   }

   public abstract void fillRect(int var1, int var2, int var3, int var4);

   public void fillCircle(double x, double y, double w, double h) {
      this.fillCircle((int)x, (int)y, (int)w, (int)h);
   }

   public abstract void fillCircle(int var1, int var2, int var3, int var4);

   public abstract void setFont(FontData var1);

   public abstract int textWidth(FontData var1, String var2);

   public abstract int charWidth(FontData var1, char var2);

   public void drawText(double x, double y, String str) {
      this.drawText((int)x, (int)y, str);
   }

   public abstract void drawText(int var1, int var2, String var3);

   public void drawChar(double x, double y, String str, int charPos) {
      this.drawChar((int)x, (int)y, str, charPos);
   }

   public abstract void drawChar(int var1, int var2, String var3, int var4);

   public void drawImage(Image image, double x, double y) {
      this.drawImageInternal(image, (int)x, (int)y);
   }

   public void drawImage(Image image) {
      this.drawImageInternal(image, 0, 0);
   }

   void drawImageInternal(Image image, int x, int y) {
      this.drawImage(image, x, y);
   }

   public abstract void drawImage(Image var1, int var2, int var3);

   public void setOrthogonal() {
      this.matrixStack.push(this.getCurrentMatrix());
      this.currentView.setOrthogonal(this);
   }

   public void pushMatrix(Matrix matrix) {
      this.matrixStack.push(this.getCurrentMatrix());
      this.transform(matrix);
   }

   public void popMatrix() {
      this.setTransform((Matrix)this.matrixStack.pop());
   }

   public void pushTranslate(int x, int y) {
      this.pushTranslate(new Point(x, y));
   }

   public void pushTranslate(int x, int y, int z) {
      this.pushTranslate(new Point(x, y, z));
   }

   public void pushTranslate(double x, double y) {
      this.pushTranslate(new Point((int)x, (int)y));
   }

   public void pushTranslate(double x, double y, double z) {
      this.pushTranslate(new Point((int)x, (int)y, (int)z));
   }

   public void pushTranslate(IPoint p) {
      this.setTranslate(p.x, p.y, p.z);
      this.translateStack.push(p);
   }

   public void pushTranslate(Point p) {
      this.pushTranslate(new IPoint(p.x, p.y, p.z));
   }

   public void popTranslate() {
      IPoint p = (IPoint)this.translateStack.pop();
      this.setTranslate(-p.x, -p.y, -p.z);
   }

   public void pushScale(int x, int y) {
      this.pushScale(new Point(x, y));
   }

   public void pushScale(int x, int y, int z) {
      this.pushScale(new Point(x, y, z));
   }

   public void pushScale(double x, double y) {
      this.pushScale(new Point((int)x, (int)y));
   }

   public void pushScale(double x, double y, double z) {
      this.pushScale(new Point((int)x, (int)y, (int)z));
   }

   public void pushScale(IPoint p) {
      this.setScale(p.x, p.y, p.z);
      this.translateStack.push(p);
   }

   public void pushScale(Point p) {
      this.pushScale(new IPoint(p.x, p.y, p.z));
   }

   public void popScale() {
      IPoint p = (IPoint)this.translateStack.pop();
      this.setScale(-p.x, -p.y, -p.z);
   }
}
