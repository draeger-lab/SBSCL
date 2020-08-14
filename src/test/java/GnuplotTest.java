import java.io.IOException;

import fern.tools.gnuplot.GnuPlot;

public class GnuplotTest {

  /**
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) throws IOException {
    GnuPlot gp = new GnuPlot();
    double[][] matrix = {
        {0, 1, 1},
        {1, 1, 2},
        {2, 1, 3},
        {1, 2, 4},
        {2, 2, 5},
        {3, 2, 6},
    };
    gp.addData(matrix, new String[]{"A", "B"}, null);
    gp.setDefaultStyle("with linespoints");

    gp.setVisible(true);
    gp.plot();
  }

}
