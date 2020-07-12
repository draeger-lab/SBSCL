package fern;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.jdom.JDOMException;

import fern.network.FeatureNotSupportedException;
import fern.network.Network;
import fern.simulation.Simulator;
import fern.simulation.algorithm.AbstractBaseTauLeaping;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.HybridMaximalTimeStep;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.simulation.observer.AmountIntervalObserver;
import fern.tools.NetworkTools;
import fern.tools.NumberTools;
import fern.tools.gnuplot.GnuPlot;

public class Start {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			Map<String,Object> orderedArgs = getArguments(args);
			
			Network net = createNetwork(orderedArgs);
			Simulator sim = createSimulator(net,orderedArgs);
			AmountIntervalObserver obs = createObserver(sim,orderedArgs);
			GnuPlot gp = runSimulation(sim,obs,orderedArgs);
			output(obs,orderedArgs, gp);
			
		}catch(Exception e) {
			System.out.println(getUsage());
			System.out.println();
			System.out.println(e.getMessage());
		}
	}

	
	private static void output(AmountIntervalObserver obs,
			Map<String, Object> orderedArgs, GnuPlot gp) throws IOException {
		obs.toGnuplot(gp);
		gp.setDefaultStyle("with linespoints");
		
		if ((Boolean)orderedArgs.get("i")) {
			gp.setVisible(true);
			gp.plot();
		}
		
		if (((String)orderedArgs.get("p")).length()>0) {
			gp.plot();
			gp.saveImage(new File((String)orderedArgs.get("p")));
		}
		
		System.out.println(gp.getData().get(0));
	}


	private static GnuPlot runSimulation(Simulator sim, AmountIntervalObserver obs,
			Map<String, Object> orderedArgs) throws IOException {
		
		GnuPlot gp = new GnuPlot();
		gp.setDefaultStyle("with linespoints");
		if ((Boolean)orderedArgs.get("i")) {
			gp.setVisible(true);
		}
		
		for (int i=0; i<(Integer)orderedArgs.get("n"); i++) {
			sim.start((Double)orderedArgs.get("time"));
			
			if ((Boolean)orderedArgs.get("i")) {
				obs.toGnuplot(gp);
				gp.plot();
				gp.clearData();
			}
		}
		
		return gp;
	}


	private static AmountIntervalObserver createObserver(Simulator sim,
			Map<String, Object> orderedArgs) {
		String[] species = (String[]) orderedArgs.get("s");
		if (species.length==0)
			species = NetworkTools.getSpeciesNames(sim.getNet(), NumberTools.getNumbersTo(sim.getNet().getNumSpecies()-1));
		
		return (AmountIntervalObserver) sim.addObserver(new AmountIntervalObserver(sim,(Double)orderedArgs.get("interval"),species));
	}


	private static Simulator createSimulator(Network net,
			Map<String, Object> orderedArgs) {
		double eps = (Double) orderedArgs.get("method");
		if (eps==0)
			return new GillespieEnhanced(net);
		else if (eps==-1)
			return new HybridMaximalTimeStep(net);
		else {
			AbstractBaseTauLeaping re = new TauLeapingSpeciesPopulationBoundSimulator(net);
			re.setEpsilon(eps);
			return re;
		}
	}


	private static Network createNetwork(Map<String, Object> orderedArgs) throws IOException, JDOMException, FeatureNotSupportedException, ClassNotFoundException {
		return NetworkTools.loadNetwork(new File((String) orderedArgs.get("file")));
	}


	private static Map<String, Object> getArguments(String[] args) {
		Map<String,Object> re = new HashMap<String, Object>();
		if (args.length<3) throw new IllegalArgumentException("Not enough arguments!");

		re.put("file", args[0]);
		re.put("time", Double.parseDouble(args[1]));
		re.put("interval", Double.parseDouble(args[2]));
		re.put("n", 1);
		re.put("s", new String[0]);
		re.put("method", 0.0);
		re.put("i", false);
		re.put("p", "");
		
		for (int i=3; i<args.length; i++) {
			if (args[i].equals("-n"))
				re.put("n", Integer.parseInt(args[++i]));
			else if (args[i].equals("-s")) {
				int s = i+1;
				while (i+1<args.length && !args[i+1].startsWith("-")) i++;
				String[] a = new String[i-s+1];
				System.arraycopy(args, s, a, 0, a.length);
				re.put("s", a);
			}else if (args[i].equals("-e"))
				re.put("method", 0.0);
			else if (args[i].equals("-h"))
				re.put("method", -1.0);
			else if (args[i].equals("-t"))
				re.put("method", Double.parseDouble(args[++i]));
			else if (args[i].equals("-i"))
				re.put("i", true);
			else if (args[i].equals("-p"))
				re.put("p", args[++i]);
			else throw new IllegalArgumentException("Unrecognized argument: "+args[i]);
		}
		
		return re;
	}


	private static String getUsage() {
		StringBuilder sb = new StringBuilder();
		sb.append("Create trajectories of the stochastic simulation of the given network\n");
		sb.append("and write the trends to stdout (in gnuplot format).\n");
		sb.append("\n");
		sb.append("Usage:\n");
		sb.append("\tjava fern.Start file time interval [-n repeats] [-s species species ...] [-h|-e|-t eps] [-i] [-p file]\n");
		sb.append("\n");
		sb.append("Mandatory arguments:\n");
		sb.append("\tfile:     a reaction network file in a supported format\n");
		sb.append("\ttime:     the time at which the simulation is supposed to end\n");
		sb.append("\tinterval: the interval of recording the amounts\n");
		sb.append("\n");
		sb.append("Optional arguments:\n");
		sb.append("\t-n:       the number of repeats of the simulation; default is 1\n");
		sb.append("\t-s:       list of species to record; default is each species in the net\n");
		sb.append("\t-e:       use the exact enhanced direct method (default)\n");
		sb.append("\t-h:       use the hybrid maximal time step method\n");
		sb.append("\t-t:       use tau leaping with given epsilon\n");
		sb.append("\t-i:       show the plot interactivly in a window\n");
		sb.append("\t-p:       write the plot to a png file\n");
		sb.append("\n");
		return sb.toString();
	}
}
