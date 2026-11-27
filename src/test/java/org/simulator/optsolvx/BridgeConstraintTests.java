package org.simulator.optsolvx;

import org.junit.jupiter.api.Test;
import org.sbml.jsbml.*;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.model.Constraint;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Constraint-level tests for FbaToOptSolvX:
 * - Count of S·v=0 constraints
 * - Correct sign (+/-) and value of stoichiometric coefficients
 * - Ignoring boundary species
 * - No constraints when net stoichiometry is zero
 * Note: We keep objective empty in these tests; warnings in logs are expected.
 */
public class BridgeConstraintTests {

    private static final double EPS = 1e-9d;

    // ---- Tests ----

    /**
     * One species, one reaction, unit stoichiometry: expect 1 constraint with -1.0 for reactant.
     */
    @Test
    public void unitStoich_reactant_negativeSign_and_count() {
        AbstractLPModel lp = buildLP("A", false, mapOf(
                entry("R1", stoich(1.0, 0.0))));

        assertEquals(1, lp.getConstraints().size(),
                "exactly one mass-balance constraint expected");

        Constraint c = findMb(lp, "A");
        assertNotNull(c, "mb_A must exist");
        assertEquals(-1.0, c.getCoefficients().get("R1"), EPS);
    }

    /**
     * Reactant -3.5 and product +4.0: net coefficient must equal +0.5.
     */
    @Test
    public void nonUnitStoich_signs_and_values() {
        AbstractLPModel lp = buildLP("S", false, mapOf(
                entry("R1", stoich(3.5, 4.0))));

        Constraint c = findMb(lp, "S");
        assertNotNull(c);
        assertEquals(+0.5, c.getCoefficients().get("R1"), EPS);
    }

    /**
     * Species appears on both sides with equal amounts:
     * net zero means no constraint should be added.
     */
    @Test
    public void species_on_both_sides_netZero_should_not_create_constraint() {
        AbstractLPModel lp = buildLP("X", false, mapOf(
                entry("R_eq", stoich(1.0, 1.0))));

        assertNull(findMb(lp, "X"),
                "No mb_X expected if all coefficients are zero");

        assertEquals(0, lp.getConstraints().size());
    }

    /**
     * Two reactions: coefficients must accumulate and filter zeros only.
     */
    @Test
    public void summing_across_multiple_reactions_and_filter_zero_entries() {
        AbstractLPModel lp = buildLP("M", false, mapOf(
                entry("R1", stoich(1.0, 0.0)),
                entry("R2", stoich(0.0, 2.0))));

        Constraint c = findMb(lp, "M");
        assertNotNull(c);
        assertEquals(-1.0, c.getCoefficients().get("R1"), EPS);
        assertEquals(+2.0, c.getCoefficients().get("R2"), EPS);

        assertEquals(2, c.getCoefficients().size(),
                "only non-zero entries must be present");
    }

    /**
     * Boundary species must be ignored entirely.
     */
    @Test
    public void boundary_species_are_ignored() {
        AbstractLPModel lp = buildLP("BND", true, mapOf(
                entry("R1", stoich(1.0, 0.0))));

        assertEquals(0, lp.getConstraints().size());
        assertNull(findMb(lp, "BND"));
    }

    /**
     * Species without reaction participation must not produce a constraint.
     */
    @Test
    public void ignore_species_with_no_participation() {
        AbstractLPModel lp = buildLP("Z", false, mapOf(
                entry("R_unused", stoich(0.0, 0.0))));

        assertEquals(0, lp.getConstraints().size());
        assertNull(findMb(lp, "Z"));
    }

    /**
     * Multiple species with different reaction participation patterns:
     * one constraint per species with the expected number of entries.
     */
    @Test
    public void multiple_species_constraint_counts_and_per_species_entries() {
        // A participates in R1 and R2.
        Map<String, double[]> A = mapOf(
                entry("R1", stoich(1.0, 0.0)),
                entry("R2", stoich(0.0, 3.0)));

        // B participates only in R1.
        Map<String, double[]> B = mapOf(
                entry("R1", stoich(0.0, 2.0)));

        AbstractLPModel lp = buildLP_twoSpecies(
                "A", false, "B", false, A, B);

        assertEquals(2, lp.getConstraints().size(),
                "exactly two mass-balance constraints (one per species)");

        Constraint cA = findMb(lp, "A");
        Constraint cB = findMb(lp, "B");

        assertNotNull(cA);
        assertNotNull(cB);

        assertEquals(2, cA.getCoefficients().size());
        assertEquals(1, cB.getCoefficients().size());
    }

    // ---- Helpers & builders ----

    /**
     * Build an LP via FbaToOptSolvX for a single species across multiple reactions.
     */
    private static AbstractLPModel buildLP(
            String speciesId,
            boolean boundary,
            Map<String, double[]> reactions) {

        SBMLDocument doc = new SBMLDocument(3, 1);
        Model m = doc.createModel("m");

        // Compartment
        Compartment c = m.createCompartment("c");
        c.setConstant(true);
        c.setSize(1.0);
        c.setSpatialDimensions(3);

        // Species
        Species s = m.createSpecies(speciesId);
        s.setCompartment("c");
        s.setBoundaryCondition(boundary);
        s.setConstant(false);
        s.setHasOnlySubstanceUnits(true);
        s.setInitialAmount(0.0d);

        // Reactions: array[0] = reactant, array[1] = product stoichiometry.
        for (Map.Entry<String, double[]> e : reactions.entrySet()) {
            String rid = e.getKey();
            double[] st = e.getValue();

            Reaction r = m.createReaction(rid);
            r.setReversible(false);
            r.setFast(false);

            if (st[0] > 0.0) {
                SpeciesReference sr = r.createReactant();
                sr.setSpecies(speciesId);
                sr.setStoichiometry(st[0]);
                sr.setConstant(true);
            }

            if (st[1] > 0.0) {
                SpeciesReference sr = r.createProduct();
                sr.setSpecies(speciesId);
                sr.setStoichiometry(st[1]);
                sr.setConstant(true);
            }
        }

        // Ensure the FBC v2 namespace is resolvable.
        final String fbcNs = FBCConstants.getNamespaceURI(
                doc.getLevel(), doc.getVersion(), 2);

        assertNotNull(fbcNs, "FBC v2 namespace must be resolvable");

        return FbaToOptSolvX.fromSBML(doc);
    }

    /**
     * Build an LP for two species (A, B) sharing reactions.
     */
    private static AbstractLPModel buildLP_twoSpecies(
            String aId,
            boolean aBoundary,
            String bId,
            boolean bBoundary,
            Map<String, double[]> aStoich,
            Map<String, double[]> bStoich) {

        SBMLDocument doc = new SBMLDocument(3, 1);
        Model m = doc.createModel("m");

        Compartment c = m.createCompartment("c");
        c.setConstant(true);
        c.setSize(1.0);
        c.setSpatialDimensions(3);

        Species A = m.createSpecies(aId);
        A.setCompartment("c");
        A.setBoundaryCondition(aBoundary);
        A.setConstant(false);
        A.setHasOnlySubstanceUnits(true);
        A.setInitialAmount(0.0d);

        Species B = m.createSpecies(bId);
        B.setCompartment("c");
        B.setBoundaryCondition(bBoundary);
        B.setConstant(false);
        B.setHasOnlySubstanceUnits(true);
        B.setInitialAmount(0.0d);

        // Collect reaction IDs used by either species.
        Set<String> rids = new LinkedHashSet<>();
        rids.addAll(aStoich.keySet());
        rids.addAll(bStoich.keySet());

        for (String rid : rids) {
            Reaction r = m.createReaction(rid);
            r.setReversible(false);
            r.setFast(false);

            double[] a = aStoich.get(rid);

            if (a != null) {
                if (a[0] > 0.0) {
                    SpeciesReference sr = r.createReactant();
                    sr.setSpecies(aId);
                    sr.setStoichiometry(a[0]);
                    sr.setConstant(true);
                }

                if (a[1] > 0.0) {
                    SpeciesReference sr = r.createProduct();
                    sr.setSpecies(aId);
                    sr.setStoichiometry(a[1]);
                    sr.setConstant(true);
                }
            }

            double[] b = bStoich.get(rid);

            if (b != null) {
                if (b[0] > 0.0) {
                    SpeciesReference sr = r.createReactant();
                    sr.setSpecies(bId);
                    sr.setStoichiometry(b[0]);
                    sr.setConstant(true);
                }

                if (b[1] > 0.0) {
                    SpeciesReference sr = r.createProduct();
                    sr.setSpecies(bId);
                    sr.setStoichiometry(b[1]);
                    sr.setConstant(true);
                }
            }
        }

        return FbaToOptSolvX.fromSBML(doc);
    }

    /**
     * Find the mass-balance constraint for a species ("mb_<sid>").
     */
    private static Constraint findMb(AbstractLPModel lp, String sid) {
        String name = "mb_" + sid;

        for (Constraint c : lp.getConstraints()) {
            if (name.equals(c.getName())) return c;
        }

        return null;
    }

    // Helpers for constructing stoichiometric test data.
    private static Map<String, double[]> mapOf(
            Map.Entry<String, double[]> e1) {

        Map<String, double[]> m = new LinkedHashMap<>();
        m.put(e1.getKey(), e1.getValue());
        return m;
    }

    private static Map<String, double[]> mapOf(
            Map.Entry<String, double[]> e1,
            Map.Entry<String, double[]> e2) {

        Map<String, double[]> m = new LinkedHashMap<>();
        m.put(e1.getKey(), e1.getValue());
        m.put(e2.getKey(), e2.getValue());
        return m;
    }

    private static Map.Entry<String, double[]> entry(String k, double[] v) {
        return new AbstractMap.SimpleImmutableEntry<>(k, v);
    }

    private static double[] stoich(double reactant, double product) {
        return new double[]{reactant, product};
    }
}