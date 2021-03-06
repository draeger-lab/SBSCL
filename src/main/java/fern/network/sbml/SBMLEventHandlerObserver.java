package fern.network.sbml;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.observer.TriggerObserver;
import java.util.HashMap;
import java.util.Map;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.sbml.SBMLinterpreter;

/**
 * Observer which handles an event of a sbml model.
 *
 * @author Florian Erhard
 */
public class SBMLEventHandlerObserver extends TriggerObserver {

  private String name;
  private MathTree trigger;
  private MathTree delay;
  private SBMLNetwork net;
  private Map<String, MathTree> variableAssignment;
  private Map<String, MathTree> parameterAssignment;
  private boolean lastStepTriggered;

  /**
   * Creates the observer.
   *
   * @param sim         the simulator
   * @param net         the sbml network
   * @param interpreter the sbmlInterpreter instance to calculate the node values
   * @param event       the event object of the sbml model
   */
  public SBMLEventHandlerObserver(Simulator sim, SBMLNetwork net, SBMLinterpreter interpreter,
      Event event) throws ModelOverdeterminedException {
    super(sim);

    this.net = net;
    parse(event, interpreter);
  }

  private void parse(Event event, SBMLinterpreter interpreter) {
    this.name = event.getId();
    this.trigger = new MathTree(interpreter, event.getTrigger().getMath());
    this.delay =
        event.getDelay() == null ? null : new MathTree(interpreter, event.getDelay().getMath());
    variableAssignment = new HashMap<>();
    parameterAssignment = new HashMap<>();

    for (int i = 0; i < event.getNumEventAssignments(); i++) {
      String var = event.getEventAssignment(i).getVariable();
      MathTree tree = new MathTree(interpreter, event.getEventAssignment(i).getMath());
      if (interpreter.getModel().containsSpecies(var)) {
        variableAssignment.put(var, tree);
      } else {
        parameterAssignment.put(var, tree);
      }
    }
  }

  private void executeEvent() {
    for (String var : variableAssignment.keySet()) {
      net.getAmountManager().setAmount(net.getSpeciesByName(var),
          (long) variableAssignment.get(var).calculate(net.getAmountManager(), getSimulator()));
    }
    for (String par : parameterAssignment.keySet()) {
      Map<String, Double> globals = ((SBMLPropensityCalculator) net.getPropensityCalculator())
          .getGlobalParameters();
      globals
          .put(par, parameterAssignment.get(par).calculate(net.getAmountManager(), getSimulator()));
    }
    getSimulator().reinitialize();
  }

  @Override
  public void activateReaction(int mu, double tau, FireType fireType,
      int times) {
  }

  @Override
  public void finished() {
  }

  @Override
  public void started() {
    lastStepTriggered = true;
  }

  @Override
  public void step() {
  }

  @Override
  public boolean trigger() {
    boolean triggered = trigger.calculate(net.getAmountManager(), getSimulator()) != 0;
    if (!lastStepTriggered && triggered) {
      lastStepTriggered = triggered;
      double delaytime =
          delay == null ? 0 : delay.calculate(net.getAmountManager(), getSimulator());
      if (delaytime <= 0) {
        executeEvent();
      } else {
        setTheta(delaytime);
      }
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
