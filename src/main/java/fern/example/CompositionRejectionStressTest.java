package fern.example;

import java.io.IOException;

import org.jdom.JDOMException;

import fern.network.Network;
import fern.network.creation.AutocatalyticNetwork;
import fern.network.fernml.FernMLNetwork;
import fern.network.modification.CatalysedNetwork;
import fern.network.modification.ReversibleNetwork;
import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.algorithm.CompositionRejection;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.observer.FireTypeObserver;
import fern.simulation.observer.Observer;
import fern.tools.Stochastics;
import fern.tools.functions.Probability;

public class CompositionRejectionStressTest {

	public static void main(String[] args) throws IOException, JDOMException {
		int repeats = 1;
		int steps = 10;
		
		Stochastics.getInstance().setSeed(1244208624286L);
		System.out.println("Seed: "+Stochastics.getInstance().getSeed() );
		AutocatalyticNetwork net = new AutocatalyticNetwork(
				new char[] {'A','B'},
				new Probability.ReactionProbability(0.55,0),
				new Probability.Constant(0.1),
				10
				);
		
		Network net2 = new FernMLNetwork(new CatalysedNetwork(new ReversibleNetwork(net, net.getReversePropensityCalculator())));
		System.out.println("Species="+net2.getNumSpecies());
		System.out.println("Reaction="+net2.getNumReactions());
		
		
		
		Simulator sim = new GillespieEnhanced(net2);
		FireTypeObserver o = new FireTypeObserver(sim);
		sim.addObserver(o);
		((GillespieEnhanced)sim).setEfficientlyAdaptSum(true);
		
		System.out.println("start");
		long m = System.currentTimeMillis();
		for (int i=0; i<repeats; i++) {
			sim.start(steps);		
		}
		System.out.println((System.currentTimeMillis()-m)+"ms for ge");
		System.out.println(o.toString());
		
		
		sim = new CompositionRejection(net2);
		o = new FireTypeObserver(sim);
		sim.addObserver(o);
		
		m = System.currentTimeMillis();
		for (int i=0; i<repeats; i++) {
			sim.start(steps);		
		}
		System.out.println((System.currentTimeMillis()-m)+"ms for cr");
		System.out.println(o.toString());
	}
	
	private static class CountFiringsObserver extends Observer {

		long firings = 0l;
		public CountFiringsObserver(Simulator sim) {
			super(sim);
		}

		@Override
		public void activateReaction(int mu, double tau, FireType fireType,
				int times) {
			firings+=times;
		}

		@Override
		public void finished() {
			System.out.println("Firings: " +firings);
		}

		@Override
		public void started() {
			firings = 0l;
		}

		@Override
		public void step() {
		}

		@Override
		public void theta(double theta) {
		}
	}
	
}
