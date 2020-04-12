import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.parsers.ParserConfigurationException;

import org.jdom.JDOMException;
import org.xml.sax.SAXException;

import fern.analysis.AutocatalyticNetworkDetection;
import fern.analysis.NodeChecker;
import fern.analysis.NodeCheckerByAnnotation;
import fern.analysis.ShortestPath;
import fern.network.FeatureNotSupportedException;
import fern.network.Network;
import fern.network.creation.AutocatalyticNetwork;
import fern.network.modification.ExtractSubNetwork;
import fern.network.modification.ReversibleNetwork;
//import fern.network.rnml.RNMLNetwork;
import fern.network.sbml.SBMLNetwork;
import fern.tools.Stochastics;
import fern.tools.NetworkTools;
import fern.tools.functions.Probability;

public class RNMLTest {

	/**
	 * @param args
	 * @throws IOException 
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws JDOMException 
	 * @throws FeatureNotSupportedException 
	 */
	public static void main(String[] args) throws ParserConfigurationException, SAXException, IOException, JDOMException, FeatureNotSupportedException {
//		Network net = new SBMLNetwork(new File("test/data/l2v1-mm.xml"));
//		NetworkDump.dump(net, new PrintWriter(System.out));
//		
//		RNMLNetwork net2 = new RNMLNetwork(net, null);
//		NetworkDump.dump(net2, new PrintWriter(System.out));
//		net2.saveToFile(new File("test/data/rnml/saved.xml"));
		
		Stochastics.getInstance().setSeed(1174567215984L);
		
		AutocatalyticNetwork net = new AutocatalyticNetwork(
				new char[] {'A','B'},
				new Probability.Constant(1),
				new Probability.Constant(1.0/(14.0)),
				3
				);
		
		Network netR = new ReversibleNetwork(net, net.getReversePropensityCalculator());
		System.out.println("OriginalNetwork:");
		NetworkTools.dumpNetwork(netR, new PrintWriter(System.out));
		System.out.println("................................................");
		
		AutocatalyticNetworkDetection detection = new AutocatalyticNetworkDetection(netR);
		detection.detect();
		detection.annotate("Autocatalytic", "yes");
		Network autoNet = new ExtractSubNetwork(netR,detection.getAutocatalyticReactions(), detection.getAutocatalyticSpecies());
		
		System.out.println();
		System.out.println("Autocatalytic Subnet");
		NetworkTools.dumpNetwork(autoNet, new PrintWriter(System.out));
		
		System.out.println("Seed: "+Stochastics.getInstance().getSeed());
		
//		NodeChecker checker = new NodeCheckerByAnnotation("Autocatalytic","yes");
//		
//		ShortestPath sp = new ShortestPath(netR);
//		for (ShortestPath.Path path : sp.computePaths(checker, "A","B"))
//			System.out.println(path.toString());
		
		
//		ShortestPath sp = new ShortestPath(netR);
//		int[] dist = sp.compute("A","B");
//		for (int i=0; i<net.getNumSpecies(); i++) {
//			System.out.println(net.getSpeciesName(i)+": "+dist[i]);
//		}
		
		
//		RNMLNetwork net2 = new RNMLNetwork(net);
//		NetworkDump.dump(net2, new PrintWriter(System.out));
//		net2.saveToFile(new File("test/data/rnml/saved.xml"));
//		NetworkDump.dump(net, new PrintWriter(System.out));
	}

}
