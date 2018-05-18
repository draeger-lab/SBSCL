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

	/**
	 * Initializes the JFreeChart and dataSet for the chart using MultiTable
	 * 
	 * @param MultiTable
	 *        The input data type to the plot API is MultiTable which gets converted
	 *        internally to DefaultCategoryDataset
	 */
	public PlotMultiTable(MultiTable table) {
		super("Model simulation");

		species = table;
		JFreeChart lineChart = ChartFactory.createLineChart("chart title", 
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