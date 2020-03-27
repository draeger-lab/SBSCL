package test;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.jdom.JDOMException;

import fern.benchmark.SimulatorCorrectness;
import fern.benchmark.SimulatorPerformance;
import fern.network.Network;
import fern.network.rnml.RNMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.controller.DefaultController;
import fern.simulation.observer.FireTypeObserver;
import fern.tools.NetworkTools;

public class Schloegl {

	/**
	 * @param args
	 * @throws JDOMException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, JDOMException {
		Network net = new RNMLNetwork(new File("test/data/rnml/schloegl.xml"));
		
		
		SimulatorPerformance bench = new SimulatorCorrectness(net,4,"X");
		
		while(true) {
			bench.benchmark();
		}
		
	}

}
