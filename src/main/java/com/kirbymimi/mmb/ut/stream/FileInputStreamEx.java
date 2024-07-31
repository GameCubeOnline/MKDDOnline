package com.kirbymimi.mmb.ut.stream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

public class FileInputStreamEx extends InputStream {
   long position = 0L;
   ByteBuffer bb;

   public static InputStream create(boolean map, String file) throws FileNotFoundException {
      return create(map, file, 0);
   }

   public static InputStream create(boolean map, String file, int start) throws FileNotFoundException {
      RandomAccessFile raf = new RandomAccessFile(file, "r");
      if (map) {
         try {
            return new FileInputStreamEx(raf.getChannel(), start);
         } catch (IOException var5) {
         }
      }

      return new FileInputStreamEx.NonMapped(raf);
   }

   public FileInputStreamEx(FileChannel channel, int start) throws IOException {
      this.bb = channel.map(MapMode.READ_ONLY, (long)start, channel.size() - (long)start);
   }

   public int read() throws IOException {
      return this.bb.get() & 255;
   }

   public int read(byte[] data) throws IOException {
      this.bb.get(data);
      return data.length;
   }

   public int read(byte[] data, int off, int len) {
      this.bb.get(data, off, len);
      return len;
   }

   public int available() {
      return this.bb.limit() - this.bb.position();
   }

   public long skip(long cnt) throws IOException {
      this.bb.position(this.bb.position() + (int)cnt);
      return cnt;
   }

   static class NonMapped extends InputStream {
      RandomAccessFile file;

      NonMapped(RandomAccessFile file) {
         this.file = file;
      }

      public int read() throws IOException {
         return this.file.read();
      }

      public int read(byte[] data) throws IOException {
         return this.file.read(data);
      }

      public int read(byte[] data, int off, int len) throws IOException {
         return this.file.read(data, off, len);
      }

      public int available() throws IOException {
         return (int)(this.file.length() - this.file.getFilePointer());
      }

      public long skip(long cnt) throws IOException {
         this.file.seek(this.file.getFilePointer() + cnt);
         return cnt;
      }

      public void close() throws IOException {
         this.file.close();
      }
   }
}
