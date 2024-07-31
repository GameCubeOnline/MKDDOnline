package com.kirbymimi.mmb.actor;

import com.kirbymimi.mmb.graphics.Graphics;
import com.kirbymimi.mmb.graphics.Renderable;
import com.kirbymimi.mmb.res.Resource;
import com.kirbymimi.mmb.state.State;
import com.kirbymimi.mmb.state.StateMachine;
import com.kirbymimi.mmb.system.KTask;
import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.ClassLinker;
import com.kirbymimi.mmb.ut.IteratorWrap;
import com.kirbymimi.mmb.ut.Judge;
import com.kirbymimi.mmb.ut.ObjComponent;
import com.kirbymimi.mmb.ut.ktml.KTML2Obj;
import com.kirbymimi.mmb.ut.ktml.KTMLEntry;
import com.kirbymimi.mmb.ut.list.SafeList;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.function.BiConsumer;

public abstract class Actor implements Runnable, Renderable {
   protected StateMachine<?, ?> state = new StateMachine(this);
   Actor parent;
   boolean paused;
   protected SafeList<ObjComponent<? extends Actor>> components = new SafeList();
   protected SafeList<Actor> children = new SafeList();
   protected SafeList<Actor.ActorMail> mailBox = new SafeList();
   protected Resource res;
   KTask kTask;
   int drawOrder;
   volatile boolean killed;
   Judge judge = new Judge(this);

   static {
      ClassLinker.entry(Actor.class, new Actor.MailPropAnnotation());
   }

   public Actor() {
      ClassLinker.updateLinks(this.getClass());
      this.res = Resource.load(this.getClass().getSimpleName());
      if (this.res != null) {
         KTMLEntry props = (KTMLEntry)this.res.condGet("Props");
         if (props != null) {
            KTMLEntry drawOrderEntry = props.get("drawOrderName");
            if (drawOrderEntry != null) {
               this.drawOrder = MMBSystem.translateDrawOrderS(drawOrderEntry.getAsString());
            }

            KTML2Obj.loadS(this, props);
         }
      }
   }

   public Resource getResource() {
      return this.res;
   }

   public boolean judgeCheck(String name) {
      return this.judge.judgeB(name);
   }

   public boolean judgeCheck(Object args, String name) {
      return this.judge.judgeB(args, name);
   }

   public void judgeSwap(Object last, Object next) {
      this.judge.remove(last);
      this.judge.entry(next);
   }

   public void judgeEntry(Object obj) {
      this.judge.entry(obj);
   }

   public void judgeRemove(Object obj) {
      this.judge.remove(obj);
   }

   public void stateTransit(State<? extends Actor> next) {
      this.judgeRemove(this.state.state());
      this.judgeEntry(next);
      this.state.transit(next);
   }

   public void startWork(Actor parent) {
      this.parent = parent;
      this.kTask = new KTask(this);
      this.kTask.setPaused(this.paused);
      MMBSystem.get().addWork(this.kTask);
   }

   public void setPaused(boolean paused) {
      this.paused = paused;
      if (this.kTask != null) {
         this.kTask.setPaused(paused);
      }

   }

   public boolean isPaused() {
      return this.paused;
   }

   public void run() {
      Iterator var2 = this.components.iterator();

      ObjComponent component;
      while(var2.hasNext()) {
         component = (ObjComponent)var2.next();
         if (!component.run()) {
            this.components.remove(component);
         }
      }

      component = null;
      Iterator it;
      synchronized(this) {
         it = this.mailBox.iterator();
         this.mailBox.clear();
      }

      Iterator var3 = (new IteratorWrap(it)).iterator();

      while(var3.hasNext()) {
         Actor.ActorMail mail = (Actor.ActorMail)var3.next();
         this.mailCallBack(mail);
      }

   }

   public void mailCallBack(Actor.ActorMail mail) {
      BiConsumer<Actor, Object> consumer = ClassLinker.getBiConsumer(this.getClass(), mail.name);
      if (consumer != null) {
         consumer.accept(this, mail.value);
      }

      Iterator var4 = this.children.iterator();

      while(var4.hasNext()) {
         Actor child = (Actor)var4.next();
         mail.childrenCallBack(this, child);
         child.mailCallBack(mail);
      }

   }

   public void mail(Actor.ActorMail mail) {
      synchronized(this) {
         this.mailBox.add(mail);
      }
   }

   public KTask getKTask() {
      return this.kTask;
   }

   public void setKTask(KTask task) {
      this.kTask = task;
   }

   public double getDelta() {
      return this.kTask.getDelta();
   }

   public void setParent(Actor parent) {
      this.parent = parent;
   }

   public void kill() {
      this.killed = true;
      if (this.parent != null) {
         this.parent.children.remove(this);
      }

      Iterator var2 = this.children.iterator();

      while(var2.hasNext()) {
         Actor actor = (Actor)var2.next();
         actor.kill();
      }

      this.kTask.kill();
   }

   public boolean killed() {
      return this.killed;
   }

   public void graphicsEntry(Graphics graphics) {
      graphics.addToDrawList(this, this.drawOrder);
      Iterator var3 = this.children.iterator();

      while(var3.hasNext()) {
         Actor act = (Actor)var3.next();
         act.graphicsEntry(graphics);
      }

   }

   public void render(Graphics graphics) {
   }

   public SafeList<Actor> getChildren() {
      return this.children;
   }

   public void addChild(Actor child) {
      this.children.add(child);
   }

   public Actor getParentActor() {
      return this.parent;
   }

   public static class ActorMail {
      String name;
      Object value;

      public ActorMail(String name, Object value) {
         this.name = name;
         this.value = value;
      }

      public ActorMail(String name) {
         this.name = name;
      }

      public void childrenCallBack(Actor parent, Actor child) {
      }
   }

   public static class MailPropAnnotation extends ClassLinker.PropAnnotation {
      public MailPropAnnotation() {
         super(ActorMailCallback.class);
      }

      public Object key(Method method) {
         return ((ActorMailCallback)method.getAnnotation(ActorMailCallback.class)).value();
      }
   }
}
