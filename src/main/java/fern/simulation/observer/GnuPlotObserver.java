package fern.simulation.observer;

import java.io.IOException;

import fern.tools.gnuplot.GnuPlot;

/**
 * Interface for certain observer which can produce gnuplot data.
 *
 * @author Florian Erhard
 */
public interface GnuPlotObserver {


  /**
   * Gets the styles for the columns. If you don't want styles, just return null!
   *
   * @return styles for the columns
   */
  public String[] getStyles();

  /**
   * Creates a new {@link GnuPlot} object and passes the actual observer data to it.
   *
   * @throws IOException if gnuplot could not be accessed
   * @return the created <code>GnuPlot</code> object
   */
  public GnuPlot toGnuplot() throws IOException;

  /**
   * Passes the actual observer data to a {@link GnuPlot} object.
   *
   * @param gnuplot the <code>GnuPlot</code> object to pass the data to
   * @throws IOException if gnuplot could not be accessed
   * @return the <code>GnuPlot</code> object
   */
  public GnuPlot toGnuplot(GnuPlot gnuplot) throws IOException;


}
