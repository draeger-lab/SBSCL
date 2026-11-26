package org.simulator.optsolvx;

import org.junit.jupiter.api.Test;
import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.model.Variable;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

import javax.xml.stream.XMLStreamException;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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

    @Test
    public void testFbcV2UnknownLowerBoundParameter() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createFbcV2Model("UNKNOWN_LB", "UB"));

        assertEquals(
                "Unresolved lower flux bound parameter UNKNOWN_LB for reaction R1.",
                exception.getMessage());
    }

    @Test
    public void testFbcV2UnknownUpperBoundParameter() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createFbcV2Model("LB", "UNKNOWN_UB"));

        assertEquals(
                "Unresolved upper flux bound parameter UNKNOWN_UB for reaction R1.",
                exception.getMessage());
    }

    @Test
    public void testFbcV2ValidBoundParameters()
            throws XMLStreamException {

        AbstractLPModel lp = createFbcV2Model("LB", "UB");

        assertBounds(lp, -10.0d, 10.0d);
    }

    @Test
    public void testFbcV2StrictMissingLowerBound() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createFbcV2Model(null, "UB", true));

        assertEquals(
                "Missing lower flux bound for reaction R1 in strict FBC v2 model.",
                exception.getMessage());
    }

    @Test
    public void testFbcV2StrictMissingUpperBound() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createFbcV2Model("LB", null, true));

        assertEquals(
                "Missing upper flux bound for reaction R1 in strict FBC v2 model.",
                exception.getMessage());
    }

    @Test
    public void testFbcV2StrictMissingBothBounds() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createFbcV2Model(null, null, true));

        assertEquals(
                "Missing lower flux bound for reaction R1 in strict FBC v2 model.",
                exception.getMessage());
    }

    @Test
    public void testFbcV2StrictWithBothBounds()
            throws XMLStreamException {

        AbstractLPModel lp = createFbcV2Model("LB", "UB", true);

        assertBounds(lp, -10.0d, 10.0d);
    }

    @Test
    public void testFbcV2NonStrictMissingLowerBound()
            throws XMLStreamException {

        AbstractLPModel lp = createFbcV2Model(null, "UB", false);

        assertBounds(lp, 0.0d, 10.0d);
    }

    @Test
    public void testFbcV2NonStrictMissingUpperBound()
            throws XMLStreamException {

        AbstractLPModel lp = createFbcV2Model("LB", null, false);

        assertBounds(lp, -10.0d, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testPositiveInfinityAsLowerBound() {
        assertThrows(IllegalArgumentException.class, () ->
                createModel(
                        bound("b1", "greaterEqual", Double.POSITIVE_INFINITY)
                                + bound("b2", "lessEqual", Double.POSITIVE_INFINITY),
                        objective(1.0d)));
    }

    @Test
    public void testNegativeInfinityAsUpperBound() {
        assertThrows(IllegalArgumentException.class, () ->
                createModel(
                        bound("b1", "greaterEqual", Double.NEGATIVE_INFINITY)
                                + bound("b2", "lessEqual", Double.NEGATIVE_INFINITY),
                        objective(1.0d)));
    }

    @Test
    public void testValidNegativeInfinityLowerBound()
            throws XMLStreamException {

        AbstractLPModel lp = createModel(
                bound("b1", "greaterEqual", Double.NEGATIVE_INFINITY)
                        + bound("b2", "lessEqual", 10.0d),
                objective(1.0d));

        assertBounds(lp, Double.NEGATIVE_INFINITY, 10.0d);
    }

    @Test
    public void testValidPositiveInfinityUpperBound()
            throws XMLStreamException {

        AbstractLPModel lp = createModel(
                bound("b1", "greaterEqual", 0.0d)
                        + bound("b2", "lessEqual", Double.POSITIVE_INFINITY),
                objective(1.0d));

        assertBounds(lp, 0.0d, Double.POSITIVE_INFINITY);
    }

    @Test
    public void testUnknownReactionInFluxBound() {
        String invalidBound = bound("b1", "greaterEqual", 0.0d)
                .replace("fbc:reaction=\"R1\"",
                        "fbc:reaction=\"UNKNOWN_REACTION\"");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createModel(invalidBound, objective(1.0d)));

        assertEquals(
                "Unknown reaction reference in flux bound: UNKNOWN_REACTION",
                exception.getMessage());
    }

    @Test
    public void testUnknownReactionInObjective() {
        String invalidObjective = objective(1.0d)
                .replace("fbc:reaction=\"R1\"",
                        "fbc:reaction=\"UNKNOWN_REACTION\"");

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createModel(
                        bound("b1", "greaterEqual", 0.0d)
                                + bound("b2", "lessEqual", 10.0d),
                        invalidObjective));

        assertEquals(
                "Unknown reaction reference in objective: UNKNOWN_REACTION",
                exception.getMessage());
    }

    @ParameterizedTest
    @ValueSource(doubles = {
            Double.NaN,
            Double.POSITIVE_INFINITY,
            Double.NEGATIVE_INFINITY
    })
    public void testNonFiniteObjectiveCoefficient(double coefficient) {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createModel(
                        bound("b1", "greaterEqual", 0.0d)
                                + bound("b2", "lessEqual", 10.0d),
                        objective(coefficient)));

        assertEquals(
                "Non-finite objective coefficient for reaction R1.",
                exception.getMessage());
    }

    @Test
    public void testObjectiveCoefficientOverflow() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> createModel(
                        bound("b1", "greaterEqual", 0.0d)
                                + bound("b2", "lessEqual", 10.0d),
                        objective(Double.MAX_VALUE)
                                + objective(Double.MAX_VALUE)));

        assertEquals(
                "Objective coefficient overflow for reaction R1.",
                exception.getMessage());
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

        String xmlValue = value == Double.POSITIVE_INFINITY
                ? "INF"
                : value == Double.NEGATIVE_INFINITY
                ? "-INF"
                : Double.toString(value);

        return "<fbc:fluxBound"
                + " fbc:id=\"" + id + "\""
                + " fbc:reaction=\"R1\""
                + " fbc:operation=\"" + operation + "\""
                + " fbc:value=\"" + xmlValue + "\"/>";
    }

    /**
     * Creates an objective term for reaction R1.
     */
    private static String objective(double coefficient) {
        String xmlValue = coefficient == Double.POSITIVE_INFINITY
                ? "INF"
                : coefficient == Double.NEGATIVE_INFINITY
                ? "-INF"
                : Double.toString(coefficient);

        return "<fbc:fluxObjective"
                + " fbc:reaction=\"R1\""
                + " fbc:coefficient=\"" + xmlValue + "\"/>";
    }

    /**
     * Creates and translates an FBC v1 model with a reversible reaction.
     */
    private static AbstractLPModel createModel(
            String bounds, String objectives)
            throws XMLStreamException {

        return createModel(bounds, objectives, true);
    }

    /**
     * Creates and translates a minimal non-strict FBC v2 model.
     */
    private static AbstractLPModel createFbcV2Model(
            String lowerBoundRef, String upperBoundRef)
            throws XMLStreamException {

        return createFbcV2Model(lowerBoundRef, upperBoundRef, false);
    }

    /**
     * Creates and translates an FBC v2 model with configurable strictness.
     */
    private static AbstractLPModel createFbcV2Model(
            String lowerBoundRef, String upperBoundRef, boolean strict)
            throws XMLStreamException {

        String lowerAttribute = lowerBoundRef == null
                ? ""
                : "fbc:lowerFluxBound=\"" + lowerBoundRef + "\"";

        String upperAttribute = upperBoundRef == null
                ? ""
                : "fbc:upperFluxBound=\"" + upperBoundRef + "\"";

        String xml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <sbml xmlns="http://www.sbml.org/sbml/level3/version1/core"
                  xmlns:fbc="http://www.sbml.org/sbml/level3/version1/fbc/version2"
                  level="3" version="1" fbc:required="false">

                <model id="testModel" fbc:strict="%s">

                    <listOfCompartments>
                        <compartment id="cell" size="1" constant="true"/>
                    </listOfCompartments>

                    <listOfSpecies>
                        <species id="A" compartment="cell"
                                 initialAmount="0" boundaryCondition="true"
                                 hasOnlySubstanceUnits="true" constant="false"/>
                        <species id="B" compartment="cell"
                                 initialAmount="0" boundaryCondition="true"
                                 hasOnlySubstanceUnits="true" constant="false"/>
                    </listOfSpecies>

                    <listOfParameters>
                        <parameter id="LB" value="-10" constant="true"/>
                        <parameter id="UB" value="10" constant="true"/>
                    </listOfParameters>

                    <listOfReactions>
                        <reaction id="R1" reversible="true" fast="false"
                                      %s
                                      %s>

                            <listOfReactants>
                                <speciesReference species="A"
                                                  stoichiometry="1"
                                                  constant="true"/>
                            </listOfReactants>

                            <listOfProducts>
                                <speciesReference species="B"
                                                  stoichiometry="1"
                                                  constant="true"/>
                            </listOfProducts>

                        </reaction>
                    </listOfReactions>

                    <fbc:listOfObjectives fbc:activeObjective="obj">
                        <fbc:objective fbc:id="obj" fbc:type="maximize">
                            <fbc:listOfFluxObjectives>
                                <fbc:fluxObjective fbc:reaction="R1"
                                                   fbc:coefficient="1"/>
                            </fbc:listOfFluxObjectives>
                        </fbc:objective>
                    </fbc:listOfObjectives>

                </model>
            </sbml>
            """.formatted(strict, lowerAttribute, upperAttribute);

        SBMLDocument document = SBMLReader.read(xml);

        return FbaToOptSolvX.fromSBML(document);
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