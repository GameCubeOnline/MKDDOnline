package gcutil;

import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.BitUT;
import com.kirbymimi.mmb.ut.list.FastList;
import com.kirbymimi.mmb.ut.list.FastStack;
import com.kirbymimi.mmb.ut.list.HashMapList;
import com.kirbymimi.mmb.ut.stream.DataInStream;
import com.kirbymimi.mmb.ut.stream.DataOutStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Iterator;

public class ISOFile {
   HashMapList<String, ISOFile.File> fileList = new HashMapList();

   public void loadISO(InputStream inputStream) throws IOException {
      DataInStream in = new DataInStream(inputStream);
      in.pushBaseSeekOff();
      int format = in.readInt();
      in.popBaseSeekOff();
      Object callBacks;
      switch(format) {
      case 1128878927:
         MMBSystem.fatalS((Object)"CISO not supported");
         callBacks = new ISOFile.CIso();
         break;
      default:
         callBacks = new ISOFile.BaseISO();
      }

      ((ISOFile.ISOCallBacks)callBacks).setupStream(in);
      this.folderEntry("files/");
      this.folderEntry("sys/");
      this.fileEntry("sys/boot.bin", in, 1088);
      this.fileEntry("sys/bi2.bin", in, 8192);
      ByteBuffer head = ByteBuffer.wrap((byte[])((ISOFile.File)this.fileList.mapGet("sys/boot.bin")).data.get().clone());
      this.fileEntry("sys/apploader.img", in, head.getInt(1024));
      in.seek((long)head.getInt(1056));
      in.pushBaseSeekOff();
      in.skip(144L);
      int len = 256;

      for(int i = 0; i != 18; ++i) {
         len += in.readInt();
      }

      in.popBaseSeekOff();
      this.fileEntry("sys/main.dol", in, len);
      in.align(256L);
      long fstDataStart = in.off();
      in.skip(8L);
      int fstCnt = in.readInt();
      long fstStringStart = fstDataStart + (long)(fstCnt * 12);
      ISOFile.FSTEntry[] entries = new ISOFile.FSTEntry[fstCnt];
      entries[0] = new ISOFile.FSTEntry();
      entries[0].path = "files/";
      FastStack<String> currPath = new FastStack();
      FastList<Integer> closeList = new FastList();
      currPath.push("files/");

      for(int i = 1; i != fstCnt; ++i) {
         while(closeList.length() != 0 && (Integer)closeList.get(closeList.length() - 1) == i) {
            closeList.remove(closeList.length() - 1);
            currPath.pop();
         }

         in.seek(fstDataStart + (long)(i * 12));
         ISOFile.FSTEntry entry = new ISOFile.FSTEntry();
         int fstHead = in.readInt();
         entry.isDir = (fstHead & -16777216) != 0;
         int nameOff = fstHead & 16777215;
         String path = (String)currPath.peek();
         if (entry.isDir) {
            path = entries[in.readInt()].path;
            closeList.add(in.readInt());
         } else {
            entry.offset = (long)in.readInt();
            entry.len = in.readInt();
         }

         in.seek((long)nameOff + fstStringStart);
         entry.path = path + in.readNullTerminatedString();
         if (entry.isDir) {
            entry.path = entry.path + "/";
            this.folderEntry(entry.path);
            currPath.push(entry.path);
         } else {
            ((ISOFile.ISOCallBacks)callBacks).goToFST(this, in, entry.offset);
            this.fileEntry(entry.path, in, entry.len);
         }

         entries[i] = entry;
      }

   }

   public void writeISO(OutputStream outputStream) throws IOException {
      ByteBuffer head = ByteBuffer.wrap((byte[])((ISOFile.File)this.fileList.mapGet("sys/boot.bin")).data.get().clone());
      int apploaderSize = ((ISOFile.File)this.fileList.mapGet("sys/apploader.img")).size();
      head.putInt(1024, apploaderSize);
      int dolOff = BitUT.alignHi(apploaderSize + 9280, 256);
      head.putInt(1056, dolOff);
      int fstOff = BitUT.alignHi(dolOff + ((ISOFile.File)this.fileList.mapGet("sys/main.dol")).size(), 256);
      head.putInt(1060, fstOff);
      ByteArrayOutputStream fstTemp = new ByteArrayOutputStream();
      ISOFile.FSTWriter fstWriter = new ISOFile.FSTWriter(this, new DataOutStream(fstTemp), fstOff);
      fstWriter.writeFolder("files/", "", -1);
      fstWriter.writeStrings();
      head.putInt(1064, fstTemp.size());
      head.putInt(1068, fstTemp.size());
      DataOutStream out = new DataOutStream(outputStream);
      out.write(head.array());
      this.fileWrite("sys/bi2.bin", out);
      this.fileWrite("sys/apploader.img", out);
      out.align(256L);
      this.fileWrite("sys/main.dol", out);
      out.align(256L);
      out.write(fstTemp.toByteArray());
      fstWriter.out = out;
      fstWriter.writeFiles();
   }

   public void fileEntry(String name, final DataInStream dataStream, final int len) throws IOException {
      final long off = dataStream.off();
      ISOFile.File file = new ISOFile.File(name, new ISOFile.ByteAccessor() {
         public byte[] get() throws IOException {
            long back = dataStream.off();
            dataStream.seek(off);
            byte[] ret = dataStream.read(len);
            dataStream.seek(back);
            return ret;
         }
      });
      dataStream.skip((long)len);
      this.fileList.put(name, file);
   }

   public void folderEntry(String name) {
      if (!this.fileList.contains(name)) {
         ISOFile.File file = new ISOFile.File(name, (ISOFile.ByteAccessor)null);
         this.fileList.put(name, file);
      }
   }

   public void fileWrite(String name, DataOutStream dataStream) throws IOException {
      ISOFile.File file = (ISOFile.File)this.fileList.mapGet(name);
      dataStream.write(file.data.get());
   }

   public void fileEntry(String name, final byte[] data) {
      ISOFile.File f;
      if (name.endsWith(".ips")) {
         f = (ISOFile.File)this.fileList.mapGet(name.substring(0, name.length() - 4));
         if (f != null) {
            try {
               byte[] newBytes = this.patch(f.data.get(), new ByteArrayInputStream(data));
               f.data = () -> {
                  return newBytes;
               };
            } catch (IOException var5) {
            }

         }
      } else {
         f = new ISOFile.File(name, new ISOFile.ByteAccessor() {
            public byte[] get() throws IOException {
               return data;
            }
         });
         this.fileList.put(name, f);
      }
   }

   public byte[] fileGet(String name) throws IOException {
      return ((ISOFile.File)this.fileList.mapGet(name)).data.get();
   }

   byte[] patch(byte[] bytesToPatch, InputStream patchStream) throws IOException {
      DataInStream in = new DataInStream(patchStream);
      int fileLen = in.readInt();
      if (bytesToPatch.length != fileLen) {
         byte[] next = new byte[fileLen];
         System.arraycopy(bytesToPatch, 0, next, 0, bytesToPatch.length);
         bytesToPatch = next;
      }

      ByteBuffer wbuf = ByteBuffer.wrap(bytesToPatch);
      in.skip(5L);

      while(true) {
         while(in.available() > 3) {
            int pos = in.readInt24();
            int i = in.readUShort();
            if (i == 0) {
               wbuf.position(in.readInt24());
               int len = in.readShort();

               for(byte val = in.readByte(); i != len; ++i) {
                  wbuf.put(val);
               }
            } else {
               try {
                  in.read(bytesToPatch, pos, i);
               } catch (Exception var11) {
                  var11.printStackTrace();
               }
            }
         }

         return bytesToPatch;
      }
   }

   public static class BaseISO extends ISOFile.ISOCallBacks {
      public void goToFST(ISOFile iso, DataInStream in, long off) throws IOException {
         in.seek(off);
      }
   }

   interface ByteAccessor {
      byte[] get() throws IOException;
   }

   public static class CIso extends ISOFile.ISOCallBacks {
      int fileLength;

      public void goToFST(ISOFile iso, DataInStream in, long off) throws IOException {
         in.seek(off - (long)(1459978240 - this.fileLength));
      }

      public void setupStream(DataInStream in) throws IOException {
         this.fileLength = in.available();
         in.setStart(32768L);
      }
   }

   public static class FSTEntry {
      boolean isDir;
      String path;
      long offset;
      int len;

      public String toString() {
         return this.path;
      }
   }

   static class FSTWriter {
      ISOFile src;
      DataOutStream out;
      FastList<String> stringPool = new FastList();
      FastList<ISOFile.FSTWriter.FSTFWrite> writeList = new FastList();
      int stringAllocator;
      int fileAllocator;
      int cIdx;

      FSTWriter(ISOFile src, DataOutStream out, int start) {
         this.src = src;
         this.out = out;
         this.fileAllocator = start + 12;
         Iterator var5 = src.fileList.iterator();

         while(var5.hasNext()) {
            ISOFile.File f = (ISOFile.File)var5.next();
            if (f.name.startsWith("files/") && f.name.compareTo("files/") != 0) {
               String name = this.fixName(f.name);
               this.fileAllocator += name.length() + 1 + 12;
            }
         }

         this.fileAllocator = BitUT.alignHi(this.fileAllocator, 256);
      }

      String fixName(String name) {
         String fName = name.substring(name.substring(0, name.length() - 1).lastIndexOf(47) + 1);
         if (fName.charAt(fName.length() - 1) == '/') {
            fName = fName.substring(0, fName.length() - 1);
         }

         return fName;
      }

      void writeFolder(String path, String name, int layer) throws IOException {
         int pathDashCnt = 0;
         byte[] var8;
         int var7 = (var8 = path.getBytes()).length;

         for(int var6 = 0; var6 < var7; ++var6) {
            byte c = var8[var6];
            if (c == 47) {
               ++pathDashCnt;
            }
         }

         int cnt = 0;
         Iterator var15 = this.src.fileList.iterator();

         ISOFile.File f;
         while(var15.hasNext()) {
            f = (ISOFile.File)var15.next();
            if (f.name.startsWith(path)) {
               ++cnt;
            }
         }

         this.out.write((int)1);
         this.strEntry(name);
         this.out.writeInt(layer > 0 ? layer : 0);
         this.out.writeInt(cnt + this.cIdx);
         var15 = this.src.fileList.iterator();

         while(true) {
            int size;
            while(true) {
               do {
                  if (!var15.hasNext()) {
                     return;
                  }

                  f = (ISOFile.File)var15.next();
               } while(!f.name.startsWith(path));

               int checkDashCnt = 0;
               byte[] var12;
               int var11 = (var12 = f.name.getBytes()).length;

               for(size = 0; size < var11; ++size) {
                  byte c = var12[size];
                  if (c == 47) {
                     ++checkDashCnt;
                  }
               }

               if (f.name.contains(".")) {
                  if (checkDashCnt != pathDashCnt) {
                     continue;
                  }
               } else if (checkDashCnt - 1 != pathDashCnt) {
                  continue;
               }
               break;
            }

            String fName = this.fixName(f.name);
            ++this.cIdx;
            if (f.isFolder()) {
               this.writeFolder(f.name, fName, layer + 1);
            } else {
               this.out.write((int)0);
               this.strEntry(fName);
               this.out.writeInt(this.fileAllocator);
               size = f.size();
               this.writeList.add(new ISOFile.FSTWriter.FSTFWrite(this.fileAllocator, f));
               this.fileAllocator += size;
               this.fileAllocator = BitUT.alignHi(this.fileAllocator, 256);
               this.out.writeInt(size);
            }
         }
      }

      void writeStrings() throws IOException {
         Iterator var2 = this.stringPool.iterator();

         while(var2.hasNext()) {
            String str = (String)var2.next();
            this.out.writeNullTerminatedString(str);
         }

      }

      void writeFiles() throws IOException {
         Iterator var2 = this.writeList.iterator();

         while(var2.hasNext()) {
            ISOFile.FSTWriter.FSTFWrite write = (ISOFile.FSTWriter.FSTFWrite)var2.next();
            this.out.zeroes((long)write.off - this.out.off());
            this.out.write(write.file.data.get());
         }

         this.out.zeroes(1459978240L - this.out.off());
      }

      void strEntry(String str) throws IOException {
         this.out.writeInt24(this.stringAllocator);
         if (!str.isEmpty()) {
            this.stringAllocator += str.length() + 1;
            this.stringPool.add(str);
         }
      }

      static class FSTFWrite {
         int off;
         ISOFile.File file;

         FSTFWrite(int off, ISOFile.File file) {
            this.off = off;
            this.file = file;
         }
      }
   }

   public static class File {
      String name;
      ISOFile.ByteAccessor data;

      public File(String name, ISOFile.ByteAccessor data) {
         this.name = name;
         this.data = data;
      }

      public boolean isFolder() {
         return this.data == null;
      }

      public int size() throws IOException {
         return this.data.get().length;
      }
   }

   public abstract static class ISOCallBacks {
      public abstract void goToFST(ISOFile var1, DataInStream var2, long var3) throws IOException;

      public void setupStream(DataInStream in) throws IOException {
      }
   }
}
