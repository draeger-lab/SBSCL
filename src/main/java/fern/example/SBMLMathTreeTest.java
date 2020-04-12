package fern.example;

import fern.network.FeatureNotSupportedException;
import fern.network.Network;
import fern.network.sbml.SBMLNetwork;
import fern.network.sbml.SBMLPropensityCalculator;
import fern.tools.NetworkTools;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

public class SBMLMathTreeTest {

	/**
	 * Dumb the MathTrees of an SBML network to Stdout
	 * 
	 * @param args
	 * @throws FeatureNotSupportedException 
	 */
	public static void main(String[] args) throws FeatureNotSupportedException, IOException, XMLStreamException {
		Network net = new SBMLNetwork(ExamplePath.find("mapk_sbml.xml"));
		for (int i=0; i<net.getNumReactions(); i++) {
			System.out.println("Reaction "+NetworkTools.getReactionNameWithAmounts(net, i));
			NetworkTools.dumpMathTree(((SBMLPropensityCalculator) net.getPropensityCalculator()).getMathTree(i));
			System.out.println();
		}
	}

}
