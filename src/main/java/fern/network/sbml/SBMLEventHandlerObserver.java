package fern.network.sbml;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.observer.TriggerObserver;
import java.util.HashMap;
import java.util.Map;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.sbml.SBMLinterpreter;
import org.simulator.sbml.astnode.ASTNodeValue;

/**
 * Observer which handles an event of a sbml model.
 * Refactored to use native ASTNode evaluation instead of MathTree.
 *
 * @author Florian Erhard
 */
public class SBMLEventHandlerObserver extends TriggerObserver {

  private String name;
  private ASTNode triggerAST;
  private ASTNode delayAST;
  private SBMLNetwork net;
  private SBMLinterpreter interpreter;
  private Map<String, ASTNode> variableAssignment;
  private Map<String, ASTNode> parameterAssignment;
  private boolean lastStepTriggered;

  public SBMLEventHandlerObserver(Simulator sim, SBMLNetwork net, SBMLinterpreter interpreter,
      Event event) throws ModelOverdeterminedException {
    super(sim);
    this.net = net;
    this.interpreter = interpreter;
    parse(event, interpreter);
  }

  private void parse(Event event, SBMLinterpreter interpreter) {
    this.name = event.getId();
    this.triggerAST = interpreter.copyAST(event.getTrigger().getMath(), true, null, null);
    this.delayAST = event.getDelay() == null ? null : interpreter.copyAST(event.getDelay().getMath(), true, null, null);
    
    variableAssignment = new HashMap<>();
    parameterAssignment = new HashMap<>();

    for (int i = 0; i < event.getNumEventAssignments(); i++) {
      String var = event.getEventAssignment(i).getVariable();
      ASTNode tree = interpreter.copyAST(event.getEventAssignment(i).getMath(), true, null, null);
      if (interpreter.getModel().containsSpecies(var)) {
        variableAssignment.put(var, tree);
      } else {
        parameterAssignment.put(var, tree);
      }
    }
  }

  private double evaluate(ASTNode ast, Simulator sim) {
    interpreter.updateSpeciesConcentration(net.getAmountManager());
    interpreter.setCurrentTime(sim.getTime());
    return ((ASTNodeValue) ast.getUserObject("SBML_SIMULATION_TEMP_VALUE")).compileDouble(sim.getTime(), 0d);
  }

  private void executeEvent() {
    for (String var : variableAssignment.keySet()) {
      net.getAmountManager().setAmount(net.getSpeciesByName(var),
          (long) evaluate(variableAssignment.get(var), getSimulator()));
    }
    for (String par : parameterAssignment.keySet()) {
      Map<String, Double> globals = ((SBMLPropensityCalculator) net.getPropensityCalculator())
          .getGlobalParameters();
      globals.put(par, evaluate(parameterAssignment.get(par), getSimulator()));
    }
    getSimulator().reinitialize();
  }

  @Override
  public void activateReaction(int mu, double tau, FireType fireType, int times) {
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
    boolean triggered = evaluate(triggerAST, getSimulator()) != 0;
    if (!lastStepTriggered && triggered) {
      lastStepTriggered = triggered;
      double delaytime = delayAST == null ? 0 : evaluate(delayAST, getSimulator());
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