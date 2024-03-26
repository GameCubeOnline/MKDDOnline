package root.ktml;

public class ClassSearch {
  public static Class<?> search(String name, String[] paths) {
    try {
      return Class.forName(name);
    } catch (Exception exception) {
      name = String.valueOf('.') + name;
      byte b;
      int i;
      String[] arrayOfString;
      for (i = (arrayOfString = paths).length, b = 0; b < i; ) {
        String s = arrayOfString[b];
        try {
          return Class.forName(String.valueOf(s) + name);
        } catch (Exception exception1) {}
        b++;
      } 
      return null;
    } 
  }
}
