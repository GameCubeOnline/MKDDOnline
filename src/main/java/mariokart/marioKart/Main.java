package marioKart;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Vector;
import org.json.JSONObject;
import root.GameReader;
import root.Root;
import root.ServerInterface;
import root.ServerUser;

public class Main {
  public static void main(String[] Args) {
    new ItemRain();
    new Root(new MKInterface());
  }
  
  public static class MKInterface implements ServerInterface {
    static int RaceSeed;
    
    static int RaceMode;
    
    static int RaceCupID;
    
    static int RaceCupSubID;
    
    static int RaceStatus;
    
    static int RaceCC;
    
    static int RaceMirror;
    
    static short RaceOptions;
    
    public ServerUser CreateUser() {
      return new Main.MKUser();
    }
    
    public String getVersion() {
      return "DoubleDashOnlineV0.0.1";
    }
    
    public GameReader.PacketsHandler[] GetRcvPackets() {
      return RcvPackets;
    }
    
    public void ProcessCommand(String Cmd, ServerUser U) {
      try {
        ServerUser Targ;
        byte b;
        int i;
        String s;
        DataInputStream in;
        byte[] name;
        String[] line = Cmd.split(" ");
        switch (line[0].toLowerCase()) {
          case "itemrain":
            if (ItemRain.Instance == null)
              return; 
            switch (line[1].toLowerCase()) {
              case "on":
                ItemRain.Instance.enabled = true;
                System.out.println("Enabled item rain !");
                break;
              case "off":
                ItemRain.Instance.enabled = false;
                System.out.println("Disabled item rain !");
                break;
              case "reload":
                ItemRain.Instance.reload();
                System.out.println("Reloaded item rain !");
                break;
            }
            return;
          case "stoprace":
            ServerUser.SendPacket((GameReader.ChildPacket)new GameReader.ControlReply(19));
            return;
          case "kick":
            Targ = ServerUser.Get(line[1]);
            if (Targ != null)
              Targ.S.close();
            return;
          case "track":
            s = System.getProperty("user.dir") + "/Tracks/" + line[1];
            if (!(new File(s)).exists())
              return;
            CustomTrackPath = String.valueOf(s) + "/";
            in = new DataInputStream(new FileInputStream(new File(String.valueOf(CustomTrackPath) + "Track.arc")));
            in.skip(52L);
            in.skip((in.readInt() - 16));
            while (in.readByte() != 0);
            name = new byte[64];
            for (i = 0; i != 64 && (b = in.readByte()) != 95; i++)
              name[i] = b; 
            in.close();
            CustomTrackName = new String(name, 0, i);
            System.out.println("Custom track loaded successfully");
            return;
        } 
        System.out.println("Can't find command : " + line[1]);
      } catch (Exception e) {
        System.out.println("Can't parse command : " + Cmd);
      } 
    }
    
    public void UserJoin(ServerUser U) throws IOException {
      while (RaceStatus != 0) {
        try {
          Thread.sleep(10L);
        } catch (Exception exception) {}
      } 
      U.Out.write((byte)(U.Master ? 1 : 0));
      U.Out.write(ServerUser.List.size());
      for (ServerUser U2 : ServerUser.List) {
        U.Out.writeByte(U2.ID);
        U.Out.writeUTF(U2.Name);
      } 
      U.SendBuffer();
      U.Name = U.ReadUTF();
    }
    
    public void UserLeave(ServerUser U) throws IOException {
      if (ServerUser.List.size() == 0)
        RaceStatus = 0; 
    }
    
    public boolean NeedJoinQueue() {
      return false;
    }
    
    public static GameReader.ChildPacket HandleUserRaceInfo(GameReader R) throws Exception {
      DataInputStream In = R.User.In;
      Main.MKUser U = (Main.MKUser)R.User;
      U.BagnoleID = In.readInt();
      U.BagnoleUser1 = In.readInt();
      U.BagnoleUser2 = In.readInt();
      RaceStartSync = true;
      GameReader.ChildPacket Ret = U.Game.HandleSync();
      if (Ret != null) {
        RaceStartSync = false;
        return new Main.RaceInfoPacket();
      } 
      return null;
    }
    
    public static GameReader.ChildPacket HandleMasterRaceInfo(GameReader R) throws IOException {
      DataInputStream In = R.User.In;
      RaceSeed = In.readInt();
      RaceMode = In.readInt();
      RaceCupID = In.readInt();
      RaceCupSubID = In.readInt();
      RaceCC = In.readInt();
      RaceMirror = In.readInt();
      RaceOptions = In.readShort();
      In.read(RacePlaces);
      return null;
    }
    
    public static GameReader.ChildPacket HandleMasterRaceInfoCourse(GameReader R) throws IOException {
      Arrays.fill(ServerUser.TestRng, 0);
      DataInputStream In = R.User.In;
      mapPoses.clear();
      int f;
      while ((f = In.readInt()) != -1) {
        float[] p = { Float.intBitsToFloat(f), In.readFloat(), In.readFloat() };
        mapPoses.add(p);
      } 
      return null;
    }
    
    public static GameReader.ChildPacket HandlePlayer(GameReader R) throws IOException {
      Main.PlayerPacket Packet2 = new Main.PlayerPacket();
      Main.MKUser U = (Main.MKUser)R.User;
      DataInputStream In = R.User.In;
      Packet2.Target = R.User;
      Packet2.NTarget = true;
      Packet2.LastTime = In.readInt();
      int Time = U.LastTime = In.readInt();
      int RNG = In.readInt();
      if (Time > 0)
        if (ServerUser.TestRng[Time] == 0) {
          ServerUser.TestRng[Time] = RNG;
        } else if (ServerUser.TestRng[Time] != RNG) {
          System.out.println("DESYNC");
        }  
      boolean Len = true;
      if (U.LastRcvTime != 0L);
      U.LastRcvTime = System.nanoTime();
      Packet2.ControllerData = In.readShort();
      return Packet2;
    }
    
    public static GameReader.ChildPacket HandleCursor(GameReader R) throws Exception {
      Arrays.fill(ServerUser.TestRng, 0);
      return new Main.CursorPacket(R.User.In.read());
    }
    
    public static GameReader.ChildPacket HandleRaceStatusChange(GameReader R) throws IOException {
      RaceStatus = R.User.In.readByte();
      return null;
    }
    
    public static GameReader.ChildPacket HandleLoadAudio(GameReader R) throws IOException {
      Main.LoadAudioPacket Ret = new Main.LoadAudioPacket(R.User.In.readInt(), R.User.In.readInt());
      Ret.Target = R.User;
      return Ret;
    }
    
    public GameReader.ChildPacket SyncDone() {
      if (RaceStartSync) {
        RaceStartSync = false;
        return new Main.RaceInfoPacket();
      } 
      return (GameReader.ChildPacket)new GameReader.ControlReply(0);
    }
    
    static byte[] RacePlaces = new byte[32];
    
    static boolean RaceStartSync;
    
    static String CustomTrackName;
    
    static String CustomTrackPath;
    
    static Vector<float[]> mapPoses = (Vector)new Vector<>();
    
    public static final GameReader.PacketsHandler[] RcvPackets;
    
    static {
      RcvPackets = new GameReader.PacketsHandler[] { R -> HandlePlayer(R), R -> HandleMasterRaceInfo(R), R -> HandleUserRaceInfo(R), R -> HandleCursor(R), R -> HandleMasterRaceInfoCourse(R), R -> HandleRaceStatusChange(R), R -> HandleLoadAudio(R) };
    }
  }
  
  public static class RaceInfoPacket extends GameReader.ChildPacket {
    public RaceInfoPacket() {
      ServerUser.ClearIDs();
      for (ServerUser U : ServerUser.List)
        U.AllocID(); 
    }
    
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Out.write(17);
      Out.writeByte(User.ID);
      Out.writeInt(Main.MKInterface.RaceSeed);
      Out.writeInt(Main.MKInterface.RaceMode);
      Out.writeInt(Main.MKInterface.RaceCupID);
      Out.writeInt(Main.MKInterface.RaceCupSubID);
      Out.writeInt(Main.MKInterface.RaceCC);
      Out.writeInt(Main.MKInterface.RaceMirror);
      Out.writeShort(Main.MKInterface.RaceOptions);
      Out.write(Main.MKInterface.RacePlaces);
      Out.writeInt(ServerUser.List.size());
      for (ServerUser u : ServerUser.List) {
        Main.MKUser U = (Main.MKUser)u;
        Out.writeInt(U.BagnoleID);
        Out.writeInt(U.BagnoleUser1);
        Out.writeInt(U.BagnoleUser2);
        Out.writeUTF(U.Name);
        U.CustomTrackNextMusic = false;
      } 
      if (Main.MKInterface.CustomTrackPath == null) {
        Out.write(0);
        return;
      } 
      if (Main.MKInterface.RaceMode == 2) {
        System.out.println("Custom tracks not supported in grand prix !");
        Out.write(0);
        return;
      } 
      try {
        byte[] arc = Files.readAllBytes(Paths.get(String.valueOf(Main.MKInterface.CustomTrackPath) + "Track.arc", new String[0]));
        Out.write(1);
        Out.writeUTF(Main.MKInterface.CustomTrackName);
        Out.writeInt(arc.length);
        User.WriteBigData(arc);
        try {
          String minimap = new String(Files.readAllBytes(Paths.get(String.valueOf(Main.MKInterface.CustomTrackPath) + "minimap.json", new String[0])));
          JSONObject json = new JSONObject(minimap);
          float tlx = json.getFloat("Top Left Corner X");
          float tlz = json.getFloat("Top Left Corner Z");
          float brx = json.getFloat("Bottom Right Corner X");
          float brz = json.getFloat("Bottom Right Corner Z");
          int or = json.getInt("Orientation");
          Out.writeFloat(tlx);
          Out.writeFloat(tlz);
          Out.writeFloat(brx);
          Out.writeFloat(brz);
          Out.writeInt(or);
        } catch (Exception e) {
          Out.write(new byte[20]);
        } 
        File F = new File(String.valueOf(Main.MKInterface.CustomTrackPath) + "lap_music_normal.ast");
        Out.writeInt(0);
        Out.writeInt(0);
        Main.MKInterface.CustomTrackPath = null;
      } catch (Exception e) {
        Out.write(0);
      } 
    }
  }
  
  public static class PlayerPacket extends GameReader.ChildPacket {
    short ControllerData;
    
    int LastTime;
    
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Out.writeByte(16);
      Out.writeByte(this.Src.ID);
      Out.writeShort(this.Src.getPing());
      Out.writeInt(this.LastTime);
      Out.writeShort(this.ControllerData);
    }
  }
  
  public static class MKUser extends ServerUser {
    public int BagnoleID;
    
    public int BagnoleUser1;
    
    public int BagnoleUser2;
    
    public boolean CustomTrackNextMusic;
  }
  
  public static class LoadAudioPacket extends GameReader.ChildPacket {
    int Off;
    
    int Len;
    
    public LoadAudioPacket(int Off, int Len) {
      this.Off = Off;
      this.Len = Len;
    }
    
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Out.write(20);
      Main.MKUser U = (Main.MKUser)User;
      if (this.Off == 0)
        U.CustomTrackNextMusic = !U.CustomTrackNextMusic;
      FileInputStream In = new FileInputStream(new File(String.valueOf(Main.MKInterface.CustomTrackPath) + (U.CustomTrackNextMusic ? "lap_music_normal.ast" : "lap_music_fast.ast")));
      In.skip(this.Off);
      byte[] Send = new byte[this.Len];
      int Rem;
      for (Rem = this.Len; Rem != 0; Rem -= In.read(Send, this.Len - Rem, Rem));
      User.DownloadData(Send);
      In.close();
    }
  }
  
  public static class CursorPacket extends GameReader.ChildPacket {
    int Cursor;
    
    public CursorPacket(int Cursor) {
      this.Cursor = Cursor;
    }
    
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Out.writeByte(18);
      Out.write(this.Cursor);
    }
  }
}
