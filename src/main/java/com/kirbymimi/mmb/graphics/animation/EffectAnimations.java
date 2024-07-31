package com.kirbymimi.mmb.graphics.animation;

import com.kirbymimi.mmb.graphics.GraphicsEffect;
import com.kirbymimi.mmb.res.Resource;
import com.kirbymimi.mmb.ut.ktml.KTMLEntry;
import java.util.HashMap;

public class EffectAnimations extends Animations<EffectAnimations.EffectAnimation> {
   HashMap<String, EffectAnimations.EffectAnimation> animations = new HashMap();

   public EffectAnimations() {
   }

   public EffectAnimations(Resource res, KTMLEntry ktml) {
      this.load(res, ktml, (String)null);
   }

   public EffectAnimations(Resource res, KTMLEntry ktml, String texturesPath) {
      this.load(res, ktml, texturesPath);
   }

   public void load(Resource res, KTMLEntry ktml, String texturesPath) {
      if (texturesPath == null) {
         texturesPath = ktml.getString("effects");
      }

      super.load(res, ktml, texturesPath, EffectAnimations.EffectAnimation.class);
   }

   public void loadSingleAnim(Resource res, KTMLEntry ktml, String name, String resPath) {
      super.loadSingleAnim(res, ktml, name, resPath, EffectAnimations.EffectAnimation.class);
   }

   public EffectAnimator createAnimator() {
      return new EffectAnimator(this);
   }

   public static class EffectAnimation extends Animations.Animation<GraphicsEffect> {
   }
}
