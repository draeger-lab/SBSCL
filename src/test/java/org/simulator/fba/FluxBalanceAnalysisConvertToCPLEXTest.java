package org.simulator.fba;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.problems.LinearProgram;

public class FluxBalanceAnalysisConvertToCPLEXTest {

    @Test
    public void testConvertToCPLEX() throws SBMLException, ModelOverdeterminedException {
        // Create a sample LinearProgram
        double[] c = {1.0, 2.0};
        LinearProgram lp = new LinearProgram(c);

        // Add constraints
        double[] constraint1 = {1.0, 1.0};
        lp.addConstraint(new LinearEqualsConstraint(constraint1, 1.0, "c1"));
        double[] constraint2 = {1.0, -1.0};
        lp.addConstraint(new LinearBiggerThanEqualsConstraint(constraint2, 0.0, "c2"));
        double[] constraint3 = {-1.0, 1.0};
        lp.addConstraint(new LinearSmallerThanEqualsConstraint(constraint3, 1.0, "c3"));

        // Set bounds
        lp.setLowerbound(new double[]{0.0, 0.0});
        lp.setUpperbound(new double[]{10.0, 10.0});

        // Convert to CPLEX format
        StringBuffer cplex = FluxBalanceAnalysis.CPLEXConverter.convertToCPLEX(lp);

        // Print the result
        System.out.println(cplex.toString());

        // Add assertions to verify the output
        assertNotNull(cplex);
        assertTrue(cplex.toString().contains("Minimize"));
        assertTrue(cplex.toString().contains("Subject To"));
        assertTrue(cplex.toString().contains("Bounds"));
        assertTrue(cplex.toString().contains("End"));
    }
}