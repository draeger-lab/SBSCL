package fern.simulation.algorithm;

import fern.network.Network;

/**
 * There are some possibilities to bind the expected change of the propensities by a
 * value epsilon in order to fulfill the leap condition. Here the expected change is bound
 * to the sum of all propensities.
 * <p>
 * Daniel T. Gillespie, Approximate accelerated stochastic simulation 
 * of chemically reacting systems, Journal of chemical physics vol 115, nr 4 (2001); Cao et al., Efficient
 * step size selection for the tau-leaping simulation method, Journal of chemical physics 124, 044109 (2006)
 * 
 * @author Florian Erhard
 * 
 *
 */
public class TauLeapingAbsoluteBoundSimulator extends
		AbstractTauLeapingPropensityBoundSimulator {

	public TauLeapingAbsoluteBoundSimulator(Network net) {
		super(net);
	}

	
	@Override
	protected double getTop(int j) {
		return getEpsilon()*a_sum;
	}

	@Override
	public String getName() {
		return "Tau Leap Propensitiy Absolute Bound";
	}
}
