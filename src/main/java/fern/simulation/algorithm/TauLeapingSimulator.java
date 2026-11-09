package fern.simulation.algorithm;

import fern.network.Network;
import fern.simulation.Simulator;
import fern.simulation.controller.SimulationController;

/**
 * An implementation of the explicit Tau-Leaping algorithm (Gillespie 2001).
 * This algorithm accelerates stochastic simulations by taking larger time steps (tau)
 * and approximating the number of reaction firings using Poisson distributions.
 */
public class TauLeapingSimulator extends Simulator {

    private double baseTau = 0.01; // Default maximum step size
    private double currentTau = baseTau;

    public TauLeapingSimulator(Network net) {
        super(net);
    }

    @Override
    public void performStep(SimulationController controller) {
        // 1. Bound leap by next scheduled theta event to ensure callbacks are processed
        double nextTheta = getNextThetaEvent();
        double actualTau = currentTau;
        if (t + actualTau >= nextTheta) {
            actualTau = nextTheta - t;
        }

        // Ensure propensities are up to date (especially after theta events modify the network)
        initializePropensities();

        // 2 & 3. Calculate expected firings and draw from seeded Poisson distribution
        long[] firings = new long[getNet().getNumReactions()];
        boolean hasFirings = false;
        for (int i = 0; i < getNet().getNumReactions(); i++) {
            double expectedFirings = a[i] * actualTau;
            if (expectedFirings > 0) {
                // Use FERN's seeded and numerically stable Poisson generator
                firings[i] = stochastics.getPoisson(expectedFirings);
                if (firings[i] > 0) {
                    hasFirings = true;
                }
            }
        }

        // 4. Save state to rollback in case of negative molecule counts (reactant depletion)
        long[] backupAmounts = new long[getNet().getNumSpecies()];
        for (int s = 0; s < backupAmounts.length; s++) {
            backupAmounts[s] = getAmountManager().getAmount(s);
        }

        // 5. Apply all firings to the network's molecule counts
        boolean success = true;
        if (hasFirings) {
            try {
                for (int i = 0; i < getNet().getNumReactions(); i++) {
                    long remaining = firings[i];
                    while (remaining > 0) {
                        // Split counts into int-sized batches to safely route through Simulator
                        int batch = (int) Math.min(remaining, Integer.MAX_VALUE);
                        // Use Simulator's batched fireReaction to preserve observer patterns
                        fireReaction(i, t, t + actualTau, batch, FireType.TauLeapNonCritical);
                        remaining -= batch;
                    }
                }
            } catch (RuntimeException e) {
                // DefaultAmountManager throws RuntimeException on negative populations
                success = false;
            }
        }

        if (!success) {
            // Rollback rejected leap to prevent partially applied, negative amounts
            for (int s = 0; s < backupAmounts.length; s++) {
                getAmountManager().setAmount(s, backupAmounts[s]);
            }
            // Retry this step with a dynamically smaller tau
            currentTau /= 2.0;
            return; // Skip time advancement; controller will re-invoke performStep
        }

        // 6. Leap accepted: advance simulation time
        t += actualTau;

        // Recover tau slightly if it was previously reduced for an overfiring event
        if (currentTau < baseTau) {
            currentTau = Math.min(baseTau, currentTau * 1.2);
        }

        // 7. Process scheduled observer callbacks if we reached a theta event
        if (t >= nextTheta) {
            thetaEvent();
        }
    }

    @Override
    public void reinitialize() {
        currentTau = baseTau;
        initializePropensities();
    }

    @Override
    public String getName() {
        return "Explicit Tau-Leaping";
    }
}