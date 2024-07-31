package com.kirbymimi.mmb.res;

import com.kirbymimi.mmb.system.MMBSystem;
import java.util.HashMap;

public class ResourceContainer {
   HashMap<Class<?>, HashMap<String, Object>> resourceMap = new HashMap();

   public <T> T getResource(Class<?> parent, Class<T> resCls, String name) {
      HashMap<String, Object> local = (HashMap)this.resourceMap.get(parent);
      if (local == null) {
         local = new HashMap();
         this.resourceMap.put(parent, local);
      }

      Object res = local.get(name);
      if (res == null) {
         try {
            res = resCls.getConstructor().newInstance();
         } catch (Exception var10) {
            if (resCls == Integer.class) {
               try {
                  res = resCls.getConstructor(Integer.TYPE).newInstance(0);
               } catch (Exception var9) {
               }
            } else if (resCls == Double.class) {
               try {
                  res = resCls.getConstructor(Double.TYPE).newInstance(0);
               } catch (Exception var8) {
               }
            } else {
               MMBSystem.fatalS((Object)"Cannot instantiate resource class");
            }
         }

         local.put(name, res);
      }

      return (T) res;
   }

   public <T> void setResource(Class<?> parent, Class<T> resCls, String name, T res) {
      HashMap<String, Object> local = (HashMap)this.resourceMap.get(parent);
      if (local == null) {
         local = new HashMap();
         this.resourceMap.put(parent, local);
      }

      local.put(name, res);
   }
}
