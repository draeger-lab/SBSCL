package fern.network.sbml;

import java.util.HashMap;
import java.util.Map;

import org.sbml.jsbml.Event;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.observer.TriggerObserver;

/**
 * Observer which handles an event of a sbml model.
 * 
 * @author Florian Erhard
 *
 */
public class SBMLEventHandlerObserver extends TriggerObserver {

	private String name;
	private MathTree trigger;
	private MathTree delay;
	private SBMLNetwork net;
	private Map<String,MathTree> variableAssignment;
	private Map<String,MathTree> parameterAssignment;
	private boolean lastStepTriggered;
	
	/**
	 * Creates the observer.
	 * 
	 * @param sim 	the simulator
	 * @param net	the sbml network
	 * @param event the event object of the sbml model
	 */
	public SBMLEventHandlerObserver(Simulator sim, SBMLNetwork net, Event event) {
		super(sim);
		
		this.net = net;
		parse(event);
	}
	
	private void parse(Event event) {
		this.name = event.getId();
		this.trigger = new MathTree(net,
				event.getTrigger().getMath(),
				((SBMLPropensityCalculator)net.getPropensityCalculator()).getGlobalParameters(),
				new HashMap<String, Double>(),
				net.getSpeciesMapping());
		this.delay = event.getDelay()==null ? null : new MathTree(net,
				event.getDelay().getMath(),
				((SBMLPropensityCalculator)net.getPropensityCalculator()).getGlobalParameters(),
				new HashMap<String, Double>(),
				net.getSpeciesMapping());
		variableAssignment = new HashMap<String, MathTree>();
		parameterAssignment = new HashMap<String, MathTree>();
		
		for (int i=0; i<event.getNumEventAssignments(); i++) {
			String var = event.getEventAssignment(i).getVariable();
			MathTree tree = new MathTree(net,
					event.getEventAssignment(i).getMath(),
					((SBMLPropensityCalculator)net.getPropensityCalculator()).getGlobalParameters(),
					new HashMap<String, Double>(),
					net.getSpeciesMapping());
			if (net.getSpeciesMapping().containsKey(var))
				variableAssignment.put(var, tree);
			else
				parameterAssignment.put(var, tree);
		}
	}
	
	private void executeEvent() {
		for (String var : variableAssignment.keySet())
			net.getAmountManager().setAmount(net.getSpeciesByName(var), (long)variableAssignment.get(var).calculate(net.getAmountManager(),getSimulator()));
		for (String par : parameterAssignment.keySet()) {
			Map<String,Double> globals = ((SBMLPropensityCalculator)net.getPropensityCalculator()).getGlobalParameters();
			globals.put(par, parameterAssignment.get(par).calculate(net.getAmountManager(),getSimulator()));
		}
		getSimulator().reinitialize();
	}

	@Override
	public void activateReaction(int mu, double tau, FireType fireType,
			int times) {}

	@Override
	public void finished() {}

	@Override
	public void started() {
		lastStepTriggered = true;
	}

	@Override
	public void step() {
	}
	
	@Override
	public boolean trigger() {
		boolean triggered = trigger.calculate(net.getAmountManager(),getSimulator())!=0;
		if (!lastStepTriggered && triggered) {
			lastStepTriggered = triggered;
			double delaytime = delay==null ? 0 : delay.calculate(net.getAmountManager(),getSimulator());
			if (delaytime<=0) 
				executeEvent();
			else
				setTheta(delaytime);
			return true;
		}
		lastStepTriggered = triggered;
		return false;
	}

	@Override
	public void theta(double theta) {
		executeEvent();
	}

	public void setSimulatorAsync(Simulator sim) {
		this.setSimulator(sim);
	}
	
	@Override
	public String toString() {
		return name;
	}

}
