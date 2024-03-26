package root;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerAccept extends Thread {
  boolean Stopped;
  ServerInterface Inter;
  public static ServerSocket S;
  public static int port;

  public ServerAccept(ServerInterface Inter) {
    this.Inter = Inter;
    try {
      BufferedReader reader = new BufferedReader(new FileReader("port.cfg"));
      port = Integer.parseInt(reader.readLine().trim());
      reader.close();
    } catch (IOException | NumberFormatException e) {
      System.out.println("Error reading port from port.cfg. Using default port 10014");
      port = 10014; // Default port if reading from file fails
    }
  }

  public void run() {
    try {
      S = new ServerSocket(port);
      S.setSoTimeout(10);
    } catch (Exception E) {
      System.out.println("Cannot create socket, exiting");
      E.printStackTrace();
      System.exit(-1);
    }
    while (!this.Stopped) {
      try {
        Socket Client = S.accept();
        ServerUser.Create(Client, this.Inter);
      } catch (Exception exception) {
      }
    }
    try {
      S.close();
    } catch (Exception exception) {
    }
  }

  public static void OutPrint(String S) {
    System.out.println(S);
  }

  public static void MalformedError(String Name) {
    OutPrint(String.valueOf(Name) + " tried to output a malformed packet");
  }

  public static void TimeOutError(String Name) {
    OutPrint(String.valueOf(Name) + " took too long to send a packet");
  }

  public static void ExitInfo(String Name) {
    OutPrint("User " + Name + " left the server");
  }
}