package root;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Vector;

public class GameReader {
   public ServerUser user;
   public Server server;
   public DataInputStream in;
   public static final GameReader.PacketsHandler[] RcvPackets = new GameReader.PacketsHandler[]{(u) -> {
      return u.handleSync();
   }, (u) -> {
      return u.reader.handleMessage();
   }, (u) -> {
      return u.reader.handleHeartBeat();
   }, (u) -> {
      return u.reader.handleMic();
   }, (u) -> {
      return u.reader.handleDownloadStatus();
   }, (u) -> {
      return u.reader.handlePing();
   }};

   public GameReader(ServerUser user) {
      this.user = user;
      this.server = user.server;
   }

   public void run() throws Exception {
      if (this.in == null) {
         this.in = new DataInputStream(this.user.sock.getInputStream());
      }

      Vector<Packet> packs = new Vector();
      boolean var2 = true;

      while(true) {
         byte next;
         try {
            this.user.sock.setSoTimeout(1);
            next = this.in.readByte();
            this.user.sock.setSoTimeout(0);
         } catch (EOFException var4) {
            throw var4;
         } catch (SocketException var5) {
            throw var5;
         } catch (Exception var6) {
            break;
         }

         if (next == -1) {
            break;
         }

         Packet pack;
         if (next >= 16) {
            pack = this.server.getRcvPackets()[next - 16].exec(this.user);
         } else {
            pack = RcvPackets[next].exec(this.user);
         }

         if (pack != null) {
            pack.src = this.user;
            packs.add(pack);
         }

         this.user.skippedTime = 0;
      }

      if (this.user.voiceReq) {
         packs.add(new Packet.Mixer());
         this.user.voiceReq = false;
      }

      this.server.sendPacket(packs);
   }

   public Packet handleMessage() throws Exception {
      short len = this.in.readShort();
      if (len > 100) {
         throw new Exception();
      } else {
         String cont = this.user.readString(len);
         if (cont.charAt(0) == '/') {
            this.server.processCommand(cont.substring(1).trim().replaceAll(" +", " "), this.user);
            return null;
         } else {
            String msg = this.user.name + ":" + cont;
            this.server.userPrint(msg);
            return new Packet.Message(msg);
         }
      }
   }

   public Packet handleHeartBeat() throws Exception {
      this.user.skippedTime = 0;
      return null;
   }

   public Packet handleMic() throws Exception {
      int len = this.in.readInt();
      System.out.println(len);
      byte[] buf = new byte[len];
      this.in.readFully(buf);
      this.server.mixer.addVoice(buf, this.user.voice, 11025);
      return null;
   }

   public Packet handleDownloadStatus() throws Exception {
      if (this.in.read() == 1) {
         this.server.debugPrint("ALL PACKETS ARRIVED");
         this.user.currDownload = null;
         return null;
      } else {
         boolean[] sendVals = this.user.currDownload.confirmed;
         byte[] rcv = new byte[sendVals.length + 7 >> 3];
         this.in.readFully(rcv);

         for(int i = 0; i != sendVals.length; ++i) {
            byte bool = rcv[i >> 3];
            sendVals[i] = (bool & 1 << (i & 7)) != 0;
         }

         double tcnt = 0.0D;
         boolean[] var8 = sendVals;
         int var7 = sendVals.length;

         for(int var6 = 0; var6 < var7; ++var6) {
            boolean b = var8[var6];
            if (b) {
               ++tcnt;
            }
         }

         this.server.debugPrint(tcnt / (double)sendVals.length * 100.0D + "% PACKETS ARRIVED");
         this.user.currDownload.Send();
         return null;
      }
   }

   public Packet handlePing() {
      if (this.user.lastPingTime != 0L) {
         this.user.addPing(System.nanoTime() - this.user.lastPingTime);
      }

      this.user.lastPingTime = System.nanoTime();
      Packet ping = new Packet.Ping();
      ping.target = this.user;
      return ping;
   }

   public interface PacketsHandler {
      Packet exec(ServerUser var1) throws Exception;
   }

   public static class TrackedInputStream extends InputStream {
      InputStream src;
      OutputStream out;

      public TrackedInputStream(InputStream src) {
         this.src = src;

         try {
            this.out = new FileOutputStream("trackedInput.bin");
         } catch (Exception var3) {
         }

      }

      public int read() throws IOException {
         int i = this.src.read();
         this.out.write(i);
         this.out.flush();
         return i;
      }

      public void close() throws IOException {
         System.out.println("closing tracked stream");
         this.out.write(222);
         this.out.write(173);
         this.out.write(190);
         this.out.write(239);
         this.src.close();
         this.out.close();
      }
   }
}
