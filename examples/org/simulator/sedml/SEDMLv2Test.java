package org.simulator.sedml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Map;

import org.jfree.ui.RefineryUtilities;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.SedML;
import org.jlibsedml.XMLException;
import org.jlibsedml.execution.IRawSedmlSimulationResults;
import org.simulator.math.odes.MultiTable;
import org.simulator.plot.PlotMultiTable;

/**
 * This test class shows how a SED-ML file can be interpreted and executed using
 *  SBML Simulator Core solvers. <br/> It makes extensive use of jlibsedml's 
 *  Execution framework which performs boiler-plate code for operations such as 
 *  post-processing of results, etc., This is main test file L1V2 SED-ML elements
 *  such as repeatedTasks and FunctionalRange
 * 
 * @author Shalin Shah
 * @since 1.5
 */
public class SEDMLv2Test {
	private static File l1v1Test = new File("files/sedmlTest/ClockSedML.xml");
	//private static File l1v2Test = new File("files/sedmlTest/l1v2/v3-example1-repeated-steady-scan-oscli.xml");
	//private static File l1v2Test = new File("files/sedmlTest/l1v2/v3-example2-oscli-nested-pulse.xml");
	//private static File l1v2Test = new File("files/sedmlTest/l1v2/v3-example3-repeated-stochastic-runs.xml");
	//private static File l1v2Test = new File("files/sedmlTest/l1v2/v3-example4-repeated-scan-oscli.xml");
	private static File l1v2Test = new File("files/sedmlTest/l1v2/v3-example5-boris-2d-scan.xml");
	private static SedML sedml = null;

	public static void main(String[] args) throws XMLException {
		sedml=Libsedml.readDocument(l1v2Test).getSedMLModel();
	    // in this SED-ML file there's just one output. If there were several,
	    // we could either iterate or get user to  decide what they want to run.
	    Output wanted = sedml.getOutputs().get(0);
	    SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sedml, wanted);
	    // This gets the raw simulation results - one for each Task that was run.
	    Map<AbstractTask, IRawSedmlSimulationResults> res = exe.runSimulations();
	    if (res==null ||res.isEmpty() || !exe.isExecuted()) {
	      fail ("Simulatation failed: " + exe.getFailureMessages().get(0));
	    }
	    // now process.In this case, there's no processing performed - we're displaying the
	    // raw results.
	    MultiTable mt = exe.processSimulationResults(wanted, res);
   
	    // plot all the reactions species
	    PlotMultiTable p = new PlotMultiTable(mt);
	    p.pack();
	    RefineryUtilities.centerFrameOnScreen(p);
	    p.setVisible( true );
	}

}
