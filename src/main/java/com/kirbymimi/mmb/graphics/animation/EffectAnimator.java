package com.kirbymimi.mmb.graphics.animation;

import com.kirbymimi.mmb.graphics.GraphicsEffect;
import java.util.function.Supplier;

public class EffectAnimator extends Animator<EffectAnimations, EffectAnimations.EffectAnimation> implements Supplier<GraphicsEffect> {
   public EffectAnimator(EffectAnimations animations) {
      super(animations);
   }

   public GraphicsEffect get() {
      return (GraphicsEffect)this.getFrame();
   }
}
