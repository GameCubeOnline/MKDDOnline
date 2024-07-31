package com.kirbymimi.mmb.graphics.animation;

import com.kirbymimi.mmb.graphics.Renderable;
import com.kirbymimi.mmb.graphics.RenderableImage;
import java.util.function.Supplier;

public class ImageAnimator extends Animator<ImageAnimations, ImageAnimations.ImageAnimation> implements Supplier<Renderable> {
   public ImageAnimator(ImageAnimations animations) {
      super(animations);
   }

   public RenderableImage get() {
      return (RenderableImage)this.getFrame();
   }
}
