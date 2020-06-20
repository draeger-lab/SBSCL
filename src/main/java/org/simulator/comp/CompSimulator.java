package org.simulator.comp;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.comp.util.CompFlatteningConverter;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.sbml.AddMetaInfo;
import org.simulator.sbml.SBMLinterpreter;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;

/**
 * Class for simulating models encoded in the SBML comp package.
 * <p>
 * This class allows to simulate models encoded in the SBML hierarchical model
 * composition package comp (http://sbml.org/Documents/Specifications/SBML_Level_3/Packages/comp).
 * The models are simulated by applying model flattening, i.e., reducing the hierarchical
 * models to standard (flat) SBML models.
 *
 * This class currently is limited to SBML core models (ODE).
 * TODO: support additional simulations (FBA, stochastic)
 *
 * @author Shalin Shah, Matthias König
 * @version $Rev$
 * @since 1.5
 */
public class CompSimulator {

  private static Logger logger = Logger.getLogger(CompSimulator.class.getName());
  private SBMLDocument doc;
  private SBMLDocument docFlat;

  /**
   * Constructor for the CompSimulator class. It reads the {@link SBMLDocument}
   * and flattens the hierarchical SBML {@link Model} (consisting of multiple sub-models)
   * into the non-hierarchical version by using the CompFlatteningConverter
   * class of JSBML.
   *
   * @param file the input SBML file with comp extension that is to be simulated
   * @throws IOException
   * @throws XMLStreamException
   */
  public CompSimulator(File file) throws IOException, XMLStreamException {

    // Read original SBML file and add meta-info about original ID
    doc = SBMLReader.read(file);
    doc = AddMetaInfo.putOrigId(doc);

    // Flatten the model extra information in userObjects
    CompFlatteningConverter compFlatteningConverter = new CompFlatteningConverter();
    docFlat = compFlatteningConverter.flatten(doc);
  }

  public SBMLDocument getDoc() {
    return doc;
  }

  public SBMLDocument getDocFlat() {
    return docFlat;
  }

  /**
   * This method initializes the {@link RosenbrockSolver} and passes
   * it to solve the flattened model.
   *
   * @param timeEnd
   * @param stepSize
   * @return
   * @throws DerivativeException
   * @throws ModelOverdeterminedException
   */
  public MultiTable solve(double timeEnd, double stepSize)
      throws DerivativeException, ModelOverdeterminedException {
    /**
     * Initialization of the {@link RosenbrockSolver} used for simulating the
     * model.
     */
    DESSolver solver = new RosenbrockSolver();
    return solve(timeEnd, stepSize, solver);
  }

  /**
   * This method computes the numerical solution of the flattened
   * SBML {@link Model} simulated using the {@link RosenbrockSolver} and
   * then maps the solutions from the flattened model back to
   * the original model.
   *
   * @param timeEnd
   * @param stepSize
   * @param solver
   * @return
   * @throws DerivativeException
   * @throws ModelOverdeterminedException
   */
  public MultiTable solve(double timeEnd, double stepSize, DESSolver solver)
      throws DerivativeException, ModelOverdeterminedException {
    solver.setStepSize(stepSize);

    Model model = docFlat.getModel();
    SBMLinterpreter interpreter = new SBMLinterpreter(model);
    if (solver instanceof AbstractDESSolver) {
      ((AbstractDESSolver) solver).setIncludeIntermediates(false);
    }

    // Compute the numerical solution of the initial value problem
    // TODO: Rel-Tolerance, Abs-Tolerance.
    MultiTable solution = solver.solve(interpreter, interpreter.getInitialValues(), 0d, timeEnd);

    // If columns other than time exists map ids back to original
    if (solution.getColumnCount() > 1) {
      logger.warn("Output contains objects, trying to plot and extract ids:\n");

      // Map the output ids back to the original model
      for (int index = 1; index < solution.getColumnCount(); index++) {
        AbstractTreeNode node = (AbstractTreeNode) doc.getElementBySId(solution.getColumnIdentifier(index));
        if (node.isSetUserObjects()) {
          logger.info("flat id: " + solution.getColumnIdentifier(index) + "\t old id:" + node.getUserObject(AddMetaInfo.ORIG_ID) + "\t model enclosing it: " + node.getUserObject(AddMetaInfo.MODEL_ID));
        }
      }
    }
    return solution;
  }
}
