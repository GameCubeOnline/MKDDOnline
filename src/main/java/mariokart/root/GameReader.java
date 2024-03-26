package root;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;

public class GameReader {
  public ServerUser User;
  
  public ServerInterface Inter;
  
  public DataInputStream In;
  
  public static final PacketsHandler[] RcvPackets;
  
  static {
    RcvPackets = new PacketsHandler[] { R -> R.HandleSync(), R -> R.HandleMessage(), R -> R.HandleHeartBeat(), R -> R.HandleMic(), R -> R.HandleDownloadStatus() };
  }
  
  public GameReader(ServerUser User) {
    this.User = User;
    this.Inter = User.Inter;
  }
  
  public void Run() throws Exception {
    this.In = this.User.In;
    this.User.Pack = new Packet();
    byte Next = -1;
    while (true) {
      ChildPacket CP;
      try {
        this.User.S.setSoTimeout(1);
        Next = this.In.readByte();
        this.User.S.setSoTimeout(0);
      } catch (Exception E) {
        break;
      } 
      if (Next == -1)
        break; 
      if (Next >= 16) {
        CP = this.Inter.GetRcvPackets()[Next - 16].Do(this);
      } else {
        CP = RcvPackets[Next].Do(this);
      } 
      if (CP != null) {
        CP.Src = this.User;
        this.User.Pack.Child.add(CP);
      } 
    } 
    if (this.User.VoiceReq) {
      this.User.Pack.Child.add(new MixerPacket());
      this.User.VoiceReq = false;
    } 
    if (this.User.CurrDownload != null && this.User.CurrDownload.doneTime != 0L) {
      if (this.User.CurrDownload.doneTime != 0L) {
        ChildPacket CP = new ControlReply(7);
        CP.Target = this.User;
        this.User.Pack.Child.add(CP);
        this.User.CurrDownload.doneTime = 0L;
      } 
    } else if (!this.User.DownloadQueue.isEmpty()) {
      this.User.CurrDownload = this.User.DownloadQueue.pop();
      this.User.CurrDownload.Send();
    } 
    if (!this.User.Pack.Child.isEmpty()) {
      ChildPacket CP = new PingPacket();
      CP.Target = this.User;
      this.User.Pack.Child.add(CP);
      this.User.SkippedTime = 0;
    } else if (!this.User.Master) {
      this.User.SkippedTime++;
      if (ServerUser.SyncCnt != 0) {
        if (this.User.SkippedTime > 5000)
          this.User.Exit = true; 
      } else if (this.User.SkippedTime > 300000) {
        this.User.Exit = true;
      } 
    } 
    ServerUser.SendPacket(this.User.Pack);
  }
  
  public ChildPacket HandleSync() throws Exception {
    ServerUser.SyncLock.lock();
    if (ServerUser.SyncCnt == 0) {
      System.out.println("Sync rcvd");
      for (ServerUser U : ServerUser.List)
        U.UserSync = false; 
    } 
    this.User.UserSync = true;
    ServerUser.SyncCnt++;
    return SyncUpdate();
  }
  
  public ChildPacket SyncUpdate() {
    if (ServerUser.SyncCnt == ServerUser.List.size()) {
      System.out.println("Sync done");
      ServerUser.SyncCnt = 0;
      for (ServerUser U : ServerUser.List) {
        U.SkippedTime = 0;
        U.LastRcvTime = 0L;
      } 
      ServerUser.SyncLock.unlock();
      return this.Inter.SyncDone();
    } 
    ServerUser.SyncLock.unlock();
    return null;
  }
  
  public ChildPacket HandleMessage() throws Exception {
    short Len = this.In.readShort();
    if (Len > 100)
      throw new Exception(); 
    String Cont = this.User.ReadUTF(Len);
    if (Cont.charAt(0) == '/' && this.User.Master) {
      this.Inter.ProcessCommand(Cont.substring(1), this.User);
      return null;
    } 
    String Msg = String.valueOf(this.User.Name) + ":" + Cont;
    System.out.println(Msg);
    return new MesagePacket(Msg);
  }
  
  public ChildPacket HandleHeartBeat() throws Exception {
    this.User.SkippedTime = 0;
    return null;
  }
  
  public ChildPacket HandleMic() throws Exception {
    int Len = this.In.readInt();
    byte[] Buf = new byte[Len];
    this.In.readFully(Buf);
    Mixer.AddVoice(Buf, this, 11025);
    return null;
  }
  
  public ChildPacket HandleDownloadStatus() throws Exception {
    if (this.In.read() == 1) {
      System.out.println("ALL PACKETS ARRIVED");
      this.User.CurrDownload = null;
      return null;
    } 
    boolean[] B = this.User.CurrDownload.confirmed;
    byte[] Rcv = new byte[B.length + 7 >> 3];
    this.In.read(Rcv);
    for (int i = 0; i != B.length; i++) {
      byte b1 = Rcv[i >> 3];
      B[i] = ((b1 & 1 << (i & 0x7)) != 0);
    } 
    double tcnt = 0.0D;
    byte b;
    int j;
    boolean[] arrayOfBoolean1;
    for (j = (arrayOfBoolean1 = B).length, b = 0; b < j; ) {
      boolean bool = arrayOfBoolean1[b];
      if (bool)
        tcnt++; 
      b++;
    } 
    System.out.println(String.valueOf(tcnt / B.length * 100.0D) + "% PACKETS ARRIVED");
    this.User.CurrDownload.Send();
    return null;
  }
  
  public static class Packet {
    public Vector<GameReader.ChildPacket> Child = new Vector<>();
  }
  
  public static abstract class ChildPacket {
    public ServerUser Src;
    
    public ServerUser Target;
    
    public boolean NTarget;
    
    public ChildPacket() {}
    
    public ChildPacket(ServerUser Src) {
      this.Src = Src;
    }
    
    public abstract void write(DataOutputStream param1DataOutputStream, ServerUser param1ServerUser) throws IOException;
    
    public int GetTime() {
      int B = -1;
      for (ServerUser U : ServerUser.List) {
        if (U.LastTime > B)
          B = U.LastTime; 
      } 
      return B + 10;
    }
  }
  
  public static class ControlReply extends ChildPacket {
    int ID;
    
    public ControlReply(int ID) {
      this.ID = ID;
    }
    
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Out.writeByte(this.ID);
    }
  }
  
  public static class MesagePacket extends ChildPacket {
    String Msg;
    
    public MesagePacket(String Msg) {
      this.Msg = Msg;
    }
    
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Out.writeByte(4);
      Out.writeUTF(this.Msg);
    }
  }
  
  public static class PingPacket extends ChildPacket {
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Out.writeByte(3);
      Out.writeShort(this.Target.getPing());
    }
  }
  
  public static class JoinPacket extends ChildPacket {
    public JoinPacket(ServerUser Src) {
      super(Src);
    }
    
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Out.writeByte(1);
      Out.writeByte(this.Src.ID);
      Out.writeUTF(this.Src.Name);
    }
  }
  
  public static class LeavePacket extends ChildPacket {
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Out.writeByte(2);
      Out.writeByte(this.Src.ID);
    }
  }
  
  public static class MixerPacket extends ChildPacket {
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Mixer.Mix(User, Out);
    }
  }
  
  public static class CommandPacket extends ChildPacket {
    int ID;
    
    int Time;
    
    public Object[] Parms;
    
    public CommandPacket(int ID, Object... Parms) {
      this.ID = ID;
      this.Parms = Parms;
      this.Time = GetTime();
    }
    
    public void write(DataOutputStream Out, ServerUser User) throws IOException {
      Out.writeByte(6);
      Out.writeInt(this.Parms.length);
      Out.writeInt(this.ID);
      Out.writeInt(this.Time);
      byte b;
      int i;
      Object[] arrayOfObject;
      for (i = (arrayOfObject = this.Parms).length, b = 0; b < i; ) {
        Object O = arrayOfObject[b];
        Class<?> Cls = O.getClass();
        if (Cls == Integer.class) {
          Out.writeInt(((Integer)O).intValue());
        } else if (Cls == Float.class) {
          Out.writeFloat(((Float)O).floatValue());
        } 
        b++;
      } 
    }
  }
  
  public void Print() {}
  
  public static interface PacketsHandler {
    GameReader.ChildPacket Do(GameReader param1GameReader) throws Exception;
  }
}
