package root;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class Voice {
   Server server;
   public HashMap<Voice, Vector<byte[]>> voiceMap = new HashMap();

   public Voice(Server server) {
      this.server = server;
   }

   void addBytes(Voice source, byte[] data) {
      this.server.voiceLock.lock();
      Vector<byte[]> bytes = (Vector)this.voiceMap.get(source);
      if (bytes == null) {
         bytes = new Vector();
         this.voiceMap.put(source, bytes);
      }

      bytes.add(data);
      this.server.voiceLock.unlock();
   }

   byte[] get(Voice src, int size) {
      size <<= 1;
      this.server.voiceLock.lock();
      int len = 0;
      Vector<byte[]> voiceBytes = (Vector)this.voiceMap.get(src);
      if (voiceBytes == null) {
         this.server.voiceLock.unlock();
         return null;
      } else {
         Iterator var6 = voiceBytes.iterator();

         while(var6.hasNext()) {
            byte[] b = (byte[])var6.next();
            len += b.length;
            if (len >= size) {
               break;
            }
         }

         if (len < size) {
            this.server.voiceLock.unlock();
            return null;
         } else {
            int cPos = 0;
            int rem = size;
            byte[] ret = new byte[size];

            while(true) {
               byte[] cByte = (byte[])voiceBytes.elementAt(0);
               voiceBytes.remove(0);
               if (rem <= cByte.length) {
                  byte[] NByte = new byte[cByte.length - rem];
                  System.arraycopy(cByte, rem, NByte, 0, cByte.length - rem);
                  voiceBytes.add(0, NByte);
                  System.arraycopy(cByte, 0, ret, cPos, rem);
                  this.server.voiceLock.unlock();
                  return ret;
               }

               System.arraycopy(cByte, 0, ret, cPos, cByte.length);
               cPos += cByte.length;
               rem -= cByte.length;
            }
         }
      }
   }
}
