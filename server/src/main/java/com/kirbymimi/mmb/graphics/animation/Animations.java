package com.kirbymimi.mmb.graphics.animation;

import com.kirbymimi.mmb.res.Resource;
import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.ktml.KTMLEntry;
import java.util.HashMap;
import java.util.Iterator;

public abstract class Animations<T extends Animations.Animation<?>> {
   HashMap<String, T> animations = new HashMap();

   public abstract Animator<?, ?> createAnimator();

   public void load(Resource res, KTMLEntry ktml, String texturesPath, Class<T> animCls) {
      Iterator var6 = ktml.iterator();

      while(var6.hasNext()) {
         KTMLEntry animEntry = (KTMLEntry)var6.next();
         if (animEntry.isParent()) {
            this.loadSingleAnim(res, animEntry, animEntry.getName(), texturesPath, animCls);
         }
      }

   }

   public void loadSingleAnim(Resource res, KTMLEntry ktml, String name, String resPath, Class<T> animCls) {
      Animations.Animation anim = null;

      try {
         anim = (Animations.Animation)animCls.newInstance();
      } catch (Exception var13) {
         MMBSystem.fatalS(var13);
      }

      Double rate = ktml.getDouble("rate");
      if (rate != null) {
         anim.rate = rate;
      }

      KTMLEntry frames = ktml.get("frames");
      if (frames == null) {
         frames = new KTMLEntry((KTMLEntry)null);
      }

      anim.animations = new Object[frames.getElementCount()];
      int imageIdx = 0;

      String path;
      for(Iterator var11 = frames.iterator(); var11.hasNext(); anim.animations[imageIdx++] = res.get(path)) {
         KTMLEntry strEntry = (KTMLEntry)var11.next();
         path = resPath + strEntry.getAsString();
      }

      Boolean bool = ktml.getBoolean("loop");
      if (bool != null) {
         anim.loop = bool;
      }

      this.animations.put(name, (T) anim);
   }

   public T get(String key) {
      return (T) this.animations.get(key);
   }

   public static class Animation<T> {
      double rate = 1.0D;
      boolean loop = true;
      Object[] animations;

      public T get(int idx) {
         return (T) this.animations[idx];
      }
   }
}
