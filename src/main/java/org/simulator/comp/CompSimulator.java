package org.simulator.comp;

import org.apache.commons.math.ode.DerivativeException;
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
 * Class for simulating comp models.
 *
 * TODO: support different simulation types (FBA, stochastic), currently limited to ode
 *
 *
 * @author Shalin Shah, atthias König
 * @version $Rev$
 * @since 1.5
 */
public class CompSimulator {
    private static Logger logger = Logger.getLogger(CompSimulator.class.getName());
    private SBMLDocument doc;
    private SBMLDocument docFlat;

    public CompSimulator(File file) throws IOException, XMLStreamException {

        // Read original SBML file and add meta-info about original ID
        doc = SBMLReader.read(file);
        doc = AddMetaInfo.putOrigId(doc);

        // Flatten the model extra information in userObjects
        CompFlatteningConverter compFlatteningConverter = new CompFlatteningConverter();
        docFlat = compFlatteningConverter.flatten(doc);
        assert docFlat != null;
    }


    public SBMLDocument getDoc() {
        return doc;
    }

    public SBMLDocument getDocFlat() {
        return docFlat;
    }

    public MultiTable solve(double timeEnd, double stepSize) throws DerivativeException, ModelOverdeterminedException {
        DESSolver solver = new RosenbrockSolver();
        return solve(timeEnd, stepSize, solver);
    }

    public MultiTable solve(double timeEnd, double stepSize, DESSolver solver) throws DerivativeException, ModelOverdeterminedException {
        solver.setStepSize(stepSize);

        // Execute the model using solver
        Model model = docFlat.getModel();

        SBMLinterpreter interpreter = new SBMLinterpreter(model);
        if (solver instanceof AbstractDESSolver) {
            ((AbstractDESSolver) solver).setIncludeIntermediates(false);
        }

        // Compute the numerical solution of the initial value problem
        // TODO: Rel-Tolerance, Abs-Tolerance.
        MultiTable solution = solver.solve(interpreter, interpreter.getInitialValues(), 0d, timeEnd);

        // If columns other than time exists map ids back to original
        if(solution.getColumnCount() > 1) {
            logger.warn("Output contains objects, trying to plot and extract ids:\n");
            // Map the output ids back to the original model
            for (int index = 1; index < solution.getColumnCount(); index++) {
                AbstractTreeNode node = (AbstractTreeNode) doc.getElementBySId(solution.getColumnIdentifier(index));

                if(node.isSetUserObjects()) {
                    System.out.println("flat id: " + solution.getColumnIdentifier(index) + "\t old id:" +
                            node.getUserObject(AddMetaInfo.ORIG_ID) + "\t model enclosing it: " + node.getUserObject(AddMetaInfo.MODEL_ID));
                }
            }
        }

        return solution;
    }

}
