package test;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.jdom.JDOMException;


import fern.network.Network;
import fern.network.rnml.RNMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.AbstractBaseTauLeaping;
import fern.simulation.algorithm.GibsonBruckSimulator;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.algorithm.TauLeapingRelativeBoundSimulator;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.simulation.controller.AndController;
import fern.simulation.controller.SimulationController;
import fern.simulation.observer.AmountAtMomentObserver;
import fern.simulation.observer.LeapObserver;
import fern.tools.NetworkTools;
import fern.tools.NumberTools;
import fern.tools.gnuplot.GnuPlot;

public class DecayProneTest {

	/**
	 * @param args
	 * @throws JDOMException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, JDOMException {
		RNMLNetwork net = new RNMLNetwork(new File("test/data/rnml/decayprone.xml"));
		
		AbstractBaseTauLeaping sim = new TauLeapingAbsoluteBoundSimulator(net);
		NetworkTools.dumpNetwork(net);
		
//		plotting(sim);
		
		allTrajectory(net,1000);
		
	}
	
	private static void plotting(Simulator sim) throws IOException {
		LeapObserver obs = (LeapObserver) sim.addObserver(new LeapObserver(sim,"S1","S2","S3"));
		GnuPlot gp = new GnuPlot();
		gp.setVisible(true);
		
		sim.start(24);
		obs.toGnuplot(gp);
		gp.plot();
	}
	
	private static void allTrajectory(Network net, int times) throws IOException {
		GnuPlot[] gp = new GnuPlot[3];
		
		Simulator[] sims = new Simulator[] {
//				new GibsonBruckSimulator(net),
				new TauLeapingAbsoluteBoundSimulator(net),
				new TauLeapingRelativeBoundSimulator(net), 
				new TauLeapingSpeciesPopulationBoundSimulator(net)
				};
		
		Map[][] histos = new Map[sims.length][3];
		
		for (int s=0; s<sims.length; s++) {
			System.out.println(sims[s].getName());
			histos[s] = trajectory(sims[s], gp, times);
			for (int i=0; i<gp.length; i++) {
				gp[i].setVisible(true);
				gp[i].plot();
			}
			System.out.println();
		}
		
		System.out.println();
		for (int s=0; s<3; s++) {
			System.out.println("Histogram distances for S"+(s+1)+":");
			for (int i=0; i<histos.length; i++) {
				for (int j=0; j<histos.length; j++) {
					System.out.printf("%.4f\t",NumberTools.calculateHistogramDistance(histos[i][s], histos[j][s]));
				}
				System.out.println();
			}
		}
	}

	private static Map[] trajectory(Simulator sim, GnuPlot[] gp, int times) throws IOException {
		if (gp[0]==null) 
			for (int i=0; i<3; i++) {
				gp[i] = new GnuPlot();
				gp[i].setDefaultStyle("with linespoints");
			}
		
		
		sim.getNet().setInitialAmount(0, 4150);
		sim.getNet().setInitialAmount(1, 39565);
		sim.getNet().setInitialAmount(2, 3445);
		
		AmountAtMomentObserver[] obs = new AmountAtMomentObserver[3];
		for (int i=0; i<3; i++) {
			obs[i] = new AmountAtMomentObserver(sim,10,"S"+(i+1));
			obs[i].setLabelFormat("%a - %l");
			sim.addObserver(obs[i]);
		}
		SimulationController c = new AndController(obs);
		
		long start = System.currentTimeMillis();
		for (int i=1; i<=times; i++) {
			sim.start(c);
			if (i%(times/100.0)==0)
				System.out.print(".");
		}
		long end = System.currentTimeMillis();
		System.out.println();
		System.out.println(dateFormat(end-start));
		
		for (int i=0; i<3; i++) 
			obs[i].toGnuplot(gp[i]);
		
		Map[] re = new Map[obs.length];
		for (int i=0; i<re.length; i++)
			re[i] = obs[i].getHistogram(0);
		return re;
	}

	private static String dateFormat(long l) {
		int ms = (int) (l % 1000);
		l/=1000;
		int s = (int) (l % 60);
		l/=60;
		int m = (int) (l % 60);
		l/=60;
		StringBuilder sb = new StringBuilder();
		if (l>0) sb.append(l+"h ");
		if (m>0) sb.append(m+"m ");
		if (s>0) sb.append(s+"s ");
		sb.append(ms+"ms ");
		return sb.toString();
	}
	
}
