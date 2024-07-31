package com.kirbymimi.mmb.ut.stream;

import java.io.IOException;
import java.io.InputStream;

public class RewindableInputStream extends InputStream {
   InputStream source;
   ByteFifo rewindFifo = new ByteFifo();
   boolean rewind;

   public RewindableInputStream(InputStream source) {
      this.source = source;
   }

   public void enableRewind() {
      this.rewind = true;
   }

   public void disableRewind() {
      this.rewind = false;
   }

   public int read() throws IOException {
      if (this.rewind) {
         int ret = this.source.read();
         this.rewindFifo.put((byte)ret);
         return ret;
      } else {
         return this.rewindFifo.isAvailable() ? this.rewindFifo.read() : this.source.read();
      }
   }

   public long skip(long n) throws IOException {
      return this.source.skip(n);
   }

   public void close() throws IOException {
      this.source.close();
   }

   public synchronized void mark(int readlimit) {
      this.source.mark(readlimit);
   }

   public synchronized void reset() throws IOException {
      this.source.reset();
   }

   public boolean markSupported() {
      return this.source.markSupported();
   }
}
