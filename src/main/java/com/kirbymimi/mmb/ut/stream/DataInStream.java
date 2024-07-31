package com.kirbymimi.mmb.ut.stream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;

public class DataInStream extends StreamCommon {
   InputStream in;
   ByteFifo rewindFifo;
   boolean rewind;
   long rewindStartPos;
   ByteArrayOutputStreamEx strReadBuf;

   public InputStream getStream() {
      return this.in;
   }

   public DataInStream(byte[] bytes) {
      this.strReadBuf = new ByteArrayOutputStreamEx();
      this.in = new DataInStream.GoodByteArrayInputStream(bytes);
   }

   public DataInStream(InputStream in) {
      this.strReadBuf = new ByteArrayOutputStreamEx();
      this.in = in;
   }

   public DataInStream(InputStream in, ByteOrder order) {
      this(in);
      this.order = order;
   }

   public long getReadBytes() {
      return this.position;
   }

   public void enableRewind() {
      if (!this.rewind) {
         this.rewindStartPos = this.position;
      }

      if (this.rewindFifo == null) {
         this.rewindFifo = new ByteFifo();
      }

      this.rewind = true;
   }

   public void disableRewind() {
      this.position = this.rewindStartPos;
      this.rewind = false;
   }

   void reposition(long l) throws IOException {
      if (this.rewindFifo != null && this.rewindFifo.isAvailable()) {
         if ((long)this.rewindFifo.available() <= l) {
            this.rewindFifo.skip(l);
            return;
         }

         l -= (long)this.rewindFifo.available();
         this.rewindFifo.clear();
      }

      this.in.skip(l);
   }

   private int pread() throws IOException {
      if (this.rewind) {
         int ret = this.in.read();
         this.rewindFifo.put((byte)ret);
         return ret;
      } else {
         return this.rewindFifo != null && this.rewindFifo.isAvailable() ? this.rewindFifo.read() : this.in.read();
      }
   }

   public void setStart(long off) throws IOException {
      this.reposition(off);
   }

   public int read() throws IOException {
      ++this.position;
      return this.pread();
   }

   public byte readByte() throws IOException {
      ++this.position;
      return (byte)this.pread();
   }

   public int readByteInt() throws IOException {
      ++this.position;
      return this.pread() & 255;
   }

   public int readCompressedInt() throws IOException {
      int value = 0;

      int next;
      do {
         next = this.readByteInt();
         value = value << 7 | next & 127;
      } while((next & 128) != 0);

      return value;
   }

   public void read(byte[] dst) throws IOException {
      for(int i = 0; i != dst.length; ++i) {
         dst[i] = (byte)this.pread();
      }

   }

   public byte[] read(int len) throws IOException {
      this.position += (long)len;
      byte[] ret = new byte[len];
      int pos = 0;
      if (this.rewindFifo != null) {
         while(this.rewindFifo.isAvailable()) {
            ret[pos] = this.rewindFifo.read();
            ++pos;
            if (pos == len) {
               return ret;
            }
         }
      }

      this.in.read(ret, pos, len - pos);
      return ret;
   }

   public byte[] readFully() throws IOException {
      return this.read(this.available());
   }

   public void read(byte[] dst, int off, int len) throws IOException {
      this.position += (long)len;
      this.in.read(dst, off, len);
   }

   public short readShort() throws IOException {
      this.position += 2L;
      return this.order == ByteOrder.LITTLE_ENDIAN ? (short)(this.pread() | this.pread() << 8) : (short)(this.pread() << 8 | this.pread());
   }

   public int readUShort() throws IOException {
      return this.readShort() & '\uffff';
   }

   public int readInt24() throws IOException {
      this.position += 3L;
      return this.order == ByteOrder.LITTLE_ENDIAN ? this.pread() | this.pread() << 8 | this.pread() << 16 : this.pread() << 16 | this.pread() << 8 | this.pread();
   }

   public int readUInt24() throws IOException {
      return this.readInt24() & 16777215;
   }

   public int readInt() throws IOException {
      this.position += 4L;
      return this.order == ByteOrder.LITTLE_ENDIAN ? this.pread() | this.pread() << 8 | this.pread() << 16 | this.pread() << 24 : this.pread() << 24 | this.pread() << 16 | this.pread() << 8 | this.pread();
   }

   public long readLong() throws IOException {
      this.position += 8L;
      return this.order == ByteOrder.LITTLE_ENDIAN ? (long)(this.pread() | this.pread() << 8 | this.pread() << 16 | this.pread() << 24 | this.pread() << 32 | this.pread() << 40 | this.pread() << 48 | this.pread() << 56) : (long)(this.pread() << 56 | this.pread() << 48 | this.pread() << 40 | this.pread() << 32 | this.pread() << 24 | this.pread() << 16 | this.pread() << 8 | this.pread());
   }

   public byte[] readBytes(int len) throws IOException {
      byte[] ret = new byte[len];

      for(int i = 0; i != len; ++i) {
         ret[i] = this.readByte();
      }

      return ret;
   }

   public short[] readShort(int len) throws IOException {
      short[] ret = new short[len];

      for(int i = 0; i != len; ++i) {
         ret[i] = this.readShort();
      }

      return ret;
   }

   public int[] readInt(int len) throws IOException {
      int[] ret = new int[len];

      for(int i = 0; i != len; ++i) {
         ret[i] = this.readInt();
      }

      return ret;
   }

   public void readInt(int[] arr) throws IOException {
      for(int i = 0; i != arr.length; ++i) {
         arr[i] = this.readInt();
      }

   }

   public long[] readLong(int len) throws IOException {
      this.position += (long)(len * 8);
      long[] ret = new long[len];

      for(int i = 0; i != len; ++i) {
         ret[i] = this.readLong();
      }

      return ret;
   }

   public String readNullTerminatedString() throws IOException {
      while(true) {
         byte val = this.readByte();
         if (val == 0) {
            String ret = new String(this.strReadBuf.getBackingBuffer(), 0, this.strReadBuf.size());
            this.strReadBuf.reset();
            return ret;
         }

         this.strReadBuf.write(val);
      }
   }

   public void readNullTerminatedString(String[] strings) throws IOException {
      for(int i = 0; i != strings.length; ++i) {
         long last = this.position;
         int i2 = -1;

         do {
            ++i2;
         } while(this.read() != 0);

         this.seek(last);
         strings[i] = new String(this.read(i2));
         this.skip(1L);
      }

   }

   public String readIntLengthUTF16() throws IOException {
      int length = this.readInt();
      char[] chars = new char[length];

      for(int i = 0; i != length; ++i) {
         chars[i] = (char)this.readShort();
      }

      return new String(chars);
   }

   public void close() throws IOException {
      this.in.close();
   }

   public int available() throws IOException {
      int rem = this.in.available();
      if (this.rewindFifo != null && !this.rewind) {
         rem += this.rewindFifo.available();
      }

      return rem;
   }

   public static class GoodByteArrayInputStream extends InputStream {
      byte[] bytes;
      int start;
      int length;
      int offset;

      public GoodByteArrayInputStream(byte[] bytes) {
         this(bytes, 0, bytes.length);
      }

      public GoodByteArrayInputStream(byte[] bytes, int start, int length) {
         if (start < 0) {
            throw new IllegalArgumentException("array start is negative");
         } else if (length < 0) {
            throw new IllegalArgumentException("array length is negative");
         } else if (bytes.length > start + length) {
            throw new ArrayIndexOutOfBoundsException("array start + length is superior to length");
         } else {
            this.bytes = bytes;
            this.start = start;
            this.length = length;
         }
      }

      public int available() throws IOException {
         return this.offset - this.length;
      }

      public long skip(long n) throws IOException {
         this.offset = (int)((long)this.offset + n);
         if (this.offset < 0) {
            throw new IOException("array underflow");
         } else if (this.start + this.offset > this.length) {
            throw new IOException("array overflow");
         } else {
            return n;
         }
      }

      public int read() throws IOException {
         if (this.start + this.offset >= this.length) {
            throw new IOException("array overflow");
         } else {
            return this.bytes[this.start + this.offset++] & 255;
         }
      }
   }
}
