package root;

import java.util.Iterator;
import java.util.concurrent.Semaphore;

public class SafeList<T> implements Iterable<T> {
  public T[] Arr = (T[])new Object[2];
  
  public T[] IterArr = (T[])new Object[0];
  
  public int CPos;
  
  public Semaphore Lock;
  
  public synchronized void add(T Obj) {
    if (this.CPos == this.Arr.length) {
      Object[] NArr = new Object[this.Arr.length * 2];
      System.arraycopy(this.Arr, 0, NArr, 0, this.CPos);
      this.Arr = (T[])NArr;
    } 
    this.Arr[this.CPos++] = Obj;
    this.IterArr = (T[])this.Arr.clone();
  }
  
  public synchronized void remove(T Obj) {
    for (int CIdx = 0; CIdx != this.CPos; ) {
      if (this.Arr[CIdx] != Obj) {
        CIdx++;
        continue;
      } 
      this.Arr[CIdx] = null;
      if (CIdx != this.CPos)
        System.arraycopy(this.Arr, CIdx + 1, this.Arr, CIdx, this.CPos - CIdx - 1); 
      this.CPos--;
      break;
    } 
    this.IterArr = (T[])this.Arr.clone();
  }
  
  public synchronized T get(int Idx) {
    if (this.CPos <= Idx)
      throw new ArrayIndexOutOfBoundsException(Idx); 
    return this.Arr[Idx];
  }
  
  public synchronized int size() {
    return this.CPos;
  }
  
  public boolean isEmpty() {
    return (this.CPos == 0);
  }
  
  public synchronized Iterator<T> iterator() {
    return new SafeListIterator<>(this);
  }
  
  public static class SafeListIterator<T> implements Iterator<T> {
    public T[] Arr;
    
    public int Len;
    
    public int CPos;
    
    SafeListIterator(SafeList<T> Lst) {
      this.Arr = Lst.IterArr;
      this.Len = Lst.CPos;
    }
    
    public boolean hasNext() {
      return (this.CPos != this.Len);
    }
    
    public T next() {
      return this.Arr[this.CPos++];
    }
  }
}
