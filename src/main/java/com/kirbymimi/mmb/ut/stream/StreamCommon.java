package com.kirbymimi.mmb.ut.stream;

import com.kirbymimi.mmb.ut.BitUT;
import java.io.IOException;
import java.nio.ByteOrder;
import java.util.Stack;

abstract class StreamCommon {
   ByteOrder order;
   long position;
   Stack<Long> baseSeekOffStack;
   Stack<Long> readBytesStack;
   long baseSeekOff;

   StreamCommon() {
      this.order = ByteOrder.BIG_ENDIAN;
      this.baseSeekOffStack = new Stack();
      this.readBytesStack = new Stack();
   }

   public void setOrder(ByteOrder newOrder) {
      this.order = newOrder;
   }

   abstract void reposition(long var1) throws IOException;

   public void skip(long len) throws IOException {
      this.position += len;
      this.reposition(len);
   }

   public void seek(long off) throws IOException {
      this.reposition(off - (this.position - this.baseSeekOff));
      this.position = off;
   }

   public void align(long align) throws IOException {
      this.seek(BitUT.alignHi(this.position, align));
   }

   public void pushBaseSeekOff() throws IOException {
      this.pushBaseSeekOff(this.position);
   }

   public void pushBaseSeekOff(long off) throws IOException {
      this.seek(off);
      this.baseSeekOffStack.push(this.baseSeekOff);
      this.readBytesStack.push(this.position);
      this.baseSeekOff = off;
      this.position = 0L;
   }

   public void popBaseSeekOff() throws IOException {
      this.reposition(-this.position);
      this.position = this.baseSeekOff;
      this.baseSeekOff = (Long)this.baseSeekOffStack.pop();
   }

   public long off() {
      return this.position;
   }
}
