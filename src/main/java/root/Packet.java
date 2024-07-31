package root;

import com.kirbymimi.mmb.math.Expression;
import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.ktml.KTMLEntry;
import com.kirbymimi.mmb.ut.list.FastList;
import com.kirbymimi.mmb.ut.stream.DataOutStream;
import com.kirbymimi.mmb.ut.stream.Scanner;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;

public abstract class Packet {
   public ServerUser src;
   public ServerUser target;
   public boolean nTarget;

   public Packet() {
   }

   public Packet(ServerUser src) {
      this.src = src;
   }

   public abstract void write(DataOutStream var1, ServerUser var2) throws IOException;

   public Packet setTarget(ServerUser target) {
      this.target = target;
      return this;
   }

   public int getTime(Server server) {
      int b = -1;
      Iterator var4 = server.list.iterator();

      while(var4.hasNext()) {
         ServerUser u = (ServerUser)var4.next();
         if (u.lastTime > b) {
            b = u.lastTime;
         }
      }

      return b + 10;
   }

   public static class Command extends Packet {
      int id;
      int time;
      public Object[] parms;
      Server server;

      public Command(Server server, int id, Object... parms) {
         this.server = server;
         this.id = id;
         this.parms = parms;
         this.time = this.getTime(server);
      }

      public void write(DataOutStream out, ServerUser user) throws IOException {
         out.write((int)6);
         out.writeInt(this.parms.length);
         out.writeInt(this.id);
         out.writeInt(this.time);
         Object[] var6;
         int var5 = (var6 = this.parms).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            Object o = var6[var4];
            Class<?> cls = o.getClass();
            if (cls == Integer.class) {
               out.writeInt((Integer)o);
            } else if (cls == Float.class) {
               out.writeFloat((Float)o);
            }
         }

      }
   }

   public static class Control extends Packet {
      int id;

      public Control(int id) {
         this.id = id;
      }

      public void write(DataOutStream out, ServerUser user) throws IOException {
         out.write(this.id);
      }
   }

   public static class Join extends Packet {
      public Join(ServerUser src) {
         super(src);
      }

      public void write(DataOutStream Out, ServerUser User) throws IOException {
         Out.write((int)1);
         Out.write(this.src.id);
         Out.writeLen16String(this.src.name);
      }
   }

   public static class Leave extends Packet {
      public Leave(ServerUser src) {
         super(src);
      }

      public void write(DataOutStream out, ServerUser user) throws IOException {
         out.write((int)2);
         out.write(this.src.id);
      }
   }

   public static class Message extends Packet {
      String msg;

      public Message(String msg) {
         this.msg = msg;
      }

      public void write(DataOutStream out, ServerUser user) throws IOException {
         out.write((int)4);
         out.writeLen16String(this.msg);
      }
   }

   public static class Mixer extends Packet {
      public void write(DataOutStream out, ServerUser user) throws IOException {
         user.getServer().mixer.Mix(user, out);
      }
   }

   public static class Patch extends Packet {
      KTMLEntry ktml;
      static HashMap<KTMLEntry, FastList<Packet.Patch.PatchLine>> cache = new HashMap();

      public Patch(KTMLEntry ktml) {
         this.ktml = ktml;
      }

      public void write(DataOutStream out, ServerUser user) throws IOException {
         Boolean enabled = this.ktml.getBoolean("enabled");
         if (enabled == null || enabled) {
            byte[] data = this.ktml.getRaw("code");
            if (data != null) {
               out.write((int)9);
               FastList<Packet.Patch.PatchLine> dataWrites = compilePatch(data, this.ktml);
               out.writeCompressedInt(dataWrites.length());
               Iterator var7 = dataWrites.iterator();

               while(var7.hasNext()) {
                  Packet.Patch.PatchLine patchLine = (Packet.Patch.PatchLine)var7.next();
                  out.writeInt((int)patchLine.addr);
                  out.writeCompressedInt(patchLine.data.length);
                  out.write(patchLine.data);
               }

            }
         }
      }

      static FastList<Packet.Patch.PatchLine> compilePatch(byte[] data, KTMLEntry ktml) {
         FastList<Packet.Patch.PatchLine> dataWrites = (FastList)cache.get(ktml);
         if (dataWrites != null) {
            return dataWrites;
         } else {
            ByteBuffer bb = ByteBuffer.allocate(8);
            synchronized(cache) {
               int idx = 0;
               dataWrites = new FastList();
               cache.put(ktml, dataWrites);
               Scanner scan = new Scanner(data);

               while(scan.hasNext()) {
                  try {
                     long addr = Long.decode("0x" + scan.next()) & 4294967295L;
                     if (addr < -2147483648L || addr < -2130706432L) {
                        throw new Exception();
                     }

                     byte[] lineData = null;
                     String value = scan.nextLine();
                      if (value.charAt(0) == '<') {
                        label70: {
                           String type = value.substring(1, value.indexOf(62));
                           value = value.substring(value.indexOf(62) + 1);
                           Expression expression = new Expression(value);
                           Double ret = expression.resolve((Object)null, (empty, line) -> {
                              return ktml.getDouble(line[0]);
                           });
                           bb.position(0);
                           String var14;
                           switch((var14 = type.toLowerCase()).hashCode()) {
                           case -1325958191:
                              if (var14.equals("double")) {
                                 bb.putDouble(ret);
                                 break label70;
                              }
                              break;
                           case 104431:
                              if (var14.equals("int")) {
                                 bb.putInt(ret.intValue());
                                 break label70;
                              }
                              break;
                           case 3039496:
                              if (var14.equals("byte")) {
                                 bb.put(ret.byteValue());
                                 break label70;
                              }
                              break;
                           case 3327612:
                              if (var14.equals("long")) {
                                 bb.putLong(ret.longValue());
                                 break label70;
                              }
                              break;
                           case 97526364:
                              if (var14.equals("float")) {
                                 bb.putFloat(ret.floatValue());
                                 break label70;
                              }
                              break;
                           case 109413500:
                              if (var14.equals("short")) {
                                 bb.putShort(ret.shortValue());
                                 break label70;
                              }
                           }

                           MMBSystem.fatalS((Object)("Bad patch format for " + ktml.getName() + ", illegal type : " + type));
                        }

                        lineData = new byte[bb.position()];
                        System.arraycopy(bb.array(), 0, lineData, 0, bb.position());
                     } else {
                        lineData = new byte[value.length() >> 1];

                        for(int i = 0; i != value.length() >> 1; ++i) {
                           lineData[i] = Integer.decode("0x" + value.substring(i << 1, i + 1 << 1)).byteValue();
                        }
                     }

                     dataWrites.add(new Packet.Patch.PatchLine(addr, lineData));
                  } catch (Exception var16) {
                     MMBSystem.fatalS((Object)("Bad patch format for " + ktml.getName() + " at line : " + idx));
                  }
               }

               try {
                  scan.close();
               } catch (Exception var15) {
               }

               return dataWrites;
            }
         }
      }

      static class PatchLine {
         long addr;
         byte[] data;

         PatchLine(long addr, byte[] data) {
            this.addr = addr;
            this.data = data;
         }
      }
   }

   public static class Ping extends Packet {
      public void write(DataOutStream out, ServerUser user) throws IOException {
         out.write((int)3);
         out.writeShort(user.getPing());
      }
   }

   public static class TCPDownload extends Packet {
      byte[] bytes;
      int start;
      int len;

      public TCPDownload(byte[] bytes, int start, int len) {
         this.bytes = bytes;
         this.start = start;
         this.len = len;
      }

      public void write(DataOutStream out, ServerUser user) throws IOException {
         out.write((int)8);
         out.write(this.bytes, this.start, this.len);
      }
   }
}
