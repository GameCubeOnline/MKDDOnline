package root;

import com.kirbymimi.mmb.actor.Actor;
import com.kirbymimi.mmb.audio.format.Sample;
import com.kirbymimi.mmb.graphics.View;
import com.kirbymimi.mmb.graphics.javaGraphics.JavaGraphics;
import com.kirbymimi.mmb.system.KThread;
import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ui.Component;
import com.kirbymimi.mmb.ut.IteratorWrap;
import com.kirbymimi.mmb.ut.Sleeper;
import com.kirbymimi.mmb.ut.TaskChain;
import com.kirbymimi.mmb.ut.ktml.KTML2Obj;
import com.kirbymimi.mmb.ut.ktml.KTMLDecoder;
import com.kirbymimi.mmb.ut.ktml.KTMLEntry;
import com.kirbymimi.mmb.ut.list.FastList;
import com.kirbymimi.mmb.ut.list.SafeList;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class Server extends KThread {
   String tag = "For God so loved the world, that he gave his only begotten Son, that whosoever believeth in him should not perish, but have everlasting life.";
   public static Vector<Server> servers = new Vector();
   boolean debug = true;
   ServerSocket sock;
   boolean exit;
   SafeList<ServerUser> list = new SafeList();
   SafeList<ServerUser> joinList = new SafeList();
   ReentrantLock joinLock = new ReentrantLock();
   volatile int syncCnt;
   ReentrantLock syncLock = new ReentrantLock();
   ReentrantLock voiceLock = new ReentrantLock();
   HashMap<Integer, Server.DesyncDetectorNode> desyncDetector = new HashMap();
   Vector<TaskChain<?>> tasks = new Vector();
   ReentrantLock desyncDetectorLock = new ReentrantLock();
   Mixer mixer = new Mixer(this);
   BanList bans = new BanList(this);
   FastList<KTMLEntry> patches = new FastList();
   Sleeper sleeper;
   volatile int desyncCount;
   int startPlayerID;
   int port = 10014;
   long rate = 10L;
   long timeOut = 1000L;
   int downloadSpeed = 6;
   String version;
   int maxPlayer;
   Component ui;
   int audioStreamPos;
   Sample audioStream;
   Voice streamVoice = new Voice(this);
   static int AUDIOSTREAMSIZE = 61;

   public Server() {
      Mixer.init();
      (new KThread(() -> {
         consoleThreadUpdate();
      })).start();
      this.bans.loadFile();
      this.loadConfig();
      this.reloadPatches();
      this.sleeper = new Sleeper(this.rate);
      servers.add(this);
      this.start();
   }

   public void createUI() {
      MMBSystem sys = MMBSystem.get();
      sys.setLogicFrameRate(60);
      sys.createWorkers(1);
      JavaGraphics graph = new JavaGraphics(60, "=)");
      this.ui = new Component("GUI.ktml", new String[]{"uis.debugger"}, graph);
      graph.setWindowSize(1280, 720);
      View view = graph.createView();
      view.setTarget(this.ui);
      graph.start();
      sys.addWork((Actor)this.ui);
   }

   public void loadConfig() {
      try {
         KTMLEntry ktml = KTMLDecoder.decode(this.fixFile("server.cfg"));
         KTML2Obj.loadS(this, ktml);
         this.rate = 1000000000L / this.rate;
         String str = ktml.getString("mixerType");
         if (str != null) {
            String var3;
            switch((var3 = str.toLowerCase()).hashCode()) {
            case -599266462:
               if (var3.equals("compress")) {
                  this.mixer.enabled = true;
                  this.mixer.enableCompress = true;
                  return;
               }
               break;
            case 3551:
               if (var3.equals("on")) {
                  this.mixer.enabled = true;
                  this.mixer.enableCompress = false;
                  return;
               }
               break;
            case 109935:
               if (var3.equals("off")) {
                  this.mixer.enabled = false;
                  return;
               }
            }

            this.userPrint("Unknown value in the mixer mode :" + str + ", possible values : \"on\", \"off\", \"compress\"");
         }
      } catch (Exception var4) {
         this.userPrint("Can't load the serverconfig file");
      }

   }

   void reloadPatches() {
      this.patches.clear();
      File patchFolder = this.fixFile("Patches");
      if (patchFolder.exists()) {
         File[] var5;
         int var4 = (var5 = patchFolder.listFiles()).length;

         for(int var3 = 0; var3 < var4; ++var3) {
            File patchFile = var5[var3];
            if (!patchFile.isDirectory()) {
               this.patches.add(KTMLDecoder.decode(patchFile));
            }
         }
      }

   }

   public void sendPatches(ServerUser dst) {
      Iterator var3 = this.patches.iterator();

      while(var3.hasNext()) {
         KTMLEntry ktml = (KTMLEntry)var3.next();
         Packet pack = new Packet.Patch(ktml);
         pack.target = dst;
         this.sendPacket((Packet)pack);
      }

   }

   public static void consoleThreadUpdate() {
      Thread.currentThread().setName("Console thread");

      while(true) {
         while(true) {
            try {
               String linefull = readLine().trim().replace(',', ' ').replaceAll(" +", " ");
               if (!linefull.isEmpty()) {
                  ((Server)servers.elementAt(0)).processCommand(linefull, (ServerUser)null);
               }
            } catch (Exception var1) {
            }
         }
      }
   }

   public void audioStreamStart(Sample audioStream) {
      this.audioStream = audioStream;
      this.audioStreamPos = 0;
   }

   public void audioStreamUpdate() {
      if (this.audioStream != null) {
         short[] vals = this.audioStream.getVals()[0];
         int rate = this.audioStream.getRate();
         int len = rate / AUDIOSTREAMSIZE;
         if (len + this.audioStreamPos > vals.length) {
            len = vals.length - this.audioStreamPos;
         }

         byte[] send = new byte[len * 2];

         for(int i = 0; i != len; ++i) {
            send[(i << 1) + 1] = (byte)(vals[i + this.audioStreamPos] & 255);
            send[i << 1] = (byte)((vals[i + this.audioStreamPos] & '\uff00') >> 8);
         }

         this.audioStreamPos += len;
         this.mixer.addVoice(send, this.streamVoice, rate);
         if (this.audioStreamPos == vals.length) {
            this.audioStream = null;
         }

      }
   }

   public static String readLine() throws IOException {
      Console cons = System.console();
      if (cons != null) {
         return cons.readLine();
      } else {
         BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
         String str = bufferedReader.readLine();
         return str == null ? "" : str;
      }
   }

   public void lockJoin() {
      this.joinLock.lock();
   }

   public void unlockJoin() {
      this.joinLock.unlock();
   }

   public Mixer getMixer() {
      return this.mixer;
   }

   public int getStartPlayerID() {
      return this.startPlayerID;
   }

   public void setStartPlayerID(int id) {
      this.startPlayerID = id;
   }

   public String getVersion() {
      return this.version;
   }

   public int maxPlayer() {
      return this.maxPlayer;
   }

   protected void setVersion(String version) {
      if (this.version == null) {
         this.version = version;
      }
   }

   public void run() {
      this.setName("Server thread");
      DatagramSocket broadCastSock = null;
      DatagramPacket broadCastPack = null;
      ArrayList broadcastList = new ArrayList();

      try {
         this.sock = new ServerSocket(this.port);
         this.sock.setSoTimeout(1);
         String addrStr = findDefaultRoute();
         if (addrStr != null) {
            byte[] broadCastAddr = InetAddress.getByName(addrStr).getAddress();
            ByteArrayOutputStream broadcastDataStream = new ByteArrayOutputStream();
            DataOutputStream broadcastDataGen = new DataOutputStream(broadcastDataStream);
            broadcastDataGen.writeUTF(this.getVersion());
            broadcastDataGen.write(broadCastAddr);
            broadcastDataGen.writeShort(this.port);
            byte[] broadcastData = broadcastDataStream.toByteArray();
            broadcastDataStream.close();
            broadCastSock = new DatagramSocket();
            broadCastSock.setBroadcast(true);
            broadCastPack = new DatagramPacket(broadcastData, broadcastData.length);
            broadCastPack.setAddress(InetAddress.getByName("255.255.255.255"));
            broadCastPack.setPort(10014);
            Iterator var10 = (new IteratorWrap(NetworkInterface.getNetworkInterfaces())).iterator();

            while(var10.hasNext()) {
               NetworkInterface networkInterface = (NetworkInterface)var10.next();
               if (!networkInterface.isLoopback() && networkInterface.isUp()) {
                  networkInterface.getInterfaceAddresses().stream().map((a) -> {
                     return a.getBroadcast();
                  }).filter(Objects::nonNull).forEach(broadcastList::add);
               }
            }
         } else {
            this.userPrint("WARNING : Cannot find the server local IP, LAN discover will be disabled.");
         }
      } catch (BindException var14) {
         this.userPrint("Socket port is already bound on your local network, you probably have another server running in the background.");
         System.exit(-1);
      } catch (Exception var15) {
         this.userPrint("Cannot create socket, exiting");
         var15.printStackTrace();
         System.exit(-1);
      }

      if (this.sock == null) {
         System.exit(-1);
      }

      this.userPrint(this.getVersion());

      int broadCastClock = 0;

      while(!this.exit) {
         try {
            Socket client = this.sock.accept();
            ServerUser.create(client, this);
         } catch (Exception var12) {
         }

         ++broadCastClock;
         Iterator var20;
         if (broadCastClock == 100 && broadCastSock != null) {
            try {
               var20 = broadcastList.iterator();

               while(var20.hasNext()) {
                  InetAddress addr = (InetAddress)var20.next();
                  broadCastPack.setAddress(addr);
                  broadCastPack.setPort(10014);
                  broadCastSock.send(broadCastPack);
               }

               broadCastClock = 0;
            } catch (Exception var13) {
            }
         }

         var20 = this.tasks.iterator();

         while(var20.hasNext()) {
            TaskChain<?> task = (TaskChain)var20.next();
            task.exec();
         }

         this.audioStreamUpdate();
         this.sleeper.update();
      }

      try {
         this.sock.close();
      } catch (Exception var11) {
      }

   }

   private static String findDefaultRoute() {
      String var0 = "netstat -rn";

      try {
         Process exec = Runtime.getRuntime().exec("netstat -rn");
         BufferedReader reader = new BufferedReader(new InputStreamReader(exec.getInputStream()));
         Pattern p = Pattern.compile("^\\s*(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+\\S+?\\s+(\\d+\\.\\d+\\.\\d+\\.\\d+)\\s+(\\d+)\\s*$");

         String network;
         String address;
         do {
            do {
               Matcher match;
               do {
                  String line;
                  if ((line = reader.readLine()) == null) {
                     return null;
                  }

                  match = p.matcher(line);
               } while(!match.matches());

               network = match.group(1);
               address = match.group(3);
            } while(!network.contentEquals("0.0.0.0"));
         } while(!address.startsWith("10.") && !address.startsWith("172.") && !address.startsWith("192.168."));

         return address;
      } catch (Exception var8) {
         return null;
      }
   }

   Packet syncUpdate() {
      if (this.syncCnt == this.list.size() && this.list.size() != 0) {
         this.debugPrint("Sync done");
         this.syncCnt = 0;

         ServerUser u;
         for(Iterator var2 = this.list.iterator(); var2.hasNext(); u.lastPingTime = 0L) {
            u = (ServerUser)var2.next();
            u.skippedTime = 0;
         }

         this.syncLock.unlock();
         return this.syncDone();
      } else {
         this.syncLock.unlock();
         return null;
      }
   }

   public void malformedError(String Name) {
      this.userPrint(Name + " tried to output a malformed packet");
   }

   public void timeOutError(String Name) {
      this.userPrint(Name + " took too long to send a packet");
   }

   public void exitInfo(String Name) {
      this.userPrint("User " + Name + " left the server");
   }

   public SafeList<ServerUser> getList() {
      return this.list;
   }

   public SafeList<ServerUser> getJoinList() {
      return this.joinList;
   }

   public void sendPacket(Packet pack) {
      Iterator var3 = this.list.iterator();

      while(var3.hasNext()) {
         ServerUser u = (ServerUser)var3.next();
         u.writer.packetLock.lock();
         u.writer.packets.add(pack);
         u.writer.packetLock.unlock();
      }

   }

   public void sendPacket(Vector<Packet> pack) {
      Iterator var3 = this.list.iterator();

      while(var3.hasNext()) {
         ServerUser u = (ServerUser)var3.next();
         u.writer.packetLock.lock();
         Iterator var5 = pack.iterator();

         while(var5.hasNext()) {
            Packet p = (Packet)var5.next();
            u.writer.packets.add(p);
         }

         u.writer.packetLock.unlock();
      }

   }

   public void sendMessage(ServerUser src, ServerUser dst, String message, String start) {
      message = (src == null ? "server" : src.getUserName()) + ":" + message;
      message = start + message;
      Packet.Message msg = new Packet.Message(message);
      msg.setTarget(dst);
      this.sendPacket((Packet)msg);
   }

   public void sendMessage(String message) {
      Packet.Message msg = new Packet.Message(message);
      this.sendPacket((Packet)msg);
   }

   public void addTask(TaskChain<?> task) {
      this.tasks.add(task);
   }

   public void reset() {
      ServerUser user;
      Iterator var2;
      for(var2 = this.list.iterator(); var2.hasNext(); user.exit = true) {
         user = (ServerUser)var2.next();
      }

      for(var2 = this.joinList.iterator(); var2.hasNext(); user.exit = true) {
         user = (ServerUser)var2.next();
      }

      this.reloadPatches();
   }

   public void clearIDs() {
      ServerUser u;
      for(Iterator var2 = this.list.iterator(); var2.hasNext(); u.id = -1) {
         u = (ServerUser)var2.next();
      }

   }

   public void reallocIDs() {
      this.clearIDs();
      Iterator var2 = this.list.iterator();

      while(var2.hasNext()) {
         ServerUser u = (ServerUser)var2.next();
         u.allocID();
      }

   }

   public ServerUser get(String id) {
      int iid = Integer.MIN_VALUE;

      try {
         iid = Integer.decode(id);
      } catch (Exception var5) {
      }

      Iterator var4 = this.list.iterator();

      ServerUser u;
      while(var4.hasNext()) {
         u = (ServerUser)var4.next();
         if (u.id == iid) {
            return u;
         }

         if (u.name.compareToIgnoreCase(id) == 0) {
            return u;
         }
      }

      var4 = this.joinList.iterator();

      while(var4.hasNext()) {
         u = (ServerUser)var4.next();
         if (u.id == iid) {
            return u;
         }

         if (u.name.compareToIgnoreCase(id) == 0) {
            return u;
         }
      }

      return null;
   }

   public ServerUser get(int id) {
      Iterator var3 = this.list.iterator();

      while(var3.hasNext()) {
         ServerUser u = (ServerUser)var3.next();
         if (u.id == id) {
            return u;
         }
      }

      return null;
   }

   public void desyncDetectorClear() {
      this.desyncCount = 0;
      this.desyncDetector.clear();
   }

   public int desyncDetectorUpdate(ServerUser src, int rng, int time) {
      if (this.list.size() < 2) {
         return this.desyncCount;
      } else {
         this.desyncDetectorLock.lock();
         Server.DesyncDetectorNode node = new Server.DesyncDetectorNode();
         Server.DesyncDetectorNode old = (Server.DesyncDetectorNode)this.desyncDetector.get(time);
         int val;
         if (old == null) {
            node.desyncTable.clear();
            node.desyncTable.put(src, rng);
            this.desyncDetector.put(time, node);
            val = this.desyncCount;
            this.desyncDetectorLock.unlock();
            return val;
         } else {
            old.desyncTable.put(src, rng);
            ++old.cnt;
            if (old.cnt == this.list.size()) {
               val = (Integer)old.desyncTable.values().toArray()[0];
               Iterator var8 = old.desyncTable.values().iterator();

               while(var8.hasNext()) {
                  Integer i = (Integer)var8.next();
                  if (i != val) {
                     this.debugPrint("Deysnc report:");
                     int idx = 0;
                     Iterator var11 = old.desyncTable.values().iterator();

                     while(var11.hasNext()) {
                        Integer i2 = (Integer)var11.next();
                        ServerUser user = this.get(idx++);
                        if (user != null) {
                           this.debugPrint(user.getUserName() + " : " + i2);
                        }
                     }

                     ++this.desyncCount;
                     break;
                  }
               }

               this.desyncDetector.remove(time);
            }

            this.desyncDetectorLock.unlock();
            return this.desyncCount;
         }
      }
   }

   public void userPrint(String text) {
      System.out.println(text);
   }

   public void debugPrint(String text) {
      if (this.debug) {
         System.out.println(text);
      }
   }

   public void statusPrint() {
      this.userPrint("Version: " + this.getVersion());
      this.userPrint("--------------------------------------------------");
      this.userPrint("User count : " + this.getList().size());
      Iterator var2 = this.getList().iterator();

      ServerUser u;
      while(var2.hasNext()) {
         u = (ServerUser)var2.next();
         this.printUser(u);
      }

      this.userPrint("--------------------------------------------------");
      this.userPrint("Joining count : " + this.getJoinList().size());
      var2 = this.getJoinList().iterator();

      while(var2.hasNext()) {
         u = (ServerUser)var2.next();
         this.userPrint(u.getUserName());
      }

      this.userPrint("--------------------------------------------------");
   }

   public void printUser(ServerUser u) {
      this.userPrint(u.getUserName() + " " + u.getID());
   }

   public String fixPath(String path) {
      String str = this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath().replace("target/classes", "");
      str = str.replace("/bin/", "");
      if (str.contains(".jar")) {
         str = str.substring(0, str.lastIndexOf(47));
      }

      return str + "/" + path;
   }

   public File fixFile(String path) {
      return MMBSystem.openFileS(path);
   }

   public abstract ServerUser createUser();

   public abstract GameReader.PacketsHandler[] getRcvPackets();

   public abstract void processCommand(String var1, ServerUser var2);

   public abstract boolean joinApproval(ServerUser var1);

   public abstract void userJoin(ServerUser var1) throws IOException;

   public abstract void userLeave(ServerUser var1) throws IOException;

   public abstract Packet syncDone();

   public abstract void print();

   public static class DesyncDetectorNode {
      int cnt = 1;
      HashMap<ServerUser, Integer> desyncTable = new HashMap();
   }
}
