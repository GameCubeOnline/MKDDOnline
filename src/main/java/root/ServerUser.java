package root;

import com.kirbymimi.mmb.res.ResourceContainer;
import com.kirbymimi.mmb.system.KThread;
import com.kirbymimi.mmb.ut.Sleeper;
import com.kirbymimi.mmb.ut.TaskChain;
import com.kirbymimi.mmb.ut.stream.DataOutStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.Vector;

public class ServerUser extends KThread {
   public static Vector<String> randomNames = new Vector();
   public static final int TIMEOUT = 5000;
   public static final int PINGBUFFERSIZE = 100;
   Socket sock;
   DataInputStream in;
   DataOutStream out;
   ByteArrayOutputStream outB;
   String name = "An user";
   boolean exit;
   GameReader reader;
   GameWriter writer = new GameWriter(this);
   int id = -1;
   volatile boolean joined;
   Server server;
   DownloadServer.Message currDownload;
   Stack<DownloadServer.Message> downloadQueue = new Stack();
   DownloadServer dlServer;
   Vector<TaskChain<ServerUser>> tasks = new Vector();
   HashSet<String> permissions = new HashSet();
   ResourceContainer resourceContainer = new ResourceContainer();
   public int[] pingBuffer = new int[100];
   volatile boolean invalidateReq;
   volatile int skippedTime = 0;
   volatile boolean userSync;
   public int lastTime;
   public int raceStatus;
   public int controllerData;
   public boolean kartDataReceived;
   public long lastPingTime = 0L;
   public volatile boolean voiceReq;
   public short mixerCVal;
   Voice voice;
   Sleeper sleeper;
   int downloadSpeed = 6;

   static {
      try {
         Scanner scan = new Scanner(newStream("NameList.txt"));

         while(scan.hasNextLine()) {
            randomNames.add(scan.nextLine());
         }

         scan.close();
      } catch (Exception var1) {
      }

   }

   static void create(Socket sock, Server server) throws SocketException {
      ServerUser ret = server.createUser();
      ret.sock = sock;
      sock.setTcpNoDelay(true);
      ret.server = server;
      ret.reader = new GameReader(ret);
      ret.downloadSpeed = server.downloadSpeed;
      ret.dlServer = new DownloadServer(server);
      ret.dlServer.start();
      ret.start();
   }

   ServerUser.ServerErrorCodes create() {
      try {
         this.in = new DataInputStream(this.sock.getInputStream());
         this.out = new DataOutStream(this.outB = new ByteArrayOutputStream());
         if (this.in.readUTF().compareTo(this.server.getVersion()) != 0) {
            return ServerUser.ServerErrorCodes.INVALIDVERSION;
         }

         if (this.server.bans.isBanned(this.sock.getInetAddress())) {
            return ServerUser.ServerErrorCodes.BANNED;
         }

         if (this.server.list.size() + this.server.joinList.size() >= this.server.maxPlayer()) {
            return ServerUser.ServerErrorCodes.FULL;
         }

         this.out.write((int)0);
         this.sendBuffer();
         this.server.joinLock.lock();
         this.name = this.in.readUTF();
         this.server.userJoin(this);
         this.sendBuffer();
         byte[] nameTransform = new byte[this.name.length()];
         int nameTransformPos = 0;
         byte[] var6;
         int var5 = (var6 = this.name.getBytes()).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            byte b = var6[var4];
            if (b >= 97 && b <= 122 || b >= 65 && b <= 90 || b >= 48 && b <= 57) {
               nameTransform[nameTransformPos++] = b;
            }
         }

         this.name = new String(nameTransform, 0, nameTransformPos);
         if (this.name.isEmpty() && randomNames.size() > 0) {
            this.name = null;
            Random rand = new Random();

            while(this.name == null) {
               String Name = (String)randomNames.get(rand.nextInt(randomNames.size()));
               if (this.server.get(Name) == null) {
                  this.name = Name;
               }
            }
         }

         this.server.userPrint("User " + this.name + " Joined.");
         this.setName(this.name + " User thread");
         this.dlServer.setName(this.name + " download thread");
         this.server.joinList.add(this);
         this.server.joinLock.unlock();
         this.voice = new Voice(this.server);
         this.sleeper = new Sleeper(this.server.rate);
         return ServerUser.ServerErrorCodes.GOOD;
      } catch (SocketTimeoutException var8) {
         this.server.timeOutError(this.name);
      } catch (Exception var9) {
         try {
            this.server.joinLock.unlock();
         } catch (Exception var7) {
         }
      }

      return ServerUser.ServerErrorCodes.UNK;
   }

   public void addPing(long ping) {
      int pingI = (int)(ping / 1000000L);

      for(int i = 0; i != this.pingBuffer.length - 1; ++i) {
         this.pingBuffer[i] = this.pingBuffer[i + 1];
      }

      this.pingBuffer[this.pingBuffer.length - 1] = pingI;
   }

   public int getPing() {
      int max = 0;
      int[] var5;
      int div = (var5 = this.pingBuffer).length;

      for(int var3 = 0; var3 < div; ++var3) {
         int i = var5[var3];
         if (i > max) {
            max = i;
         }
      }

      if (max < 0) {
         max = 0;
      }

      if (max > 1000) {
         max = 1000;
      }

      long l = 0L;
      div = 100;
      int[] var8;
      int var7 = (var8 = this.pingBuffer).length;

      int diff;
      int ret;
      for(diff = 0; diff < var7; ++diff) {
         ret = var8[diff];
         l += (long)ret;
         if (ret == 0) {
            --div;
         }
      }

      if (div == 0) {
         return 1;
      } else {
         ret = (int)(l / (long)div);
         if (ret < 0) {
            ret = 0;
         }

         if (ret > 1000) {
            ret = 1000;
         }

         diff = max - ret;
         if (diff < 20) {
            return ret;
         } else if (diff < 40) {
            return max;
         } else {
            return ret + 40;
         }
      }
   }

   void leave() {
      this.server.joinLock.lock();

      try {
         if (!this.joined) {
            this.server.joinList.remove(this);
         } else {
            this.server.list.remove(this);
         }

         this.sock.close();
         Vector<Packet> packs = new Vector();
         packs.add(this.getLeavePacket());
         this.server.syncLock.lock();
         if (this.server.syncCnt != 0 && this.userSync) {
            --this.server.syncCnt;
         }

         Packet pack = this.server.syncUpdate();
         if (pack != null) {
            packs.add(pack);
         }

         this.server.sendPacket(packs);
         this.server.sendMessage((ServerUser)null, (ServerUser)null, this.getUserName() + " left the game", "");

         ServerUser u;
         for(Iterator var4 = this.server.list.iterator(); var4.hasNext(); u.lastPingTime = 0L) {
            u = (ServerUser)var4.next();
         }

         this.dlServer.stop = true;
         this.dlServer.interrupt();
         this.server.userLeave(this);
      } catch (Exception var5) {
         var5.printStackTrace();
      }

      this.server.joinLock.unlock();
   }

   public Packet getLeavePacket() {
      return new Packet.Leave(this);
   }

   public void writeNullTerminatedString(String str) throws IOException {
      if (str != null) {
         this.out.write(str.getBytes());
      }

      this.out.write((byte)0);
   }

   public String readString(int len) throws IOException {
      byte[] str = new byte[len];

      for(int i = 0; i != len; ++i) {
         str[i] = this.in.readByte();
      }

      return new String(str);
   }

   public void writeBigData(byte[] data) throws IOException {
      if (data.length > 8192) {
         int i;
         for(i = 0; i != data.length / 8192; ++i) {
            this.out.write(data, i * 8192, 8192);
            this.sendBuffer();

            try {
               Thread.sleep(1L);
            } catch (Exception var4) {
            }
         }

         this.out.write(data, i * 8192, data.length - i * 8192);
      } else {
         this.out.write(data);
      }

   }

   public void writeRLE4(byte[] data) throws IOException {
      DataInputStream in = new DataInputStream(new ByteArrayInputStream(data));
      int rem = data.length / 4;
      int var4 = 0;

      while(rem != 0) {
         int i1;
         if (rem < 4) {
            for(i1 = 0; i1 != rem; ++i1) {
               this.out.writeInt(in.readInt());
            }

            return;
         }

         i1 = in.readInt();
         int i2 = in.readInt();
         if (i1 != i2) {
            this.rleWritePontentialToken(i1);
            this.rleWritePontentialToken(i2);
            var4 += 8;
            rem -= 2;
         } else {
            int i3 = in.readInt();
            if (i1 != i3) {
               this.rleWritePontentialToken(i1);
               this.rleWritePontentialToken(i2);
               this.rleWritePontentialToken(i3);
               var4 += 12;
               rem -= 3;
            } else {
               int i4 = in.readInt();
               if (i1 != i4) {
                  this.rleWritePontentialToken(i1);
                  this.rleWritePontentialToken(i2);
                  this.rleWritePontentialToken(i3);
                  this.rleWritePontentialToken(i4);
                  var4 += 16;
                  rem -= 4;
               } else {
                  int lRem = rem;

                  for(rem -= 4; rem > 0 && (i2 = in.readInt()) == i1; --rem) {
                  }

                  int len = lRem - rem;
                  this.out.writeInt(1380730179);
                  this.out.writeInt(len);
                  this.out.writeInt(i1);
                  var4 += 12;
                  if (rem > 0) {
                     this.out.writeInt(i2);
                     var4 += 4;
                     --rem;
                  }
               }
            }
         }
      }

   }

   private void rleWritePontentialToken(int i) throws IOException {
      this.out.writeInt(i);
      if (i == 1380730179) {
         this.out.writeInt(i);
      }

   }

   public void readRLE4(byte[] output) {
      ByteArrayOutputStream outB = new ByteArrayOutputStream();
      DataOutputStream out = new DataOutputStream(outB);

      try {
         while(out.size() != output.length) {
            int code = this.in.readInt();
            if (code != 1380730179) {
               out.writeInt(code);
            } else {
               int len = this.in.readInt();
               int value = this.in.readInt();

               for(int i = 0; i != len; ++i) {
                  out.writeInt(value);
               }
            }
         }
      } catch (Exception var8) {
      }

      System.arraycopy(outB.toByteArray(), 0, output, 0, output.length);
   }

   public void sendPacket(Packet packet) {
      packet.target = this;
      this.server.sendPacket(packet);
   }

   public boolean fastSend(Packet packet) {
      try {
         packet.write(this.out, this);
         this.sendBuffer();
         return true;
      } catch (IOException var3) {
         this.server.debugPrint("fast send : " + this.getUserName());
         this.exit = true;
         return false;
      }
   }

   void sendBuffer() throws IOException {
      if (this.outB.size() != 0) {
         byte[] send = this.outB.toByteArray();
         this.sock.getOutputStream().write(send);
         this.outB.reset();
      }
   }

   public void run() {
      this.main();
      this.leave();
   }

   public void setExit() {
      this.server.debugPrint("set exit : " + this.getUserName());
      this.exit = true;
   }

   public <T> T getResource(Class<?> parent, Class<T> resCls, String name) {
      return this.resourceContainer.getResource(parent, resCls, name);
   }

   public <T> void setResource(Class<?> parent, Class<T> resCls, String name, T res) {
      this.resourceContainer.setResource(parent, resCls, name, res);
   }

   public DataOutStream getOut() {
      return this.out;
   }

   public DataInputStream getIn() {
      return this.in;
   }

   public int getID() {
      return this.id;
   }

   public String getUserName() {
      return this.name;
   }

   public Server getServer() {
      return this.server;
   }

   public int[] getPingBuf() {
      return this.pingBuffer;
   }

   public int getDownloadSpeed() {
      return this.downloadSpeed;
   }

   public void setDownloadSpeed(int downloadSpeed) {
      this.downloadSpeed = downloadSpeed;
   }

   public static boolean isAdmin(ServerUser user) {
      return user == null || user.permissions.contains("admin");
   }

   public void addPermission(String permission) {
      this.permissions.add(permission);
   }

   public void removePermission(String permission) {
      this.permissions.remove(permission);
   }

   void main() {
      int err;
      if ((err = this.create().ordinal()) != 0) {
         try {
            this.out.write(err);
            this.sendBuffer();
         } catch (Exception var7) {
         }

      } else {
         while(true) {
            if (!this.joined) {
               synchronized(this) {
                  if (!this.server.joinApproval(this)) {
                     try {
                        this.wait(2L);
                        if (this.exit || this.sock.isClosed()) {
                           return;
                        }
                     } catch (InterruptedException var13) {
                     }
                     continue;
                  }
               }
            }

            this.server.joinLock.lock();
            this.server.list.add(this);
            this.server.joinList.remove(this);
            this.joined = true;
            this.server.joinLock.unlock();
            this.allocID();
            this.server.sendPacket((Packet)(new Packet.Join(this)));
            this.server.sendMessage((ServerUser)null, (ServerUser)null, this.getUserName() + " joined the game", "");

            try {
               this.out.write((int)0);
               this.sendBuffer();
               this.sock.setSoTimeout(0);
            } catch (IOException var8) {
               this.server.debugPrint("io exception : " + this.getUserName());
               this.exit = true;
            }

            this.server.sendPatches(this);
            Vector taskRemove = new Vector();

            while(!this.exit) {
               try {
                  this.reader.run();
                  this.writer.run();
                  if (this.currDownload != null && this.currDownload.doneTime != 0L) {
                     if (this.currDownload.doneTime != 0L) {
                        Packet pack = new Packet.Control(7);
                        pack.target = this;
                        this.server.sendPacket((Packet)pack);
                        this.currDownload.doneTime = 0L;
                     }
                  } else if (!this.downloadQueue.isEmpty()) {
                     this.currDownload = (DownloadServer.Message)this.downloadQueue.pop();
                     this.currDownload.Send();
                  }

                  long last = this.lastPingTime;
                  if (this.lastPingTime != 0L && (System.nanoTime() - this.lastPingTime) / 1000000000L > this.server.timeOut) {
                     this.server.userPrint("Auto disconnected " + this.getUserName());
                     this.exit = true;
                  }

                  Iterator var6 = this.tasks.iterator();

                  TaskChain t;
                  while(var6.hasNext()) {
                     t = (TaskChain)var6.next();
                     if (t.exec()) {
                        taskRemove.add(t);
                     }
                  }

                  var6 = taskRemove.iterator();

                  while(var6.hasNext()) {
                     t = (TaskChain)var6.next();
                     this.tasks.remove(t);
                  }

                  taskRemove.clear();
                  this.sleeper.update();
               } catch (EOFException var9) {
                  this.server.userPrint("Auto disconnected " + this.getUserName());
                  this.exit = true;
               } catch (SocketTimeoutException var10) {
                  this.server.debugPrint("socket time out : " + this.getUserName());
                  var10.printStackTrace();
               } catch (SocketException var11) {
                  this.server.debugPrint("idk lol : " + this.getUserName());
                  this.exit = true;
                  this.server.exitInfo(this.name);
               } catch (Exception var12) {
                  var12.printStackTrace();
                  this.server.malformedError(this.name);
                  this.exit = true;
               }
            }

            return;
         }
      }
   }

   public void addTask(TaskChain<ServerUser> task) {
      this.tasks.add(task);
   }

   public void allocID() {
      this.server.joinLock.lock();
      this.id = this.server.startPlayerID;

      while(true) {
         Iterator var2 = this.server.list.iterator();

         ServerUser U;
         do {
            if (!var2.hasNext()) {
               this.server.joinLock.unlock();
               return;
            }

            U = (ServerUser)var2.next();
         } while(this == U || U.id != this.id);

         ++this.id;
      }
   }

   public void kick() {
      try {
         this.server.debugPrint("kicking : " + this.getUserName());
         this.sock.close();
      } catch (Exception var2) {
      }

   }

   public void downloadData(byte[] data) {
      this.downloadQueue.push(new DownloadServer.Message(data, this));
   }

   public void downloadData(byte[] data, String downloadType) {
      this.downloadQueue.push(new DownloadServer.Message(data, this, downloadType));
   }

   public Packet handleSync() throws Exception {
      this.server.syncLock.lock();
      if (this.server.syncCnt == 0) {
         this.server.debugPrint("Sync rcvd");

         ServerUser u;
         for(Iterator var2 = this.server.list.iterator(); var2.hasNext(); u.userSync = false) {
            u = (ServerUser)var2.next();
         }
      }

      this.userSync = true;
      ++this.server.syncCnt;
      return this.server.syncUpdate();
   }

   static final DataInputStream newStream(String path) {
      DataInputStream i = null;
      if (ServerUser.class.getResource("/root/" + path) == null) {
         i = new DataInputStream(ServerUser.class.getClassLoader().getResourceAsStream("src/root/" + path));
      } else {
         i = new DataInputStream(ServerUser.class.getResourceAsStream("/root/" + path));
      }

      return i;
   }

   static enum ServerErrorCodes {
      GOOD,
      BANNED,
      FULL,
      INVALIDVERSION,
      UNK;
   }

   public static class UserCommand {
      int id;
      ServerUser u;

      public UserCommand(int id, ServerUser u) {
         this.id = id;
         this.u = u;
      }
   }
}
