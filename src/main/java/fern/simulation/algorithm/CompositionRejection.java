/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.simulation.algorithm;

import fern.network.ConstantAmountManager;
import fern.network.AmountManager;
import fern.network.DefaultAmountManager;
import fern.network.Network;
import fern.network.PropensityCalculator;
import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.controller.SimulationController;


/**
 * 
 * This is an enhanced version of the original Direct method developed by
 * Gillespie. Just like the algorithm of Gibson and Bruck it uses a dependency graph to know what propensities
 * have to be recalculated. Additionally, the composition and rejection method proposed in Slepoy 2008 is used
 * to determine the next firing reaction.
 * <p>
 * For references see Daniel T. Gillespie., A General Method for Numerically Simulating
 * the Stochastic Time Evolution of Coupled Chemical Reactions, J.Comp.Phys. 22, 403 (1976)
 * and M.A.Gibson and J.Bruck, Efficient Exact Stochastic Simulation of Chemical
 * Systems with Many Species and Many Channels, J.Phys.Chem.A., Vol 104, no 9, 2000
 * and Alexander Slepoy Aidan P. Thompson and Steven J. Plimpton, A constant-time kinetic Monte Carlo
 * algorithm for simulation of large biochemical reaction networks,  J. Chem. Phys. 128, 205101 (2008); DOI:10.1063/1.2919546
 * 
 * @author Florian Erhard
 * @see GillespieEnhanced
 * @see DependencyGraph
 */
public class CompositionRejection extends GillespieEnhanced {

	private GroupContainer groups;
	
	public CompositionRejection(Network net) {
		super(net);
		
		groups = new GroupContainer(this);
	}
	
	
	@Override
	public boolean isEfficientlyAdaptSum() {
		return true;
	}

	@Override
	public void initialize() {
		super.initialize();
		groups.initialize(a);
	}
	
	@Override
	public void reinitialize() {
		super.reinitialize();
		groups.initialize(a);
	}
	
	@Override
	public void performStep(SimulationController control) {

		if (changed) {
			initialize();
		}
		
		// obtain mu and tau by the direct method described in chapter 5A page 417ff
		double tau = directMCTau(groups.getA_sum());
		
		if (!Double.isInfinite(tau)) {
			changed = false;
			while (t<=getNextThetaEvent() && t+tau>getNextThetaEvent() && !changed)
				thetaEvent();
			
			if (changed) {
				performStep(control);
				return;
				
			}
			int mu = groups.drawReaction(stochastics, a);
			
			fireReaction(mu, t+tau, FireType.GillespieEnhanced);
			
			for (int alpha : dep.getDependent(mu)) {
				double old = a[alpha];
				a[alpha] = getPropensityCalculator().calculatePropensity(alpha,getAmountManager(), this);
				groups.propensityChanged(alpha, old, a[alpha]);
			}
		}

		t+=tau;
		
		if (Double.isInfinite(tau))
			thetaEvent();
	}
	
	

	@Override
	public String getName() {
		return "Composition rejection";
	}

	
}
