package fern.example;

import java.io.IOException;

import org.jdom.JDOMException;

import fern.benchmark.Benchmark;
import fern.benchmark.SimulatorPerformance;
import fern.benchmark.SimulatorRandomNumbers;
import fern.network.Network;
import fern.network.fernml.FernMLNetwork;

/**
 * Use the signal transduction pathway network of the epidermal growth factor proposed by [1] to
 * introduce the benchmark system. For more information please refer to {@link Benchmark}.
 * <p>
 * References: [1] Lee D.-Y., Metabolic Engineering 8, 112-122 (2006)
 *
 * @author Florian Erhard
 */
public class MapkBenchmark {

  public static void main(String[] args) throws IOException, JDOMException {
    Network net = new FernMLNetwork(ExamplePath.find("mapk.xml"));

    /*
     * Load the network into the benchmark system. Feel free to try other benchmarks!
     */
    SimulatorPerformance bench = new SimulatorRandomNumbers(net, 1000);
    bench.setShowSteps(10);

    /*
     * We only want GibsonBruck and the newest Tau leaping method.
     */
    bench.getSimulators()[0] = null;
    bench.getSimulators()[1] = null;
    bench.getSimulators()[3] = null;
    bench.getSimulators()[4] = null;

		while (true) {
			bench.benchmark();
		}


  }

}
