/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */

import java.io.File;
import java.io.IOException;

import fern.network.AnnotationManager;
import fern.network.FeatureNotSupportedException;
import fern.network.sbml.SBMLNetwork;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

import javax.xml.stream.XMLStreamException;


public class SBMLNetworkTest {

	/**
	 * @param args
	 * @throws FeatureNotSupportedException 
	 */
	public static void main(String[] args) throws FeatureNotSupportedException, IOException, XMLStreamException, ModelOverdeterminedException {
		SBMLNetwork net = new SBMLNetwork(new File("test/data/l1v1-minimal.xml"));
		
		for (int r = 0; r<net.getNumSpecies(); r++) {
			System.out.println(net.getInitialAmount(r));
		}
	}

}
