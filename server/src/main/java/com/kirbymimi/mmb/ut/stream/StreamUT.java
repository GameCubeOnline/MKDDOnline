package com.kirbymimi.mmb.ut.stream;

import com.kirbymimi.mmb.system.MMBSystem;
import com.kirbymimi.mmb.ut.ArrayUT;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class StreamUT {
   public static void readObject(DataInputStream in, Object o) {
      Method objRead = null;

      try {
         objRead = o.getClass().getMethod("specialRead", DataInputStream.class, Field.class);
      } catch (Exception var13) {
      }

      try {
         Field[] var6;
         int var5 = (var6 = o.getClass().getDeclaredFields()).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            Field f = var6[var4];
            if (!Modifier.isTransient(f.getModifiers()) && !Modifier.isFinal(f.getModifiers())) {
               Class<?> type = f.getType();
               boolean backAcc = f.isAccessible();
               f.setAccessible(true);
               if (type == Byte.TYPE) {
                  f.setByte(o, in.readByte());
               } else if (type == Short.TYPE) {
                  f.setShort(o, in.readShort());
               } else if (type == Integer.TYPE) {
                  f.setInt(o, in.readInt());
               } else if (type == Long.TYPE) {
                  f.setLong(o, in.readLong());
               } else if (type == Float.TYPE) {
                  f.setFloat(o, in.readFloat());
               } else if (type == Double.TYPE) {
                  f.setDouble(o, in.readDouble());
               } else if (type == Boolean.TYPE) {
                  f.setBoolean(o, in.readBoolean());
               } else if (type == Character.TYPE) {
                  f.setChar(o, in.readChar());
               } else if (type == String.class) {
                  f.set(o, in.readUTF());
               } else if (type.isInstance(Object[].class)) {
                  int cnt = in.readInt();
                  if (cnt != 0) {
                     Object[] nobj = ArrayUT.newArray(type, cnt);

                     for(int i = 0; i != cnt; ++i) {
                        readObject(in, nobj[i]);
                     }

                     f.set(o, nobj);
                  }
               } else {
                  Object fieldVal;
                  if (objRead != null && (fieldVal = objRead.invoke(o, in, f)) != null) {
                     f.set(o, fieldVal);
                  } else if (type.isInstance(Object.class) && in.readBoolean()) {
                     fieldVal = type.newInstance();
                     readObject(in, fieldVal);
                     f.set(o, fieldVal);
                  }
               }

               f.setAccessible(backAcc);
            }
         }
      } catch (Exception var14) {
         MMBSystem.fatalS(var14);
      }

   }

   public static void writeObject(DataOutputStream out, Object o) {
      Method objWrite = null;

      try {
         objWrite = o.getClass().getMethod("specialWrite", DataOutputStream.class, Field.class);
      } catch (Exception var11) {
      }

      try {
         Field[] var6;
         int var5 = (var6 = o.getClass().getDeclaredFields()).length;

         for(int var4 = 0; var4 < var5; ++var4) {
            Field f = var6[var4];
            if (!Modifier.isTransient(f.getModifiers()) && !Modifier.isFinal(f.getModifiers())) {
               Class<?> type = f.getType();
               boolean backAcc = f.isAccessible();
               f.setAccessible(true);
               if (type == Byte.TYPE) {
                  out.write(f.getByte(o));
               } else if (type == Short.TYPE) {
                  out.writeShort(f.getShort(o));
               } else if (type == Integer.TYPE) {
                  out.writeInt(f.getInt(o));
               } else if (type == Long.TYPE) {
                  out.writeLong(f.getLong(o));
               } else if (type == Float.TYPE) {
                  out.writeFloat(f.getFloat(o));
               } else if (type == Double.TYPE) {
                  out.writeDouble(f.getDouble(o));
               } else if (type == Boolean.TYPE) {
                  out.writeBoolean(f.getBoolean(o));
               } else if (type == Character.TYPE) {
                  out.writeChar(f.getChar(o));
               } else if (type == String.class) {
                  out.writeUTF((String)f.get(o));
               } else if (type == Object[].class) {
                  Object[] arr = (Object[])f.get(o);
                  if (arr != null && arr.length != 0) {
                     out.writeInt(arr.length);

                     for(int i = 0; i != arr.length; ++i) {
                        writeObject(out, arr[i]);
                     }
                  } else {
                     out.writeInt(0);
                  }
               } else {
                  if (objWrite != null && (Boolean)objWrite.invoke(o, out, f)) {
                     continue;
                  }

                  if (type == Object.class) {
                     Object obj = f.get(o);
                     if (obj == null) {
                        out.writeBoolean(false);
                     } else {
                        out.writeBoolean(true);
                        writeObject(out, obj);
                     }
                  }
               }

               f.setAccessible(backAcc);
            }
         }
      } catch (Exception var12) {
         MMBSystem.fatalS(var12);
      }

   }

   public static void readArray(DataInputStream in, int[] arr) throws IOException {
      for(int i = 0; i != arr.length; ++i) {
         arr[i] = in.readInt();
      }

   }
}
