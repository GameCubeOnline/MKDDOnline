package com.kirbymimi.mmb.graphics;

import java.util.HashMap;

public abstract class GraphicsEffect {
   public HashMap<Renderable, Renderable> cache = new HashMap();

   public Renderable load(Renderable data) {
      Renderable effData = (Renderable)this.cache.get(data);
      if (effData == null) {
         effData = this.createEffect(data);
         this.cache.put(data, effData);
      }

      return effData;
   }

   public abstract Renderable createEffect(Renderable var1);
}
