package com.kirbymimi.mmb.res;

import com.kirbymimi.mmb.graphics.RenderableImage;
import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.ktml.KTMLDecoder;
import com.kirbymimi.mmb.ut.ktml.KTMLEntry;
import com.kirbymimi.mmb.ut.list.FastList;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import javax.imageio.ImageIO;

public class Resource {
   String name;
   static HashMap<String, Resource> loaded = new HashMap();
   HashMap<String, Object> resources = new HashMap();

   public static Resource load(String name) {
      Resource res = (Resource)loaded.get(name);
      if (res == null) {
         res = new Resource(name);
         if (!MMBSystem.openFileS("files/" + name).exists()) {
            return null;
         }

         loaded.put(name, res);
      }

      return res;
   }

   public Resource(String name) {
      this.name = name;
      File f = MMBSystem.openFileS("files/" + name);
      if (f.isDirectory()) {
         this.recurseAddDir(f, "");
      }

      List<Resource.ResLookup> lookups = MMBSystem.getResLookup();
      if (lookups != null) {
         FastList<Resource.ResReplace> replaceList = new FastList();
         Iterator var6 = this.resources.entrySet().iterator();

         label70:
         while(var6.hasNext()) {
            Entry<String, Object> res = (Entry)var6.next();
            String type = null;
            if (res.getValue() instanceof KTMLEntry) {
               type = ((KTMLEntry)res.getValue()).getType();
            }

            if (type != null) {
               Class<?> cls = null;
               Iterator var10 = lookups.iterator();

               Resource.ResLookup lookup;
               while(true) {
                  if (!var10.hasNext()) {
                     continue label70;
                  }

                  lookup = (Resource.ResLookup)var10.next();

                  try {
                     cls = Class.forName(lookup.name + type);
                     break;
                  } catch (Exception var14) {
                  }
               }

               replaceList.add(new Resource.ResReplace(res, cls, lookup.priority));
            }
         }

         int stage = 0;

         for(int remaining = replaceList.size(); remaining != 0; ++stage) {
            Iterator var18 = replaceList.iterator();

            while(var18.hasNext()) {
               Resource.ResReplace replace = (Resource.ResReplace)var18.next();
               if (replace.priority == stage) {
                  --remaining;

                  try {
                     Object obj = replace.replace.getConstructor(Resource.class, replace.entry.getValue().getClass()).newInstance(this, replace.entry.getValue());
                     this.resources.replace((String)replace.entry.getKey(), obj);
                  } catch (InvocationTargetException var12) {
                     var12.getCause().printStackTrace();
                     MMBSystem.fatalS((Object)("Cannot instanciate class : " + replace.replace + " for resource shortcut"));
                  } catch (Exception var13) {
                     var13.printStackTrace();
                     MMBSystem.fatalS((Object)("Cannot instanciate class : " + replace.replace + " for resource shortcut"));
                  }
               }
            }
         }

      }
   }

   private void recurseAddDir(File dir, String name) {
      File[] var6;
      int var5 = (var6 = dir.listFiles()).length;

      for(int var4 = 0; var4 < var5; ++var4) {
         File f = var6[var4];
         if (f.isDirectory()) {
            this.recurseAddDir(f, name + f.getName() + "/");
         } else {
            try {
               Object obj = this.createRes(f.getName(), Files.readAllBytes(f.toPath()));
               String putName = name + f.getName();
               int formatIdx = putName.lastIndexOf(46);
               if (formatIdx != -1) {
                  putName = putName.substring(0, formatIdx);
               }

               this.resources.put(putName, obj);
            } catch (IOException var10) {
               MMBSystem.fatalS((Exception)var10);
            }
         }
      }

   }

   private Object createRes(String name, byte[] data) throws IOException {
      int formatIdx = name.lastIndexOf(46);
      String format = "";
      if (formatIdx != -1) {
         format = name.substring(formatIdx + 1);
      }

      switch(format.hashCode()) {
      case 111145:
         if (format.equals("png")) {
            return new RenderableImage(ImageIO.read(new ByteArrayInputStream(data)));
         }
         break;
      case 3302600:
         if (format.equals("ktml")) {
            return KTMLDecoder.decode(data);
         }
      }

      return data;
   }

   public <T> T get(String name) {
      Object o = this.resources.get(name);
      if (o == null) {
         MMBSystem.fatalS((Object)("Missing resource : " + name + " inside " + this.name));
      }

      try {
         return (T) o;
      } catch (Exception var4) {
         MMBSystem.fatalS((Object)("Bad resource type for : " + name + " inside " + this.name + ", got : " + o.getClass()));
         return null;
      }
   }

   public <T> T condGet(String name) {
      Object o = this.resources.get(name);
      if (o == null) {
         return null;
      } else {
         try {
            return (T) o;
         } catch (Exception var4) {
            return null;
         }
      }
   }

   public static class ResLookup {
      int priority;
      String name;

      public ResLookup(int priority, String name) {
         this.priority = priority;
         this.name = name;
      }
   }

   static class ResReplace {
      Entry<String, Object> entry;
      Class<?> replace;
      int priority;

      ResReplace(Entry<String, Object> entry, Class<?> replace, int priority) {
         this.entry = entry;
         this.replace = replace;
         this.priority = priority;
      }
   }
}
