package root;

import com.kirbymimi.mmb.ut.list.SafeList;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.Scanner;

public class BanList {
   Server server;
   public SafeList<InetAddress> ips = new SafeList();

   public BanList(Server server) {
      this.server = server;
   }

   public void ban(InetAddress address) {
      this.ips.add(address);
      this.saveFile();
   }

   public boolean isBanned(InetAddress address) {
      Iterator var3 = this.ips.iterator();

      while(var3.hasNext()) {
         InetAddress address2 = (InetAddress)var3.next();
         if (address2.equals(address)) {
            return true;
         }
      }

      return false;
   }

   public void saveFile() {
      try {
         FileOutputStream fOut = new FileOutputStream(this.server.fixFile("banIPs.txt"));
         PrintWriter printer = new PrintWriter(fOut);
         Iterator var4 = this.ips.iterator();

         while(var4.hasNext()) {
            InetAddress addr = (InetAddress)var4.next();
            printer.println(addr.toString().substring(1));
         }

         printer.close();
      } catch (Exception var5) {
      }

   }

   public void loadFile() {
      try {
         FileInputStream fIn = new FileInputStream(this.server.fixFile("banIPs.txt"));
         Scanner scanner = new Scanner(fIn);

         while(scanner.hasNext()) {
            this.ips.add(InetAddress.getByName(scanner.next()));
         }

         scanner.close();
      } catch (Exception var3) {
      }

   }
}
