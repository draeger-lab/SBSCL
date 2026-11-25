import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.io.File;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.math.ode.DerivativeException;
import org.junit.jupiter.api.Test;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.io.CSVImporter;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.AdamsMoultonSolver;
import org.simulator.math.odes.AdaptiveStepsizeIntegrator;
import org.simulator.math.odes.DormandPrince54Solver;
import org.simulator.math.odes.DormandPrince853Solver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.math.odes.LSODA.LSODAIntegrator;
import org.simulator.sbml.SBMLinterpreter;

/**
 * Simple test to check whether every solver can solve ode systems from SBML models.
 * This test is build on 00002 case of SBML Test Suite. 
 */
public class SBMLModelSolveTest {

    // values from settings.txt
	private static final double ABS_TOL = 1e-7;
	private static final double REL_TOL = 1e-4;
	private static final double CSV_ASSERT_TOL = 1e-6;  
    private static final double steps = 0.1;            // (duration - start)/steps

	private static void check(AbstractDESSolver solver)
			throws XMLStreamException, IOException, ModelOverdeterminedException, SBMLException,
			DerivativeException {
		String base = new File(System.getProperty("user.dir"), "src/test/resources").getAbsolutePath();
		String sbmlPath = base + "/sbml/00001/00001-sbml-l3v1.xml";
		String csvPath = base + "/sbml/00001/00001-results.csv";

		Model model = (new SBMLReader()).readSBML(sbmlPath).getModel();
		assertNotNull(model, "SBML model should be present");

		CSVImporter importer = new CSVImporter();
		MultiTable expected = importer.readMultiTableFromCSV(model, csvPath);
		assertNotNull(expected, "Reference CSV should be loaded");

		double[] timePoints = expected.getTimePoints();

		((AdaptiveStepsizeIntegrator) solver).setAbsTol(ABS_TOL);
		((AdaptiveStepsizeIntegrator) solver).setRelTol(REL_TOL);
		solver.setStepSize(steps); 

		SBMLinterpreter interpreter = new SBMLinterpreter(model);
		MultiTable actual = solver.solve(interpreter, interpreter.getInitialValues(), timePoints);

		assertNotNull(actual, solver.getName() + " returned null solution");
		assertFalse(solver.isUnstable(), solver.getName() + " reported unstable integration");
		assertEquals(expected.getRowCount(), actual.getRowCount(), solver.getName() + " row count");

		MultiTable.Block.Column expectedS1 = expected.getColumn("S1");
		MultiTable.Block.Column expectedS2 = expected.getColumn("S2");
		MultiTable.Block.Column actualS1 = actual.getColumn("S1");
		MultiTable.Block.Column actualS2 = actual.getColumn("S2");
		assertNotNull(expectedS1, "Reference CSV missing S1 column");
		assertNotNull(expectedS2, "Reference CSV missing S2 column");
		assertNotNull(actualS1, solver.getName() + " missing S1 column");
		assertNotNull(actualS2, solver.getName() + " missing S2 column");

		int rows = expected.getRowCount();
		for (int row = 0; row < rows; row++) {
			assertEquals(expected.getTimePoint(row), actual.getTimePoint(row), 1e-14,
					solver.getName() + " time at row " + row);
			assertEquals(expectedS1.getValue(row), actualS1.getValue(row), CSV_ASSERT_TOL,
					solver.getName() + " S1 at row " + row);
			assertEquals(expectedS2.getValue(row), actualS2.getValue(row), CSV_ASSERT_TOL,
					solver.getName() + " S2 at row " + row);
		}
	}

	@Test
	void lsoda() throws XMLStreamException, IOException, ModelOverdeterminedException,
			SBMLException, DerivativeException {
		check(new LSODAIntegrator());
	}

	@Test
	void rosenbrock() throws XMLStreamException, IOException, ModelOverdeterminedException,
			SBMLException, DerivativeException {
		check(new RosenbrockSolver());
	}

	@Test
	void dormandPrince54() throws XMLStreamException, IOException, ModelOverdeterminedException,
			SBMLException, DerivativeException {
		check(new DormandPrince54Solver());
	}

	@Test
	void dormandPrince853() throws XMLStreamException, IOException, ModelOverdeterminedException,
			SBMLException, DerivativeException {
		check(new DormandPrince853Solver());
	}

	@Test
	void adamsMoulton() throws XMLStreamException, IOException, ModelOverdeterminedException,
			SBMLException, DerivativeException {
		check(new AdamsMoultonSolver());
	}
}
