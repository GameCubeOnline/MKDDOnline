package marioKart;

import java.io.File;
import java.util.Scanner;
import java.util.Vector;
import root.GameReader;
import root.ServerUser;
import root.ktml.KTML2Obj;
import root.ktml.KTMLDecoder;
import root.ktml.KTMLParent;

public class ItemRain extends Thread {
  public static ItemRain Instance;

  public double itemPerSecond = 10.0D;
  public int spawnType = 1;
  public boolean enabled = false;
  public double spawnMinHeight = 5000.0D;
  public double spawnRndHeight = 2500.0D;
  public double spawnRadius = 20000.0D;
  public int[] spawnTable = new int[]{0, 1, 2, 3, 4, 5, 6, 8, 10, 11, 13, 15};
  public int[] spawnWeight = new int[]{100, 33, 100, 100, 33, 60, 20, 60, 10, 33, 20, 100};

  public ItemRain() {
    if (Instance != null)
      return;
    reload();
    Instance = this;
    start();
  }

  public void reload() {
    try {
      KTMLParent p = KTMLDecoder.decode(new Scanner(new File("itemrain.cfg")));
      (new KTML2Obj()).load(this, p);
    } catch (Exception e) {
      System.out.println("Can't load the item rain file");
    }
  }

  @Override
  public void run() {
    double clock = 0.0D;
    while (true) {
      try {
        Thread.sleep(10L);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      if (!enabled || Main.MKInterface.RaceStatus != 2)
        continue;
      clock += 0.01D;
      double spawn = 1.0D / itemPerSecond;
      while (clock >= spawn) {
        clock -= spawn;
        int maxWeight = 0;
        for (int weight : spawnWeight) {
          maxWeight += weight;
        }
        int item = (int) (Math.random() * maxWeight);
        for (int i = 0; i < spawnWeight.length; i++) {
          if (item < spawnWeight[i]) {
            item = spawnTable[i];
            break;
          }
          item -= spawnWeight[i];
        }
        Vector<float[]> poses = Main.MKInterface.mapPoses;
        if (poses.isEmpty()) {
          clock = 0.0D;
          break;
        }
        float[] spos = new float[]{0, 0, 0}; // Initialize with default value

        switch (spawnType) {
          case 0:
            float[] pos = poses.elementAt((int) (Math.random() * poses.size()));
            spos = pos.clone();
            spos[0] += spawnRadius * (Math.random() - 0.5D);
            spos[2] += spawnRadius * (Math.random() - 0.5D);
            break;
          case 1:
            float minX = Float.MAX_VALUE;
            float maxX = Float.MIN_VALUE;
            float minZ = Float.MAX_VALUE;
            float maxZ = Float.MIN_VALUE;
            for (float[] f : poses) {
              if (f[0] < minX)
                minX = f[0];
              if (f[0] > maxX)
                maxX = f[0];
              if (f[2] < minZ)
                minZ = f[2];
              if (f[2] > maxZ)
                maxZ = f[2];
            }
            while (true) {
              float testX = (float) ((maxX - minX) * Math.random() + minX);
              float testZ = (float) ((maxZ - minZ) * Math.random() + minZ);
              boolean found = false;
              for (float[] f : poses) {
                float xDst = testX - f[0];
                float zDst = testZ - f[2];
                if (Math.sqrt(xDst * xDst + zDst * zDst) <= spawnRadius) {
                  spos = new float[]{testX, f[1], testZ};
                  spos[1] += spawnMinHeight + Math.random() * spawnRndHeight;
                  found = true;
                  break;
                }
              }
              if (found)
                break;
            }
            break;
          default:
            spos = new float[]{0, 0, 0};
            break;
        }
        GameReader.CommandPacket cmd = new GameReader.CommandPacket(0,
                new Object[]{item, spos[0], spos[1], spos[2], 0, 0, 0});
        ServerUser.SendPacket(cmd);
      }
    }
  }
}
