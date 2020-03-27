package fern.benchmark;

import java.io.IOException;

import fern.network.Network;
import fern.simulation.Simulator.FireType;
import fern.simulation.observer.FireTypeObserver;
import fern.tools.gnuplot.GnuPlot;

/**
 * Benchmark the {@link FireType}s for a given net. Use this benchmark, if you want to know,
 * how many SSA steps are performed at tau leaping for a given network. 
 * The <code>benchmark</code> method can be invoked
 * repeatedly to calculate the average over many simulations.
 * 
 * @author Florian Erhard
 *
 */
public class SimulatorFireTypes extends SimulatorTime {
	
	private FireTypeObserver[] obs;
	private GnuPlot gnuplotRandom;
	
	/**
	 * Create the benchmark and defines the time each simulator has to run in one iteration.
	 * @param net the network to benchmark
	 * @param time running time for the simulators
	 */
	public SimulatorFireTypes(Network net, double time) {
		super(net, time);
		
		obs = new FireTypeObserver[simulators.length];
		for (int i=0; i<obs.length; i++) {
			obs[i] = new FireTypeObserver(simulators[i]);
			simulators[i].addObserver(obs[i]);
			obs[i].setLabelFormat("%a");
		}
		gnuplotRandom = new GnuPlot();
		
	}
	

	/**
	 * Present results of this benchmark is gnuplot and text to stdout.
	 */
	@Override
	public void present() {
		super.present();
		
		for (int i=0; i<obs.length; i++) {
			obs[i].toGnuplot(gnuplotRandom);
			System.out.println(obs[i].toString());
		}
		
		gnuplotRandom.setVisible(true);
		try {
			gnuplotRandom.plot();
		} catch (IOException e) {}
		gnuplotRandom.clearData();
	}



}
