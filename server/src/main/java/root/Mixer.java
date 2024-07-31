package root;

import com.kirbymimi.mmb.audio.format.Sample;
import com.kirbymimi.mmb.system.KThread;
import com.kirbymimi.mmb.ut.stream.DataOutStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.Vector;

public class Mixer {
   boolean enabled = true;
   boolean enableCompress = true;
   public static final int FREQ = 16000;
   public static final double FREQD = 16000.0D;
   public static final int PPS = 10;
   public static Sample currentStream;
   public Server server;
   static OutputStream debugOut;
   static volatile boolean initialized = false;

   public void setEnableCompress(boolean compress) {
      this.enableCompress = compress;
   }

   public static void init() {
      if (!initialized) {
         initialized = true;
         (new KThread() {
            public void run() {
               this.setName("Mixer heartbeat thread");
               Thread.currentThread().setPriority(10);

               while(true) {
                  while(true) {
                     try {
                        Thread.sleep(100L);
                        Iterator var2 = Server.servers.iterator();

                        while(var2.hasNext()) {
                           Server server = (Server)var2.next();

                           ServerUser u;
                           for(Iterator var4 = server.list.iterator(); var4.hasNext(); u.voiceReq = true) {
                              u = (ServerUser)var4.next();
                           }
                        }
                     } catch (Exception var5) {
                     }
                  }
               }
            }
         }).start();
      }
   }

   public Mixer(Server server) {
      this.server = server;
   }

   public void addVoice(byte[] data, Voice source, int rate) {
      if (this.enabled) {
         data = this.resample(data, (double)rate);
         Iterator var5 = this.server.list.iterator();

         while(var5.hasNext()) {
            ServerUser u = (ServerUser)var5.next();
            if (u.voice != source) {
               u.voice.addBytes(source, data);
            }
         }

      }
   }

   public byte[] resample(byte[] data, double rate) {
      double d = (double)data.length * (16000.0D / rate);
      if (Math.floor(d) != d) {
         d = (double)(((int)d & -2) + 2);
      }

      byte[] dst = new byte[(int)d];
      int cPos = 0;
      double clock = 0.0D;
      double clockRate = rate / 16000.0D;

      try {
         int i = 0;

         while(i != data.length) {
            dst[cPos] = data[i];
            dst[cPos + 1] = data[i + 1];
            cPos += 2;

            for(clock += clockRate; clock >= 1.0D; i += 2) {
               --clock;
            }
         }
      } catch (Exception var13) {
      }

      return dst;
   }

   public void Mix(ServerUser dst, DataOutStream out) throws IOException {
      Vector<byte[]> datas = new Vector();
      Iterator var5 = dst.server.list.iterator();

      while(var5.hasNext()) {
         ServerUser u = (ServerUser)var5.next();
         byte[] data = dst.voice.get(u.voice, 3200);
         if (data != null) {
            datas.add(data);
         }
      }

      byte[] data = dst.voice.get(this.server.streamVoice, 3200);
      if (data != null) {
         datas.add(data);
      }

      if (datas.size() != 0) {
         int[] sums = new int[3200];
         Iterator var11 = datas.iterator();

         while(var11.hasNext()) {
             data = (byte[]) var11.next();

            for(int i = 0; i != data.length; i += 2) {
               sums[i >> 1] += (short)(data[i + 1] & 255 | data[i] << 8 & '\uff00');
            }
         }

         out.write((int)5);
         if (this.enableCompress) {
            this.Compress4Bit(dst, sums, datas.size(), out);
         } else {
            this.noCompress(dst, sums, datas.size(), out);
         }

      }
   }

   public void noCompress(ServerUser dst, int[] sums, int chanCnt, DataOutStream out) throws IOException {
      out.write((int)0);
      out.writeInt(sums.length);
      long div = 4294967295L;
      int[] var10 = sums;
      int var9 = sums.length;

      for(int var8 = 0; var8 < var9; ++var8) {
         int i = var10[var8];
         i *= 2;
         i = (int)((long)i * div >> 32);
         if (i > 32767) {
            i = 32767;
         }

         if (i < -32767) {
            i = -32767;
         }

         i >>= 8;
         out.write((byte)i);
      }

   }

   public void Compress3Bit(ServerUser dst, int[] sums, int chanCnt, DataOutStream out) throws IOException {
      ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
      Mixer.BitStream bits = new Mixer.BitStream(tempOut);
      short cVal = dst.mixerCVal;
      long div = (long)(4.294967295E9D / Math.pow((double)chanCnt, 0.6D)) & 4294967295L;
      int[] var13 = sums;
      int var12 = sums.length;

      for(int var11 = 0; var11 < var12; ++var11) {
         int i = var13[var11];
         i = (int)((long)i * div >> 32);
         if (i > 32767) {
            i = 32767;
         }

         if (i < -32768) {
            i = -32768;
         }

         i >>= 8;
         if (cVal == i) {
            bits.write(3, 0);
         } else {
            int Sub = i - cVal;
            if (Sub > 3) {
               Sub = 3;
            } else if (Sub < -4) {
               Sub = -4;
            }

            cVal = (short)(cVal + Sub);
            if (Sub < 0) {
               Sub = -Sub + 3;
            }

            bits.write(3, Sub);
         }
      }

      dst.mixerCVal = cVal;
      bits.Close(3);
      tempOut.close();
      out.writeInt(sums.length);
      out.write(tempOut.toByteArray());
   }

   public void Compress4Bit(ServerUser dst, int[] sums, int chanCnt, DataOutStream out) throws IOException {
      out.write((int)1);
      ByteArrayOutputStream tempOut = new ByteArrayOutputStream();
      Mixer.BitStream bits = new Mixer.BitStream(tempOut);
      short cVal = 0;
      long div = (long)(4.294967295E9D / Math.pow((double)chanCnt, 0.6D)) & 4294967295L;
      int[] var13 = sums;
      int var12 = sums.length;

      for(int var11 = 0; var11 < var12; ++var11) {
         int i = var13[var11];
         i *= 2;
         i = (int)((long)i * div >> 32);
         if (i > 32767) {
            i = 32767;
         }

         if (i < -32767) {
            i = -32767;
         }

         i >>= 9;
         if (cVal == i) {
            bits.write(4, 0);
         } else {
            int sub = i - cVal;
            if (sub > 7) {
               sub = 7;
            } else if (sub < -8) {
               sub = -8;
            }

            cVal = (short)(cVal + sub);
            if (sub < 0) {
               sub = -sub + 7;
            }

            bits.write(4, sub);
         }
      }

      dst.mixerCVal = cVal;
      bits.Close(4);
      tempOut.close();
      out.writeInt(sums.length);
      out.write(tempOut.toByteArray());
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
}
