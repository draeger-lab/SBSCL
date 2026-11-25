package org.simulator.fba;

import org.junit.jupiter.api.Test;
import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.model.Constraint;
import org.optsolvx.model.OptimizationDirection;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FluxBalanceAnalysisConvertToCPLEXTest {

    @Test
    public void testConvertToCPLEX() {
        AbstractLPModel lp = new AbstractLPModel();

        // Define variables and bounds.
        lp.addVariable("x", 0.0d, 10.0d);
        lp.addVariable("y", 0.0d, 10.0d);

        // Define objective.
        lp.setObjective(
                coefficients(1.0d, 2.0d),
                OptimizationDirection.MINIMIZE);

        // Add constraints.
        lp.addConstraint(
                "c1",
                coefficients(1.0d, 1.0d),
                Constraint.Relation.EQ,
                1.0d);

        lp.addConstraint(
                "c2",
                coefficients(1.0d, -1.0d),
                Constraint.Relation.GEQ,
                0.0d);

        lp.addConstraint(
                "c3",
                coefficients(-1.0d, 1.0d),
                Constraint.Relation.LEQ,
                1.0d);

        lp.build();

        String result = FluxBalanceAnalysis.CPLEXConverter
                .convertToCPLEX(lp).toString();

        // Verify objective and optimization direction.
        assertTrue(result.startsWith("Minimize\n"));
        assertTrue(result.contains("obj: 1.0 x0 + 2.0 x1"));

        // Verify constraints.
        assertTrue(result.contains("Subject To"));
        assertTrue(result.contains("c0: 1.0 x0 + 1.0 x1 = 1.0"));
        assertTrue(result.contains("c1: 1.0 x0 - 1.0 x1 >= 0.0"));
        assertTrue(result.contains("c2: - 1.0 x0 + 1.0 x1 <= 1.0"));

        // Verify bounds.
        assertTrue(result.contains("Bounds"));
        assertTrue(result.contains("0.0 <= x0 <= 10.0"));
        assertTrue(result.contains("0.0 <= x1 <= 10.0"));

        assertTrue(result.endsWith("End\n"));
    }

    @Test
    public void testMaximizationAndInfiniteBounds() {
        AbstractLPModel lp = new AbstractLPModel();

        lp.addVariable(
                "x",
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);

        lp.addVariable(
                "y",
                0.0d,
                Double.POSITIVE_INFINITY);

        lp.addVariable(
                "z",
                Double.NEGATIVE_INFINITY,
                10.0d);

        lp.setObjective(
                coefficients(1.0d, -2.0d),
                OptimizationDirection.MAXIMIZE);

        lp.build();

        String result = FluxBalanceAnalysis.CPLEXConverter
                .convertToCPLEX(lp).toString();

        assertTrue(result.startsWith("Maximize\n"));
        assertTrue(result.contains("obj: 1.0 x0 - 2.0 x1"));

        assertTrue(result.contains("x0 free"));
        assertTrue(result.contains("0.0 <= x1 <= +infinity"));
        assertTrue(result.contains("-infinity <= x2 <= 10.0"));
    }

    @Test
    public void testUnbuiltModelThrowsException() {
        AbstractLPModel lp = new AbstractLPModel();

        assertThrows(
                IllegalStateException.class,
                () -> FluxBalanceAnalysis.CPLEXConverter.convertToCPLEX(lp));
    }

    @Test
    public void testNullModelThrowsException() {
        assertThrows(
                IllegalArgumentException.class,
                () -> FluxBalanceAnalysis.CPLEXConverter.convertToCPLEX(null));
    }

    private static Map<String, Double> coefficients(double x, double y) {
        Map<String, Double> coefficients = new LinkedHashMap<>();
        coefficients.put("x", x);
        coefficients.put("y", y);
        return coefficients;
    }
}