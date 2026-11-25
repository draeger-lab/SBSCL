package org.simulator.optsolvx;

import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.model.Constraint;
import org.optsolvx.model.OptimizationDirection;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.fbc.*;
import org.simulator.sbml.SBMLinterpreter;

import java.text.MessageFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Translates SBML/FBC models into solver-independent LP models.
 * Maps reactions to variables, builds mass-balance constraints,
 * and sets the active objective.
 * Uses SBMLinterpreter to evaluate initial assignments and assignment rules.
 *
 * StoichiometryMath is not supported because FBC requires SBML Level 3.
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

        // Evaluate SBML initial assignments and assignment rules.
        final SBMLinterpreter interpreter;

        try {
            interpreter = new SBMLinterpreter(m);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Could not initialize the SBML interpreter.", e);
        }

        // Prepare LP model
        final AbstractLPModel lp = new AbstractLPModel();

        // Cache SBML lists once to avoid repeated getter calls in hot paths
        final List<Reaction> reactions = m.getListOfReactions();   // cached reaction list
        final List<Species>  speciesList = m.getListOfSpecies();   // cached species list

        // Resolve FBC namespaces for level/version
        final int level = doc.getLevel();
        final int version = doc.getVersion();
        final String fbcNSv1 = FBCConstants.getNamespaceURI(level, version, 1);
        final String fbcNSv2 = FBCConstants.getNamespaceURI(level, version, 2);

        // --- 1) Variables (reactions) with bounds ---------------------------------------------
        // Default bounds if FBC has no values: 0 .. +inf (conventional for FBA)
        final Map<String, Double> lb = new LinkedHashMap<String, Double>(Math.max(16, reactions.size() * 2));
        final Map<String, Double> ub = new LinkedHashMap<String, Double>(Math.max(16, reactions.size() * 2));

        for (Reaction r : reactions) {
            String rid = r.getId();
            double lower = 0.0d;
            double upper = Double.POSITIVE_INFINITY;

            // FBC v2: bounds via FBCReactionPlugin lower/upper "Parameter" instances
            if (r.isSetPlugin(fbcNSv2)) {
                FBCReactionPlugin rp = (FBCReactionPlugin) r.getPlugin(fbcNSv2);
                Parameter lpi = rp.getLowerFluxBoundInstance();
                Parameter upi = rp.getUpperFluxBoundInstance();
                if (lpi != null) lower = valueOf(lpi, interpreter);
                if (upi != null) upper = valueOf(upi, interpreter);
            }
            // Store preliminary bounds; FBC v1 may override below via FluxBounds list
            lb.put(rid, lower);
            ub.put(rid, upper);
        }

        // FBC v1: combine explicit flux bounds.
        FBCModelPlugin mpV1 = (FBCModelPlugin) m.getPlugin(fbcNSv1);

        if (mpV1 != null && mpV1.isSetListOfFluxBounds()) {
            final Map<String, Double> explicitLb = new LinkedHashMap<>();
            final Map<String, Double> explicitUb = new LinkedHashMap<>();

            for (FluxBound fb : mpV1.getListOfFluxBounds()) {
                String rid = fb.getReaction();

                if (rid == null) continue;

                double value = fb.getValue();

                switch (fb.getOperation()) {
                    case GREATER_EQUAL:
                        explicitLb.merge(rid, value, Math::max);
                        break;

                    case LESS_EQUAL:
                        explicitUb.merge(rid, value, Math::min);
                        break;

                    case EQUAL:
                        explicitLb.merge(rid, value, Math::max);
                        explicitUb.merge(rid, value, Math::min);
                        break;

                    default:
                        LOGGER.warning(
                                "FBC v1: Unsupported FluxBound operation on " + rid);
                }
            }

            lb.putAll(explicitLb);
            ub.putAll(explicitUb);
        }

        // Add variables to LP.
        for (Reaction r : reactions) {
            String rid = r.getId();

            double lower = nvl(lb.get(rid), 0.0d);
            double upper = nvl(ub.get(rid), Double.POSITIVE_INFINITY);

            if (Double.isNaN(lower) || Double.isNaN(upper) || lower > upper) {
                throw new IllegalArgumentException(
                        MessageFormat.format(
                                "Invalid bounds for reaction {0}: [{1}, {2}]",
                                rid, lower, upper));
            }

            lp.addVariable(rid, lower, upper);
        }

        // --- 2) Mass-balance constraints S·v = 0 (ignore boundary species) ---------------------
        for (int si = 0; si < speciesList.size(); si++) {
            final Species s = speciesList.get(si);
            final String sid = s.getId(); // cache id once per species
            final boolean isBoundary = s.isSetBoundaryCondition() && s.getBoundaryCondition();
            if (isBoundary) continue;

            // Pre-size to reduce rehashing; order preserved for readability
            final Map<String, Double> coeffs = new LinkedHashMap<String, Double>(
                    Math.max(16, reactions.size()));

            // For each reaction, accumulate stoichiometry of species sid
            for (int ri = 0; ri < reactions.size(); ri++) {
                final Reaction r  = reactions.get(ri);
                final String rid  = r.getId(); // cache id once per reaction
                double sum = 0.0d;

                // Reactants contribute negative stoichiometry
                final List<SpeciesReference> reactants = r.getListOfReactants();
                for (int k = 0; k < reactants.size(); k++) {
                    final SpeciesReference sr = reactants.get(k);
                    if (sid.equals(sr.getSpecies())) {
                        sum -= stoich(sr, interpreter);
                    }
                }

                // Products contribute positive stoichiometry
                final List<SpeciesReference> products = r.getListOfProducts();
                for (int k = 0; k < products.size(); k++) {
                    final SpeciesReference sr = products.get(k);
                    if (sid.equals(sr.getSpecies())) {
                        sum += stoich(sr, interpreter);
                    }
                }

                if (sum != 0.0d) {
                    coeffs.put(rid, sum);
                }
            }

            if (!coeffs.isEmpty()) {
                lp.addConstraint("mb_" + sid, coeffs, Constraint.Relation.EQ, 0.0d);
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

                    if (rid != null) {
                        obj.merge(rid, fo.getCoefficient(), Double::sum);
                    }
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

    /**
     * Returns the evaluated parameter value.
     */
    private static double valueOf(
            Parameter p, SBMLinterpreter interpreter) {

        if (p == null) {
            return 0.0d;
        }

        double value = interpreter.getCurrentValueOf(p.getId());

        if (Double.isNaN(value)) {
            throw new IllegalArgumentException(
                    "Could not evaluate flux bound: " + p.getId());
        }

        return value;
    }

    /** Null-safe value with default. */
    private static double nvl(Double v, double def) {
        return (v != null) ? v.doubleValue() : def;
    }

    /**
     * Returns the evaluated stoichiometric coefficient.
     */
    private static double stoich(
            SpeciesReference sr, SBMLinterpreter interpreter) {

        if (sr == null) {
            return 0.0d;
        }

        double value;

        if (sr.isSetId()) {
            // Respect assignments targeting the species reference.
            value = interpreter.getCurrentStoichiometry(sr.getId());
        } else {
            value = sr.isSetStoichiometry()
                    ? sr.getStoichiometry()
                    : 1.0d;
        }

        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(
                    "Invalid stoichiometry for species: " + sr.getSpecies());
        }

        return value;
    }
}
