package root;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

public class Mixer {
  static ReentrantLock Lock = new ReentrantLock();
  
  static HashMap<Object, MixerVoice> VoiceMap = new HashMap<>();
  
  static Vector<MixerVoice> VoiceList = new Vector<>();
  
  public static final int FREQ = 16000;
  
  public static final double FREQD = 16000.0D;
  
  public static final int PPS = 10;
  
  public static void Init() {
    (new Thread() {
        public void run() {
          Thread.currentThread().setPriority(10);
          while (true) {
            try {
              Thread.sleep(100L);
              for (ServerUser U : ServerUser.List)
                U.VoiceReq = true; 
            } catch (Exception exception) {}
          } 
        }
      }).start();
  }
  
  public static void AddVoice(byte[] Data, Object Source, int Rate) {
    Data = Resample(Data, Rate);
    System.out.println("Rcvd mic :" + Data.length);
    Lock.lock();
    MixerVoice Mix = VoiceMap.get(Source);
    if (Mix == null) {
      Mix = new MixerVoice();
      VoiceMap.put(Source, Mix);
      VoiceList.add(Mix);
    } 
    Mix.addBytes(Data);
    Lock.unlock();
  }
  
  public static void RemoveVoice(Object Source) {
    VoiceMap.remove(Source);
  }
  
  public static byte[] Resample(byte[] Data, double Rate) {
    double d = Data.length * 16000.0D / Rate;
    if (Math.ceil(d) != d)
      d = (((int)d & 0xFFFFFFFE) + 2); 
    byte[] Dst = new byte[(int)d];
    int CPos = 0;
    double Clock = 0.0D;
    double ClockRate = Rate / 16000.0D;
    for (int i = 0; i != Data.length; ) {
      Dst[CPos] = Data[i];
      Dst[CPos + 1] = Data[i + 1];
      CPos += 2;
      Clock += ClockRate;
      while (Clock >= 1.0D) {
        Clock--;
        i += 2;
      } 
    } 
    return Dst;
  }
  
  public static void Mix(ServerUser Dst, DataOutputStream Out) throws IOException {}
  
  public static void NoCompress(ServerUser Dst, int[] Sums, int ChanCnt, DataOutputStream Out) throws IOException {
    Out.writeInt(Sums.length);
    long Div = (long)(4.294967295E9D / Math.pow(ChanCnt, 0.6D)) & 0xFFFFFFFFL;
    byte b;
    int i, arrayOfInt[];
    for (i = (arrayOfInt = Sums).length, b = 0; b < i; ) {
      int j = arrayOfInt[b];
      j = (int)(j * Div >> 32L);
      if (j > 32767)
        j = 32767; 
      if (j < -32768)
        j = 32768; 
      j >>= 8;
      Out.write((byte)j);
      b++;
    } 
  }
  
  public static void Compress3Bit(ServerUser Dst, int[] Sums, int ChanCnt, DataOutputStream Out) throws IOException {
    ByteArrayOutputStream TempOut = new ByteArrayOutputStream();
    BitStream Bits = new BitStream(TempOut);
    short CVal = Dst.MixerCVal;
    long Div = (long)(4.294967295E9D / Math.pow(ChanCnt, 0.6D)) & 0xFFFFFFFFL;
    byte b;
    int i, arrayOfInt[];
    for (i = (arrayOfInt = Sums).length, b = 0; b < i; ) {
      int j = arrayOfInt[b];
      j = (int)(j * Div >> 32L);
      if (j > 32767)
        j = 32767; 
      if (j < -32768)
        j = -32768; 
      j >>= 8;
      if (CVal == j) {
        Bits.Write(3, 0);
      } else {
        int Sub = j - CVal;
        if (Sub > 3) {
          Sub = 3;
        } else if (Sub < -4) {
          Sub = -4;
        } 
        CVal = (short)(CVal + Sub);
        if (Sub < 0)
          Sub = -Sub + 3; 
        Bits.Write(3, Sub);
      } 
      b++;
    } 
    Dst.MixerCVal = CVal;
    Bits.Close(3);
    TempOut.close();
    Out.writeInt(TempOut.size());
    Out.write(TempOut.toByteArray());
  }
  
  public static void Compress4Bit(ServerUser Dst, int[] Sums, int ChanCnt, DataOutputStream Out) throws IOException {
    ByteArrayOutputStream TempOut = new ByteArrayOutputStream();
    BitStream Bits = new BitStream(TempOut);
    short CVal = Dst.MixerCVal;
    long Div = (long)(4.294967295E9D / Math.pow(ChanCnt, 0.6D)) & 0xFFFFFFFFL;
    byte b;
    int i, arrayOfInt[];
    for (i = (arrayOfInt = Sums).length, b = 0; b < i; ) {
      int j = arrayOfInt[b];
      j *= 16;
      j = (int)(j * Div >> 32L);
      if (j > 32767)
        j = 32767; 
      if (j < -32768)
        j = 32768; 
      j >>= 8;
      if (CVal == j) {
        Bits.Write(4, 0);
      } else {
        int Sub = j - CVal;
        if (Sub > 7) {
          Sub = 7;
        } else if (Sub < -8) {
          Sub = -8;
        } 
        CVal = (short)(CVal + Sub);
        if (Sub < 0)
          Sub = -Sub + 7; 
        Bits.Write(4, Sub);
      } 
      b++;
    } 
    Dst.MixerCVal = CVal;
    Bits.Close(4);
    TempOut.close();
    Out.writeInt(TempOut.size());
    Out.write(TempOut.toByteArray());
  }
  
  public static class BitStream {
    OutputStream Dst;
    
    byte CVal;
    
    int Rem = 8;
    
    BitStream(OutputStream Dst) {
      this.Dst = Dst;
    }
    
    public void Write(int Len, int V) throws IOException {
      if (Len <= this.Rem) {
        this.CVal = (byte)(this.CVal | V << this.Rem - Len);
        this.Rem -= Len;
        return;
      } 
      int Sub = Len - this.Rem;
      Write(this.Rem, V >> Len - this.Rem);
      this.Dst.write(this.CVal);
      this.Rem = 8;
      this.CVal = 0;
      Write(Sub, V);
    }
    
    public void Close(int Al) throws IOException {
      if (this.Rem == 0)
        this.Dst.write(this.CVal); 
      while (this.Rem != 0)
        Write(Al, 0); 
    }
  }
  
  public static class MixerVoice {
    ReentrantLock Lock;
    
    Vector<byte[]> Bytes;
    
    public MixerVoice() {
      this.Lock = new ReentrantLock();
      this.Bytes = (Vector)new Vector<>();
    }
    
    public void addBytes(byte[] Data) {
      this.Lock.lock();
      this.Bytes.add(Data);
      this.Lock.unlock();
    }
    
    public byte[] Get(int Size) {
      byte[] CByte;
      Size <<= 1;
      this.Lock.lock();
      int Len = 0;
      for (byte[] B : this.Bytes) {
        Len += B.length;
        if (Len >= Size)
          break; 
      } 
      if (Len < Size) {
        this.Lock.unlock();
        return null;
      } 
      int CPos = 0;
      int Rem = Size;
      byte[] Ret = new byte[Size];
      while (true) {
        CByte = this.Bytes.elementAt(0);
        this.Bytes.remove(0);
        if (Rem > CByte.length) {
          System.arraycopy(CByte, 0, Ret, CPos, CByte.length);
          CPos += CByte.length;
          Rem -= CByte.length;
          continue;
        } 
        break;
      } 
      byte[] NByte = new byte[CByte.length - Rem];
      System.arraycopy(CByte, Rem, NByte, 0, CByte.length - Rem);
      this.Bytes.add(0, NByte);
      System.arraycopy(CByte, 0, Ret, CPos, Rem);
      this.Lock.unlock();
      return Ret;
    }
  }
}
