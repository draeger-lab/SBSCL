package org.simulator.optsolvx;

import org.junit.Test;
import static org.junit.Assert.*;

import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.solver.LPSolution;
import org.optsolvx.solver.CommonsMathSolver;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.fbc.*;

public class BridgeSmokeTest {

    @Test
    public void maps_fbc_v2_bounds_and_objective() {
        SBMLDocument doc = new SBMLDocument(3, 2);
        Model m = doc.createModel("toy_fbc2");
        Compartment c = m.createCompartment("c"); c.setConstant(true); c.setSize(1.0);

        Species x = m.createSpecies("X");
        x.setCompartment("c");
        x.setBoundaryCondition(false);

        Reaction rin = m.createReaction("R_in");
        rin.setReversible(false);
        rin.createProduct().setSpecies("X");

        Reaction rout = m.createReaction("R_out");
        rout.setReversible(false);
        rout.createReactant().setSpecies("X");

        // --- Attach FBC v2 plugin to model and reactions
        final String fbcNS = FBCConstants.getNamespaceURI(doc.getLevel(), doc.getVersion(), 2);
        FBCModelPlugin fbcModel = (FBCModelPlugin) m.getPlugin(fbcNS);
        if (fbcModel == null) {
            fbcModel = new FBCModelPlugin(m);
            m.addExtension(FBCConstants.shortLabel, fbcModel);
        }

        FBCReactionPlugin fbcIn = (FBCReactionPlugin) rin.getPlugin(fbcNS);
        if (fbcIn == null) {
            fbcIn = new FBCReactionPlugin(rin);
            rin.addExtension(FBCConstants.shortLabel, fbcIn);
        }
        FBCReactionPlugin fbcOut = (FBCReactionPlugin) rout.getPlugin(fbcNS);
        if (fbcOut == null) {
            fbcOut = new FBCReactionPlugin(rout);
            rout.addExtension(FBCConstants.shortLabel, fbcOut);
        }

        // Bounds via Parameters referenced by FBC (v2 style)
        Parameter lbIn = m.createParameter("LB_IN"); lbIn.setConstant(true); lbIn.setValue(0.0);
        Parameter ubIn = m.createParameter("UB_IN"); ubIn.setConstant(true); ubIn.setValue(10.0);
        Parameter lbOut= m.createParameter("LB_OUT");lbOut.setConstant(true);lbOut.setValue(0.0);
        Parameter ubOut= m.createParameter("UB_OUT");ubOut.setConstant(true);ubOut.setValue(10.0);

        fbcIn.setLowerFluxBound(lbIn.getId());
        fbcIn.setUpperFluxBound(ubIn.getId());
        fbcOut.setLowerFluxBound(lbOut.getId());
        fbcOut.setUpperFluxBound(ubOut.getId());

        // Objective: maximize v_out
        Objective obj = fbcModel.createObjective("obj");
        obj.setType(Objective.Type.MAXIMIZE);
        FluxObjective fo = obj.createFluxObjective();
        fo.setReaction(rout.getId());
        fo.setCoefficient(1.0);
        fbcModel.setActiveObjective(obj.getId());

        // --- Bridge & solve
        AbstractLPModel lp = FbaToOptSolvX.fromSBML(doc);
        LPSolution sol = new OptSolvXSolverAdapter(new CommonsMathSolver()).solve(lp);

        assertTrue(sol.isFeasible());
        assertEquals(10.0, sol.getObjectiveValue(), 1e-6); // max v_out = 10
        assertEquals(10.0, sol.getVariableValues().get("R_in"), 1e-6);
        assertEquals(10.0, sol.getVariableValues().get("R_out"), 1e-6);
    }
}
