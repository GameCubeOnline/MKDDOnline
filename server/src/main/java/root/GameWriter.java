package root;

import com.kirbymimi.mmb.ut.stream.DataOutStream;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class GameWriter {
   ServerUser user;
   DataOutStream out;
   ReentrantLock packetLock = new ReentrantLock();
   Vector<Packet> packets = new Vector();

   public GameWriter(ServerUser user) {
      this.user = user;
      this.out = user.out;
   }

   public void run() throws Exception {
      this.out = this.user.out;
      this.packetLock.lock();
      Object[] o = this.packets.toArray();
      this.packets.clear();
      this.packetLock.unlock();
      Packet[] packs = new Packet[o.length];

      for(int i = 0; i != o.length; ++i) {
         packs[i] = (Packet)o[i];
      }

      Packet[] var6 = packs;
      int var5 = packs.length;

      for(int var4 = 0; var4 < var5; ++var4) {
         Packet pack = var6[var4];
         if (pack.target != null) {
            if (pack.nTarget) {
               if (pack.target == this.user) {
                  continue;
               }
            } else if (pack.target != this.user) {
               continue;
            }
         }

         pack.write(this.out, this.user);
      }

      this.user.sendBuffer();
   }
}
