package fern.network;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.jdom.JDOMException;

import fern.network.fernml.FernMLNetwork;

public class NetworkLoader {

  /**
   * Gets types of networks that are readable by FERN. They are returned as String[N][2] containing
   * for N types a description and the file pattern.
   *
   * @return readable file types
   */
  public static String[][] getAvailableTypes() {
    return new String[][]{{"FernML (*.xml)", "*.xml"},};
  }

  /**
   * Tries to read the given file and returns the network in it (if there is one)
   *
   * @param file network file
   * @return network object
   * @throws IOException                  file could not be found
   * @throws JDOMException                file could not be parsed correctly
   * @throws FeatureNotSupportedException file contains a not supported sbml feature
   * @throws ClassNotFoundException       the class SBMLNetwork could not be found
   */
  public static Network readNetwork(File file)
      throws IOException, JDOMException, FeatureNotSupportedException,
      ClassNotFoundException {
    BufferedReader r = new BufferedReader(new FileReader(file));
    Network net = null;
    String line;
    while ((line = r.readLine()) != null) {
      if (line.toLowerCase().contains("<sbml")) {
        // use reflection to instantiate the sbml network
        // this makes sure that the package fern.network.sbml can be deleted
        try {
          net = (Network) ClassLoader.getSystemClassLoader()
              .loadClass(fern.network.sbml.SBMLNetwork.class.getName()).getConstructor(File.class)
              .newInstance(file);
        } catch (Exception e) {
          r.close();
          e.printStackTrace();
          throw new ClassNotFoundException(
            "The SBMLNetwork could not be loaded!\n"
                + e.getClass().getSimpleName() + " message: " + e.getMessage());
        }
        break;
      } else if (line.toLowerCase().contains("<fernml")) {
        net = new FernMLNetwork(file);
        break;
      }
    }
    r.close();
    return net;
  }

}
