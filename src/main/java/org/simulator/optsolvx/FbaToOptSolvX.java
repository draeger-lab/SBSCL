package org.simulator.optsolvx;

import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.model.Constraint;
import org.optsolvx.model.OptimizationDirection;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.fbc.*;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * SBML/FBC -> OptSolvX bridge (minimal functional version).
 * Maps reactions to variables (with bounds), builds S·v=0 constraints, and sets the active objective.
 * NOTE: This intentionally ignores InitialAssignments and StoichiometryMath for now (TODO).
 */
public final class FbaToOptSolvX {
    private static final Logger LOGGER = Logger.getLogger(FbaToOptSolvX.class.getName());

    private FbaToOptSolvX() { /* no instances */ }

    /** Build an OptSolvX LP model from SBML/FBC (v1 or v2). */
    public static AbstractLPModel fromSBML(SBMLDocument doc) {
        if (doc == null || !doc.isSetModel()) {
            throw new IllegalArgumentException("SBMLDocument must contain a Model.");
        }
        final Model m = doc.getModel();

        // Prepare LP model
        final AbstractLPModel lp = new AbstractLPModel();

        // Resolve FBC namespaces for level/version
        final int level = doc.getLevel();
        final int version = doc.getVersion();
        final String fbcNSv1 = FBCConstants.getNamespaceURI(level, version, 1);
        final String fbcNSv2 = FBCConstants.getNamespaceURI(level, version, 2);

        // --- 1) Variables (reactions) with bounds ---------------------------------------------
        // Default bounds if FBC has no values: 0 .. +inf (conventional for FBA)
        final Map<String, Double> lb = new LinkedHashMap<>();
        final Map<String, Double> ub = new LinkedHashMap<>();

        for (Reaction r : m.getListOfReactions()) {
            String rid = r.getId();
            double lower = 0.0d;
            double upper = Double.POSITIVE_INFINITY;

            // FBC v2: bounds via FBCReactionPlugin lower/upper "Parameter" instances
            if (r.isSetPlugin(fbcNSv2)) {
                FBCReactionPlugin rp = (FBCReactionPlugin) r.getPlugin(fbcNSv2);
                Parameter lpi = rp.getLowerFluxBoundInstance();
                Parameter upi = rp.getUpperFluxBoundInstance();
                if (lpi != null) lower = valueOf(lpi);
                if (upi != null) upper = valueOf(upi);
            }
            // Store preliminary bounds; FBC v1 may override below via FluxBounds list
            lb.put(rid, lower);
            ub.put(rid, upper);
        }

        // FBC v1: FluxBounds at model level (override)
        FBCModelPlugin mpV1 = (FBCModelPlugin) m.getPlugin(fbcNSv1);
        if (mpV1 != null && mpV1.isSetListOfFluxBounds()) {
            for (FluxBound fb : mpV1.getListOfFluxBounds()) {
                String rid = fb.getReaction();
                if (rid == null) continue;
                switch (fb.getOperation()) { // Java 8 compatible switch
                    case GREATER_EQUAL:
                        lb.put(rid, fb.getValue());
                        break;
                    case LESS_EQUAL:
                        ub.put(rid, fb.getValue());
                        break;
                    case EQUAL:
                        lb.put(rid, fb.getValue());
                        ub.put(rid, fb.getValue());
                        break;
                    default:
                        LOGGER.warning("FBC v1: Unsupported FluxBound operation on " + rid);
                }
            }
        }

        // Add variables to LP
        for (Reaction r : m.getListOfReactions()) {
            String rid = r.getId();
            double lower = nvl(lb.get(rid), 0.0d);
            double upper = nvl(ub.get(rid), Double.POSITIVE_INFINITY);
            lp.addVariable(rid, lower, upper);
        }

        // --- 2) Mass-balance constraints S·v = 0 (ignore boundary species) ---------------------
        for (Species s : m.getListOfSpecies()) {
            boolean isBoundary = s.isSetBoundaryCondition() && s.getBoundaryCondition();
            if (isBoundary) continue;

            Map<String, Double> coeffs = new LinkedHashMap<>();
            // For each reaction, accumulate stoichiometry of species s
            for (Reaction r : m.getListOfReactions()) {
                double sum = 0.0d;
                // Reactants contribute negative stoichiometry
                for (SpeciesReference sr : r.getListOfReactants()) {
                    if (s.getId().equals(sr.getSpecies())) {
                        sum += -stoich(sr);
                    }
                }
                // Products contribute positive stoichiometry
                for (SpeciesReference sr : r.getListOfProducts()) {
                    if (s.getId().equals(sr.getSpecies())) {
                        sum += +stoich(sr);
                    }
                }
                if (sum != 0.0d) {
                    coeffs.put(r.getId(), sum);
                }
            }
            if (!coeffs.isEmpty()) {
                lp.addConstraint("mb_" + s.getId(), coeffs, Constraint.Relation.EQ, 0.0d);
            } else {
                // Optional: many species simply don't appear; keep quiet to avoid noisy logs
            }
        }

        // --- 3) Objective (active FBC objective) -----------------------------------------------
        Map<String, Double> obj = new LinkedHashMap<>();
        OptimizationDirection dir = OptimizationDirection.MAXIMIZE; // sensible default

        // Prefer FBC v2 at model level; fall back to v1
        FBCModelPlugin mpV2 = (FBCModelPlugin) m.getPlugin(fbcNSv2);
        FBCModelPlugin mp = (mpV2 != null) ? mpV2 : mpV1;

        if (mp != null && mp.isSetActiveObjective()) {
            Objective o = mp.getActiveObjectiveInstance();
            if (o != null) {
                // Direction
                Objective.Type t = o.getType();
                if (t == Objective.Type.MINIMIZE) {
                    dir = OptimizationDirection.MINIMIZE;
                } else if (t == Objective.Type.MAXIMIZE) {
                    dir = OptimizationDirection.MAXIMIZE;
                }
                // Coefficients
                for (FluxObjective fo : o.getListOfFluxObjectives()) {
                    String rid = fo.getReaction();
                    if (rid != null) obj.put(rid, fo.getCoefficient());
                }
            } else {
                LOGGER.warning("No active FBC Objective instance set; using empty objective.");
            }
        } else {
            LOGGER.warning("FBC Objective not set; using empty objective (objective=0).");
        }

        if (!obj.isEmpty()) {
            lp.setObjective(obj, dir);
        } else {
            // Keep objective empty → objective value will be 0.0; direction still recorded if needed
            lp.setObjective(new LinkedHashMap<String, Double>(), dir);
        }

        // --- 4) Finalize & Log -----------------------------------------------------------------
        lp.build();
        LOGGER.info(MessageFormat.format(
                "FbaToOptSolvX: built LP (vars={0}, cons={1}, objectiveVars={2}, dir={3})",
                lp.getVariables().size(),
                lp.getConstraints().size(),
                obj.size(),
                dir
        ));
        return lp;
    }

    // --- Helpers -----------------------------------------------------------------------------

    /** Get numeric value from a Parameter (defaults to 0.0 if unset). */
    private static double valueOf(Parameter p) {
        if (p == null) return 0.0d;
        // Prefer explicit value; ignoring units & possible initial assignments for now (TODO)
        return p.isSetValue() ? p.getValue() : 0.0d;
    }

    /** Null-safe value with default. */
    private static double nvl(Double v, double def) {
        return (v != null) ? v.doubleValue() : def;
    }

    /** Basic stoichiometry extraction (ignores StoichiometryMath & InitialAssignments for now). */
    private static double stoich(SpeciesReference sr) {
        if (sr == null) return 0.0d;
        return sr.isSetStoichiometry() ? sr.getStoichiometry() : 1.0d;
    }
}
