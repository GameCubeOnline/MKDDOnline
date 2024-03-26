package root;

import java.io.DataOutputStream;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class GameWriter {
  ServerUser User;
  
  DataOutputStream Out;
  
  SafeList<ServerUser> InList = new SafeList<>();
  
  ReentrantLock PacketLock = new ReentrantLock();
  
  Vector<GameReader.Packet> Packets = new Vector<>();
  
  public GameWriter(ServerUser User) {
    this.User = User;
  }
  
  public void Run() throws Exception {
    this.Out = this.User.Out;
    this.PacketLock.lock();
    Object[] O = this.Packets.toArray();
    this.Packets.clear();
    this.PacketLock.unlock();
    GameReader.Packet[] Packs = new GameReader.Packet[O.length];
    for (int i = 0; i != O.length; ) {
      Packs[i] = (GameReader.Packet)O[i];
      i++;
    } 
    byte b;
    int j;
    GameReader.Packet[] arrayOfPacket1;
    for (j = (arrayOfPacket1 = Packs).length, b = 0; b < j; ) {
      GameReader.Packet Pack = arrayOfPacket1[b];
      for (GameReader.ChildPacket CP : Pack.Child) {
        if (CP.Target != null && (
          CP.NTarget ? (
          CP.Target == this.User) : (
          
          CP.Target != this.User)))
          continue; 
        CP.write(this.Out, this.User);
      } 
      b++;
    } 
    this.User.SendBuffer();
  }
}
