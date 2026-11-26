package org.simulator.optsolvx;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.solver.LPSolution;
import org.optsolvx.solver.LPSolverAdapter;
import org.optsolvx.solver.OptSolvXConfig;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.fbc.*;

import static org.junit.jupiter.api.Assertions.*;

public class BridgeObjectiveTest {

    private static final double TOLERANCE = 1e-6d;

    @ParameterizedTest
    @CsvSource({
            "commons-math, CommonsMathSolver",
            "ojalgo, OjAlgoSolver"
    })
    public void maps_fbc_v2_bounds_and_objective(
            String solver, String expectedBackend) {

        SBMLDocument doc = new SBMLDocument(3, 2);
        Model m = doc.createModel("toy_fbc2");

        Compartment c = m.createCompartment("c");
        c.setConstant(true);
        c.setSize(1.0d);

        Species x = m.createSpecies("X");
        x.setCompartment("c");
        x.setBoundaryCondition(false);
        x.setConstant(false);
        x.setHasOnlySubstanceUnits(true);
        x.setInitialAmount(0.0d);

        Reaction rin = m.createReaction("R_in");
        rin.setReversible(false);

        SpeciesReference product = rin.createProduct();
        product.setSpecies("X");
        product.setStoichiometry(1.0d);
        product.setConstant(true);

        Reaction rout = m.createReaction("R_out");
        rout.setReversible(false);

        SpeciesReference reactant = rout.createReactant();
        reactant.setSpecies("X");
        reactant.setStoichiometry(1.0d);
        reactant.setConstant(true);

        // Attach FBC v2 plugins.
        final String fbcNS = FBCConstants.getNamespaceURI(
                doc.getLevel(), doc.getVersion(), 2);

        FBCModelPlugin fbcModel =
                (FBCModelPlugin) m.getPlugin(fbcNS);

        if (fbcModel == null) {
            fbcModel = new FBCModelPlugin(m);
            m.addExtension(FBCConstants.shortLabel, fbcModel);
        }

        FBCReactionPlugin fbcIn =
                (FBCReactionPlugin) rin.getPlugin(fbcNS);

        if (fbcIn == null) {
            fbcIn = new FBCReactionPlugin(rin);
            rin.addExtension(FBCConstants.shortLabel, fbcIn);
        }

        FBCReactionPlugin fbcOut =
                (FBCReactionPlugin) rout.getPlugin(fbcNS);

        if (fbcOut == null) {
            fbcOut = new FBCReactionPlugin(rout);
            rout.addExtension(FBCConstants.shortLabel, fbcOut);
        }

        // Define flux bounds.
        Parameter lbIn = m.createParameter("LB_IN");
        lbIn.setConstant(true);
        lbIn.setValue(0.0d);

        Parameter ubIn = m.createParameter("UB_IN");
        ubIn.setConstant(true);
        ubIn.setValue(10.0d);

        Parameter lbOut = m.createParameter("LB_OUT");
        lbOut.setConstant(true);
        lbOut.setValue(0.0d);

        Parameter ubOut = m.createParameter("UB_OUT");
        ubOut.setConstant(true);
        ubOut.setValue(10.0d);

        fbcIn.setLowerFluxBound(lbIn.getId());
        fbcIn.setUpperFluxBound(ubIn.getId());
        fbcOut.setLowerFluxBound(lbOut.getId());
        fbcOut.setUpperFluxBound(ubOut.getId());

        // Maximize the outgoing flux.
        Objective obj = fbcModel.createObjective("obj");
        obj.setType(Objective.Type.MAXIMIZE);

        FluxObjective fo = obj.createFluxObjective();
        fo.setReaction(rout.getId());
        fo.setCoefficient(1.0d);

        fbcModel.setActiveObjective(obj.getId());

        // Translate and solve the LP model.
        AbstractLPModel lp = FbaToOptSolvX.fromSBML(doc);
        LPSolverAdapter backend = OptSolvXConfig.resolve(lp, solver);

        assertNotNull(backend);
        assertEquals(expectedBackend, backend.getClass().getSimpleName());

        LPSolution sol = new OptSolvXSolverAdapter(backend).solve(lp);

        assertTrue(sol.isFeasible());
        assertEquals(10.0d, sol.getObjectiveValue(), TOLERANCE);
        assertEquals(10.0d, sol.getVariableValues().get("R_in"), TOLERANCE);
        assertEquals(10.0d, sol.getVariableValues().get("R_out"), TOLERANCE);
    }
}