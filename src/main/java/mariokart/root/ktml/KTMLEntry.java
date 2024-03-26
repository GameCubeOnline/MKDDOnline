package root.ktml;

public class KTMLEntry {
  String name;
  
  String type;
  
  Object value;
  
  public String toString() {
    return String.valueOf(this.name) + " " + this.value;
  }
  
  public String getName() {
    return this.name;
  }
  
  public String getType() {
    return this.type;
  }
  
  public Object getValue() {
    return this.value;
  }
  
  public KTMLParent getAsParent() {
    if (this.value instanceof KTMLParent)
      return (KTMLParent)this.value; 
    return null;
  }
  
  public String getAsString() {
    if (this.value instanceof String)
      return (String)this.value; 
    return null;
  }
  
  public Boolean getAsBoolean() {
    if (this.value instanceof Boolean)
      return (Boolean)this.value; 
    return null;
  }
  
  public Integer getAsInt() {
    if (this.value instanceof Integer)
      return (Integer)this.value; 
    return null;
  }
  
  public Long getAsLong() {
    if (this.value instanceof Long)
      return (Long)this.value; 
    return null;
  }
  
  public Double getAsDouble() {
    if (this.value instanceof Double)
      return (Double)this.value; 
    return null;
  }
  
  public Float getAsFloat() {
    if (this.value instanceof Double)
      return Float.valueOf(((Double)this.value).floatValue()); 
    return null;
  }
  
  public <T> T getAsEnum(Class<T> enumCls) {
    String s = getAsString();
    if (s == null);
    return (T)Enum.valueOf((Class)enumCls, s);
  }
}
