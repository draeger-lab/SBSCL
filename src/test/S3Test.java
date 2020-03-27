package test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.jdom.JDOMException;

import fern.network.AbstractNetworkImpl;
import fern.network.rnml.RNMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.AbstractTauLeapingPropensityBoundSimulator;
import fern.simulation.algorithm.GibsonBruckSimulator;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.algorithm.TauLeapingRelativeBoundSimulator;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.simulation.controller.DefaultController;
import fern.simulation.observer.AmountAtMomentObserver;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.FireTypeObserver;
import fern.simulation.observer.InstantOutputObserver;
import fern.tools.ConfigReader;
import fern.tools.gnuplot.GnuPlot;

public class S3Test {

	public static void main(String[] args) throws IOException, JDOMException {
		ConfigReader cfg = new ConfigReader("test/configs/s3.cfg");
		
		AbstractNetworkImpl net = new RNMLNetwork(new File(cfg.getAsString("file")));
		
		Simulator[] sims = new Simulator[] {
				new GillespieSimple(net),
				new TauLeapingAbsoluteBoundSimulator(net),
				new TauLeapingRelativeBoundSimulator(net),
				new TauLeapingSpeciesPopulationBoundSimulator(net)
		};
		AmountAtMomentObserver[] obs = new AmountAtMomentObserver[sims.length];
		FireTypeObserver[] types = new FireTypeObserver[sims.length];
		
		for (int i=0; i<sims.length; i++) {
			obs[i] = new AmountAtMomentObserver(sims[i],cfg.getAsDouble("moment"),cfg.getAsStringArr("species"));
			obs[i].setLabelFormat("%a %l");
			types[i] = new FireTypeObserver(sims[i]);
			sims[i].addObserver(obs[i]);
			sims[i].addObserver(types[i]);
		}
		
		GnuPlot mg = new GnuPlot();
		mg.setDefaultStyle("with linespoints");
		
		for (int i=0; i<=1000000; i++) {
			for (int j=0; j<sims.length; j++)
				sims[j].start(obs[j]);
			
			if (i%1000==0) {
				for (int j=0; j<sims.length; j++)
					obs[j].toGnuplot(mg);
				mg.plot();
				mg.setVisible(true);
				mg.clearData();
				for (int j=0; j<sims.length; j++)
					System.out.println(types[j].toString());
			}
		}
	}
	
}
