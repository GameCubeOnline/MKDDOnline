package root.ktml;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class FieldUT {
  public static final Field getField(Class<?> cls, String field) {
    while (true) {
      try {
        return cls.getDeclaredField(field);
      } catch (Exception e) {
        cls = cls.getSuperclass();
        if (cls == null)
          return null; 
      } 
    } 
  }
  
  public static final Method getMethod(Class<?> cls, String method) {
    while (true) {
      try {
        return cls.getDeclaredMethod(method, null);
      } catch (Exception e) {
        cls = cls.getSuperclass();
        if (cls == null)
          return null; 
      } 
    } 
  }
  
  public static final Method getMethod(Class<?> cls, String method, Class... args) {
    while (true) {
      try {
        return cls.getDeclaredMethod(method, args);
      } catch (Exception e) {
        cls = cls.getSuperclass();
        if (cls == null)
          return null; 
      } 
    } 
  }
  
  public static <T> T newInstance(Class<T> cls) {
    try {
      return cls.newInstance();
    } catch (Exception exception) {
      return null;
    } 
  }
  
  public static <T> T newInstance(Class<T> cls, Object[] args) {
    try {
      if (args == null)
        return cls.newInstance(); 
      Class[] classes = new Class[args.length];
      for (int i = 0; i != args.length; ) {
        classes[i] = args[i].getClass();
        i++;
      } 
      return cls.getConstructor(classes).newInstance(args);
    } catch (Exception exception) {
      return null;
    } 
  }
}
