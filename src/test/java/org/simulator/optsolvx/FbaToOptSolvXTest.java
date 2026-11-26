package org.simulator.optsolvx;

import org.junit.jupiter.api.Test;
import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.model.Variable;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import javax.xml.stream.XMLStreamException;

import static org.junit.jupiter.api.Assertions.*;

public class FbaToOptSolvXTest {

    private static final double TOLERANCE = 1e-6;

    @Test
    public void testMultipleLowerBounds() throws XMLStreamException {
        AbstractLPModel lp = createModel(
                bound("b1", "greaterEqual", 3.0d)
                        + bound("b2", "greaterEqual", 1.0d)
                        + bound("b3", "lessEqual", 10.0d),
                objective(1.0d));

        assertBounds(lp, 3.0d, 10.0d);
    }

    @Test
    public void testMultipleUpperBounds() throws XMLStreamException {
        AbstractLPModel lp = createModel(
                bound("b1", "greaterEqual", 0.0d)
                        + bound("b2", "lessEqual", 5.0d)
                        + bound("b3", "lessEqual", 10.0d),
                objective(1.0d));

        assertBounds(lp, 0.0d, 5.0d);
    }

    @Test
    public void testNegativeLowerBound() throws XMLStreamException {
        AbstractLPModel lp = createModel(
                bound("b1", "greaterEqual", -10.0d)
                        + bound("b2", "lessEqual", 10.0d),
                objective(1.0d));

        assertBounds(lp, -10.0d, 10.0d);
    }

    @Test
    public void testMissingBoundsForReversibleReaction()
            throws XMLStreamException {

        AbstractLPModel lp = createModel(
                "", objective(1.0d), true);

        assertBounds(lp,
                Double.NEGATIVE_INFINITY,
                Double.POSITIVE_INFINITY);
    }

    @Test
    public void testMissingBoundsForIrreversibleReaction()
            throws XMLStreamException {

        AbstractLPModel lp = createModel(
                "", objective(1.0d), false);

        assertBounds(lp, 0.0d, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testExplicitBoundsTakePrecedenceOverReversibility()
            throws XMLStreamException {

        AbstractLPModel lp = createModel(
                bound("b1", "greaterEqual", -10.0d)
                        + bound("b2", "lessEqual", 10.0d),
                objective(1.0d), false);

        assertBounds(lp, -10.0d, 10.0d);
    }

    @Test
    public void testEqualityBound() throws XMLStreamException {
        AbstractLPModel lp = createModel(
                bound("b1", "equal", 5.0d),
                objective(1.0d));

        assertBounds(lp, 5.0d, 5.0d);
    }

    @Test
    public void testConflictingBounds() {
        assertThrows(IllegalArgumentException.class, () ->
                createModel(
                        bound("b1", "greaterEqual", 10.0d)
                                + bound("b2", "lessEqual", 5.0d),
                        objective(1.0d)));
    }

    @Test
    public void testRepeatedObjectiveCoefficients()
            throws XMLStreamException {

        AbstractLPModel lp = createModel(
                bound("b1", "greaterEqual", 0.0d)
                        + bound("b2", "lessEqual", 10.0d),
                objective(2.0d) + objective(3.0d));

        assertEquals(
                5.0d,
                lp.getObjectiveCoefficients().get("R1"),
                TOLERANCE);
    }

    @Test
    public void testCancellingObjectiveCoefficients()
            throws XMLStreamException {

        AbstractLPModel lp = createModel(
                bound("b1", "greaterEqual", 0.0d)
                        + bound("b2", "lessEqual", 10.0d),
                objective(2.0d) + objective(-2.0d));

        assertEquals(
                0.0d,
                lp.getObjectiveCoefficients().getOrDefault("R1", 0.0d),
                TOLERANCE);
    }

    /**
     * Verifies the translated variable bounds.
     */
    private static void assertBounds(
            AbstractLPModel lp, double lower, double upper) {

        assertTrue(lp.isBuilt());

        Variable variable = lp.getVariable("R1");

        assertNotNull(variable);
        assertEquals(lower, variable.getLowerBound(), TOLERANCE);
        assertEquals(upper, variable.getUpperBound(), TOLERANCE);
    }

    /**
     * Creates an FBC v1 flux bound.
     */
    private static String bound(
            String id, String operation, double value) {

        return "<fbc:fluxBound"
                + " fbc:id=\"" + id + "\""
                + " fbc:reaction=\"R1\""
                + " fbc:operation=\"" + operation + "\""
                + " fbc:value=\"" + value + "\"/>";
    }

    /**
     * Creates an objective term for reaction R1.
     */
    private static String objective(double coefficient) {
        return "<fbc:fluxObjective"
                + " fbc:reaction=\"R1\""
                + " fbc:coefficient=\"" + coefficient + "\"/>";
    }

    /**
     * Creates and translates a minimal FBC v1 model.
     */
    private static AbstractLPModel createModel(
            String bounds, String objectives)
            throws XMLStreamException {

        return createModel(bounds, objectives, true);
    }

    /**
     * Creates and translates an FBC v1 model with configurable reversibility.
     */
    private static AbstractLPModel createModel(
            String bounds, String objectives, boolean reversible)
            throws XMLStreamException {

        String xml =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                        + "<sbml"
                        + " xmlns=\"http://www.sbml.org/sbml/level3/version1/core\""
                        + " xmlns:fbc=\"http://www.sbml.org/sbml/level3/version1/fbc/version1\""
                        + " level=\"3\" version=\"1\" fbc:required=\"false\">"

                        + "<model id=\"testModel\">"

                        + "<listOfCompartments>"
                        + "<compartment id=\"cell\" size=\"1\" constant=\"true\"/>"
                        + "</listOfCompartments>"

                        + "<listOfSpecies>"
                        + "<species id=\"A\" compartment=\"cell\""
                        + " initialAmount=\"0\" boundaryCondition=\"true\""
                        + " hasOnlySubstanceUnits=\"true\" constant=\"false\"/>"
                        + "<species id=\"B\" compartment=\"cell\""
                        + " initialAmount=\"0\" boundaryCondition=\"true\""
                        + " hasOnlySubstanceUnits=\"true\" constant=\"false\"/>"
                        + "</listOfSpecies>"

                        + "<listOfReactions>"
                        + "<reaction id=\"R1\" reversible=\""
                        + reversible + "\" fast=\"false\">"
                        + "<listOfReactants>"
                        + "<speciesReference species=\"A\""
                        + " stoichiometry=\"1\" constant=\"true\"/>"
                        + "</listOfReactants>"
                        + "<listOfProducts>"
                        + "<speciesReference species=\"B\""
                        + " stoichiometry=\"1\" constant=\"true\"/>"
                        + "</listOfProducts>"
                        + "</reaction>"
                        + "</listOfReactions>"

                        + (bounds.isEmpty() ? ""
                        : "<fbc:listOfFluxBounds>"
                        + bounds
                        + "</fbc:listOfFluxBounds>")

                        + "<fbc:listOfObjectives fbc:activeObjective=\"obj\">"
                        + "<fbc:objective fbc:id=\"obj\" fbc:type=\"maximize\">"
                        + "<fbc:listOfFluxObjectives>"
                        + objectives
                        + "</fbc:listOfFluxObjectives>"
                        + "</fbc:objective>"
                        + "</fbc:listOfObjectives>"

                        + "</model>"
                        + "</sbml>";

        SBMLDocument document = SBMLReader.read(xml);

        return FbaToOptSolvX.fromSBML(document);
    }
}