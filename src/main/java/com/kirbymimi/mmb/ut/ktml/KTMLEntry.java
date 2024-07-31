package com.kirbymimi.mmb.ut.ktml;

import com.kirbymimi.mmb.ut.IteratorWrap;
import com.kirbymimi.mmb.ut.list.HashMapList;
import java.util.Iterator;

public class KTMLEntry implements Iterable<KTMLEntry> {
   String name;
   String type;
   Object value;
   KTMLEntry parent;

   public KTMLEntry(KTMLEntry parent) {
      this.parent = parent;
   }

   public KTMLEntry() {
   }

   public void setAsParent() {
      this.value = new HashMapList(false);
   }

   public String toString() {
      if (this.type == null) {
         return this.name == null ? this.value.toString() : this.name + " " + this.value;
      } else {
         return this.name == null ? this.type + " " + this.value : this.type + " " + this.name + " " + this.value;
      }
   }

   public KTMLEntry getParent() {
      return this.parent;
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

   public KTMLEntry getEntry(int idx) {
      return this.value != null && this.value instanceof HashMapList ? (KTMLEntry)((HashMapList)this.value).get(idx) : null;
   }

   public KTMLEntry getEntry(String name) {
      return this.value != null && this.value instanceof HashMapList ? (KTMLEntry)((HashMapList)this.value).mapGet(name) : null;
   }

   public String getString(int idx) {
      KTMLEntry entry = this.getEntry(idx);
      return entry == null ? null : entry.getAsString();
   }

   public String getString(String name) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : entry.getAsString();
   }

   public String getAsString() {
      return this.value instanceof String ? (String)this.value : null;
   }

   public Boolean getBoolean(int idx) {
      KTMLEntry entry = this.getEntry(idx);
      return entry == null ? null : entry.getAsBoolean();
   }

   public Boolean getBoolean(String name) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : entry.getAsBoolean();
   }

   public Boolean getAsBoolean() {
      return this.value instanceof Boolean ? (Boolean)this.value : null;
   }

   public byte[] getRaw(String name) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : entry.getAsRaw();
   }

   public byte[] getAsRaw() {
      return this.value instanceof byte[] ? (byte[])this.value : null;
   }

   public Character getChar(int idx) {
      KTMLEntry entry = this.getEntry(idx);
      return entry == null ? null : entry.getAsChar();
   }

   public Character getChar(String name) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : entry.getAsChar();
   }

   public Character getAsChar() {
      if (this.value instanceof Character) {
         return (Character)this.value;
      } else {
         return this.value instanceof String && ((String)this.value).length() == 1 ? ((String)this.value).charAt(0) : null;
      }
   }

   public Byte getByte(int idx) {
      KTMLEntry entry = this.getEntry(idx);
      return entry == null ? null : entry.getAsByte();
   }

   public Byte getByte(String name) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : entry.getAsByte();
   }

   public Byte getAsByte() {
      return !(this.value instanceof Number) ? null : this.getAsNumber().byteValue();
   }

   public Short getShort(int idx) {
      KTMLEntry entry = this.getEntry(idx);
      return entry == null ? null : entry.getAsShort();
   }

   public Short getShort(String name) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : entry.getAsShort();
   }

   public Short getAsShort() {
      return !(this.value instanceof Number) ? null : this.getAsNumber().shortValue();
   }

   public Integer getInt(int idx) {
      KTMLEntry entry = this.getEntry(idx);
      return entry == null ? null : entry.getAsInt();
   }

   public Integer getInt(String name) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : entry.getAsInt();
   }

   public Integer getAsInt() {
      return !(this.value instanceof Number) ? null : this.getAsNumber().intValue();
   }

   public Long getLong(int idx) {
      KTMLEntry entry = this.getEntry(idx);
      return entry == null ? null : entry.getAsLong();
   }

   public Long getLong(String name) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : entry.getAsLong();
   }

   public Long getAsLong() {
      return !(this.value instanceof Number) ? null : this.getAsNumber().longValue();
   }

   public Double getDouble(int idx) {
      KTMLEntry entry = this.getEntry(idx);
      return entry == null ? null : entry.getAsDouble();
   }

   public Double getDouble(String name) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : entry.getAsDouble();
   }

   public Double getAsDouble() {
      return !(this.value instanceof Number) ? null : this.getAsNumber().doubleValue();
   }

   public Float getFloat(int idx) {
      KTMLEntry entry = this.getEntry(idx);
      return entry == null ? null : entry.getAsFloat();
   }

   public Float getFloat(String name) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : entry.getAsFloat();
   }

   public Float getAsFloat() {
      return !(this.value instanceof Number) ? null : this.getAsNumber().floatValue();
   }

   public Number getAsNumber() {
      return (Number)this.value;
   }

   public <T> T getEnum(int idx, Class<T> enumCls) {
      KTMLEntry entry = this.getEntry(idx);
      return entry == null ? null : (T) entry.getAsEnum(enumCls);
   }

   public <T> T getEnum(String name, Class<T> enumCls) {
      KTMLEntry entry = this.getEntry(name);
      return entry == null ? null : (T) entry.getAsEnum(enumCls);
   }

   public <T> T getAsEnum(Class<T> enumCls) {
      String s = getAsString();
      if (s == null);
      return (T)Enum.valueOf((Class)enumCls, s);
   }

   public boolean isParent() {
      return this.value != null && this.value instanceof HashMapList;
   }

   public void add(KTMLEntry entry) {
      if (this.value != null && this.value instanceof HashMapList) {
         ((HashMapList)this.value).put(entry.name, entry);
      }
   }

   public KTMLEntry get(String key) {
      return this.value != null && this.value instanceof HashMapList ? (KTMLEntry)((HashMapList)this.value).mapGet(key) : null;
   }

   public KTMLEntry get(int idx) {
      if (this.value != null && this.value instanceof HashMapList) {
         return ((HashMapList)this.value).size() <= idx ? null : (KTMLEntry)((HashMapList)this.value).get(idx);
      } else {
         return null;
      }
   }

   public int getElementCount() {
      return this.value != null && this.value instanceof HashMapList ? ((HashMapList)this.value).size() : -1;
   }

   public Iterator<KTMLEntry> iterator() {
      return this.value != null && this.value instanceof HashMapList ? ((HashMapList)this.value).iterator() : null;
   }

   public Iterable<KTMLEntry> iterable() {
      return this.value != null && this.value instanceof HashMapList ? new IteratorWrap(((HashMapList)this.value).iterator()) : null;
   }
}
