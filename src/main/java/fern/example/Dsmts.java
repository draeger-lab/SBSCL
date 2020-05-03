package fern.example;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import fern.network.FeatureNotSupportedException;
import fern.network.sbml.SBMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.CompositionRejection;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.IntervalObserver;
import fern.tools.NumberTools;
import fern.tools.gnuplot.GnuPlot;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

import javax.xml.stream.XMLStreamException;

/**
 * Perform a series of tests (refer to http://www.calibayes.ncl.ac.uk/Resources/dsmts).
 * You have to specify the path to the unpacked dsmts archive. The method test produces
 * one line of text containing the test results for each species in the model. If specified,
 * it also produces 4 plots:  
 * <ul>
 * <li>average trend curve of the simulated trajectories and the analytical determined</li>
 * <li>stddev trend curve of the simulated trajectories and the analytical determined</li>
 * <li>deviation of the simulated averages to the real ones (the z values described in the dsmts user guide)</li>
 * <li>deviation of the simulated stddevs to the real ones (the y values described in the dsmts user guide)</li>
 * </ul>
 * 
 * It may be wise to leave the producePlot flag set to false in the for loop because for
 * each test model 4 windows will pop up!
 * 
 */
public class Dsmts {

	public static void main(String[] args) throws IOException {
		int runs = 10000;
		String path = "dsmts/";
		
		for (String f : new File(path).list()) {
			try {
			if (f.endsWith(".xml"))
				System.out.println(test(path+f.substring(0,f.length()-4),runs,false));
			} catch (Exception e) {
				System.out.println("Error: "+e.getMessage());
			}
		}
		
		//System.out.println(test("dsmts3/dsmts-001-01",runs, true));
	}
	
	private static String test(String test, int runs, boolean producePlot) throws FeatureNotSupportedException, IOException, XMLStreamException, ModelOverdeterminedException {
		
		// usual stuff: load network, create simulator and add the observer
		SBMLNetwork net = new SBMLNetwork(new File(test+".xml"));
		Simulator sim = new CompositionRejection(net);
		net.registerEvents(sim);  // important for sbml event handling!
		IntervalObserver obs = (IntervalObserver)sim.addObserver(new AmountIntervalObserver(sim,1,NumberTools.getNumbersTo(net.getNumSpecies()-1)));
		
		// collect the data also outside of the observer since we want to calculate stddevs
		double[][][] data = new double[runs][][]; // runs, time, species
		
		// perform the simulations and collect the data
		for (int i=0; i<runs; i++) {
			sim.start(50);
			data[i] = obs.getRecentData();
		}
		
		// load / calculate the averages and stddevs
		double[][] avg = getCSV(new File(test+"-mean.csv"));
		double[][] calcAvg = getAvg(data);
		double[][] stddev = getCSV(new File(test+"-sd.csv"));
		double[][] calcStddev = getStddevSq(data, avg); // squared!!!

		// count occurrences of avg outside [-3:3] of the transformed means (see the dsmts user guide)
		int[] numA = new int[avg[0].length-1];
		double[][] Z = new double[avg[0].length][Math.min(calcAvg.length, avg.length)]; 
		double z;
		for (int t=0; t<Math.min(calcAvg.length, avg.length); t++) {
			for (int s=1; s<avg[t].length; s++) {
				z = Math.sqrt(runs)*(calcAvg[t][s]-avg[t][s])/stddev[t][s];
				if (Double.isNaN(z)) continue;
				Z[s][t] = z;
				
//				System.out.println(String.format("Zeit: %d  %.3f in %.3f +/- %.3f   z=%.3f", t, calcAvg[t][s], avg[t][s], stddev[t][s],z));
				
				if (Math.abs(z)>3) {
					numA[s-1]++;
				}
			}
			Z[0][t]=t;
		}
				
				
		
		// count occurrences of stddev outside [-5:5] of the transformed means
		int[] numS = new int[avg[0].length-1];
		double[][] Y = new double[avg[0].length][Math.min(calcAvg.length, avg.length)]; 
		double y;
		for (int t=0; t<Math.min(calcAvg.length, avg.length); t++){
			for (int s=1; s<avg[t].length; s++) {
				y = Math.sqrt(runs/2.0)*(calcStddev[t][s]/(stddev[t][s]*stddev[t][s])-1);
				if (Double.isNaN(y)) continue;
				Y[s][t] = y;
				
//				System.out.println(String.format("Zeit: %d  Ist %.3f Soll %.3f   y=%.3f", t, Math.sqrt(calcStddev[t][s]), stddev[t][s],y));
				
				if (Math.abs(y)>5)
					numS[s-1]++;
			}
			Y[0][t]=t;
		}
		
		// if you want plots, you will get them!
		if (producePlot) {
			String[] species = new String[net.getNumSpecies()];
			for (int i=0; i<species.length; i++) species[i] = net.getSpeciesName(i)+" - calculated";
			String[] species2 = new String[net.getNumSpecies()];
			for (int i=0; i<species2.length; i++) species2[i] = net.getSpeciesName(i)+" - analytic";
			
			// the stddevs are still squared... we do not want that
			for (int t=0; t<Math.min(calcAvg.length, avg.length); t++)
				for (int s=1; s<avg[t].length; s++) 
					calcStddev[t][s]=Math.sqrt(calcStddev[t][s]);
			
			
			GnuPlot gp = new GnuPlot();
			gp.setTitle("Avg");
			gp.addCommand("set title \"Average trend curve\"");
			gp.setDefaultStyle("with linespoints");
			gp.addData(calcAvg,false,species,null);
			gp.addData(avg,false,species2,null);
			gp.setVisible(true);
			gp.plot();
			
			gp = new GnuPlot();
			gp.setTitle("Stddev");
			gp.addCommand("set title \"Stddev trend curve\"");
			gp.setDefaultStyle("with linespoints");
			gp.addData(calcStddev,false,species,null);
			gp.addData(stddev,false,species2,null);
			gp.setVisible(true);
			gp.plot();
			
			gp = new GnuPlot();
			gp.setTitle("Avg");
			gp.addCommand("set title \"Difference of avg to N(1,0) in [stddev]\"");
			gp.setDefaultStyle("with linespoints");
			gp.addData(Z, true, species, null);
			gp.setVisible(true);
			gp.plot();
			
			gp = new GnuPlot();
			gp.setTitle("Stddev");
			gp.addCommand("set title \"Difference of stddev to N(1,0) in [stddev]\"");
			gp.setDefaultStyle("with linespoints");
			gp.addData(Y, true, species, null);
			gp.setVisible(true);
			gp.plot();
			
		}
		
		StringBuilder sb = new StringBuilder();
		for (int s=0; s<numA.length; s++)
			sb.append(String.format("Test %s (%s):  Avg outside of [-3:3]: %d   Stddev outside of [-5:5]: %d\n", test.substring(test.lastIndexOf('/')+1), net.getSpeciesName(s), numA[s], numS[s]));
			
		return sb.toString();
	}
	

	/**
	 * Load a csv file into a double matrix
	 * 
	 * @param file
	 * @return
	 * @throws IOException
	 */
	private static double[][] getCSV(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		reader.readLine(); // ignore the titles
		
		List<double[]> re = new LinkedList<double[]>();
		String line;
		String[] tok;
		double[] ele;
		
		while((line=reader.readLine())!=null) {
			tok = line.split(",");
			ele = new double[tok.length];
			for (int i=0; i<ele.length; i++)
				ele[i] = Double.parseDouble(tok[i]);
			re.add(ele);
		}
		return re.toArray(new double[re.size()][]);
	}
	
	/**
	 * Calculate the averages of several runs for each timestep and each species
	 * @param data
	 * @return
	 */
	private static double[][] getAvg(double[][][] data) {
		int count = 51;
		
		double[][] re = new double[count][data[0][0].length];
		for (int r=0; r<data.length; r++)
			for (int t=0; t<count; t++)
				for (int s=0; s<data[0][0].length; s++)
					re[t][s]+=getData(data,r,t,s);
		
		for (int t=0; t<re.length; t++) {
			for (int s=0; s<re[t].length; s++)
				re[t][s]/=data.length;
			re[t][0] = t;
		}
		return re;
	}
	
	/**
	 * Calculate the squared stddevs of several runs for each timestep and each species
	 * @param data
	 * @param avg
	 * @return
	 */
	private static double[][] getStddevSq(double[][][] data, double[][] avg) {
		int count = 51;
		
		double[][] re = new double[count][data[0][0].length];
		for (int r=0; r<data.length; r++)
			for (int t=0; t<count; t++)
				for (int s=0; s<data[0][0].length; s++)
					re[t][s]+=(getData(data,r,t,s)-avg[t][s])*(getData(data,r,t,s)-avg[t][s]);
		
		for (int t=0; t<re.length; t++) {
			for (int s=0; s<re[t].length; s++)
				re[t][s]/=(data.length);
			re[t][0] = t;
		}
		return re;
	}
	
	private static double getData(double[][][] data, int r, int t, int s) {
		return t<data[r].length ? data[r][t][s] : data[r][data[r].length-1][s];
	}

	
	
		
}
