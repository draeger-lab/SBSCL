/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.simulation;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import fern.network.AmountManager;
import fern.network.Network;
import fern.network.PropensityCalculator;
import fern.simulation.controller.DefaultController;
import fern.simulation.controller.SimulationController;
import fern.simulation.observer.Observer;
import fern.simulation.observer.TriggerObserver;
import fern.tools.NumberTools;
import fern.tools.Stochastics;


/**
 * Base class for stochastic simulators. Extending classes just have to implement <code>
 * performStep</code> which gets invoked as long as the simulation is lasting. They also may
 * override <code>initialize</code> but should in that case call <code>super.initialize()</code>
 * to avoid unwished effects.
 * <p>
 * Simulators also should handle the special time <code>theta</code>: is is a moment in time
 * when <code>thetaEvent</code> is supposed to be invoked (e.g. to measure species populations
 * at this moment). Consider this, if you want to implement a simulator.
 * <p>
 * The <code>fireReaction</code> are supposed to be invoked when a simulator causes a reaction
 * to fire.
 * <p>
 * If an extending class sticks to these conventions, it can take full benefit of the 
 * observer system: One or more {@link Observer} can be registered at a simulator and 
 * observe certain aspects of the simulation (see the {@link Observer}s javadoc for more
 * information).
 * 
 * 
 * 
 * @author Florian Erhard
 *
 */
public abstract class Simulator {
	
	private double volume = 0;
	
	
	private Network net;
	private AmountManager amountManager;
	private PropensityCalculator propensityCalculator;
	private Observer[] observer;
	private DefaultController timeController = null;
	private ThetaQueue thetaQueue = null;
	private boolean interpolateTheta = false;
	private DelayedThetaInvocationParameters interpolationParameters = null;
	
	
	
	/**
	 * Contains the actual time of the simulation.
	 */
	protected double t = 0;

	/**
	 * Contains the propensities of the reactions.
	 */
	protected double[] a;

	/**
	 * Contains a shortcut to the {@link Stochastics} framework.
	 */
	protected Stochastics stochastics = Stochastics.getInstance();
	
	/**
	 * Creates a new simulator for the given network.
	 * 
	 * @param net	the network to simulate
	 */
	public Simulator(Network net) {
		this.net = net;
		this.amountManager = net.getAmountManager();
		this.propensityCalculator = net.getPropensityCalculator();
		observer = new Observer[0];
		a = new double[net.getNumReactions()];
	}
	
	/**
	 * Starts the simulation up to a given time. It just uses a {@link DefaultController}
	 * and calls {@link Simulator#start(SimulationController)}.
	 * 
	 * @param time	simulation time
	 */
	public void start(double time) {
		if (timeController==null)
			timeController = new DefaultController(time);
		timeController.setTime(time);
		start(timeController);
	}
	
	/**
	 * Start the simulation with the given {@link SimulationController}.
	 * Basically calls {@link #preRun()},{@link #run(SimulationController)} and {@link #postRun()}.
	 * 
	 * @param control the <code>SimulationController</code>
	 */
	public void start(SimulationController control) {
		preRun();
		run(control);
		postRun();
	}
	
	/**
	 * Initializes this simulator for the first run (or a further run starting from time 0).
	 */
	public void preRun() {
		initialize();
		
		for (int o=0; o<observer.length; o++)
			observer[o].started();
	}
	
	/**
	 * Runs a simulation, after it's initialized by calling {@link #preRun()} (allows to continue a simulation).
	 * @param control
	 */
	public void run(SimulationController control) {
		while(control.goOn(this)) {
			
			for (Observer o : observer) {
				if (o instanceof TriggerObserver)
					((TriggerObserver)o).trigger();
			}
			
			for (int o=0; o<observer.length; o++)
				observer[o].step();
			
			performStep(control);
			
			if (interpolationParameters!=null) {
				for (Observer o : interpolationParameters.observers) 
					o.theta(interpolationParameters.interpolationTheta);
				interpolationParameters=null;
			}
				
		}
	}
	
	/**
	 * Finalizes a simulation, i.e. call it, when this simulation will not be continued.
	 */
	public void postRun() {
		for (int o=0; o<observer.length; o++){
			observer[o].finished();
			observer[o].print();
		}
	}
	
	/**
	 * Fires a reaction. It calls the observer and the {@link AmountManager}
	 * @param mu		reaction to be fired
	 * @param t			time at which the firing occurs
	 * @param fireType	type of the firing
	 */
	protected void fireReaction(int mu, double t, FireType fireType) {
		for (int o=0; o<observer.length; o++)
			observer[o].activateReaction(mu,t, fireType, 1);
		// change the amount of the reactants
		if (!Double.isInfinite(t))
			amountManager.performReaction(mu,1);
	}
	
	/**
	 * Fires a reaction. It calls the observer and the {@link AmountManager}
	 * @param mu		reaction to be fired
	 * @param t_start	start of the firings
	 * @param t_end		end of the firings
	 * @param times		number of firings
	 * @param fireType	type of the firings
	 */
	protected void fireReaction(int mu, double t_start, double t_end, int times, FireType fireType) {
		for (int o=0; o<observer.length; o++)
			observer[o].activateReaction(mu,t,fireType, times);
		// change the amount of the reactants
		if (!Double.isInfinite(t))
			amountManager.performReaction(mu,times);
	}
	
	/**
	 * Gets called when the simulator reaches the special time <code>theta</code>. It
	 * invokes {@link Observer#theta(double)} of the appropriate observer.
	 * <p>
	 * It has to be called in extending classes.
	 * <p>
	 * It also handles the interpolation.
	 */
	protected void thetaEvent() {
//		double theta = getNextThetaEvent();
//		for (int o=0; o<observer.length; o++)
//			if (theta==observer[o].getTheta())
//				observer[o].theta(theta);
		
		
		double theta = getNextThetaEvent();
		LinkedList<Observer> obs = thetaQueue.getNextObserversAndRemove();
		
		double t_save = t;
		t = theta;
		boolean fired = false;
		for (Observer o : observer) {
			if (o instanceof TriggerObserver)
				fired |= ((TriggerObserver)o).trigger();
		}
		if (!fired) t = t_save;
		
		if (interpolateTheta) {
			long[] beforeThetaAmounts = new long[net.getNumSpecies()];
			for (int i=0; i<net.getNumSpecies(); i++) beforeThetaAmounts[i] = amountManager.getAmount(i);
			interpolationParameters = new DelayedThetaInvocationParameters(
					getTime(),
					beforeThetaAmounts,
					theta,
					obs
					);
					
		} else {
			for (Observer o : obs) 
				o.theta(theta);
			
		}
	}
	
	/**
	 * Initializes the algorithm:
	 * <ul><li>set t=0</li><li>reset the {@link AmountManager}</li><li>recalculate the propensities</li></ul>
	 * Gets called at the very beginning of <code>start</code>
	 */
	public void initialize() {
		t=0;
		amountManager.resetAmount();
		thetaQueue = new ThetaQueue();
		
		initializePropensities();
	}

	/**
	 * Initializes the propensities. Also, reinitialize them whenever
	 * any event is executed.
	 */
	public void initializePropensities() {
		for (int i = 0; i < a.length; i++) {
			a[i] = propensityCalculator.calculatePropensity(i, amountManager, this);
		}
	}

	
	/**
	 * Reset propensities when a event has been executed.
	 */
	public abstract void reinitialize();
	
	/**
	 * Performs one simulation step. Between steps the terminating condition is checked.
	 * The simulators controller is passed, if an extending class wants to check it
	 * within one step. Implementing algorithms cannot be sure that the propensities
	 * are correct at the beginning of a step (e.g. if the volume changed). They should
	 * override {@link Simulator#setAmount(int, long)} and {@link Simulator#setVolume(double)}
	 * if they need correct values!
	 * 
	 * @param control	the simulators controller
	 */
	public abstract void performStep(SimulationController control);
	/**
	 * Gets the name of the algorithm.
	 * @return name of the algorithm
	 */
	public abstract String getName();
	
	/**
	 * Gets the actual simulation time.
	 * @return actual simulation time
	 */
	public double getTime() {
		return t;
	}

	/**
	 * Is called by {@link Observer#setTheta(double)}.
	 * 
	 * @param obs		the observer which is registering 
	 * @param theta 	the theta to register
	 */
	public void registerNewTheta(Observer obs, double theta) {
		thetaQueue.pushTheta(theta, obs);
	}
	
	/**
	 * Theta defines a moment, where the simulator has to invoke <code>theta</code> of
	 * a observer. It is used e.g. to determine the amounts of species at one moments. 
	 * Extending class just have to call {@link Simulator#thetaEvent()} which basically
	 * calls the observer.
	 * 
	 * @return the theta
	 */
	public double getNextThetaEvent() {
//		double theta = Double.POSITIVE_INFINITY;
//		for (Observer o : observer)
//			theta = Math.min(theta,o.getTheta());
//		return theta;
		return thetaQueue.getNextTheta();
	}

	/**
	 * Gets the {@link AmountManager}.
	 * @return the <code>AmountManager</code>
	 */
	protected AmountManager getAmountManager() {
		return amountManager;
	}
	
	/**
	 * Gets the amount of the given species.
	 * 
	 * @param species	species index
	 * @return 			amount of species
	 * 
	 * @see AmountManager#getAmount(int)
	 */
	public double getAmount(int species) {
		if (interpolateTheta && interpolationParameters!=null)
			return NumberTools.interpolateLinear(interpolationParameters.interpolationTheta, interpolationParameters.beforeTheta, getTime(), (int)interpolationParameters.beforeThetaAmounts[species], (int)amountManager.getAmount(species));
		else
			return amountManager.getAmount(species);
	}
	
	/**
	 * Sets the amount of the given species. Propensities have to be recalculated!
	 * 
	 * @param species	species index
	 * @param amount	amount of species
	 * 
	 * @see AmountManager#setAmount(int, long)
	 */
	public void setAmount(int species, long amount) {
		amountManager.setAmount(species, amount);
	}
	
	/**
	 * Gets the {@link PropensityCalculator}.
	 * @return the <code>PropensityCalculator</code>
	 */
	public PropensityCalculator getPropensityCalculator() {
		return propensityCalculator;
	}

	/**
	 * Gets the simulation network.
	 * @return the network
	 */
	public Network getNet() {
		return net;
	}
	
	/**
	 * Adds an observer to the list of observers.
	 * 
	 * @param observer	the observer to add
	 * @return			the added observer
	 */
	public Observer addObserver(Observer observer) {
		if (observer.getSimulator()!=this)
			throw new IllegalArgumentException("Observer doesn't belong to this simulator!");
		Observer[] n = new Observer[this.observer.length+1];
		System.arraycopy(this.observer, 0, n, 0, this.observer.length);
		n[n.length-1] = observer;
		this.observer = n;
		return observer;
	}

	/**
	 * Sets the seed for a particular model.
	 *
	 * @param seed
	 */
	public void setStochasticSeed(long seed) {
		stochastics.setSeed(seed);
	}
	
	
	/**
	 * Gets the volume of the reaction network.
	 * 
	 * @return volume
	 */
	public double getVolume() {
		return volume;
	}
		
	
	/**
	 * Sets the volume of the reaction network.
	 * 
	 * @param volume the volume
	 */
	public void setVolume(double volume) {
		this.volume = volume;
	}
	
	/**
	 * Gets whether the amount values are returned interpolated for theta events.
	 * 
	 * @return interpolated
	 */
	public boolean isInterpolateTheta() {
		return interpolateTheta;
	}

	/**
	 * Sets whether the amount values are returned interpolated for theta events. 
	 */	
	public void setInterpolateTheta(boolean interpolateTheta) {
		this.interpolateTheta = interpolateTheta;
	}

	/**
	 * Gets the current propensity for the given reaction.
	 * 
	 * @param reaction	index of the reaction
	 * @return			propensity for the reaction
	 */
	public double getPropensity(int reaction) {
		return a[reaction];
	}

	/**
	 * Defines different types of a firing for reactions.
	 * 
	 * @author Florian Erhard
	 *
	 */
	public enum FireType {
		GillespieSimple,GillespieEnhanced, GibsonBruck, TauLeapCritical, TauLeapNonCritical
	}

	
	private static class DelayedThetaInvocationParameters
	{
		public double beforeTheta = -1;
		public long[] beforeThetaAmounts = null;
		public double interpolationTheta = 0; 
		public LinkedList<Observer> observers;
		
		public DelayedThetaInvocationParameters(double beforeTheta, long[] beforeThetaAmounts, double interpolationTheta, LinkedList<Observer> observers) {
			this.beforeTheta = beforeTheta;
			this.beforeThetaAmounts = beforeThetaAmounts;
			this.interpolationTheta = interpolationTheta;
			this.observers = observers;
		}
	}
	
	/**
	 * Manages the registered thetas
	 * 
	 * @author Florian Erhard
	 *
	 */
	private static class ThetaQueue {
		private Map<Double, LinkedList<Observer>> thetas;
		private double nextTheta = Double.POSITIVE_INFINITY;
		
		public ThetaQueue() {
			thetas = new HashMap<Double, LinkedList<Observer>>();
		}
		
		public void pushTheta(double theta, Observer obs) {
			if (!thetas.containsKey(theta))
				thetas.put(theta,new LinkedList<Observer>());
			thetas.get(theta).add(obs);
			nextTheta = Math.min(nextTheta, theta);
		}
		
		public double getNextTheta() {
			return nextTheta;
		}
		
		public LinkedList<Observer> getNextObserversAndRemove() {
			LinkedList<Observer> re = thetas.remove(nextTheta);
			nextTheta = Double.POSITIVE_INFINITY;
			for (Double t : thetas.keySet())
				nextTheta = Math.min(nextTheta,t);
			return re;
		}
	}


	
	
}
