package marioKart;

import com.kirbymimi.mmb.ut.ktml.KTML2Obj;
import com.kirbymimi.mmb.ut.ktml.KTMLDecoder;
import java.util.Iterator;
import java.util.Vector;
import root.Packet;
import root.ServerUser;

public class ItemRain extends Thread {
   public double itemPerSecond = 10.0D;
   public int spawnType = 1;
   public boolean enabled = false;
   public double spawnMinHeight = 5000.0D;
   public double spawnRndHeight = 2500.0D;
   public double spawnRadius = 20000.0D;
   public MKServer server;
   public int[] spawnTable = new int[]{0, 1, 2, 3, 4, 5, 6, 8, 10, 11, 13, 15};
   public int[] spawnWeight = new int[]{100, 33, 100, 100, 33, 60, 20, 60, 10, 33, 20, 100};

   public ItemRain(MKServer server) {
      this.setName("Item rain thread");
      this.server = server;
      this.reload();
      this.start();
   }

   public void reload() {
      try {
         KTML2Obj.loadS(this, KTMLDecoder.decode(this.server.fixFile("itemrain.cfg")));
      } catch (Exception var2) {
         System.out.println("can't load the item rain file");
      }

   }

   public void run() {
      double clock = 0.0D;

      while(true) {
         while(true) {
            label74:
            while(true) {
               do {
                  do {
                     try {
                        Thread.sleep(10L);
                     } catch (Exception var19) {
                     }
                  } while(!this.enabled);
               } while(this.server.raceStatus != 2);

               Iterator var4 = this.server.getList().iterator();

               ServerUser u;
               do {
                  if (!var4.hasNext()) {
                     clock += 0.01D;
                     double spawn = 1.0D / this.itemPerSecond;

                     while(clock >= spawn) {
                        clock -= spawn;
                        int maxWeight = 0;
                        int[] var9;
                        int var8 = (var9 = this.spawnWeight).length;

                        int item;
                        int i;
                        for(i = 0; i < var8; ++i) {
                           item = var9[i];
                           maxWeight += item;
                        }

                        item = (int)(Math.random() * (double)maxWeight);

                        for(i = 0; i != this.spawnWeight.length; ++i) {
                           if (item < this.spawnWeight[i]) {
                              item = this.spawnTable[i];
                              break;
                           }

                           item -= this.spawnWeight[i];
                        }

                        Vector<float[]> poses = this.server.mapPoses;
                        if (poses.isEmpty()) {
                           clock = 0.0D;
                           continue label74;
                        }

                        float[] spos = null;
                        switch(this.spawnType) {
                        case 0:
                           float[] pos = (float[])poses.elementAt((int)(Math.random() * (double)poses.size()));
                           spos = (float[])pos.clone();
                           spos[0] = (float)((double)spos[0] + this.spawnRadius * (Math.random() - 0.5D));
                           spos[2] = (float)((double)spos[2] + this.spawnRadius * (Math.random() - 0.5D));
                           break;
                        case 1:
                           float minX = Float.MAX_VALUE;
                           float maxX = Float.MIN_VALUE;
                           float minZ = Float.MAX_VALUE;
                           float maxZ = Float.MIN_VALUE;
                           Iterator var14 = poses.iterator();

                           while(var14.hasNext()) {
                              float[] f = (float[])var14.next();
                              if (f[0] < minX) {
                                 minX = f[0];
                              }

                              if (f[0] > maxX) {
                                 maxX = f[0];
                              }

                              if (f[2] < minZ) {
                                 minZ = f[2];
                              }

                              if (f[2] > maxZ) {
                                 maxZ = f[2];
                              }
                           }

                           label105:
                           while(true) {
                              float testX = (float)((double)(maxX - minX) * Math.random() + (double)minX);
                              float testZ = (float)((double)(maxZ - minZ) * Math.random() + (double)minZ);
                              Iterator var16 = poses.iterator();

                              while(var16.hasNext()) {
                                 float[] f = (float[])var16.next();
                                 float xDst = testX - f[0];
                                 float zDst = testZ - f[2];
                                 if (!(Math.sqrt((double)(xDst * xDst + zDst * zDst)) > this.spawnRadius)) {
                                    spos = new float[]{testX, f[1], testZ};
                                    spos[1] = (float)((double)spos[1] + this.spawnMinHeight + Math.random() * this.spawnRndHeight);
                                    break label105;
                                 }
                              }
                           }
                        }

                        Packet.Command cmd = new Packet.Command(this.server, 0, new Object[]{item, spos[0], spos[1], spos[2], 0, 0, 0});
                        this.server.sendPacket(cmd);
                     }
                     continue label74;
                  }

                  u = (ServerUser)var4.next();
               } while(((MKServer.MKUser)u).mem1size >= 2181038080L);

               this.server.userPrint("Can't enable item rain if one of the user isn't using the dolphin patch.");
               this.enabled = false;
            }
         }
      }
   }
}
