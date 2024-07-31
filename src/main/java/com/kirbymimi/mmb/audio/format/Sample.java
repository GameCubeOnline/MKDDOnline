package com.kirbymimi.mmb.audio.format;

import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.stream.DataInStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteOrder;
import java.nio.file.Files;

public class Sample {
   int rate;
   short[][] vals;

   public int getRate() {
      return this.rate;
   }

   public short[][] getVals() {
      return this.vals;
   }

   public static Sample parseWave(File input) {
      try {
         byte[] data = Files.readAllBytes(input.toPath());
         return parseWave((InputStream)(new ByteArrayInputStream(data)));
      } catch (IOException var2) {
         return null;
      }
   }

   public static Sample parseWave(InputStream input) {
      Sample sample = new Sample();

      try {
         DataInStream in = new DataInStream(input);
         in.setOrder(ByteOrder.LITTLE_ENDIAN);
         in.skip(12L);
         if (in.readInt() == 1247104587) {
            in.skip(28L);
         }

         in.skip(4L);
         int format = in.readShort();
         int chanCnt = in.readShort();
         sample.rate = in.readInt();
         in.skip(6L);
         short sampleSize = in.readShort();

         while(in.readInt() != 1635017060) {
            in.skip((long)(in.readInt() + 4));
         }

         int len = in.readInt();
         int samplelen = len / chanCnt / (sampleSize >> 4);
         sample.vals = new short[chanCnt][samplelen / 2];
         switch(format) {
         case 1:
            switch(sampleSize) {
            case 8:
               parse8BitPCM(sample, in);
               break;
            case 16:
               parse16BitPCM(sample, in);
               break;
            case 24:
               parse24BitPCM(sample, in);
               break;
            case 32:
               parse32BitPCM(sample, in);
            }
         case 2:
         default:
            break;
         case 3:
            MMBSystem.fatalS((Object)"Float PCM not supported :(");
         }

         in.close();
      } catch (Exception var8) {
         MMBSystem.fatalS(var8);
      }

      return sample;
   }

   public static void parse8BitPCM(Sample sample, DataInStream in) throws IOException {
      int len = sample.vals[0].length;
      int len2 = sample.vals.length;

      for(int pos = 0; pos != len; ++pos) {
         for(int pos2 = 0; pos2 != len2; ++pos2) {
            sample.vals[pos2][pos] = (short)(in.readInt() << 8);
         }
      }

   }

   public static void parse16BitPCM(Sample sample, DataInStream in) throws IOException {
      int len = sample.vals[0].length;
      int len2 = sample.vals.length;

      for(int pos = 0; pos != len; ++pos) {
         for(int pos2 = 0; pos2 != len2; ++pos2) {
            sample.vals[pos2][pos] = in.readShort();
         }
      }

   }

   public static void parse24BitPCM(Sample sample, DataInStream in) {
      MMBSystem.fatalS((Object)"24 bit PCM not supported :(");
   }

   public static void parse32BitPCM(Sample sample, DataInStream in) throws IOException {
      int len = sample.vals[0].length;
      int len2 = sample.vals.length;

      for(int pos = 0; pos != len; ++pos) {
         for(int pos2 = 0; pos2 != len2; ++pos2) {
            sample.vals[pos2][pos] = (short)(in.readInt() >> 16);
         }
      }

   }
}
