/*
 * Created on 05.10.2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.tools;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Reads simple config files for test cases and makes the data of it accessible.
 * <p>
 * The file has to contain lines like<br><code>field=value</code>
 * <p>
 * If a line starts with // or # it is ignored.
 *
 * @author Florian Erhard
 */
public class ConfigReader {

  private HashMap<String, String> config;

  /**
   * Reads the given file and stores the data.
   *
   * @param fn config file
   * @throws IOException
   */
  public ConfigReader(String fn) throws IOException {
    config = new HashMap<>();

    BufferedReader bf = new BufferedReader(new FileReader(fn));
    String line;
    while ((line = bf.readLine()) != null) {
      line = line.trim();
      if (line.startsWith("//") || line.startsWith("#")) {
        line = "";
      }

      if (line.length() > 0) {
        parseLine(line);
      }

    }
  }

  private void parseLine(String line) {
    String key = line.substring(0, line.indexOf('='));
    String value = line.substring(line.indexOf('=') + 1);

    config.put(key, value);
  }

  /**
   * Gets the value of the line <code>key=value</code> as string
   *
   * @param key name of the field
   * @return value
   */
  public String getAsString(String key) {
    return config.get(key);
  }

  /**
   * Gets the value of the line <code>key=value</code> as string array (the value is simply splitted
   * by ,)
   *
   * @param key name of the field
   * @return value
   */
  public String[] getAsStringArr(String key) {
    return config.get(key).split(",");
  }

  /**
   * Gets the value of the line <code>key=value</code> as double
   *
   * @param key name of the field
   * @return value
   */
  public double getAsDouble(String key) {
    return Double.parseDouble(config.get(key));
  }

  /**
   * Gets the value of the line <code>key=value</code> as int
   *
   * @param key name of the field
   * @return value
   */
  public int getAsInt(String key) {
    return Integer.parseInt(config.get(key));
  }

}
