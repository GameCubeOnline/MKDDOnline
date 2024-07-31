package root;

import com.kirbymimi.mmb.audio.format.Sample;
import java.io.File;
import java.io.IOException;

public class BaseCommands {
   public static boolean admin(String cmd, Server server) throws IOException {
      String[] line = cmd.split(" ");
      String var3;
      ServerUser targ;
      switch((var3 = line[0].toLowerCase()).hashCode()) {
      case -1618876223:
         if (var3.equals("broadcast")) {
            int idx = cmd.indexOf(32, 5);
            server.sendMessage("broadcast:" + cmd.substring(idx));
            return true;
         }
         break;
      case -934594754:
         if (var3.equals("rename")) {
            ServerUser dst = server.get(line[1]);
            if (dst == null) {
               return true;
            }

            dst.name = line[2];
            return true;
         }
         break;
      case -432943614:
         if (var3.equals("stopstream")) {
            server.audioStream = null;
            return true;
         }
         break;
      case 3553:
         if (var3.equals("op")) {
            targ = server.get(line[1]);
            if (targ != null) {
               targ.addPermission("admin");
            }

            return true;
         }
         break;
      case 97295:
         if (var3.equals("ban")) {
            targ = server.get(cmd.substring(4));
            if (targ != null) {
               targ.kick();
               server.bans.ban(targ.sock.getInetAddress());
            }

            return true;
         }
         break;
      case 3079714:
         if (var3.equals("deop")) {
            targ = server.get(line[1]);
            if (targ != null) {
               targ.addPermission("admin");
            }

            return true;
         }
         break;
      case 3127582:
         if (var3.equals("exit")) {
            System.exit(0);
            return true;
         }
         break;
      case 3291718:
         if (var3.equals("kick")) {
            targ = server.get(cmd.substring(5));
            if (targ != null) {
               targ.kick();
            }

            return true;
         }
         break;
      case 106934957:
         if (var3.equals("print")) {
            server.print();
            return true;
         }
         break;
      case 108404047:
         if (var3.equals("reset")) {
            server.reset();
            return true;
         }
         break;
      case 1358443222:
         if (var3.equals("audiostream")) {
            server.audioStreamStart(Sample.parseWave(new File(line[1])));
            return true;
         }
      }

      return false;
   }

   public static boolean user(String cmd, ServerUser user, Server server) throws IOException {
      String[] line = cmd.split(" ");
      String var4;
      switch((var4 = line[0].toLowerCase()).hashCode()) {
      case 108417:
         if (var4.equals("msg")) {
            ServerUser dst = server.get(line[1]);
            if (dst == null) {
               server.sendMessage((ServerUser)null, user, "can't find message target", ">");
               return true;
            }

            int idx = cmd.indexOf(32, 5);
            server.sendMessage(user, dst, cmd.substring(idx), ">");
            server.sendMessage(dst, user, cmd.substring(idx), "<");
            return true;
         }
         break;
      case 102846135:
         if (var4.equals("leave")) {
            if (user != null) {
               user.kick();
            }

            return true;
         }
      }

      return false;
   }
}
