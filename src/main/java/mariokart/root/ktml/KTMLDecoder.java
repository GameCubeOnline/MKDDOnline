package root.ktml;

import java.util.Scanner;

public class KTMLDecoder {
  Scanner scan;
  
  public KTMLDecoder(Scanner scan) {
    this.scan = scan;
  }
  
  public static KTMLParent decode(Scanner scan) {
    return (new KTMLDecoder(scan)).decode();
  }
  
  public KTMLParent decode() {
    KTMLParent ret = subDecode();
    this.scan.close();
    return ret;
  }
  
  public KTMLParent subDecode() {
    KTMLParent ret = new KTMLParent();
    while (this.scan.hasNext()) {
      KTMLEntry e = decodeElement();
      if (e == null)
        break; 
      ret.add(e);
    } 
    return ret;
  }
  
  public KTMLEntry decodeElement() {
    String ctoken2, ctoken = readString();
    switch (ctoken.charAt(0)) {
      case '<':
        ctoken2 = readString();
        if (ctoken2.charAt(ctoken2.length() - 1) == ':')
          return createEntry(ctoken, ctoken2, readString()); 
        return createEntry(ctoken, null, ctoken2);
      case '"':
        if (!ctoken.endsWith(":"))
          return createEntry(null, null, ctoken); 
        return createEntry(null, ctoken, readString());
      case '}':
        return null;
    } 
    return createEntry(null, null, ctoken);
  }
  
  public KTMLEntry createEntry(String type, String name, String value) {
    KTMLEntry entry = new KTMLEntry();
    if (name != null) {
      name = name.replace(":", "");
      name = name.replace("\"", "");
      entry.name = name;
    } 
    if (type != null)
      entry.type = type.substring(1, type.length() - 1); 
    switch (value.charAt(0)) {
      case '"':
        value = value.replaceFirst("\"", "");
        entry.value = value.replaceFirst("(?s)\"(?!.*?\")", "");
        return entry;
      case '{':
        entry.value = subDecode();
        if (value.length() != 1) {
          KTMLParent entrylst = new KTMLParent();
          int len = Integer.decode(value.substring(2)).intValue();
          for (int i = 0; i != len; i++) {
            for (KTMLEntry e : ((KTMLParent)entry.value).elementList)
              entrylst.add(e); 
          } 
          entry = new KTMLEntry();
          entry.value = entrylst;
          entry.name = name;
          return entry;
        } 
        return entry;
    } 
    try {
      entry.value = Integer.valueOf(Long.decode(value).intValue());
    } catch (Exception exception) {
      try {
        entry.value = Double.valueOf(Double.parseDouble(value));
      } catch (Exception exception1) {
        try {
          entry.value = Boolean.valueOf(Boolean.parseBoolean(value));
        } catch (Exception exception2) {
          if (type != null) {
            int funcIdx = type.lastIndexOf('.');
            if (funcIdx != -1)
              try {
                Class<?> cls = Class.forName(type.substring(0, funcIdx));
                entry.value = cls.getMethod(type.substring(funcIdx + 1, type.length()), new Class[] { String.class }).invoke(null, new Object[] { entry.value });
              } catch (Exception exception3) {} 
          } 
        } 
      } 
    } 
    return entry;
  }
  
  public String readString() {
    String ret = this.scan.next();
    if (ret.charAt(0) != '"')
      return ret; 
    while (true) {
      if (ret.length() > 2 && ret.charAt(ret.length() - 2) != '\\' && (
        ret.endsWith("\"") || ret.endsWith(":")))
        return ret; 
      ret = String.valueOf(ret) + this.scan.next();
    } 
  }
}
