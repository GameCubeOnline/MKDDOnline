package com.kirbymimi.mmb.ut.ktml;

import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.stream.Scanner;
import java.io.File;
import java.util.Iterator;

public class KTMLDecoder {
   Scanner scan;

   public KTMLDecoder(Scanner scan) {
      this.scan = scan;
   }

   public KTMLDecoder(byte[] data) {
      this.scan = new Scanner(data);
   }

   public static KTMLEntry decode(Scanner scan) {
      return (new KTMLDecoder(scan)).decode();
   }

   public static KTMLEntry decode(byte[] data) {
      return (new KTMLDecoder(data)).decode();
   }

   public static KTMLEntry decode(File file) {
      try {
         KTMLEntry ret = (new KTMLDecoder(new Scanner(file))).decode();
         if (ret.name == null) {
            ret.name = file.getName();
         }

         return ret;
      } catch (Exception var2) {
         return null;
      }
   }

   public KTMLEntry decode() {
      KTMLEntry ret = new KTMLEntry();
      ret.setAsParent();
      this.subDecode(ret);
      this.scan.close();
      return ret;
   }

   public void subDecode(KTMLEntry parent) {
      while(true) {
         if (this.scan.hasNext()) {
            KTMLEntry e = this.decodeElement(parent);
            if (e != null) {
               e.parent = parent;
               parent.add(e);
               continue;
            }
         }

         return;
      }
   }

   public KTMLEntry decodeElement(KTMLEntry parent) {
      String ctoken = this.readString();
      switch(ctoken.charAt(0)) {
      case '"':
         if (!ctoken.endsWith(":")) {
            return this.createEntry(parent, (String)null, (String)null, ctoken);
         }

         return this.createEntry(parent, (String)null, ctoken, this.readString());
      case '<':
         if (!ctoken.endsWith(":")) {
            String ctoken2 = this.readString();
            if (ctoken2.charAt(ctoken2.length() - 1) == ':') {
               return this.createEntry(parent, ctoken, ctoken2, this.readString());
            }

            return this.createEntry(parent, ctoken, (String)null, ctoken2);
         }

         String var4;
         switch((var4 = ctoken.substring(1, ctoken.length() - 2)).hashCode()) {
         case -1305664359:
            if (var4.equals("extends")) {
               String extendName = this.forceReadString();
               KTMLEntry search = parent.parent;
               KTMLEntry ext = null;

               while(true) {
                  if (search == null) {
                     MMBSystem.fatalS((Object)("Can't find entry to extend : " + extendName));
                  }

                  ext = search.getEntry(extendName);
                  if (ext != null) {
                     Iterator var9 = ext.iterator();

                     while(var9.hasNext()) {
                        KTMLEntry entry = (KTMLEntry)var9.next();
                        parent.add(entry);
                     }
                     break;
                  }

                  search = search.getParent();
               }
            }
            break;
         case 112680:
            if (var4.equals("raw")) {
               parent.type = "raw";
            }
            break;
         case 3575610:
            if (var4.equals("type")) {
               parent.type = this.forceReadString();
            }
         }

         return this.decodeElement(parent);
      case '}':
         return null;
      default:
         return this.createEntry(parent, (String)null, (String)null, ctoken);
      }
   }

   public KTMLEntry createEntry(KTMLEntry parent, String type, String name, String value) {
      KTMLEntry entry = new KTMLEntry();
      if (name != null) {
         name = name.replace(":", "");
         name = name.replace("\"", "");
         entry.name = name;
      }

      if (parent.type != null && parent.type.compareTo("raw") == 0) {
         entry.value = this.scan.readUntil("}").getBytes();
         return entry;
      } else {
         if (type != null) {
            entry.type = type.substring(1, type.length() - 1);
         }

         switch(value.charAt(0)) {
         case '"':
            value = value.replaceFirst("\"", "");
            entry.value = value.replaceFirst("(?s)\"(?!.*?\")", "");
            break;
         case '{':
            entry.setAsParent();
            entry.parent = parent;
            this.subDecode(entry);
            return entry;
         default:
            try {
               entry.value = Long.decode(value).intValue();
            } catch (Exception var11) {
               try {
                  entry.value = Double.parseDouble(value);
               } catch (Exception var10) {
                  try {
                     entry.value = Boolean.parseBoolean(value);
                  } catch (Exception var9) {
                     if (type != null) {
                        int funcIdx = type.lastIndexOf(46);
                        if (funcIdx != -1) {
                           try {
                              Class<?> cls = Class.forName(type.substring(0, funcIdx));
                              entry.value = cls.getMethod(type.substring(funcIdx + 1, type.length()), String.class).invoke((Object)null, entry.value);
                           } catch (Exception var8) {
                           }
                        }
                     }
                  }
               }
            }
         }

         return entry;
      }
   }

   public String forceReadString() {
      String str = this.readString();
      if (str.charAt(0) != '"' || str.charAt(str.length() - 1) != '"') {
         MMBSystem.fatalS((Object)("not a string : " + str));
      }

      return str.substring(1, str.length() - 1);
   }

   public String readString() {
      String ret;
      while((ret = this.scan.next()).charAt(0) == ';') {
         ret = this.scan.nextLine();
      }

      if (ret.charAt(0) != '"') {
         return ret;
      } else {
         while(ret.length() < 2 || ret.charAt(ret.length() - 2) == '\\' || !ret.endsWith("\"") && !ret.endsWith(":")) {
            ret = ret + this.scan.next();
         }

         return ret;
      }
   }
}
