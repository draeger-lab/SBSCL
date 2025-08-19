package org.simulator.optsolvx;

import org.optsolvx.model.AbstractLPModel;
import org.sbml.jsbml.SBMLDocument;

/** Maps SBML/FBC to an OptSolvX LP model. */
public final class FbaToOptSolvX {

    private FbaToOptSolvX() {}

    /** Build an OptSolvX model from SBML/FBC. */
    public static AbstractLPModel fromSBML(SBMLDocument doc) {
        // TODO: extract reactions -> variables
        // TODO: bounds from FBC
        // TODO: S·v = 0 constraints
        // TODO: objective (coeffs + direction)
        // TODO: model.build()
        return null; // placeholder
    }
}
