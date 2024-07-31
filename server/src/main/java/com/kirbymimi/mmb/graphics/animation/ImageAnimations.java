package com.kirbymimi.mmb.graphics.animation;

import com.kirbymimi.mmb.graphics.RenderableImage;
import com.kirbymimi.mmb.res.Resource;
import com.kirbymimi.mmb.ut.ktml.KTMLEntry;

public class ImageAnimations extends Animations<ImageAnimations.ImageAnimation> {
   public ImageAnimations() {
   }

   public ImageAnimations(Resource res, KTMLEntry ktml) {
      this.load(res, ktml, (String)null);
   }

   public ImageAnimations(Resource res, KTMLEntry ktml, String texturesPath) {
      this.load(res, ktml, texturesPath);
   }

   public void load(Resource res, KTMLEntry ktml, String texturesPath) {
      if (texturesPath == null) {
         texturesPath = ktml.getString("textures");
      }

      super.load(res, ktml, texturesPath, ImageAnimations.ImageAnimation.class);
   }

   public void loadSingleAnim(Resource res, KTMLEntry ktml, String name, String resPath) {
      super.loadSingleAnim(res, ktml, name, resPath, ImageAnimations.ImageAnimation.class);
   }

   public ImageAnimator createAnimator() {
      return new ImageAnimator(this);
   }

   public static class ImageAnimation extends Animations.Animation<RenderableImage> {
   }
}
