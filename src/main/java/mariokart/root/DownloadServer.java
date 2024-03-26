package root;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.concurrent.SynchronousQueue;

public class DownloadServer extends Thread {
  public SynchronousQueue<Message> messageQueue = new SynchronousQueue<>();

  byte[] transfer = new byte[1032];

  DatagramSocket socket;

  DatagramPacket pack = new DatagramPacket(new byte[0], 0);

  Message msg;

  boolean stop;

  int port;

  public DownloadServer() {
    try {
      BufferedReader reader = new BufferedReader(new FileReader("port.cfg"));
      this.port = Integer.parseInt(reader.readLine().trim());
      reader.close();
    } catch (IOException | NumberFormatException e) {
      this.port = 10014; // Default port if reading from file fails
    }
  }

  public void run() {
    try {
      this.socket = new DatagramSocket();
    } catch (Exception e) {
      System.out.println("cannot create download server socket");
      System.exit(-1);
    }
    while (!this.stop) {
      try {
        this.msg = this.messageQueue.take();
        this.pack.setAddress(this.msg.target.S.getInetAddress());
        this.pack.setPort(port); // Use the port read from port.cfg
        int i = 0;
        for (; i != this.msg.data.length / 1024; i++)
          sendBytes(this.msg.data, i * 1024, 1024, i);
        if ((this.msg.data.length & 0x3FF) != 0)
          sendBytes(this.msg.data, i * 1024, this.msg.data.length - i * 1024, i);
        this.msg.doneTime = System.currentTimeMillis();
      } catch (Exception exception) {
      }
    }
    this.socket.close();
  }

  void sendBytes(byte[] data, int off, int len, int idx) throws IOException {
    if (this.msg.confirmed[idx])
      return;
    try {
      Thread.sleep(1L);
    } catch (Exception exception) {
    }
    int checkSum = 0;
    for (int i = off; i != off + len; ) {
      checkSum += data[i] & 0xFF;
      i++;
    }
    System.arraycopy(data, off, this.transfer, 8, len);
    this.transfer[4] = (byte) ((idx & 0xFF000000) >> 24);
    this.transfer[5] = (byte) ((idx & 0xFF0000) >> 16);
    this.transfer[6] = (byte) ((idx & 0xFF00) >> 8);
    this.transfer[7] = (byte) ((idx & 0xFF) >> 0);
    checkSum += this.transfer[4] + this.transfer[5] + this.transfer[6] + this.transfer[7];
    this.transfer[0] = (byte) ((checkSum & 0xFF000000) >> 24);
    this.transfer[1] = (byte) ((checkSum & 0xFF0000) >> 16);
    this.transfer[2] = (byte) ((checkSum & 0xFF00) >> 8);
    this.transfer[3] = (byte) ((checkSum & 0xFF) >> 0);
    this.pack.setData(this.transfer, 0, len + 8);
    this.socket.send(this.pack);
  }

  public static class Message {
    byte[] data;

    boolean[] confirmed;

    ServerUser target;

    long doneTime;

    public Message(byte[] data, ServerUser target) {
      this.data = data;
      this.target = target;
      this.confirmed = new boolean[(data.length + 1023) / 1024];
    }

    public void Send() {
      try {
        this.target.DlServer.messageQueue.put(this);
      } catch (Exception exception) {
      }
    }
  }
}