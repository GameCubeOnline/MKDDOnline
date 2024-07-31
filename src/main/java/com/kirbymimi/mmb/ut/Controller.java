package com.kirbymimi.mmb.ut;

import com.kirbymimi.mmb.ut.list.SafeList;
import java.util.HashMap;
import java.util.Iterator;

public class Controller {
   HashMap<String, Controller.Button> map = new HashMap();
   SafeList<Controller.Button> entryReqs = new SafeList();
   SafeList<String> removeReqs = new SafeList();

   public synchronized void entry(String name, Object data) {
      Controller.Button button = (Controller.Button)this.map.get(name);
      if (button == null) {
         button = new Controller.Button(name, data);
         this.entryReqs.add(button);
      } else {
         button.data = data;
      }

   }

   public synchronized void remove(String name) {
      if (this.map.containsKey(name)) {
         this.removeReqs.add(name);
      }
   }

   public synchronized void update(double delta) {
      Iterator var4 = this.removeReqs.iterator();

      while(var4.hasNext()) {
         String str = (String)var4.next();
         this.map.remove(str);
      }

      Controller.Button button;
      for(var4 = (new IteratorWrap(this.map.values().iterator())).iterator(); var4.hasNext(); button.time += delta) {
         button = (Controller.Button)var4.next();
      }

      var4 = this.entryReqs.iterator();

      while(var4.hasNext()) {
         button = (Controller.Button)var4.next();
         System.out.println(button.name);
         this.map.put(button.name, button);
      }

      this.entryReqs.clear();
      this.removeReqs.clear();
   }

   public Controller.Button getButton(String name) {
      return (Controller.Button)this.map.get(name);
   }

   public double getTime(String name) {
      Controller.Button button = this.getButton(name);
      return button == null ? -1.0D : button.time;
   }

   public <T> T getValue(String name) {
      Controller.Button button = this.getButton(name);
      if (button == null) {
         return null;
      } else {
         try {
            return (T) button.data;
         } catch (Exception var4) {
            return null;
         }
      }
   }

   public double getValueDouble(String name) {
      Controller.Button button = this.getButton(name);
      return button == null ? 0.0D : (Double)button.data;
   }

   public static class Button {
      String name;
      double time;
      Object data;

      Button(String name, Object data) {
         this.name = name;
         this.data = data;
      }

      public double time() {
         return this.time;
      }

      public double getDataDouble() {
         return (Double)this.data;
      }
   }
}
