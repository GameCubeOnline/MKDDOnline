package root;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashSet;
import java.util.Random;
import java.util.Scanner;
import java.util.Stack;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class ServerUser extends Thread {
  public static final int TimeOut = 5000;
  
  public static InetAddress MasterAddress;
  
  public static Vector<String> RandomName = new Vector<>();
  
  static {
    try {
      MasterAddress = InetAddress.getByName("localhost");
      Scanner Scan = new Scanner(NewStream("../NameList.txt"));
      while (Scan.hasNextLine())
        RandomName.add(Scan.nextLine()); 
      Scan.close();
    } catch (Exception exception) {}
  }
  
  public static int[] TestRng = new int[16777216];
  
  public static SafeList<ServerUser> List = new SafeList<>();
  
  public static SafeList<ServerUser> JoinList = new SafeList<>();
  
  public static ReentrantLock JoinLock = new ReentrantLock();
  
  public static HashSet<String> BanMap = new HashSet<>();
  
  public Socket S;
  
  public DataInputStream In;
  
  public DataOutputStream Out;
  
  public ByteArrayOutputStream OutB;
  
  public boolean Master;
  
  public String Name = "An user";
  
  public boolean Left;
  
  public boolean Exit;
  
  public GameReader Game;
  
  public GameWriter GameWrite = new GameWriter(this);
  
  public int ID = -1;
  
  public volatile boolean Joined;
  
  public ServerInterface Inter;
  
  public DownloadServer.Message CurrDownload;
  
  public Stack<DownloadServer.Message> DownloadQueue = new Stack<>();
  
  public DownloadServer DlServer = new DownloadServer();
  
  GameReader.Packet Pack;
  
  public int[] PingBuf = new int[10];
  
  volatile boolean InvalidateReq;
  
  public long LastRcvTime;
  
  volatile int SkippedTime = 0;
  
  volatile boolean UserSync;
  
  public int LastTime;
  
  public static volatile int SyncCnt;
  
  public static ReentrantLock SyncLock = new ReentrantLock();
  
  public volatile boolean VoiceReq;
  
  public short MixerCVal;
  
  public static void Create(Socket S, ServerInterface Inter) throws SocketException {
    ServerUser Ret = Inter.CreateUser();
    Ret.S = S;
    S.setTcpNoDelay(true);
    Ret.Inter = Inter;
    Ret.Game = new GameReader(Ret);
    Ret.DlServer.start();
    Ret.start();
  }
  
  public boolean Create() {
    JoinLock.lock();
    if (List.size() + JoinList.size() >= 16) {
      JoinLock.unlock();
      return false;
    } 
    try {
      if ((List.isEmpty() && JoinList.isEmpty()) || List.size() + JoinList.size() >= 8)
        this.Master = true; 
      this.In = new DataInputStream(this.S.getInputStream());
      this.Out = new DataOutputStream(this.OutB = new ByteArrayOutputStream());
      if (this.In.readUTF().compareTo(this.Inter.getVersion()) != 0) {
        JoinLock.unlock();
        return false;
      } 
      this.Inter.UserJoin(this);
      if (this.Name.isEmpty() && RandomName.size() > 0) {
        this.Name = null;
        Random Rand = new Random();
        while (this.Name == null) {
          String Name = RandomName.get(Rand.nextInt(RandomName.size()));
          if (Get(Name) != null)
            continue; 
          this.Name = Name;
        } 
      } 
      if (BanMap.contains(this.Name)) {
        ServerAccept.OutPrint("User " + this.Name + " tried to join but is banned.");
        JoinLock.unlock();
        return false;
      } 
      ServerAccept.OutPrint("User " + this.Name + " Joined.");
      setName(String.valueOf(this.Name) + " User thread");
      this.DlServer.setName(String.valueOf(this.Name) + " download thread");
      if (this.Master)
        this.Joined = true; 
      JoinList.add(this);
      JoinLock.unlock();
      return true;
    } catch (SocketTimeoutException E) {
      ServerAccept.TimeOutError(this.Name);
    } catch (Exception exception) {}
    JoinLock.unlock();
    return false;
  }
  
  public String ReadUTF() throws IOException {
    short Len = this.In.readShort();
    byte[] Str = new byte[Len];
    for (int i = 0; i != Len; ) {
      Str[i] = this.In.readByte();
      i++;
    } 
    return new String(Str);
  }
  
  public String ReadUTF(int Len) throws IOException {
    byte[] Str = new byte[Len];
    for (int i = 0; i != Len; ) {
      Str[i] = this.In.readByte();
      i++;
    } 
    return new String(Str);
  }
  
  public int getPing() {
    long l = 0L;
    byte b;
    int i, arrayOfInt[];
    for (i = (arrayOfInt = this.PingBuf).length, b = 0; b < i; ) {
      int j = arrayOfInt[b];
      l += j;
      b++;
    } 
    int ret = (int)(l / this.PingBuf.length);
    if (ret < 0)
      ret = 0; 
    if (ret > 1000)
      ret = 1000; 
    return ret;
  }
  
  public void Leave() {
    if (this.Left)
      return; 
    JoinLock.lock();
    try {
      if (!this.Joined) {
        JoinList.remove(this);
      } else {
        List.remove(this);
      } 
      this.S.close();
      GameReader.Packet Pack = new GameReader.Packet();
      GameReader.ChildPacket CP = new GameReader.LeavePacket();
      CP.Src = this;
      Pack.Child.add(CP);
      SyncLock.lock();
      if (SyncCnt != 0 && this.UserSync)
        SyncCnt--; 
      CP = this.Game.SyncUpdate();
      if (CP != null)
        Pack.Child.add(CP); 
      SendPacket(Pack);
      for (ServerUser U : List)
        U.LastRcvTime = 0L; 
      Mixer.RemoveVoice(this);
      this.DlServer.stop = true;
      this.DlServer.interrupt();
      this.Left = true;
      this.Inter.UserLeave(this);
    } catch (Exception exception) {}
    JoinLock.unlock();
  }
  
  public String ReadString() throws IOException {
    byte[] NameBuf = new byte[31];
    int NameCPos = 0;
    int B;
    while ((B = this.In.read()) != 0) {
      if (NameCPos == NameBuf.length)
        ServerAccept.MalformedError(new String(NameBuf)); 
      NameBuf[NameCPos++] = (byte)B;
    } 
    return new String(NameBuf, 0, NameCPos);
  }
  
  public void WriteString(String Str) throws IOException {
    if (Str != null)
      this.Out.write(Str.getBytes()); 
    this.Out.write(0);
  }
  
  public void WriteBigData(byte[] Send) throws IOException {
    if (Send.length > 8192) {
      int i = 0;
      for (; i != Send.length / 8192; i++) {
        this.Out.write(Send, i * 8192, 8192);
        SendBuffer();
        try {
          Thread.sleep(1L);
        } catch (Exception exception) {}
      } 
      this.Out.write(Send, i * 8192, Send.length - i * 8192);
    } else {
      this.Out.write(Send);
    } 
  }
  
  public void WriteRLE4(byte[] Data) throws IOException {
    DataInputStream In = new DataInputStream(new ByteArrayInputStream(Data));
    int Rem = Data.length / 4;
    int WCnt = 0;
    while (Rem != 0) {
      if (Rem < 4) {
        for (int i = 0; i != Rem; ) {
          this.Out.writeInt(In.readInt());
          i++;
        } 
        break;
      } 
      int i1 = In.readInt();
      int i2 = In.readInt();
      if (i1 != i2) {
        RLEWritePontentialToken(i1);
        RLEWritePontentialToken(i2);
        WCnt += 8;
        Rem -= 2;
        continue;
      } 
      int i3 = In.readInt();
      if (i1 != i3) {
        RLEWritePontentialToken(i1);
        RLEWritePontentialToken(i2);
        RLEWritePontentialToken(i3);
        WCnt += 12;
        Rem -= 3;
        continue;
      } 
      int i4 = In.readInt();
      if (i1 != i4) {
        RLEWritePontentialToken(i1);
        RLEWritePontentialToken(i2);
        RLEWritePontentialToken(i3);
        RLEWritePontentialToken(i4);
        WCnt += 16;
        Rem -= 4;
        continue;
      } 
      int LRem = Rem;
      Rem -= 4;
      for (; Rem > 0 && (i2 = In.readInt()) == i1; Rem--);
      int Len = LRem - Rem;
      this.Out.writeInt(1380730179);
      this.Out.writeInt(Len);
      this.Out.writeInt(i1);
      WCnt += 12;
      if (Rem > 0) {
        this.Out.writeInt(i2);
        WCnt += 4;
        Rem--;
      } 
    } 
  }
  
  private void RLEWritePontentialToken(int i) throws IOException {
    this.Out.writeInt(i);
    if (i == 1380730179)
      this.Out.writeInt(i); 
  }
  
  public void SendBuffer() throws IOException {
    if (this.OutB.size() == 0)
      return; 
    byte[] send = this.OutB.toByteArray();
    this.S.getOutputStream().write(send);
    this.OutB.reset();
  }
  
  public void run() {
    Run();
    for (; !this.Left; Leave());
  }
  
  public void Run() {
    if (!Create())
      return; 
    if (this.Inter.NeedJoinQueue()) {
      while (!this.Joined) {
        synchronized (this) {
          try {
            wait(2L);
            if (this.Exit)
              return; 
          } catch (InterruptedException interruptedException) {}
        } 
      } 
    } else {
      this.Joined = true;
    } 
    JoinLock.lock();
    List.add(this);
    JoinList.remove(this);
    JoinLock.unlock();
    AllocID();
    SendPacket(new GameReader.JoinPacket(this));
    while (!this.Exit) {
      try {
        this.Game.Run();
        this.GameWrite.Run();
        this.Game.Print();
      } catch (SocketTimeoutException socketTimeoutException) {
      
      } catch (SocketException E) {
        this.Exit = true;
        ServerAccept.ExitInfo(this.Name);
      } catch (Exception E) {
        E.printStackTrace();
        ServerAccept.MalformedError(this.Name);
        this.Exit = true;
      } 
    } 
  }
  
  public void AllocID() {
    if (this.ID == 0)
      return; 
    JoinLock.lock();
    this.ID = 0;
    label19: while (true) {
      for (ServerUser U : List) {
        if (this != U && 
          U.ID == this.ID) {
          this.ID++;
          continue label19;
        } 
      } 
      break;
    } 
    if (this.ID == 0)
      this.Master = true; 
    JoinLock.unlock();
  }
  
  public static void ClearIDs() {
    for (ServerUser U : List)
      U.ID = -1; 
  }
  
  public static class UserCommand {
    int ID;
    
    ServerUser U;
    
    public UserCommand(int ID, ServerUser U) {
      this.ID = ID;
      this.U = U;
    }
  }
  
  public static void SendPacket(GameReader.ChildPacket Pack) {
    GameReader.Packet Send = new GameReader.Packet();
    Send.Child.add(Pack);
    SendPacket(Send);
  }
  
  public static void SendPacket(GameReader.Packet Pack) {
    for (ServerUser U : List) {
      U.GameWrite.PacketLock.lock();
      U.GameWrite.Packets.add(Pack);
      U.GameWrite.PacketLock.unlock();
    } 
  }
  
  public static ServerUser Get(String ID) {
    int IID = Integer.MIN_VALUE;
    try {
      IID = Integer.decode(ID).intValue();
    } catch (Exception exception) {}
    for (ServerUser U : List) {
      if (U.ID == IID)
        return U; 
      if (U.Name.compareToIgnoreCase(ID) == 0)
        return U; 
    } 
    return null;
  }
  
  public void DownloadData(byte[] data) {
    this.DownloadQueue.push(new DownloadServer.Message(data, this));
  }
  
  public static void BanListEntry(ServerUser U) {
    BanMap.add(U.Name);
  }
  
  static final DataInputStream NewStream(String Path) {
    DataInputStream I;
    if (ServerUser.class.getResource("/root/" + Path) == null) {
      I = new DataInputStream(ServerUser.class.getClassLoader().getResourceAsStream(Path));
    } else {
      I = new DataInputStream(ServerUser.class.getResourceAsStream("/root/" + Path));
    } 
    return I;
  }
}
