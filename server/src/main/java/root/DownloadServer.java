package root;

import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.SynchronousQueue;

public class DownloadServer extends Thread {
   public SynchronousQueue<DownloadServer.Message> messageQueue = new SynchronousQueue();
   byte[] transfer = new byte[1032];
   DatagramSocket socket;
   DatagramPacket pack = new DatagramPacket(new byte[0], 0);
   DownloadServer.Message msg;
   boolean stop;
   Server server;

   public DownloadServer(Server server) {
      this.server = server;
   }

   public void run() {
      try {
         this.socket = new DatagramSocket();
      } catch (Exception var2) {
         System.out.println("cannot create download server socket");
         System.exit(-1);
      }

      while(!this.stop) {
         try {
            this.msg = (DownloadServer.Message)this.messageQueue.take();
            int i;
            if (this.msg.downloadType.compareTo("TCP") == 0) {
               for(i = 0; i != this.msg.data.length / 1024; ++i) {
                  this.sendBytesTCP(this.msg.target, this.msg.data, i * 1024, 1024);
               }

               if (this.msg.data.length % 1024 != 0) {
                  this.sendBytesTCP(this.msg.target, this.msg.data, i * 1024, this.msg.data.length - i * 1024);
               }
            } else {
               this.pack.setAddress(this.msg.target.sock.getInetAddress());
               this.pack.setPort(this.msg.target.server.port);

               for(i = 0; i != this.msg.data.length / 1024; ++i) {
                  this.sendBytes(this.msg.target, this.msg.data, i * 1024, 1024, i);
               }

               if (this.msg.data.length % 1024 != 0) {
                  this.sendBytes(this.msg.target, this.msg.data, i * 1024, this.msg.data.length - i * 1024, i);
               }

               this.msg.doneTime = System.currentTimeMillis();
            }
         } catch (Exception var3) {
         }
      }

      if (this.socket != null) {
         this.socket.close();
      }

   }

   void sendBytes(ServerUser user, byte[] data, int off, int len, int idx) throws IOException {
      if (!this.msg.confirmed[idx]) {
         try {
            Thread.sleep((long)user.getDownloadSpeed());
         } catch (Exception var8) {
         }

         int checkSum = 0;

         for(int i = off; i != off + len; ++i) {
            checkSum += data[i] & 255;
         }

         System.arraycopy(data, off, this.transfer, 8, len);
         this.transfer[4] = (byte)((idx & -16777216) >> 24);
         this.transfer[5] = (byte)((idx & 16711680) >> 16);
         this.transfer[6] = (byte)((idx & '\uff00') >> 8);
         this.transfer[7] = (byte)((idx & 255) >> 0);
         checkSum += (this.transfer[4] & 255) + (this.transfer[5] & 255) + (this.transfer[6] & 255) + (this.transfer[7] & 255);
         this.transfer[0] = (byte)((checkSum & -16777216) >> 24);
         this.transfer[1] = (byte)((checkSum & 16711680) >> 16);
         this.transfer[2] = (byte)((checkSum & '\uff00') >> 8);
         this.transfer[3] = (byte)((checkSum & 255) >> 0);
         this.pack.setData(this.transfer, 0, len + 8);
         this.socket.send(this.pack);
      }
   }

   void sendBytesTCP(ServerUser targ, byte[] data, int off, int len) throws IOException {
      while(System.nanoTime() - targ.lastPingTime >= this.server.timeOut * 100000000L) {
         try {
            Thread.sleep((long)targ.getDownloadSpeed());
         } catch (Exception var7) {
         }
      }

      targ.sendPacket(new Packet.TCPDownload(data, off, len));

      try {
         Thread.sleep((long)targ.getDownloadSpeed());
      } catch (Exception var6) {
      }

   }

   public static class BitStream {
      OutputStream dst;
      byte cVal;
      int rem = 8;

      BitStream(OutputStream dst) {
         this.dst = dst;
      }

      public void write(int len, int v) throws IOException {
         if (len <= this.rem) {
            this.cVal = (byte)(this.cVal | v << this.rem - len);
            this.rem -= len;
         } else {
            int sub = len - this.rem;
            this.write(this.rem, v >> len - this.rem);
            this.dst.write(this.cVal);
            this.rem = 8;
            this.cVal = 0;
            this.write(sub, v);
         }
      }

      public void Close(int al) throws IOException {
         if (this.rem == 0) {
            this.dst.write(this.cVal);
         }

         while(this.rem != 0) {
            this.write(al, 0);
         }

      }
   }

   public static class Compressor {
      int inPos;
      int outPos;
      byte[] ret;
      byte[] temp;
      int tempPos = 0;
      int badValueIdx = 0;
      DownloadServer.BitStream stream;

      public byte[] losslessCompress(byte[] data) {
         this.inPos = 0;
         this.outPos = 3;
         this.ret = new byte[data.length + 1];
         this.temp = new byte[data.length];
         this.ret[0] = 1;
         this.ret[3] = data[0];
         this.ret[4] = data[1];
         short lastVal = (short)(data[0] << 8 | data[1]);

         for(int i = 2; i != data.length; i += 2) {
            short nextVal = (short)(data[i] << 8 | data[i + 1]);
            int dist = nextVal - lastVal;
            if (dist <= 4095) {
            }

            ++this.badValueIdx;
            lastVal = nextVal;
         }

         return data;
      }
   }

   public static class Message {
      byte[] data;
      boolean[] confirmed;
      ServerUser target;
      long doneTime;
      String downloadType;

      public Message(byte[] data, ServerUser target) {
         this.downloadType = "TCP";
         this.data = data;
         this.target = target;
         this.confirmed = new boolean[(data.length + 1023) / 1024];
      }

      public Message(byte[] data, ServerUser target, String downloadType) {
         this(data, target);
         this.downloadType = downloadType;
      }

      public void Send() {
         try {
            this.target.dlServer.messageQueue.put(this);
         } catch (Exception var2) {
         }

      }
   }
}
