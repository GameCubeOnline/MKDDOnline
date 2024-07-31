package com.kirbymimi.mmb.ut.stream;

import java.io.IOException;

public class ByteFifo {
   byte[] fifo;
   int read;
   int write;

   public ByteFifo(int len) {
      this.fifo = new byte[len];
   }

   public ByteFifo() {
      this(16384);
   }

   public void put(byte b) throws IOException {
      this.fifo[this.write++] = b;
      if (this.write == this.fifo.length) {
         this.write = 0;
      }

      if (this.read == this.write) {
         throw new IOException();
      }
   }

   public byte read() throws IOException {
      if (this.write == this.read) {
         throw new IOException();
      } else {
         byte ret = this.fifo[this.read++];
         if (this.read == this.fifo.length) {
            this.read = 0;
         }

         return ret;
      }
   }

   public void skip(long cnt) {
      this.read = (int)((long)this.read + cnt);
      if (this.read >= 1024) {
         this.read -= 16384;
      }

   }

   public void clear() {
      this.read = this.write;
   }

   public boolean isAvailable() {
      return this.write != this.read;
   }

   public int available() {
      int ret = this.write - this.read;
      if (ret < 0) {
         ret += 16384;
      }

      return ret;
   }
}
