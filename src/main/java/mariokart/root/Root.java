package root;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;

public class Root {
  public Root(final ServerInterface Inter) {
    Mixer.Init();
    (new Thread() {
        public void run() {
          (new ServerAccept(Inter)).run();
        }
      }).start();
    while (true) {
      try {
        String linefull = readLine().trim().replace(',', ' ').replaceAll(" +", " ");
        if (linefull.isEmpty())
          continue; 
        Inter.ProcessCommand(linefull, null);
      } catch (Exception exception) {}
    } 
  }
  
  public static String readLine() throws IOException {
    Console Cons = System.console();
    if (Cons != null)
      return Cons.readLine(); 
    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
    String Str = bufferedReader.readLine();
    if (Str == null)
      return ""; 
    return Str;
  }
}
