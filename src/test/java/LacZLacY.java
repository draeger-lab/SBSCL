import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.jdom.JDOMException;

import fern.benchmark.SimulatorCorrectness;
import fern.benchmark.SimulatorFireTypes;
import fern.benchmark.SimulatorPerformance;
import fern.benchmark.SimulatorRandomNumbers;
import fern.benchmark.SimulatorTime;
import fern.network.Network;
import fern.network.rnml.RNMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.controller.DefaultController;
import fern.simulation.observer.FireTypeObserver;
import fern.tools.NetworkTools;

public class LacZLacY {

	/**
	 * @param args
	 * @throws JDOMException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, JDOMException {
		Network net = new RNMLNetwork(new File("test/data/rnml/lacy1000.xml"));
		
//		Simulator sim = new GillespieEnhanced(net);
//		sim.start(new DefaultController(1000));
//		NetworkTools.useActualAmountAsInitialAmount(net);
		
		SimulatorPerformance bench = new SimulatorCorrectness(net,1);
		bench.setShowSteps(10);
		
		while(true) {
			bench.benchmark();
		}
		
//		AmountAtMomentObserver obs = new AmountAtMomentObserver(sim,1,"LacZlactose");
//		
//		sim.addObserver(obs);
//		GnuPlot gnuplot = new GnuPlot();
//		gnuplot.setDefaultStyle("with linespoints");
//		
//		while (true) {
//			sim.start(obs);
//			gnuplot.setVisible(true);
//			try {
//				obs.toGnuPlot(gnuplot);
//				gnuplot.plot();
//			} catch (IOException e) {}
//			gnuplot.clearData();
//		}
		
	}

}
