package gcutil;

import com.kirbymimi.mmb.ut.BitUT;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class ASTFile {
   public static final int MAXBLOCKSIZE = 10080;
   public boolean pcm16;
   public int bitDepth;
   public int chanCount;
   public int sampleRate;
   public int sampleCount;
   public int loopStart;
   public int loopEnd;
   public short[] samples;

   public void cast(ASTFile src) {
      if (src.pcm16 != this.pcm16) {
         System.out.println("Converting to/from DPCM isn't supported");
         System.exit(-1);
      }

      if (src.chanCount != this.chanCount) {
         int i;
         if (src.chanCount == 1) {
            this.samples = new short[src.samples.length << 1];

            for(i = 0; i != src.samples.length; ++i) {
               this.samples[(i << 1) + 1] = this.samples[i << 1] = src.samples[i];
            }
         } else {
            this.samples = new short[src.samples.length >> 1];

            for(i = 0; i != this.samples.length; ++i) {
               this.samples[i] = (short)((src.samples[i << 1] + src.samples[(i << 1) + 1]) / 2);
            }
         }
      } else {
         this.samples = new short[src.samples.length];
         System.arraycopy(src.samples, 0, this.samples, 0, src.samples.length);
      }

      this.loopStart = src.loopStart;
      this.loopEnd = src.loopEnd;
      if (src.sampleRate != this.sampleRate) {
         short[] prev = this.samples;
         double rate = (double)this.sampleRate / (double)src.sampleRate;
         this.loopStart = (int)((double)this.loopStart * rate);
         this.loopEnd = (int)((double)this.loopEnd * rate);
         int len = (int)Math.floor((double)prev.length * rate);
         this.samples = new short[BitUT.alignHi(len, 32)];
         double prevPos = 0.0D;
         if (rate < 1.0D) {
            int i;
            if (this.chanCount == 2) {
               for(i = 0; (double)prev.length > prevPos + 1.0D; i += 2) {
                  this.samples[i] = prev[(int)prevPos];
                  this.samples[i + 1] = prev[(int)prevPos + 1];
                  prevPos += 1.0D / rate * 2.0D;
               }
            } else if (this.chanCount == 1) {
               for(i = 0; (double)prev.length > prevPos; ++i) {
                  this.samples[i] = prev[(int)prevPos * this.chanCount];
                  prevPos += 1.0D / rate;
               }
            } else {
               for(i = 0; i != len / this.chanCount; ++i) {
                  for(int i2 = 0; i2 != this.chanCount; ++i2) {
                     this.samples[i * this.chanCount + i2] = prev[(int)prevPos * this.chanCount + i2];
                  }

                  prevPos += 1.0D / rate;
               }
            }
         } else {
            System.out.println("TODO");
            System.exit(-1);
         }
      }

   }

   public void load(DataInputStream in) {
      try {
         in.skip(8L);
         this.pcm16 = in.readShort() == 1;
         this.bitDepth = in.readShort();
         this.chanCount = in.readShort();
         in.skip(2L);
         this.sampleRate = in.readInt();
         this.sampleCount = in.readInt();
         this.loopStart = in.readInt();
         this.loopEnd = in.readInt();
         this.samples = new short[BitUT.alignHi(this.chanCount * this.sampleCount, 32)];
         int currSample = 0;
         in.skip(32L);

         while(this.samples.length > currSample && in.available() > 0) {
            in.skip(4L);
            int blkLen = in.readInt();
            in.skip(24L);
            if (blkLen + currSample > this.samples.length) {
               blkLen = this.samples.length - currSample;
            }

            if (blkLen > in.available() / this.chanCount) {
               blkLen = in.available() / this.chanCount;
            }

            for(int i2 = 0; i2 != this.chanCount; ++i2) {
               int currSample2 = currSample + i2;

               for(int i = 0; i != blkLen / this.chanCount; ++i) {
                  this.samples[currSample2] = this.bitDepth == 8 ? (short)(in.readByte() << 8) : in.readShort();
                  currSample2 += this.chanCount;
               }
            }

            currSample += blkLen;
         }
      } catch (Exception var7) {
         var7.printStackTrace();
         System.out.println("Bad AST file");
         System.exit(-1);
      }

   }

   public void save(DataOutputStream out) {
      boolean var3 = false;

      try {
         out.writeInt(1398035021);
         int blocksCount = this.samples.length / 10080;
         if (this.samples.length % 10080 != 0) {
            ++blocksCount;
         }

         int dataLen = this.samples.length;
         if (this.bitDepth == 16) {
            dataLen *= 2;
         }

         dataLen += blocksCount * 32;
         out.writeInt(dataLen);
         out.writeShort(this.pcm16 ? 1 : 0);
         out.writeShort(this.bitDepth);
         out.writeShort(this.chanCount);
         out.writeShort(65535);
         out.writeInt(this.sampleRate);
         out.writeInt(this.samples.length / this.chanCount);
         out.writeInt(this.loopStart);
         out.writeInt(this.loopEnd);
         out.writeInt(10080);
         out.writeInt(0);
         out.writeInt(2130706432);

         int i;
         for(i = 0; i != 5; ++i) {
            out.writeInt(0);
         }

         int remLen = this.samples.length;
         int cSample = 0;

         for(i = 0; i != blocksCount; ++i) {
            out.writeInt(1112294219);
            int len = remLen;
            if (remLen > 10080) {
               len = 10080;
            }

            out.writeInt(len);

            int i3;
            for(i3 = 0; i3 != 6; ++i3) {
               out.writeInt(0);
            }

            for(i3 = 0; i3 != this.chanCount; ++i3) {
               int cSample2 = cSample + i3;
               int i2;
               if (this.bitDepth == 8) {
                  for(i2 = 0; i2 != len / this.chanCount; ++i2) {
                     out.write(this.samples[cSample2]);
                     cSample2 += this.chanCount;
                  }
               }

               if (this.bitDepth == 16) {
                  for(i2 = 0; i2 != len / this.chanCount; ++i2) {
                     out.writeShort(this.samples[cSample2]);
                     cSample2 += this.chanCount;
                  }
               }
            }

            remLen -= len;
            cSample += len;
         }
      } catch (Exception var11) {
         var11.printStackTrace();
         System.out.println("Bad AST file");
         System.exit(-1);
      }

   }
}
