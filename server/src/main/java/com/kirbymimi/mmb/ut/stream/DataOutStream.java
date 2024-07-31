package com.kirbymimi.mmb.ut.stream;

import com.kirbymimi.mmb.system.MMBSystem;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.util.Arrays;

public class DataOutStream extends StreamCommon {
   OutputStream out;

   public OutputStream getStream() {
      return this.out;
   }

   public DataOutStream(byte[] bytes) {
      this.out = new ByteArrayOutputStreamEx(bytes);
   }

   public DataOutStream(OutputStream out) {
      this.out = out;
   }

   public DataOutStream(OutputStream out, ByteOrder order) {
      this(out);
      this.order = order;
   }

   public OutputStream getBackingStream() {
      return this.out;
   }

   void reposition(long l) throws IOException {
      if (this.out instanceof SkippableOutputStream) {
         ((SkippableOutputStream)this.out).skip(l);
      } else {
         if (l >= 0L) {
            this.zeroes(l);
         } else {
            MMBSystem.fatalS((Object)"Cannot skip values in an output stream");
         }

      }
   }

   public void write(byte b) throws IOException {
      ++this.position;
      this.out.write(b);
   }

   public void write(int i) throws IOException {
      ++this.position;
      this.out.write(i);
   }

   public void zeroes(int cnt) throws IOException {
      this.write((int)0, cnt);
   }

   public void zeroes(long cnt) throws IOException {
      this.write(0, cnt);
   }

   public void write(byte b, int cnt) throws IOException {
      this.position += (long)cnt;

      for(int i = 0; i != cnt; ++i) {
         this.out.write(b);
      }

   }

   public void write(int i, int cnt) throws IOException {
      this.position += (long)cnt;
      if (cnt > 16384) {
         byte[] fill = new byte[16384];
         Arrays.fill(fill, (byte)i);

         while(cnt > 16384) {
            cnt -= 16384;
            this.out.write(fill);
         }
      }

      this.position += (long)cnt;

      for(int i2 = 0; i2 != cnt; ++i2) {
         this.out.write(i);
      }

   }

   public void write(int i, long cnt) throws IOException {
      this.position += cnt;
      if (cnt > 16384L) {
         byte[] fill = new byte[16384];
         Arrays.fill(fill, (byte)i);

         while(cnt > 16384L) {
            cnt -= 16384L;
            this.out.write(fill);
         }
      }

      for(long i2 = 0L; i2 != cnt; ++i2) {
         this.out.write(i);
      }

   }

   public void writeCompressedInt(int i) throws IOException {
      while(true) {
         int write = i & 127;
         i >>= 7;
         if (i == 0) {
            this.write(write);
            return;
         }

         write |= 128;
         this.write(write | 128);
      }
   }

   public void write(byte[] src) throws IOException {
      this.position += (long)src.length;
      this.out.write(src);
   }

   public void write(byte[] dst, int off, int len) throws IOException {
      this.position += (long)len;
      this.out.write(dst, off, len);
   }

   public void writeShort(int s) throws IOException {
      this.position += 2L;
      if (this.order == ByteOrder.BIG_ENDIAN) {
         this.out.write(s >> 8);
         this.out.write(s);
      } else {
         this.out.write(s);
         this.out.write(s >> 8);
      }
   }

   public void writeInt24(int i) throws IOException {
      this.position += 3L;
      if (this.order == ByteOrder.BIG_ENDIAN) {
         this.out.write(i >> 16);
         this.out.write(i >> 8);
         this.out.write(i);
      } else {
         this.out.write(i);
         this.out.write(i >> 8);
         this.out.write(i >> 16);
      }
   }

   public void writeInt(int i) throws IOException {
      this.position += 4L;
      if (this.order == ByteOrder.BIG_ENDIAN) {
         this.out.write(i >> 24);
         this.out.write(i >> 16);
         this.out.write(i >> 8);
         this.out.write(i);
      } else {
         this.out.write(i);
         this.out.write(i >> 8);
         this.out.write(i >> 16);
         this.out.write(i >> 24);
      }
   }

   public void writeLong(long l) throws IOException {
      this.position += 8L;
      if (this.order == ByteOrder.BIG_ENDIAN) {
         this.out.write((int)(l >> 56));
         this.out.write((int)(l >> 48));
         this.out.write((int)(l >> 40));
         this.out.write((int)(l >> 32));
         this.out.write((int)(l >> 24));
         this.out.write((int)(l >> 16));
         this.out.write((int)(l >> 8));
         this.out.write((int)l);
      } else {
         this.out.write((int)l);
         this.out.write((int)(l >> 8));
         this.out.write((int)(l >> 16));
         this.out.write((int)(l >> 24));
         this.out.write((int)(l >> 32));
         this.out.write((int)(l >> 40));
         this.out.write((int)(l >> 48));
         this.out.write((int)(l >> 56));
      }
   }

   public void writeFloat(float f) throws IOException {
      this.writeInt(Float.floatToRawIntBits(f));
   }

   public void writeDouble(double d) throws IOException {
      this.writeLong(Double.doubleToLongBits(d));
   }

   public void writeByte(byte[] bytes) throws IOException {
      byte[] var5 = bytes;
      int var4 = bytes.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         byte b = var5[var3];
         this.writeLong((long)b);
      }

   }

   public void writeShort(short[] shorts) throws IOException {
      short[] var5 = shorts;
      int var4 = shorts.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         short s = var5[var3];
         this.writeShort(s);
      }

   }

   public void writeInt(int[] ints) throws IOException {
      int[] var5 = ints;
      int var4 = ints.length;

      for(int var3 = 0; var3 < var4; ++var3) {
         int i = var5[var3];
         this.writeInt(i);
      }

   }

   public void writeLong(long[] longs) throws IOException {
      long[] var6 = longs;
      int var5 = longs.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         long l = var6[var4];
         this.writeLong(l);
      }

   }

   public void writeNullTerminatedString(String str) throws IOException {
      this.writeRawString(str);
      this.write((int)0);
   }

   public void writeLen16String(String str) throws IOException {
      this.writeShort(str.length());
      this.writeRawString(str);
   }

   public void writeNewLineString(String str) throws IOException {
      this.writeRawString(str);
      this.writeRawString(System.lineSeparator());
   }

   public void writeRawString(String str) throws IOException {
      byte[] var5;
      int var4 = (var5 = str.getBytes()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         byte b = var5[var3];
         this.write(b);
      }

   }

   public void readIntLengthUTF16(String str) throws IOException {
      this.writeShort((short)str.length());
      char[] var5;
      int var4 = (var5 = str.toCharArray()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         char c = var5[var3];
         this.writeShort((short)c);
      }

   }

   public void close() throws IOException {
      this.out.close();
   }
}
