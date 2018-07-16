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
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.jlibsedml.execution.IProcessedSedMLSimulationResults;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * This class adds output data-plot support to SBSCL
 * 
 * @author Shalin Shah
 * @since 1.5
 */
@SuppressWarnings("serial")
public class PlotProcessedSedmlResults extends ApplicationFrame {
	private IProcessedSedMLSimulationResults species;
	private DefaultCategoryDataset graphData;
	private String title;

	/**
	 * Initializes the JFreeChart and dataSet for the chart using MultiTable
	 * 
	 * @param MultiTable
	 *        The input data type to the plot API is MultiTable which gets converted
	 *        internally to DefaultCategoryDataset
	 */
	public PlotProcessedSedmlResults(IProcessedSedMLSimulationResults data, String title) {
		super(title);

		this.title = title;
		species = data;
		JFreeChart lineChart = ChartFactory.createLineChart(title, 
				"time", "Population", createDataset(),
				PlotOrientation.VERTICAL, true, true, false);

		ChartPanel chartPanel = new ChartPanel( lineChart );
		chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
		setContentPane(chartPanel);

	}
	
	public PlotProcessedSedmlResults(IProcessedSedMLSimulationResults data) {
		super("Output plot");

		this.title = "Output plot";
		species = data;
		JFreeChart lineChart = ChartFactory.createLineChart(title, 
				"time", "Population", createDataset(),
				PlotOrientation.VERTICAL, true, true, false);

		ChartPanel chartPanel = new ChartPanel( lineChart );
		chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
		setContentPane(chartPanel);

	}

	/**
	 * Helper function that converts IProcessedSedMLSimulationResults to DataSet for LineChart
	 */
	private DefaultCategoryDataset createDataset() {
		graphData = new DefaultCategoryDataset();

		double[][] data = species.getData();
		String[] headers = species.getColumnHeaders();
		
		// Add all the data generators to the chart
		for(int col = 0; col < data[0].length; col++) {
			
			for(int row = 0; row < data.length; row++) {
				graphData.addValue(data[row][col], headers[col],  String.valueOf(row));
			}
		}

		return graphData;
	}
}