package marioKart;

import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.TaskChain;
import com.kirbymimi.mmb.ut.ktml.KTML2Obj;
import com.kirbymimi.mmb.ut.ktml.KTMLDecoder;
import com.kirbymimi.mmb.ut.ktml.KTMLEntry;
import java.util.Iterator;
import java.util.Random;
import root.ServerUser;

public class AutoServer {
   MKServer server;
   TaskChain<AutoServer> task;
   KTMLEntry ktml;
   boolean enabled;
   boolean trackLoadDone;
   long selectTimeOut = 30000000000L;
   long battleTimeOut = 420000000000L;
   long raceStartTimeOut = 300000000000L;
   long raceFatalTimeOut = 20000000000L;
   long afkTimeOut = 6000L;
   long selectStartTime;
   long battleStartTime;
   long raceStartStartTime;
   long raceFatalStartTime;
   int raceProgress;

   public AutoServer(MKServer server) {
      this.server = server;
      this.reload();
      this.task = new TaskChain<>(this, (auto, task) -> auto.__run());
      server.addTask(this.task);
   }

   public void reload() {
      try {
         this.ktml = KTMLDecoder.decode(MMBSystem.openFileS("autoserver.cfg"));
         Long l = this.ktml.getLong("selectTimeOut");
         if (l != null) {
            this.selectTimeOut = l * 1000000000L;
         }

         l = this.ktml.getLong("battleTimeOut");
         if (l != null) {
            this.battleTimeOut = l * 1000000000L;
         }

         l = this.ktml.getLong("raceStartTimeOut");
         if (l != null) {
            this.raceStartTimeOut = l * 1000000000L;
         }

         l = this.ktml.getLong("afkTimeOut");
         if (l != null) {
            this.afkTimeOut = l * 100L;
         }

         Boolean b = this.ktml.getBoolean("enabled");
         if (b != null) {
            this.enabled = b;
         }
      } catch (Exception e) {
         this.server.userPrint("Can't load the auto server file");
         System.exit(-1);
      }
   }

   public TaskChain.Function<AutoServer> __run() {
      try {
         this.run();
      } catch (Exception e) {
         e.printStackTrace(); // Log the exception
      }
      return null;
   }

   public void run() {
      if (this.enabled) {
         if (this.server.getList().isEmpty()) {
            this.reset();
         }

         long ctime = System.nanoTime();
         Iterator<ServerUser> userIterator;
         ServerUser user;
         int weight;

         switch (this.server.raceStatus) {
            case 0:
               if (this.trackLoadDone) {
                  return;
               }

               if (this.server.getList().isEmpty()) {
                  this.selectStartTime = System.nanoTime();
                  return;
               }

               if (ctime > this.selectStartTime + (this.server.customTrackPath != null ? this.selectTimeOut * 3L : this.selectTimeOut)) {
                  userIterator = this.server.getList().iterator();
                  while (userIterator.hasNext()) {
                     user = userIterator.next();
                     if (!user.kartDataReceived) {
                        user.kick();
                     }
                  }
               }

               userIterator = this.server.getList().iterator();
               while (userIterator.hasNext()) {
                  user = userIterator.next();
                  if (!user.kartDataReceived) {
                     return;
                  }
               }

               int weightSum = 0;
               Iterator<KTMLEntry> ktmlIterator = this.ktml.iterator();
               while (ktmlIterator.hasNext()) {
                  KTMLEntry entry = ktmlIterator.next();
                  if (entry.isParent()) {
                     Integer i = entry.getInt("weight");
                     if (i != null) {
                        weightSum += i;
                     }
                  }
               }

               Random rand = new Random();
               weight = rand.nextInt(weightSum);
               KTMLEntry result = null;
               ktmlIterator = this.ktml.iterator();
               while (ktmlIterator.hasNext()) {
                  KTMLEntry entry = ktmlIterator.next();
                  if (entry.isParent()) {
                     Integer i = entry.getInt("weight");
                     if (i != null) {
                        if (i > weight) {
                           result = entry;
                           break;
                        }
                        weight -= i;
                     }
                  }
               }

               KTMLEntry coursesEntry = result.get("courses");
               KTMLEntry courseEntry = coursesEntry.get(rand.nextInt(coursesEntry.getElementCount()));
               Object o = courseEntry.getValue();
               if (o instanceof Integer) {
                  this.server.raceCupSubID = (Integer) o;
               } else if (o instanceof String) {
                  this.server.loadCustomTrack((String) o);
                  this.server.raceCupSubID = 36;
               }

               RaceProfile race = new RaceProfile();
               KTML2Obj.loadS(race, result);
               String resultName = result.getName();
               switch (resultName) {
                  case "versus":
                     this.server.raceMode = 3;
                     break;
                  case "balloon":
                     this.server.raceMode = 4;
                     break;
                  case "bomb":
                     this.server.raceMode = 6;
                     break;
                  case "shine":
                     this.server.raceMode = 7;
                     break;
               }

               this.server.raceSeed = race.seed;
               this.server.raceCC = race.cc;
               this.server.raceMirror = race.mirror ? 1 : 0;
               this.server.raceOptions = (short) (race.itemBox << 8 | race.laps & 255);

               for (int i = 0; i < 8; ++i) {
                  this.server.racePlaces[(i << 2) + 3] = (byte) i;
               }

               this.server.raceInfoReceived = true;
               this.raceStartStartTime = System.nanoTime();
               this.battleStartTime = System.nanoTime();
               this.raceProgress = 0;
               this.trackLoadDone = true;
               break;

            case 1:
            case 2:
            case 3:
               userIterator = this.server.getList().iterator();
               while (userIterator.hasNext()) {
                  user = userIterator.next();
                  weight = (Integer) user.getResource(AutoServer.class, Integer.class, "afkTimeOut");
                  if (user.controllerData == 0) {
                     if (++weight == this.afkTimeOut) {
                        user.kick();
                     }
                  } else {
                     weight = 0;
                  }
               }

               if (this.server.raceMode == 3) {
                  if (ctime > this.raceStartStartTime + this.raceStartTimeOut) {
                     this.server.stopRace();
                  }
               } else if (ctime > this.battleStartTime + this.battleTimeOut) {
                  this.server.stopRace();
               }

               switch (this.raceProgress) {
                  case 0:
                     userIterator = this.server.getList().iterator();
                     while (userIterator.hasNext()) {
                        user = userIterator.next();
                        if (user.raceStatus == 2) {
                           this.raceProgress = 1;
                           return;
                        }
                     }
                     return;

                  case 1:
                     userIterator = this.server.getList().iterator();
                     while (userIterator.hasNext()) {
                        user = userIterator.next();
                        if (user.raceStatus != 3) {
                           return;
                        }
                     }
                     this.raceProgress = 2;
                     this.raceFatalStartTime = System.nanoTime();
                     return;

                  case 2:
                     if (ctime > this.raceFatalStartTime + this.raceFatalTimeOut) {
                        userIterator = this.server.getList().iterator();
                        while (userIterator.hasNext()) {
                           user = userIterator.next();
                           user.kick();
                        }
                     }
                     this.server.sendPacket(new MKServer.CursorPacket(2));
                     this.reset();
                     return;
               }
               break;
         }
      }
   }

   void reset() {
      this.selectStartTime = System.nanoTime();
      this.trackLoadDone = false;
   }

   public static class RaceProfile {
      int seed = (new Random()).nextInt();
      int cc = 2;
      int raceID = 36;
      boolean mirror;
      int laps;
      int itemBox;
   }
}
