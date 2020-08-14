package org.simulator.examples;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.jfree.ui.RefineryUtilities;
import org.jlibsedml.AbstractTask;
import org.jlibsedml.Curve;
import org.jlibsedml.Libsedml;
import org.jlibsedml.Output;
import org.jlibsedml.Plot2D;
import org.jlibsedml.SedML;
import org.jlibsedml.XMLException;
import org.jlibsedml.execution.IProcessedSedMLSimulationResults;
import org.jlibsedml.execution.IRawSedmlSimulationResults;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.simulator.plot.PlotProcessedSedmlResults;
import org.simulator.sedml.SedMLSBMLSimulatorExecutor;

import de.binfalse.bflog.LOGGER;

/**
 * This test class shows how a SED-ML file can be interpreted and executed using SBML Simulator Core
 * solvers. <br> It makes extensive use of jlibsedml's Execution framework which performs
 * boiler-plate code for operations such as post-processing of results, etc., This is main test file
 * L1V2 SED-ML elements such as repeatedTasks and FunctionalRange
 *
 * @author Shalin Shah
 * @since 1.5
 */
public class SEDMLExample {

  private static SedML sedml = null;

  public static void main(String[] args) throws XMLException, OWLOntologyCreationException {
    if (args[0] == null) {
      LOGGER.warn("Please give file file name as argument.");
      return;
    }
    File file = new File(args[0]);
    String sedmlDir = file.getAbsoluteFile().getParentFile().getAbsolutePath();

    LOGGER.warn("Opening file: " + file + " Collecting tasks...");
    sedml = Libsedml.readDocument(file).getSedMLModel();

    // in this SED-ML file there's just one output. If there were several,
    // we could either iterate or get user to  decide what they want to run.
    Output wanted = sedml.getOutputs().get(2);
    SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sedml, wanted, sedmlDir);
    // This gets the raw simulation results - one for each Task that was run.
    LOGGER.warn("Collecting tasks...");
    Map<AbstractTask, List<IRawSedmlSimulationResults>> res = exe.run();
    if (res == null || res.isEmpty() || !exe.isExecuted()) {
      fail("Simulatation failed: " + exe.getFailureMessages().get(0));
      return;
    }

    // now process.In this case, there's no processing performed - we're displaying the
    // raw results.
    LOGGER.warn("Outputs wanted: " + wanted.getId());
    IProcessedSedMLSimulationResults prRes = exe.processSimulationResults(wanted, res);

    if (wanted.isPlot2d()) {
      Plot2D plots = (Plot2D) wanted;
      List<Curve> curves = plots.getListOfCurves();

      // plot all processed results as per curve descriptions
      PlotProcessedSedmlResults p = new PlotProcessedSedmlResults(prRes, curves,
          plots.getElementName());
      p.pack();
      RefineryUtilities.centerFrameOnScreen(p);
      p.setVisible(true);
    }
  }

}
