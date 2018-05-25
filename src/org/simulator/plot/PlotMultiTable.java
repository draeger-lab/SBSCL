/*
 * $Id$
 * $URL$
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
 * 8. Duke University, Durham, NC, US
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
import java.util.Iterator;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.ui.ApplicationFrame;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.MultiTable.Block.Column;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

/**
 * This class adds output data-plot support to SBSCL
 * 
 * @author Shalin Shah
 * @since 1.5
 */
@SuppressWarnings("serial")
public class PlotMultiTable extends ApplicationFrame {
	private MultiTable species;
	private DefaultCategoryDataset data;
	private String title;

	/**
	 * Initializes the JFreeChart and dataSet for the chart using MultiTable
	 * 
	 * @param MultiTable
	 *        The input data type to the plot API is MultiTable which gets converted
	 *        internally to DefaultCategoryDataset
	 */
	public PlotMultiTable(MultiTable table, String title) {
		super(title);

		this.title = title;
		species = table;
		JFreeChart lineChart = ChartFactory.createLineChart(title, 
				"time", "conentration (nM)", createDataset(),
				PlotOrientation.VERTICAL, true, true, false);

		ChartPanel chartPanel = new ChartPanel( lineChart );
		chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
		setContentPane(chartPanel);

	}
	
	public PlotMultiTable(MultiTable table) {
		super("Output plot");

		this.title = "Output plot";
		species = table;
		JFreeChart lineChart = ChartFactory.createLineChart(title, 
				"time", "conentration (nM)", createDataset(),
				PlotOrientation.VERTICAL, true, true, false);

		ChartPanel chartPanel = new ChartPanel( lineChart );
		chartPanel.setPreferredSize( new java.awt.Dimension( 560 , 367 ) );
		setContentPane(chartPanel);

	}

	/**
	 * Helper function that converts MultiTable to DataSet for LineChart
	 */
	private DefaultCategoryDataset createDataset() {
		data = new DefaultCategoryDataset();


		for(int i = 1; i < species.getColumnCount(); i++) {
			Column col = species.getColumn(i);

			int time_step = 0;
			for (Iterator<Double> iter = col.iterator(); iter.hasNext(); time_step++){
				data.addValue(iter.next().doubleValue(), col.getColumnName(),  String.valueOf(species.getTimePoint(time_step)));
			}
		}

		return data;
	}
}