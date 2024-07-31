package com.kirbymimi.mmb.ut.stream;

import com.kirbymimi.mmb.system.MMBSystem;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Function;

public class Scanner implements Closeable {
   DataInStream src;
   ByteArrayOutputStreamEx output = new ByteArrayOutputStreamEx();
   Function<String, String> returnFunction;

   public Scanner(InputStream input) {
      this.src = new DataInStream(input);
   }

   public Scanner(byte[] input) {
      this.src = new DataInStream(new ByteArrayInputStream(input));
   }

   public Scanner(File input) {
      try {
         this.src = new DataInStream(new FileInputStream(input));
      } catch (Exception var3) {
         MMBSystem.fatalS(var3);
      }

   }

   public String next() {
      return this.readUntil(System.lineSeparator(), " ");
   }

   public String nextLine() {
      return this.readUntil(System.lineSeparator());
   }

   public void close() {
      try {
         this.output.close();
      } catch (IOException var2) {
      }

   }

   public String peek() {
      this.src.enableRewind();
      String ret = this.next();
      this.src.disableRewind();
      return ret;
   }

   public String peekLine() {
      this.src.enableRewind();
      String ret = this.nextLine();
      this.src.disableRewind();
      return ret;
   }

   public void setReturnFunction(Function<String, String> func) {
      this.returnFunction = func;
   }

   public String readUntil(String stop) {
      return this.readUntil(new byte[][]{stop.getBytes()});
   }

   public String readUntil(String... stop) {
      byte[][] stops = new byte[stop.length][];

      for(int i = 0; i != stops.length; ++i) {
         stops[i] = stop[i].getBytes();
      }

      return this.readUntil(stops).trim();
   }

   public String readUntil(byte[][] stop) {
      while(true) {
         try {
            if (this.src.available() == 0) {
               String ret = new String(this.output.getBackingBuffer(), 0, this.output.size());
               this.output.reset();
               if (this.returnFunction != null) {
                  return (String)this.returnFunction.apply(ret);
               }

               return ret;
            }

            byte curr = this.src.readByte();
            this.output.write(curr);
         } catch (Exception var7) {
            MMBSystem.fatalS(var7);
         }

         byte[][] var5 = stop;
         int var4 = stop.length;

         label55:
         for(int var3 = 0; var3 < var4; ++var3) {
            byte[] cstop = var5[var3];
            if (cstop.length <= this.output.size()) {
               for(int i = 0; i != cstop.length; ++i) {
                  if (cstop[i] != this.output.getBackingBuffer()[this.output.size() - (cstop.length - i)]) {
                     continue label55;
                  }
               }

               String ret = new String(this.output.getBackingBuffer(), 0, this.output.size() - cstop.length);
               this.output.reset();
               if (ret.isEmpty()) {
                  break;
               }

               if (this.returnFunction != null) {
                  return (String)this.returnFunction.apply(ret);
               }

               return ret;
            }
         }
      }
   }

   public boolean hasNext() {
      try {
         if (this.src.available() == 0) {
            return false;
         }
      } catch (IOException var2) {
         MMBSystem.fatalS((Exception)var2);
      }

      this.src.enableRewind();
      String str = this.next();
      this.src.disableRewind();
      return !str.isEmpty();
   }
}
