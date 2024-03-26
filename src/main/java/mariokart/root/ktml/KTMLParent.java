package root.ktml;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

public class KTMLParent implements Iterable<KTMLEntry> {
  Vector<KTMLEntry> elementList = new Vector<>();
  
  HashMap<String, KTMLEntry> elementMap = new HashMap<>();
  
  public void add(KTMLEntry entry) {
    this.elementList.add(entry);
    this.elementMap.put(entry.name, entry);
  }
  
  public KTMLEntry get(String key) {
    return this.elementMap.get(key);
  }
  
  public KTMLEntry get(int idx) {
    if (this.elementList.size() <= idx)
      return null; 
    return this.elementList.elementAt(idx);
  }
  
  public int getElementCount() {
    return this.elementList.size();
  }
  
  public Object getValue(String str) {
    KTMLEntry K = get(str);
    if (K == null)
      return null; 
    return K.getValue();
  }
  
  public KTMLParent getParent(String str) {
    KTMLEntry K = get(str);
    if (K == null)
      return null; 
    return K.getAsParent();
  }
  
  public String getString(String str) {
    KTMLEntry K = get(str);
    if (K == null)
      return null; 
    return K.getAsString();
  }
  
  public Boolean getBoolean(String str) {
    KTMLEntry K = get(str);
    if (K == null)
      return null; 
    return K.getAsBoolean();
  }
  
  public Integer getInt(String str) {
    KTMLEntry K = get(str);
    if (K == null)
      return null; 
    return K.getAsInt();
  }
  
  public Double getDouble(String str) {
    KTMLEntry K = get(str);
    if (K == null)
      return null; 
    return K.getAsDouble();
  }
  
  public <T> T getEnum(Class<T> enumCls, String str) {
    KTMLEntry K = get(str);
    if (K == null)
      return null; 
    return K.getAsEnum(enumCls);
  }
  
  public Object getValue(int idx) {
    KTMLEntry K = get(idx);
    if (K == null)
      return null; 
    return K.getValue();
  }
  
  public KTMLParent getParent(int idx) {
    KTMLEntry K = get(idx);
    if (K == null)
      return null; 
    return K.getAsParent();
  }
  
  public String getString(int idx) {
    KTMLEntry K = get(idx);
    if (K == null)
      return null; 
    return K.getAsString();
  }
  
  public Boolean getBoolean(int idx) {
    KTMLEntry K = get(idx);
    if (K == null)
      return null; 
    return K.getAsBoolean();
  }
  
  public Integer getInt(int idx) {
    KTMLEntry K = get(idx);
    if (K == null)
      return null; 
    return K.getAsInt();
  }
  
  public Double getDouble(int idx) {
    KTMLEntry K = get(idx);
    if (K == null)
      return null; 
    return K.getAsDouble();
  }
  
  public <T> T getEnum(Class<T> enumCls, int idx) {
    KTMLEntry K = get(idx);
    if (K == null)
      return null; 
    return K.getAsEnum(enumCls);
  }
  
  public Iterator<KTMLEntry> iterator() {
    return this.elementList.iterator();
  }
}
