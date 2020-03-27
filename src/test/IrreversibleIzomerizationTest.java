package test;

import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

import fern.network.rnml.RNMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.AbstractBaseTauLeaping;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.observer.LeapObserver;
import fern.tools.gnuplot.GnuPlot;

public class IrreversibleIzomerizationTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws JDOMException 
	 */
	public static void main(String[] args) throws IOException, JDOMException {
		RNMLNetwork net = new RNMLNetwork(new File("test/data/rnml/isomerization.xml"));
		
		AbstractBaseTauLeaping sim = new TauLeapingAbsoluteBoundSimulator(net);
		sim.setEpsilon(0.05);
		plotting(sim);
	}
	
	private static void plotting(Simulator sim) throws IOException {
		LeapObserver obs = (LeapObserver) sim.addObserver(new LeapObserver(sim,"S1"));
		GnuPlot gp = new GnuPlot();
		gp.setVisible(true);
		
		sim.start(5);
		obs.toGnuplot(gp);
		gp.plot();
	}

}
