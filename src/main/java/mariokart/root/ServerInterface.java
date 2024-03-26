package root;

import java.io.IOException;

public interface ServerInterface {
  String getVersion();
  
  ServerUser CreateUser();
  
  GameReader.PacketsHandler[] GetRcvPackets();
  
  void ProcessCommand(String paramString, ServerUser paramServerUser);
  
  boolean NeedJoinQueue();
  
  void UserJoin(ServerUser paramServerUser) throws IOException;
  
  void UserLeave(ServerUser paramServerUser) throws IOException;
  
  GameReader.ChildPacket SyncDone();
}
