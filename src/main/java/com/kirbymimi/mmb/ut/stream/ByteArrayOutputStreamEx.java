package com.kirbymimi.mmb.ut.stream;

import java.io.ByteArrayOutputStream;

public class ByteArrayOutputStreamEx extends ByteArrayOutputStream implements SkippableOutputStream {
   public ByteArrayOutputStreamEx(byte[] data) {
      this.buf = data;
   }

   public ByteArrayOutputStreamEx() {
   }

   public byte[] getBackingBuffer() {
      return this.buf;
   }

   public void skip(long cnt) {
      this.count = (int)((long)this.count + cnt);
   }

   public void seek(int cnt) {
      this.count = cnt;
   }
}
