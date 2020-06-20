/*
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2016 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
 * 8. Duke University, USA
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.plot;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartUtilities;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jlibsedml.Curve;
import org.jlibsedml.execution.IProcessedSedMLSimulationResults;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * This class adds output data-plot support to SBSCL
 *
 * @author Shalin Shah
 * @since 1.5
 */
@SuppressWarnings("serial")
public class PlotProcessedSedmlResults extends ApplicationFrame {

  private IProcessedSedMLSimulationResults species;
  private XYSeriesCollection graphData;
  private String title;
  private List<Curve> curves;
  private JFreeChart lineChart;

  private static final int CHART_WIDTH = 1366;
  private static final int CHART_HEIGHT = 768;

  /**
   * Initializes the JFreeChart and dataSet for the chart using IProcessedSedMLSimulationResults
   *
   * @param data The input data type to the plot API is 2D data wrapped with jsedmllib data structure
   *                                         which gets converted internally to XY line plot
   */
  public PlotProcessedSedmlResults(IProcessedSedMLSimulationResults data, String title) {
    super(title);
    this.title = title;
    this.species = data;
    this.lineChart = ChartFactory.createXYLineChart(this.title, ".", ".", createDataset(), PlotOrientation.VERTICAL, true, true, false);
    ChartPanel chartPanel = new ChartPanel(this.lineChart);
    chartPanel.setPreferredSize(new java.awt.Dimension(CHART_WIDTH, CHART_HEIGHT));
    setContentPane(chartPanel);
  }

  public PlotProcessedSedmlResults(IProcessedSedMLSimulationResults data, List<Curve> curves, String title) {
    super(title);
    this.title = title;
    this.species = data;
    this.curves = curves;
    this.lineChart = ChartFactory.createXYLineChart(this.title, ".", ".", createDataset(), PlotOrientation.VERTICAL, true, true, false);
    ChartPanel chartPanel = new ChartPanel(this.lineChart);
    chartPanel.setPreferredSize(new java.awt.Dimension(CHART_WIDTH, CHART_HEIGHT));
    setContentPane(chartPanel);
  }

  /**
   * Helper function that converts IProcessedSedMLSimulationResults to DataSet for LineChart
   */
  private XYSeriesCollection createDataset() {
    graphData = new XYSeriesCollection();
    // For each curve in the current output element of sedml file
    // simple extract data generators and plot them
    for (Curve cur : this.curves) {
      Double[] xData = species.getDataByColumnId(cur.getXDataReference());
      Double[] yData = species.getDataByColumnId(cur.getYDataReference());
      XYSeries series = new XYSeries(cur.getId(), false);
      for (int row = 0; row < Math.min(xData.length, yData.length); row++) {
        series.add(xData[row], yData[row]);
      }
      graphData.addSeries(series);
    }
    return graphData;
  }

  /**
   * Helper function that can save the generated plot (simulationPath sedml file) as a PNG image
   * with fileName in the results folder
   */
  public void savePlot(String simulationPath, String fileName)
      throws IOException {
    // Get full folder for sedml xml file
    String outputPath = getFolderPathForTestResource(simulationPath);
    // Store the plots in the results folder in the same directory
    outputPath = outputPath + "/results/simulation_core/" + fileName + ".png";
    OutputStream out = FileUtils.openOutputStream(new File(outputPath));
    // Use default width and height for chart size and save as png
    ChartUtilities.writeChartAsPNG(out, this.lineChart, CHART_WIDTH, CHART_HEIGHT);
    out.close();
  }

  /**
   * Gets a folder path for given test resource.
   *
   * @param resourcePath Example: resourcePath="/fba/e_coli_core.xml"
   */
  private static String getFolderPathForTestResource(String resourcePath) {
    String path = null;
    File currentDir = new File(System.getProperty("user.dir"));
    path = currentDir.getAbsolutePath() + "/src/test/resources" + resourcePath;
    return Paths.get(path).getParent().getFileName().toString();
  }
}
