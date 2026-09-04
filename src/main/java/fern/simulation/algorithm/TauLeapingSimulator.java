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

    private double tau = 0.01; // Default step size

    public TauLeapingSimulator(Network net) {
        super(net);
    }

    /**
     * Executes one time step (tau) of the simulation using the SimulationController.
     */
    @Override
    public void performStep(SimulationController controller) {
        long[] firings = new long[getNet().getNumReactions()];

        // 1. Calculate propensities (likelihoods) for all reactions
        for (int i = 0; i < getNet().getNumReactions(); i++) {
            // FERN expects the index (i), the AmountManager, and the Simulator instance
            double propensity = getNet().getPropensityCalculator().calculatePropensity(i, getNet().getAmountManager(), this);

            // 2. Calculate the expected number of firings in time tau
            double expectedFirings = propensity * tau;

            // 3. Draw from a Poisson distribution
            if (expectedFirings > 0) {
                firings[i] = getPoisson(expectedFirings);
            } else {
                firings[i] = 0;
            }
        }

        // 4. Apply all firings to the network's molecule counts
        for (int i = 0; i < getNet().getNumReactions(); i++) {
            if (firings[i] > 0) {
                for (long j = 0; j < firings[i]; j++) {
                    // FERN typically uses the AmountManager to execute reactions by their integer index
                    getNet().getAmountManager().performReaction(i, 1);
                }
            }
        }
    }

    /**
     * A lightweight Poisson random number generator (Knuth's algorithm).
     * This avoids needing to import external Apache Commons Math libraries.
     */
    private long getPoisson(double lambda) {
        double L = Math.exp(-lambda);
        double p = 1.0;
        long k = 0;
        do {
            k++;
            p *= Math.random();
        } while (p > L);
        return k - 1;
    }

    @Override
    public void reinitialize() {
        // Reset any algorithm-specific parameters if the simulation restarts
    }

    @Override
    public String getName() {
        return "Explicit Tau-Leaping";
    }
}