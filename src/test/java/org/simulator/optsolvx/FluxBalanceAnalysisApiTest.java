package org.simulator.optsolvx;

import org.junit.jupiter.api.Test;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.simulator.fba.FluxBalanceAnalysis;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class FluxBalanceAnalysisApiTest {

    @Test
    void getValue_rejectsUnknownReactionId() throws Exception {
        String sbml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <sbml xmlns="http://www.sbml.org/sbml/level3/version1/core"
                      xmlns:fbc="http://www.sbml.org/sbml/level3/version1/fbc/version2"
                      level="3" version="1" fbc:required="false">
                  <model id="testModel" fbc:strict="true">
                    <listOfCompartments>
                      <compartment id="c" size="1" constant="true"/>
                    </listOfCompartments>
                    <listOfSpecies>
                      <species id="S" compartment="c"
                               initialAmount="0"
                               boundaryCondition="false"
                               hasOnlySubstanceUnits="false"
                               constant="false"/>
                    </listOfSpecies>
                    <listOfParameters>
                      <parameter id="lb" value="0" constant="true"/>
                      <parameter id="ub" value="10" constant="true"/>
                    </listOfParameters>
                    <listOfReactions>
                      <reaction id="R_in" reversible="false" fast="false"
                                fbc:lowerFluxBound="lb"
                                fbc:upperFluxBound="ub">
                        <listOfProducts>
                          <speciesReference species="S"
                                            stoichiometry="1"
                                            constant="true"/>
                        </listOfProducts>
                      </reaction>
                      <reaction id="R_out" reversible="false" fast="false"
                                fbc:lowerFluxBound="lb"
                                fbc:upperFluxBound="ub">
                        <listOfReactants>
                          <speciesReference species="S"
                                            stoichiometry="1"
                                            constant="true"/>
                        </listOfReactants>
                      </reaction>
                      <reaction id="R_zero" reversible="false" fast="false"
                                fbc:lowerFluxBound="lb"
                                fbc:upperFluxBound="lb">
                        <listOfProducts>
                          <speciesReference species="S"
                                            stoichiometry="1"
                                            constant="true"/>
                        </listOfProducts>
                      </reaction>
                    </listOfReactions>
                    <fbc:listOfObjectives fbc:activeObjective="obj">
                      <fbc:objective fbc:id="obj" fbc:type="maximize">
                        <fbc:listOfFluxObjectives>
                          <fbc:fluxObjective fbc:reaction="R_out"
                                             fbc:coefficient="1"/>
                        </fbc:listOfFluxObjectives>
                      </fbc:objective>
                    </fbc:listOfObjectives>
                  </model>
                </sbml>
                """;

        SBMLDocument document = SBMLReader.read(
                new ByteArrayInputStream(
                        sbml.getBytes(StandardCharsets.UTF_8)));

        FluxBalanceAnalysis fba = new FluxBalanceAnalysis(document);

        assertTrue(fba.solve());

        // Valid reaction with a non-zero flux.
        assertEquals(10.0d, fba.getValue("R_out"), 1e-6);

        // Valid reaction with a zero flux.
        assertEquals(0.0d, fba.getValue("R_zero"), 1e-6);

        // Unknown reaction IDs must not return zero.
        assertThrows(NullPointerException.class,
                () -> fba.getValue("unknown_reaction"));
    }
}
