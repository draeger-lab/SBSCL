import java.io.File;
import java.io.IOException;

import org.jdom.JDOMException;

import fern.benchmark.SimulatorCorrectness;
import fern.benchmark.SimulatorFireTypes;
import fern.benchmark.SimulatorPerformance;
import fern.benchmark.SimulatorRandomNumbers;
import fern.benchmark.SimulatorTime;
import fern.network.Network;
import fern.network.rnml.RNMLNetwork;
import fern.tools.ConfigReader;

public class BenchmarkTest {

	/**
	 * @param args
	 * @throws JDOMException 
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException, JDOMException {
		Network net = new RNMLNetwork(new File("test/data/rnml/s3.xml"));
		
		
		SimulatorPerformance bench = new SimulatorCorrectness(net,0.1,"S3");
		
		while(true) {
			bench.benchmark();
		}	
	}

}
