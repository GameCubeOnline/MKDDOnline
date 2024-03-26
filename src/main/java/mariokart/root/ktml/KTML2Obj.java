package root.ktml;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Vector;

public class KTML2Obj {
  String[] classPaths;
  
  public void setClassSearch(String[] classPaths) {
    this.classPaths = classPaths;
  }
  
  public void load(Object obj, KTMLParent ktml) {
    try {
      Class<?> cls = obj.getClass();
      for (KTMLEntry v : ktml) {
        Field f = null;
        if (v.name != null)
          f = FieldUT.getField(cls, v.name); 
        if (f == null) {
          Method m = FieldUT.getMethod(cls, (v.name == null) ? "onToken" : v.name, new Class[] { KTMLEntry.class });
          if (m == null)
            continue; 
          m.invoke(obj, new Object[] { v });
          continue;
        } 
        boolean acc = f.isAccessible();
        f.setAccessible(true);
        Object fieldSet = subLoad(obj, v, f);
        if (fieldSet != null)
          f.set(obj, fieldSet); 
        f.setAccessible(acc);
      } 
      try {
        Method m = cls.getMethod("loadDone", null);
        m.invoke(obj, null);
      } catch (Exception exception) {}
    } catch (Exception e) {
      e.printStackTrace();
    } 
  }
  
  Object subLoad(Object obj, KTMLEntry v, Field f) throws Exception {
    Class<?> type = f.getType();
    if (!type.isPrimitive()) {
      if (type == Integer.class)
        return v.getAsInt(); 
      if (type == Long.class)
        return v.getAsLong(); 
      if (type == Boolean.class)
        return v.getAsBoolean(); 
      if (type == Float.class)
        return v.getAsFloat(); 
      if (type == Double.class)
        return v.getAsDouble(); 
      if (type.isEnum())
        return v.getAsEnum(f.getType()); 
      if (type == Vector.class) {
        Vector<Object> vec = (Vector)f.get(obj);
        for (KTMLEntry child : v.getAsParent()) {
          Object childObj = null;
          Class<?> childCls = null;
          if (child.type != null)
            childCls = ClassSearch.search(child.type, this.classPaths); 
          if (childCls == null && v.type != null)
            ClassSearch.search(v.type, this.classPaths); 
          if (childCls != null) {
            Class<?> ccls = obj.getClass();
            while (ccls != Object.class) {
              try {
                childObj = childCls.getConstructor(new Class[] { ccls }).newInstance(new Object[] { obj });
                break;
              } catch (Exception e) {
                ccls = ccls.getSuperclass();
              } 
            } 
            if (childObj == null)
              childObj = childCls.newInstance(); 
          } else {
            try {
              Class<?> c = (Class)((ParameterizedType)f.getGenericType()).getActualTypeArguments()[0];
              try {
                childObj = c.getConstructor(new Class[] { obj.getClass() }).newInstance(new Object[] { obj });
              } catch (Exception e) {
                childObj = c.newInstance();
              } 
            } catch (Exception e) {
              e.printStackTrace();
            } 
          } 
          if (childObj == null)
            childObj = obj.getClass().newInstance(); 
          load(childObj, child.getAsParent());
          vec.add(childObj);
        } 
        return null;
      } 
      if (type.isArray()) {
        KTMLParent p = v.getAsParent();
        Object ret = Array.newInstance(type.getComponentType(), p.getElementCount());
        int i = 0;
        for (KTMLEntry val : p.elementList)
          Array.set(ret, i++, val.value); 
        return ret;
      } 
      if (type == String.class)
        return v.getAsString(); 
      if (type == String[].class) {
        Vector<KTMLEntry> lst = ((KTMLParent)v.value).elementList;
        String[] ret = new String[lst.size()];
        int cidx = 0;
        for (KTMLEntry entry : lst)
          ret[cidx++] = (String)entry.value; 
        return ret;
      } 
      Class<?> instCls = type;
      if (v.type != null) {
        Class<?> searchCls = ClassSearch.search(v.type, this.classPaths);
        if (searchCls != null)
          instCls = searchCls; 
      } 
      Object inst = null;
      try {
        Constructor<?> cons = instCls.getConstructor(new Class[] { String.class });
        inst = cons.newInstance(new Object[] { v.value });
      } catch (Exception exception) {}
      if (inst == null)
        inst = instCls.newInstance(); 
      KTMLParent instData = v.getAsParent();
      if (instData != null)
        load(inst, instData); 
      return inst;
    } 
    return v.getValue();
  }
}
