/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom.JDOMException;
import org.xml.sax.SAXException;

import fern.network.AbstractNetworkImpl;
import fern.network.FeatureNotSupportedException;
import fern.network.rnml.RNMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GibsonBruckSimulator;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.algorithm.TauLeapingRelativeBoundSimulator;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.simulation.controller.DefaultController;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.FireTypeObserver;
import fern.simulation.observer.IntervalObserver;
import fern.simulation.observer.LeapObserver;
import fern.simulation.observer.ReactionIntervalObserver;
import fern.tools.ConfigReader;
import fern.tools.NetworkTools;
import fern.tools.gnuplot.GnuPlot;


public class MMTest {

	/**
	 * @param args
	 * @throws FeatureNotSupportedException 
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws JDOMException 
	 */
	public static void main(String[] args) throws FeatureNotSupportedException, IOException, ParserConfigurationException, SAXException, JDOMException {
		ConfigReader cfg = new ConfigReader("test/configs/mapk.cfg");
		
		AbstractNetworkImpl net = new RNMLNetwork(new File(cfg.getAsString("file")));
//		Network net = new ReversibleNetwork(
//				net2,
//				new KineticConstantPropensityCalculator(net2.getAdjacencyListProducts(),new double[]{0.1})
//				);
		
		NetworkTools.dumpNetwork(net, new PrintWriter(System.out));
		
		Simulator sim = new TauLeapingSpeciesPopulationBoundSimulator(net);
		IntervalObserver amount = 
			new AmountIntervalObserver(sim,cfg.getAsDouble("step"),cfg.getAsStringArr("species"));
		amount.setThetaMethod(false);
//		amount.setPlotQuality(true);
//		IntervalObserver react = 
//			new ReactionIntervalObserver(sim,cfg.getAsDouble("step"),cfg.getAsStringArr("species"));
		
		FireTypeObserver fireObs = new FireTypeObserver(sim);
		fireObs.setPrintWriter(new PrintWriter(System.out));
		sim.addObserver(fireObs);
		
		amount.setLabelFormat("%l - %a");
		
		sim.addObserver(amount);
//		sim.addObserver(leap);
//		sim.addObserver(react);
		
		GnuPlot ag = new GnuPlot();
		ag.setDefaultStyle("with linespoints");
//		GnuPlot rg = new GnuPlot();
		
		ag.setVisible(true);
//		rg.setVisible(true);
		
		for (int i=0; i<1000; i++) {
			sim.start(new DefaultController(cfg.getAsDouble("time")));	
			amount.toGnuplot(ag);
//			leap.toGnuPlot(ag);
//			react.toGnuPlotAvg(rg);
			ag.plot();
//			rg.plot();
			ag.clearData();
//			rg.clearData();
		}
		
		

	}
	

}
