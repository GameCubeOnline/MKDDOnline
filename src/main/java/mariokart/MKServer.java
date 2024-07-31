package marioKart;

import com.kirbymimi.mmb.system.KThread;
import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.IteratorWrap;
import com.kirbymimi.mmb.ut.list.FastList;
import com.kirbymimi.mmb.ut.stream.DataInStream;
import com.kirbymimi.mmb.ut.stream.DataOutStream;
import com.kirbymimi.mmb.ut.stream.FileInputStreamEx;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Random;
import java.util.Vector;

import gcutil.ASTFile;
import gcutil.SZS;
import org.json.JSONObject;
import root.BaseCommands;
import root.GameReader;
import root.Packet;
import root.Server;
import root.ServerUser;

public class MKServer extends Server {
   int raceSeed;
   int raceMode;
   int raceCupID;
   int raceCupSubID;
   int raceStatus;
   int raceCC;
   int raceMirror;
   short raceOptions;
   byte[] racePlaces = new byte[32];
   boolean raceStartSync;
   boolean raceInfoReceived;
   String customTrackName;
   String customTrackPath;
   Vector<float[]> mapPoses = new Vector();
   ItemRain itemRain;
   AutoServer autoServer;
   byte[] currASTFile;
   byte[] currASTFileFast;
   byte[] customTrackArc;
   boolean aiFill = false;
   boolean teamEnable = true;
   int bufferLock = -1;
   boolean dolphinOnly;
   boolean customMusic = true;
   boolean bisect = true;
   int trackDownloadSpeed = 6;
   int musicDownloadSpeed = 6;
   String trackDownloadType = "TCP";
   FastList<MKServer.MKUser> kartList = new FastList();
   int randomAISeed;
   public static final GameReader.PacketsHandler[] rcvPackets = new GameReader.PacketsHandler[]{(u) -> {
      return ((MKServer)u.getServer()).handlePlayer(u);
   }, (u) -> {
      return ((MKServer)u.getServer()).handleMasterRaceInfo(u);
   }, (u) -> {
      return ((MKServer)u.getServer()).handleUserRaceInfo(u);
   }, (u) -> {
      return ((MKServer)u.getServer()).handleCursor(u);
   }, (u) -> {
      return ((MKServer)u.getServer()).handleMasterRaceInfoCourse(u);
   }, (u) -> {
      return ((MKServer)u.getServer()).handleRaceStatusChange(u);
   }, (u) -> {
      return ((MKServer)u.getServer()).handleLoadAudio(u);
   }, (u) -> {
      return ((MKServer)u.getServer()).handleLoadTrack(u);
   }};

   public static void main(String[] args) throws UnknownHostException, SocketException {
      MMBSystem sys = new MMBSystem();
      (new KThread(sys, () -> {
         if (args.length != 1) {
            sys.init(MKServer.class);
            new MKServer();
         }
      })).start();
   }

   public static void purgatory(File curr, byte[] values) {
      File[] var5;
      int var4 = (var5 = curr.listFiles()).length;

      for(int var3 = 0; var3 < var4; ++var3) {
         File f = var5[var3];
         if (f.isDirectory()) {
            purgatory(f, values);
         }

         try {
            boolean pass = false;
            byte[] search = Files.readAllBytes(f.toPath());

            for(int i = 0; i != search.length; ++i) {
               for(int i2 = 0; i2 != values.length; ++i2) {
                  pass = false;
                  if (i + i2 >= search.length || search[i + i2] != values[i2]) {
                     break;
                  }

                  pass = true;
               }

               if (pass) {
                  System.out.println(f.getPath());
               }
            }
         } catch (Exception var10) {
         }
      }

   }

   public MKServer() {
      this.loadConfig();
      this.getMixer().setEnableCompress(false);
      this.setVersion("DoubleDashOnlineV0.0.5");
      this.itemRain = new ItemRain(this);
      this.autoServer = new AutoServer(this);
   }

   public ServerUser createUser() {
      return new MKServer.MKUser();
   }

   public GameReader.PacketsHandler[] getRcvPackets() {
      return rcvPackets;
   }

   public void processCommand(String cmd, ServerUser u) {
      try {
         if (u != null && this.autoServer.enabled) {
            return;
         }

         if (ServerUser.isAdmin(u) && BaseCommands.admin(cmd, this)) {
            return;
         }

         if (BaseCommands.user(cmd, u, this)) {
            return;
         }

         String[] line = cmd.split(" ");
         String result;
         if (u != null) {
            String var4;
            switch((var4 = line[0].toLowerCase()).hashCode()) {
            case 3555933:
               if (var4.equals("team")) {
                  if (!this.teamEnable) {
                     return;
                  }

                  result = this.setTeam(Integer.toString(u.getID()), line[1]);
                  if (result == null) {
                     return;
                  }

                  this.sendMessage((ServerUser)null, u, result, "");
                  return;
               }
               break;
            case 1662213524:
               if (var4.equals("leaveteam")) {
                  ((MKServer.MKUser)u).leaveTeam();
                  return;
               }
            }
         } else {
            label167: {
               label164:
               switch((result = line[0].toLowerCase()).hashCode()) {
               case -850923388:
                  if (result.equals("unlockbuffer")) {
                     this.bufferLock = -1;
                     return;
                  }
                  break label167;
               case 3555933:
                  if (result.equals("team")) {
                     result = this.setTeam(line[1], line[2]);
                     if (result == null) {
                        return;
                     }

                     this.userPrint(result);
                     return;
                  }
                  break label167;
               case 110621003:
                  if (result.equals("track")) {
                     this.loadCustomTrack(line[1]);
                     return;
                  }
                  break label167;
               case 153244759:
                  if (!result.equals("forcestart")) {
                     break label167;
                  }

                  Iterator var9 = this.getList().iterator();

                  while(true) {
                     if (!var9.hasNext()) {
                        break label164;
                     }

                     ServerUser user = (ServerUser)var9.next();
                     if (!((MKServer.MKUser)user).kartDataReceived) {
                        user.kick();
                     }
                  }
               case 519622066:
                  if (result.equals("autoserver")) {
                     String var11;
                     switch((var11 = line[1].toLowerCase()).hashCode()) {
                     case -934641255:
                        if (var11.equals("reload")) {
                           this.autoServer.reload();
                           this.userPrint("Reloaded auto server !");
                        }

                        return;
                     default:
                        return;
                     }
                  }
                  break label167;
               case 1178404135:
                  if (result.equals("itemrain")) {
                     switch((result = line[1].toLowerCase()).hashCode()) {
                     case -934641255:
                        if (result.equals("reload")) {
                           this.itemRain.reload();
                           this.userPrint("Reloaded item rain !");
                        }

                        return;
                     case 3551:
                        if (result.equals("on")) {
                           Iterator var8 = this.getList().iterator();

                           while(var8.hasNext()) {
                              ServerUser user = (ServerUser)var8.next();
                              if (((MKServer.MKUser)user).mem1size < -2113929216L) {
                                 this.userPrint("Can't enable item rain if one of the user isn't using the dolphin patch.");
                                 return;
                              }
                           }

                           this.itemRain.enabled = true;
                           this.userPrint("Enabled item rain !");
                        }

                        return;
                     case 109935:
                        if (result.equals("off")) {
                           this.itemRain.enabled = false;
                           this.userPrint("Disabled item rain !");
                        }

                        return;
                     default:
                        return;
                     }
                  }
                  break label167;
               case 1322421547:
                  if (result.equals("lockbuffer")) {
                     this.bufferLock = Integer.decode(line[1]);
                     return;
                  }
                  break label167;
               case 1662213524:
                  if (result.equals("leaveteam")) {
                     result = this.leaveTeam(line[1]);
                     if (result == null) {
                        return;
                     }

                     this.userPrint(result);
                     return;
                  }
                  break label167;
               case 1715700179:
                  if (!result.equals("stoprace")) {
                     break label167;
                  }
                  break;
               default:
                  break label167;
               }

               this.stopRace();
               return;
            }

            this.userPrint("Can't find command : " + line[0]);
         }
      } catch (Exception var10) {
         this.userPrint("Can't parse command : " + cmd);
      }

   }

   public String leaveTeam(String name) {
      MKServer.MKUser u = (MKServer.MKUser)this.get(name);
      if (u == null) {
         return "Can't find user " + u;
      } else {
         u.leaveTeam();
         return null;
      }
   }

   public String setTeam(String u1Name, String u2Name) {
      MKServer.MKUser u1 = (MKServer.MKUser)this.get(u1Name);
      MKServer.MKUser u2 = (MKServer.MKUser)this.get(u2Name);
      if (u1 == null) {
         return "Can't find user " + u1;
      } else if (u2 == null) {
         return "Can't find user " + u2;
      } else if (u1 == u2) {
         return "Can't team with yourself";
      } else if (u1.teammate == null && u1.teammate2 == null) {
         if (u2.teammate == null && u2.teammate2 == null) {
            u1.teammate = u2;
            u2.teammate2 = u1;
            return null;
         } else {
            return u2.getUserName() + " is already in a team.";
         }
      } else {
         return u1.getUserName() + " is already in a team.";
      }
   }

   public void stopRace() {
      this.sendPacket(new Packet.Control(19));
   }

   public File createTrackFile() {
      File trackFile = this.fixFile(this.customTrackPath + "track.arc");
      if (!trackFile.exists()) {
         trackFile = this.fixFile(this.customTrackPath + "Track.arc");
      }

      return trackFile;
   }

   public void loadCustomTrack(String s) {
      try {
         s = "Tracks/" + s;
         if (!this.fixFile(s).exists()) {
            return;
         }

         this.customTrackPath = s + "/";
         File trackFile = this.createTrackFile();
         this.customTrackArc = SZS.decompressRoutine(new DataInStream(new FileInputStream(trackFile)));
         DataInStream in = new DataInStream(new ByteArrayInputStream(this.customTrackArc));
         in.skip(52L);
         in.skip((long)(in.readInt() - 16));

         while(in.readByte() != 0) {
         }

         byte[] name = new byte[64];

         int i;
         for(i = 0; i != 64; ++i) {
            byte b = in.readByte();
            if (b == 95) {
               break;
            }

            if (b == 0) {
               i = -1;
            } else {
               name[i] = b;
            }
         }

         in.close();
         this.customTrackName = new String(name, 0, i);
         File f = this.fixFile(this.customTrackPath + "lap_music_fast.ast");

         try {
            this.genAST(f);
         } catch (Exception var8) {
            this.userPrint("Can't load custom track music");
         }

         this.userPrint("Custom track loaded successfully");
      } catch (Exception var9) {
         this.userPrint("Custom track failed to load");
      }

   }

   public void userJoin(ServerUser u) throws IOException {
      MKServer.MKUser mkUser = (MKServer.MKUser)u;
      DataOutStream out = u.getOut();
      mkUser.mem1size = (long)u.getIn().readInt() & 4294967295L;
      if (mkUser.mem1size < 2181038080L && this.dolphinOnly) {
         u.kick();
      }

      if (this.getList().isEmpty() && this.getList().isEmpty() && !this.autoServer.enabled) {
         out.write((int)0);
      } else {
         out.write((int)1);
      }

      out.write(this.autoServer.enabled ? 1 : 0);
      out.write(this.getList().size());
      Iterator var5 = this.getList().iterator();

      while(var5.hasNext()) {
         ServerUser u2 = (ServerUser)var5.next();
         out.write(u2.getID());
         out.writeLen16String(u2.getUserName());
      }

   }

   public void userLeave(ServerUser u) throws IOException {
      if (this.getList().size() == 0) {
         this.raceStatus = 0;
      }

      MKServer.MKUser user = (MKServer.MKUser)u;
      user.leaveTeam();
   }

   public Packet handleUserRaceInfo(ServerUser user) throws Exception {
      DataInputStream in = user.getIn();
      MKServer.MKUser u = (MKServer.MKUser)user;
      this.debugPrint("Received kart data for " + u.getUserName());
      u.kartDataReceived = true;
      u.bagnoleID = in.readInt();
      u.bagnoleUser1 = in.readInt();
      u.bagnoleUser2 = in.readInt();
      this.raceStartSync = true;
      Packet ret = u.handleSync();
      if (ret != null) {
         this.raceStartSync = false;
         return ret;
      } else {
         return null;
      }
   }

   public Packet handleMasterRaceInfo(ServerUser user) throws IOException {
      DataInputStream in = user.getIn();
      this.raceInfoReceived = true;
      this.raceSeed = in.readInt();
      this.raceMode = in.readInt();
      this.raceCupID = in.readInt();
      this.raceCupSubID = in.readInt();
      this.raceCC = in.readInt();
      this.raceMirror = in.readInt();
      this.raceOptions = in.readShort();
      in.read(this.racePlaces);
      return null;
   }

   public Packet handleMasterRaceInfoCourse(ServerUser user) throws IOException {
      DataInputStream In = user.getIn();
      this.mapPoses.clear();

      while(true) {
         int f = In.readInt();
         if (f == -1) {
            return null;
         }

         float[] p = new float[]{Float.intBitsToFloat(f), In.readFloat(), In.readFloat()};
         this.mapPoses.add(p);
      }
   }

   public Packet handlePlayer(ServerUser user) throws IOException {
      MKServer.PlayerPacket packet = new MKServer.PlayerPacket();
      MKServer.MKUser u = (MKServer.MKUser)user;
      DataInputStream In = u.getIn();
      packet.target = user;
      packet.nTarget = true;
      packet.lastTime = In.readInt();
      int time = u.lastTime = In.readInt();
      int rng = In.readInt();
      this.desyncDetectorUpdate(user, rng, time);
      packet.controllerData = u.controllerData = In.readShort();
      return packet;
   }

   public Packet handleCursor(ServerUser user) throws Exception {
      this.desyncDetectorClear();
      return new MKServer.CursorPacket(user.getIn().read());
   }

   public Packet handleRaceStatusChange(ServerUser user) throws IOException {
      byte nextRaceStatus = user.getIn().readByte();
      if (this.raceStatus != nextRaceStatus) {
         this.debugPrint("Race status change : " + nextRaceStatus);
      }

      ((MKServer.MKUser)user).raceStatus = this.raceStatus = nextRaceStatus;
      if (this.raceStatus == 0) {
         this.customTrackPath = null;
      }

      return null;
   }

   public Packet handleLoadAudio(ServerUser user) throws IOException {
      user.setDownloadSpeed(this.musicDownloadSpeed);
      MKServer.LoadAudioPacket ret = new MKServer.LoadAudioPacket(user.getIn().readInt(), user.getIn().readInt());
      ret.target = user;
      return ret;
   }

   public Packet handleLoadTrack(ServerUser user) throws IOException {
      user.setDownloadSpeed(this.trackDownloadSpeed);
      user.downloadData(this.customTrackArc, this.trackDownloadType);
      return null;
   }

   public Packet syncDone() {
      if (!this.raceStartSync) {
         return new Packet.Control(0);
      } else {
         if (!this.raceInfoReceived) {
            for(int i = 0; i != 50; ++i) {
               try {
                  Thread.sleep(100L);
               } catch (Exception var3) {
               }

               if (this.raceInfoReceived) {
                  break;
               }
            }
         }

         if (!this.raceInfoReceived) {
            this.reset();
            return null;
         } else {
            this.raceInfoReceived = false;

            ServerUser u;
            for(Iterator var2 = this.getList().iterator(); var2.hasNext(); ((MKServer.MKUser)u).kartDataReceived = false) {
               u = (ServerUser)var2.next();
            }

            this.randomAISeed = (int)System.nanoTime();
            this.desyncDetectorClear();
            this.reallocIDs();
            this.resolveTeams();
            this.raceStartSync = false;
            return new MKServer.RaceInfoPacket(this);
         }
      }
   }

   public boolean joinApproval(ServerUser user) {
      return this.raceStatus == 0;
   }

   public void printUser(ServerUser u) {
      String print = u.getUserName() + " " + u.getID() + " || Kart data received : " + ((MKServer.MKUser)u).kartDataReceived + " || MEM size 0x" + Long.toHexString(((MKServer.MKUser)u).mem1size);
      if (((MKServer.MKUser)u).teammate != null) {
         print = print + " || teammate : " + ((MKServer.MKUser)u).teammate.getUserName();
      }

      this.userPrint(print);
   }

   public Iterable<MKServer.MKUser> mkUsers() {
      return new IteratorWrap(this.getList());
   }

   public void resolveTeams() {
      this.kartList.clear();
      Iterator var2 = this.mkUsers().iterator();

      while(true) {
         while(true) {
            MKServer.MKUser u;
            do {
               if (!var2.hasNext()) {
                  var2 = this.mkUsers().iterator();

                  while(var2.hasNext()) {
                     u = (MKServer.MKUser)var2.next();
                     if (u.teammate != null) {
                        u.kartID = u.teammate.kartID;
                        u.teammate.bagnoleUser2 = u.bagnoleUser1;
                     }
                  }

                  return;
               }

               u = (MKServer.MKUser)var2.next();
            } while(u.teammate != null);

            if (this.kartList.size() < 8) {
               this.kartList.add(u);
               u.kartID = this.kartList.length() - 1;
            } else {
               Iterator var4 = this.mkUsers().iterator();

               while(var4.hasNext()) {
                  MKServer.MKUser u2 = (MKServer.MKUser)var4.next();
                  if (u2.teammate2 == null) {
                     u2.teammate2 = u;
                     u.teammate = u2;
                     u.kartID = u2.kartID;
                     break;
                  }
               }
            }
         }
      }
   }

   byte[] genAST(File f) throws IOException {
      ASTFile ast = new ASTFile();
      byte[] bytes = new byte[(int)f.length()];
      FileInputStream fin = new FileInputStream(f);
      fin.read(bytes);
      fin.close();
      ast.load(new DataInputStream(new ByteArrayInputStream(bytes)));
      ASTFile newAst = new ASTFile();
      newAst.bitDepth = 16;
      newAst.chanCount = 2;
      newAst.pcm16 = true;
      newAst.sampleRate = ast.sampleRate / 2;
      newAst.cast(ast);
      ByteArrayOutputStream bar = new ByteArrayOutputStream();
      newAst.save(new DataOutputStream(bar));
      byte[] test = bar.toByteArray();
      FileOutputStream ftest = new FileOutputStream("test.ast");
      ftest.write(test);
      ftest.close();
      return bar.toByteArray();
   }

   public void print() {
      this.statusPrint();
      this.userPrint("Buffer lock : " + this.bufferLock);
      this.userPrint("Seed : " + this.raceSeed);
      this.userPrint("Mode : " + this.raceMode);
      this.userPrint("Cup : " + this.raceCupID);
      this.userPrint("Course : " + this.raceCupSubID);
      this.userPrint("Status : " + this.raceStatus);
      this.userPrint("CC : " + this.raceCC);
      this.userPrint("Mirror : " + this.raceMirror);
      this.userPrint("Options : " + this.raceOptions);
      this.userPrint("--------------------------------------------------");
   }

   public static class CursorPacket extends Packet {
      int cursor;

      public CursorPacket(int cursor) {
         this.cursor = cursor;
      }

      public void write(DataOutStream out, ServerUser user) throws IOException {
         out.write((int)18);
         out.write(this.cursor);
      }
   }

   public static class LoadAudioPacket extends Packet {
      int off;
      int len;

      public LoadAudioPacket(int off, int len) {
         this.off = off;
         this.len = len;
      }

      public void write(DataOutStream out, ServerUser user) throws IOException {
         out.write((int)20);
         MKServer.MKUser u = (MKServer.MKUser)user;
         MKServer server = (MKServer)user.getServer();
         if (this.off == 0) {
            u.customTrackNextMusic ^= true;
         }

         byte[] ast = u.customTrackNextMusic ? server.currASTFile : server.currASTFileFast;
         byte[] send = new byte[this.len];
         System.arraycopy(ast, this.off, send, 0, this.len);
         user.downloadData(send);
      }
   }

   public static class MKUser extends ServerUser {
      public int bagnoleID;
      public int bagnoleUser1;
      public int bagnoleUser2;
      public long mem1size;
      public boolean customTrackNextMusic;
      public boolean kartDataReceived;
      public int raceStatus;
      public short controllerData;
      public MKServer.MKUser teammate;
      public MKServer.MKUser teammate2;
      public int kartID;

      public MKServer getServer() {
         return (MKServer) super.getServer();
      }

      public Packet getLeavePacket() {
         return new MKServer.MKUser.MKLeavePacket(this);
      }

      public void leaveTeam() {
         if (this.teammate != null) {
            this.teammate.teammate2 = null;
            this.teammate = null;
         }

         if (this.teammate2 != null) {
            this.teammate2.teammate = null;
            this.teammate = null;
         }

      }

      public static class MKLeavePacket extends Packet {
         public MKLeavePacket(ServerUser src) {
            super(src);
         }

         public void write(DataOutStream out, ServerUser user) throws IOException {
            out.write((int) 2);
            out.write(this.src.getID());
            out.write(((MKServer.MKUser) this.src).kartID);
         }
      }
   }

   public static class PlayerPacket extends Packet {
      short controllerData;
      int lastTime;

      public void write(DataOutStream out, ServerUser user) throws IOException {
         out.write((int)16);
         out.write(this.src.getID());
         out.writeShort(this.src.getPing());
         out.writeInt(this.lastTime);
         out.writeShort(this.controllerData);
      }
   }

   public static class RaceInfoPacket extends Packet {
      MKServer server;

      public RaceInfoPacket(Server server) {
         this.server = (MKServer)server;
      }

      public void write(DataOutStream out, ServerUser srcUser) throws IOException {
         MKServer.MKUser src = (MKServer.MKUser)srcUser;
         out.write((int)17);
         out.write(src.getID());
         out.writeInt(this.server.bufferLock);
         out.writeInt(this.server.raceSeed);
         out.writeInt(this.server.raceMode);
         out.writeInt(this.server.raceCupID);
         out.writeInt(this.server.raceCupSubID);
         out.writeInt(this.server.raceCC);
         out.writeInt(this.server.raceMirror);
         out.writeShort(this.server.raceOptions);
         out.write(this.server.racePlaces);
         out.writeInt(this.server.getList().size());

         MKServer.MKUser u;
         Iterator var5;
         for(var5 = this.server.mkUsers().iterator(); var5.hasNext(); u.customTrackNextMusic = false) {
            u = (MKServer.MKUser)var5.next();
            out.writeLen16String(u.getUserName());
         }

         out.writeInt(this.server.aiFill ? 8 : this.server.kartList.size());
         var5 = this.server.kartList.iterator();

         while(var5.hasNext()) {
            u = (MKServer.MKUser)var5.next();
            out.writeInt(u.bagnoleID);
            out.writeInt(u.bagnoleUser1);
            out.writeInt(u.getID());
            out.writeInt(u.bagnoleUser2);
            out.writeInt(u.teammate2 != null ? u.teammate2.getID() : -1);
         }

         Random rand = new Random((long)this.server.randomAISeed);
         if (this.server.aiFill) {
            for(int i = 0; i != 8 - this.server.kartList.length(); ++i) {
               out.writeInt(rand.nextInt(21));
               out.writeInt(rand.nextInt(20) + 1);
               out.writeInt(-1);
               out.writeInt(rand.nextInt(20) + 1);
               out.writeInt(-1);
            }
         }

         out.writeInt(src.kartID);
         if (this.server.customTrackPath == null) {
            out.write((int)0);
         } else if (this.server.raceMode == 2) {
            this.server.userPrint("Custom tracks not supported in grand prix !");
            out.write((int)0);
         } else {
            try {
               out.write((int)1);
               out.writeLen16String(this.server.customTrackName);
               out.writeInt(this.server.customTrackArc.length);

               try {
                  String minimap = new String(Files.readAllBytes(Paths.get(this.server.customTrackPath + "minimap.json")));
                  JSONObject json = new JSONObject(minimap);
                  float tlx = json.getFloat("Top Left Corner X");
                  float tlz = json.getFloat("Top Left Corner Z");
                  float brx = json.getFloat("Bottom Right Corner X");
                  float brz = json.getFloat("Bottom Right Corner Z");
                  int or = json.getInt("Orientation");
                  out.writeFloat(tlx);
                  out.writeFloat(tlz);
                  out.writeFloat(brx);
                  out.writeFloat(brz);
                  out.writeInt(or);
               } catch (Exception var12) {
                  out.write(new byte[20]);
               }

               File f = this.server.fixFile(this.server.customTrackPath + "lap_music_normal.ast");
               if (this.server.customMusic && f.exists()) {
                  this.server.currASTFile = this.server.genAST(f);
                  out.writeInt(this.server.currASTFile.length);
                  File f2 = this.server.fixFile(this.server.customTrackPath + "lap_music_fast.ast");
                  if (f2.exists()) {
                     this.server.currASTFileFast = this.server.genAST(f2);
                  } else {
                     this.server.currASTFile = this.server.currASTFile;
                  }

                  out.writeInt(this.server.currASTFileFast.length);
               } else {
                  out.writeInt(0);
                  out.writeInt(0);
               }
            } catch (Exception var13) {
               var13.printStackTrace();
               this.server.reset();
            }

         }
      }
   }
}
