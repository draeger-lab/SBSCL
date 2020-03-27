package test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.jdom.JDOMException;

import fern.network.AbstractKineticConstantPropensityCalculator;
import fern.network.rnml.RNMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.algorithm.AbstractBaseTauLeaping;
import fern.simulation.algorithm.GibsonBruckSimulator;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.algorithm.TauLeapingRelativeBoundSimulator;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.simulation.controller.DefaultController;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.FireTypeObserver;
import fern.simulation.observer.InstantOutputObserver;
import fern.simulation.observer.Observer;
import fern.tools.NetworkTools;
import fern.tools.Stochastics;
import fern.tools.gnuplot.GnuPlot;

public class TauLeapingTest {

	/**
	 * @param args
	 * @throws JDOMException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, JDOMException {
		RNMLNetwork net = new RNMLNetwork(new File("test/data/rnml/lacz.xml"));
		NetworkTools.dumpNetwork(net);
		
		Simulator ssa = new GillespieSimple(net);		
//		((AbstractBaseTauLeaping) ssa).setEpsilon(0.1);
//		((AbstractBaseTauLeaping) ssa).setUseSimpleFactor(2);
//		((AbstractBaseTauLeaping) ssa).setVerbose(true);
		
		AmountIntervalObserver obs = new AmountIntervalObserver(ssa,10,"LacZ");
		ssa.addObserver(obs);
		ssa.addObserver(new CellGrowthObserver(ssa,2100));
		ssa.addObserver(new FireTypeObserver(ssa)).setPrintWriter(new PrintWriter(System.out));
//		ssa.addObserver(new InstantOutputObserver(ssa,new PrintWriter(System.out)));
		ssa.addObserver(new Observer(ssa) {
			long num = 0;
			@Override
			public void activateReaction(int mu, double tau, FireType fireType, int times) {
				num+=times;
			}

			@Override
			public void finished() {}

			@Override
			public void started() {
				setTheta(0);
			}

			@Override
			public void step() {}

			@Override
			public void theta(double theta) {
				System.out.println(getSimulator().getTime()+"\t"+num+"\t"+getSimulator().getAmount(getSimulator().getNet().getSpeciesByName("LacZ")));
				setTheta(theta+100);
			}
			
		});
		
		
		GnuPlot gp = new GnuPlot();
		
		for (int i=0; i<1; i++) {
			long start = System.currentTimeMillis();
			ssa.start(10*35*60);
			System.out.println(System.currentTimeMillis()-start);
			gp.setVisible(true);
			obs.toGnuplot(gp);
			gp.plot();
//			gp.clearData();
			
		}
		
		
		
		if (true) return;
		NetworkTools.useActualAmountAsInitialAmount(net);
		
		NetworkTools.dumpNetwork(net);
		
		AbstractBaseTauLeaping sim = new TauLeapingAbsoluteBoundSimulator(net);
		sim.setVerbose(true);
		
		sim.start(1);
	}
	
	private static class CellGrowthObserver extends Observer {

		
		double generationTime;
		double recentTime;
		public CellGrowthObserver(Simulator sim, double generationTime) {
			super(sim);
			
			this.generationTime = generationTime;
		}
		
		@Override
		public void activateReaction(int mu, double tau, FireType fireType, int times) {
		}

		@Override
		public void finished() {}

		@Override
		public void started() {
			getSimulator().setVolume(1);
			recentTime = Double.NEGATIVE_INFINITY;
		}

		@Override
		public void step() {
			
			if((int)(getSimulator().getTime()/generationTime)>(int)(recentTime/generationTime)){
				for (int i=0; i<getSimulator().getNet().getNumSpecies(); i++) {
					if (getSimulator().getNet().getSpeciesByName("PLac")!=i)
						getSimulator().setAmount(i,getSimulator().getAmount(i)/2);
					else
						getSimulator().setAmount(i,1);
				}
			}
			
			double vol = 1.0+getSimulator().getTime()/generationTime;
			int rnap = getSimulator().getNet().getSpeciesByName("RNAP");
			int ribosome = getSimulator().getNet().getSpeciesByName("Ribosome");
			getSimulator().setAmount(rnap, (long) Stochastics.getInstance().getNormal(35.0*vol,3.5)); 
			getSimulator().setAmount(ribosome, (long) Stochastics.getInstance().getNormal(350*vol,35));
			getSimulator().setVolume(vol);
			
			recentTime = getSimulator().getTime();
		}

		@Override
		public void theta(double theta) {
			// TODO Auto-generated method stub
			
		}
		
	}

}
