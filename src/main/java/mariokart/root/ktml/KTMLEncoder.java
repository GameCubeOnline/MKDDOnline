package root.ktml;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class KTMLEncoder {
  PrintWriter printer;
  
  public ByteArrayOutputStream outStream;
  
  public KTMLEncoder(File output) throws FileNotFoundException {
    this.printer = new PrintWriter(output);
  }
  
  public KTMLEncoder() {
    this.outStream = new ByteArrayOutputStream();
    this.printer = new PrintWriter(this.outStream);
  }
  
  public void add(String type, String name, Object value) {
    this.printer.println(String.valueOf('<') + type + "> \"" + name + "\" " + value);
  }
  
  public void add(String name, Object value) {
    this.printer.println("\"" + name + "\" " + value);
  }
  
  public void add(Object Value) {
    this.printer.println(Value);
  }
  
  public void addType(String type, Object value) {
    this.printer.println(String.valueOf('<') + type + "> " + value);
  }
  
  public void closeType() {
    this.printer.println('}');
  }
  
  public void close() {
    this.printer.close();
  }
  
  public byte[] getBytes() {
    return this.outStream.toByteArray();
  }
}
