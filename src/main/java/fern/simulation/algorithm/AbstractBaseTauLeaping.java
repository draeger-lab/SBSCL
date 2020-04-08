package fern.simulation.algorithm;

import java.util.Map;


import cern.colt.bitvector.BitVector;
import fern.network.AbstractKineticConstantPropensityCalculator;
import fern.network.Network;
import fern.simulation.controller.SimulationController;
import fern.tools.NetworkTools;
import fern.tools.NumberTools;


/**
 * Base class for all tau leaping procedures (which are different in the methods choosing 
 * the timestep candidates for critical and noncritical reactions). It extends <code>GillespieEnhanced</code>
 * because it uses the SSP algorithm, when the timestep candidate is to small.
 * <p>
 * Each tau leaping algorithm only works with an {@link AbstractKineticConstantPropensityCalculator} and
 * only if the highest order of a reaction is maximal 3. 
 * <p>
 * For references see Daniel T. Gillespie, Approximate accelerated stochastic simulation 
 * of chemically reacting systems, Journal of chemical physics vol 115, nr 4 (2001); Cao et al., Efficient
 * step size selection for the tau-leaping simulation method, Journal of chemical physics 124, 044109 (2006)
 * 
 * @author Florian Erhard
 * @see GillespieEnhanced
 */
public abstract class AbstractBaseTauLeaping extends GillespieEnhanced {

	private double 	langevinThreshold 	= Double.POSITIVE_INFINITY;
	private double 	useSimpleFactor 	= 10;
	private int 	numSimpleCalls 		= 100;
	private int 	nCritical 			= 10;
	private double	epsilon				= 0.03;
	private int[][] v					= null;
	private BitVector criticals			= null;
	protected boolean verbose = false;
	
	protected Map<Integer,Integer>[] reactantHistos;
	protected Map<Integer,Integer>[] productHistos;
	
	
	/**
	 * Create the simulator for a given network. 
	 * @param net	the simulation network
	 */
	@SuppressWarnings("unchecked")
	public AbstractBaseTauLeaping(Network net) {
		super(net);
		reactantHistos = new Map[net.getNumReactions()];
		for (int i=0; i<reactantHistos.length; i++)
			reactantHistos[i] = NumberTools.createHistogramAsMap(net.getReactants(i));
		productHistos = new Map[net.getNumReactions()];
		for (int i=0; i<productHistos.length; i++)
			productHistos[i] = NumberTools.createHistogramAsMap(net.getProducts(i));
		
		v = new int[net.getNumSpecies()][net.getNumReactions()];
		for (int species=0; species<v.length; species++) {
			for (int reaction=0; reaction<v[species].length; reaction++) {
				if (reactantHistos[reaction].containsKey(species))
					v[species][reaction]-=reactantHistos[reaction].get(species);
				if (productHistos[reaction].containsKey(species))
					v[species][reaction]=productHistos[reaction].get(species);
			}
		}
	}
	
	/**
	 * choose the timestep candidate for the noncritical reactions by some method.
	 * @param criticals Bitvector identifying the critical reactions
	 * @return timestep candidate for noncritical reactions
	 */
	protected abstract double chooseTauNonCriticals(BitVector criticals);
	
	/**
	 * choose the timestep candidate for the critical reactions
	 * @param criticals Bitvector identifying the critical reactions
	 * @return timestep candidate for critical reactions
	 */
	protected double chooseTauCriticals(BitVector criticals) {
		double a_sum_c = 0;
		for (int i=0; i<getNet().getNumReactions(); i++)
			if (criticals.get(i))
				a_sum_c += a[i];
		
		return stochastics.getExponential(a_sum_c);
	}
	
	
	/**
	 * Performs a tau leaping step.
	 * First, critical reactions (that could exhaust one of its reactants) are idenfied.
	 * The threshold for criticals can be controlled by the field nCriticals.
	 * Second, a candidate timestep is chosen for noncritical reactions. If this timestep
	 * is to little (controlled by useSimpleFactor), <code>numSimpleCalls</code> steps
	 * from the SSP-Algorithm GillespieEnhanced are perform. Otherwise a second timestep
	 * candidate is drawn for the criticals (such that only one critical reaction is firing
	 * and only once in this leap). The smaller candidate is then used as tau.
	 */
	@Override
	public void performStep(SimulationController control) {
		
		double tau1,tau2,tau3;
		
		recalculatePropensities();
		if (Double.isInfinite(getTime()))
			return;
		
		
		tau3 = getNextThetaEvent()-getTime();
		
		while (control.goOn(this) && getNextThetaEvent()<=getTime())
			thetaEvent();
		
		
			
		
		if (verbose) {
			System.out.println("Step started at ("+getTime()+")\n-----------------\n");
			System.out.println("use simple threshold: "+(useSimpleFactor/a_sum));
		}
		
		identifyCriticals();
		
		if (verbose)
			System.out.println("critical reactions: \n"+NetworkTools.getReactionNameWithAmounts(getNet(), NumberTools.getContentAsArray(criticals))+"\n");
		
		tau1 = chooseTauNonCriticals(criticals);
		
		boolean success = false;
		while (!success) {
			
			if (verbose)
				System.out.println("Chose tau': "+tau1);
		
			if (tau1<useSimpleFactor/a_sum) {
				if (verbose)
					System.out.println("Perform "+numSimpleCalls+" SSA steps");
				
				for (int i=0; i<numSimpleCalls && control.goOn(this); i++)
					super.performStep(control);
				success = true;
			}
			else {
				tau2 = chooseTauCriticals(criticals);
				
				if (verbose)
					System.out.println("Chose tau'': "+tau2);
				
				getNet().getAmountManager().save();
				if (tau3 < tau1 && tau3 < tau2) {
					success = leapBy(tau3, criticals,FireType.TauLeapNonCritical);
					if (verbose)
						System.out.println("Leaped to theta");
				}
				else if (tau1 < tau2) {
					success = leapBy(tau1, criticals,FireType.TauLeapNonCritical);
					if (verbose)
						System.out.println("Leaped tau'");
					
				} else {
					int crit = identifyTheOnlyCriticalReaction(criticals);
					fireReaction(crit, t+tau2, FireType.TauLeapCritical);
					success = leapBy(tau2, criticals,FireType.TauLeapCritical);
					if (verbose)
						System.out.println("Leaped tau''");
				}
				
				if (!success) {
					if (verbose)
						System.out.println("Not successful, decrease tau'!");
					
					tau1/=2.0;
					getNet().getAmountManager().rollback();
				}
			}
		}
		
		
		if (verbose)
			System.out.println("\n-----------------\nStep done at ("+getTime()+")!\n");
	}

	
	private void recalculatePropensities() {
		a_sum = 0;
		
		for (int i=0; i<a.length; i++) {
			a[i] = getPropensityCalculator().calculatePropensity(i, getAmountManager(), this);
			a_sum += a[i];
		}
		
		if (a_sum==0) t = Double.POSITIVE_INFINITY;
	}

	/**
	 * Draws the only critical reaction which is supposed to be fired, if the second
	 * tau candidate has won.
	 * @param criticals Bitvector identifying the critical reactions
	 * @return
	 */
	private int identifyTheOnlyCriticalReaction(BitVector criticals) {
		double a_critical_sum = 0;
		for (int j=0; j<criticals.size(); j++) 
			if (criticals.get(j))
				a_critical_sum+=a[j];
		
		double r2 = stochastics.getUnif();
		double test = r2*a_critical_sum;
		
		double sum = 0;
		for (int i=0; i<criticals.size(); i++) {
			if (!criticals.get(i)) continue;
			sum+=a[i];
			if (sum>=test) return i;
		}
		
		throw new RuntimeException("Drawing variable aborted!");
	}


	/**
	 * Identifies critical reactions.
	 * @return Bitvector identifying the critical reactions
	 */
	private void identifyCriticals() {
		if (criticals==null)
			criticals = new BitVector(getNet().getNumReactions());
		else
			criticals.clear();
		
		for (int j=0; j<criticals.size(); j++)
			if (a[j]>0 && computeL(j)<nCritical )
				criticals.set(j);
	}

	/**
	 * Determines the maximal number of times that the reaction <code>reaction</code>
	 * can fire before exhausting one of its reactants.
	 * @param reaction the index of the reaction
	 * @return maximal number of firing times
	 */
	private int computeL(int reaction) {
		int firings = Integer.MAX_VALUE;
		
		Map<Integer,Integer> reactantHisto = this.reactantHistos[reaction];
		for (Integer reactant : reactantHisto.keySet()) 
			firings = Math.min(firings,(int)Math.floor(getAmountManager().getAmount(reactant)/reactantHisto.get(reactant)));
		
		return firings;
	}

	/**
	 * Tries to perform the tau leap by generating random numbers for the times of
	 * firings of each noncritical reaction. If the leap is unsuccessful (because the
	 * reactants of one reaction are exhausted), false is returned (but the already performed
	 * firings are not canceled). If the parameter for the used Poisson distribution
	 * is greater than <code>langevinThreshold</code>, a Normal distribution is used
	 * as an approximation (which is faster).
	 * @param tau the timestep for the leap
	 * @param criticals Bitvector identifying the critical reactions
	 * @return
	 */
	private boolean leapBy(double tau, BitVector criticals, FireType fireType) {
		int max = 0;
		int sum = 0;
		try {
			int times;
			double at;
			for (int i=0; i<getNet().getNumReactions(); i++) {
				if (criticals.get(i)) continue;
				at = a[i]*tau;
				if (at>langevinThreshold) 
					times = Math.max(0, (int) Math.round(at+Math.sqrt(at)*stochastics.getNormal()));
				else 
					times = stochastics.getPoisson(at);
				if (times>0)
					fireReaction(i, t, t+tau, times, fireType);
				max = Math.max(max, times);
				sum+=times;
			}
			if (verbose)
				System.out.println("Sum="+sum+" Max="+max);
			t+=tau;
			return true;
		} catch (RuntimeException e) {
			return false;
		}
	}

	protected int getV(int species, int reaction) {
		return v[species][reaction];
	}

	/**
	 * The Langevin threshold determines, when the normal distribution is used as an
	 * approximation for the poisson distribution. The default value is Infinity (no
	 * approximation).
	 * @return the langevinThreshold
	 */
	public double getLangevinThreshold() {
		return langevinThreshold;
	}


	/**
	 * The Langevin threshold determines, when the normal distribution is used as an
	 * approximation for the poisson distribution. The default value is Infinity (no
	 * approximation).
	 * @param langevinThreshold the langevinThreshold to set
	 */
	public void setLangevinThreshold(double langevinThreshold) {
		this.langevinThreshold = langevinThreshold;
	}


	/**
	 * The useSimpleFactor determines, when the tau leaping is abandoned and the SSA
	 * method is used (if tau<useSimpleFactor/a_0). The default value is 10.
	 * @return the useSimpleFactor
	 */
	public double getUseSimpleFactor() {
		return useSimpleFactor;
	}


	/**
	 * The useSimpleFactor determines, when the tau leaping is abandoned and the SSA
	 * method is used (if tau<useSimpleFactor/a_0).The default value is 10.
	 * @param useSimpleFactor the useSimpleFactor to set
	 */
	public void setUseSimpleFactor(double useSimpleFactor) {
		this.useSimpleFactor = useSimpleFactor;
	}


	/**
	 * The numSimpleCalls determines, how often the SSA is called, when tau leaping is
	 * abandoned. The default value is 100.
	 * @return the numSimpleCalls
	 */
	public int getNumSimpleCalls() {
		return numSimpleCalls;
	}


	/**
	 * The numSimpleCalls determines, how often the SSA is called, when tau leaping is
	 * abandoned. The default value is 100.
	 * @param numSimpleCalls the numSimpleCalls to set
	 */
	public void setNumSimpleCalls(int numSimpleCalls) {
		this.numSimpleCalls = numSimpleCalls;
	}


	/**
	 * The nCritical determines, when a reaction is called critical. It is critical, when
	 * the maximum number, a reaction can fire before exhausting one of its reactants
	 * is less than nCritical. The default value is 10.
	 * @return the nCritical
	 */
	public int getNCritical() {
		return nCritical;
	}


	/**
	 * The nCritical determines, when a reaction is called critical. It is critical, when
	 * the maximum number, a reaction can fire before exhausting one of its reactants
	 * is less than nCritical. The default value is 10.
	 * @param critical the nCritical to set
	 */
	public void setNCritical(int critical) {
		nCritical = critical;
	}

	/**
	 * The epsilon is an error control parameter and bounds the expected change of the 
	 * propensity functions by epsilon * a_sum. It has to be 0 &lt; epsilon &lt;&lt; 1.
	 * The default value is 0.03.
	 * @return the epsilon
	 */
	public double getEpsilon() {
		return epsilon;
	}

	/**
	 * The epsilon is an error control parameter and bounds the expected change of the 
	 * propensity functions by epsilon * a_sum. It has to be 0 &lt; epsilon &lt;&lt; 1.
	 * The default value is 0.03.
	 * @param epsilon the epsilon to set
	 */
	public void setEpsilon(double epsilon) {
		this.epsilon = epsilon;
	}
	
	/**
	 * Gets if each step is to print to stdout.
	 * 
	 * @return verbose
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * Sets if each step is to print to stdout.
	 * 
	 * @param verbose verbose
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	/**
	 * Returns the name of this simulator
	 * @return simulator name
	 */
	@Override
	public String getName() {
		return "Base Tau Leap";
	}
	
}
