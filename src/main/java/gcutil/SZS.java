package gcutil;

import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.stream.DataInStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class SZS {
   public static byte[] decompressRoutine(DataInStream input) {
      byte cb = 0;
      int rb = 0;
      boolean var6 = false;

      try {
         input.enableRewind();
         if (input.readInt() != 1499560496) {
            input.disableRewind();
            return input.readFully();
         } else {
            input.disableRewind();
            input.skip(4L);
            ByteBuffer out = ByteBuffer.allocate(input.readInt());
            input.skip(8L);

            while(out.position() != out.capacity()) {
               if (rb == 0) {
                  cb = input.readByte();
                  rb = 8;
               }

               if ((cb & 128) != 0) {
                  out.put(input.readByte());
               } else {
                  int wb = input.readByte();
                  int gb = (wb & 15) << 8;
                  gb |= input.readByte() & 255;
                  int len;
                  if ((wb & 240) == 0) {
                     len = (input.readByte() & 255) + 18;
                  } else {
                     len = ((wb & 240) >> 4) + 2;
                  }

                  int bPos = out.position();
                  out.position(bPos - gb - 1);

                  for(int i = 0; i != len; ++i) {
                     out.put(bPos, out.get());
                     ++bPos;
                  }

                  out.position(bPos);
               }

               cb = (byte)(cb << 1);
               --rb;
            }

            input.close();
            FileOutputStream poubelle = new FileOutputStream("ntm.bin");
            poubelle.write(out.array());
            poubelle.close();
            return out.array();
         }
      } catch (Exception var9) {
         MMBSystem.fatalS((Object)"Couldn't decompress the szz.");
         return null;
      }
   }
}
