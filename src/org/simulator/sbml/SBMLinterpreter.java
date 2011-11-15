/*
 * $Id$ $URL:
 * https://sbml
 * -simulator.svn.sourceforge.net/svnroot/sbml-simulator/trunk/src/org
 * /sbml/simulator/math/SBMLinterpreter.java $
 * --------------------------------------------------------------------- This
 * file is part of SBMLsimulator, a Java-based simulator for models of
 * biochemical processes encoded in the modeling language SBML.
 * 
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
 * 
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation. A copy of the license agreement is provided in the file
 * named "LICENSE.txt" included with this software distribution and also
 * available online as <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.sbml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.FirstOrderDifferentialEquations;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Assignment;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.FunctionDefinition;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.RateRule;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Rule;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.Variable;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.validator.OverdeterminationValidator;
import org.simulator.math.RNG;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DESAssignment;
import org.simulator.math.odes.DESystem;
import org.simulator.math.odes.EventDESystem;
import org.simulator.math.odes.FastProcessDESystem;
import org.simulator.math.odes.IntegrationException;
import org.simulator.math.odes.RichDESystem;
import org.simulator.sbml.astnode.ASTNodeInterpreterWithTime;
import org.simulator.sbml.astnode.ASTNodeObject;
import org.simulator.sbml.astnode.AssignmentRuleObject;
import org.simulator.sbml.astnode.CompartmentOrParameterValue;
import org.simulator.sbml.astnode.FunctionValue;
import org.simulator.sbml.astnode.LocalParameterValue;
import org.simulator.sbml.astnode.NamedValue;
import org.simulator.sbml.astnode.RateRuleObject;
import org.simulator.sbml.astnode.ReactionValue;
import org.simulator.sbml.astnode.RootFunctionValue;
import org.simulator.sbml.astnode.SpeciesReferenceValue;
import org.simulator.sbml.astnode.SpeciesValue;
import org.simulator.sbml.astnode.StoichiometryObject;

/**
 * <p>
 * This DifferentialEquationSystem takes a model in SBML format and maps it to a
 * data structure that is understood by the {@link AbstractDESSolver} of EvA2.
 * Therefore, this class implements all necessary functions expected by SBML.
 * </p>
 * 
 * @author Alexander D&ouml;rr
 * @author Andreas Dr&auml;ger
 * @author Roland Keller
 * @author Dieudonn&eacute; Motsou Wouamba
 * @date 2007-09-06
 * @version $Rev$
 * @since 0.9
 */
public class SBMLinterpreter implements ValueHolder, EventDESystem,
    RichDESystem, FastProcessDESystem, FirstOrderDifferentialEquations {
  
  /**
   * A logger.
   */
  private static final Logger logger = Logger.getLogger(SBMLinterpreter.class
      .getName());
  
  /**
   * Generated serial version UID
   */
  private static final long serialVersionUID = 3453063382705340995L;
  
  /**
   * Contains a list of all algebraic rules transformed to assignment rules for
   * further processing
   */
  private List<AssignmentRule> algebraicRules;
  
  /**
   * Hashes the id of all species located in a compartment to the position of
   * their compartment in the Y vector. When a species has no compartment, it is
   * hashed to null.
   */
  private Map<String, Integer> compartmentHash;
  
  /**
   * This field is necessary to also consider local parameters of the current
   * reaction because it is not possible to access these parameters from the
   * model. Hence we have to memorize an additional reference to the Reaction
   * and thus to the list of these parameters.
   */
  protected Reaction currentReaction;
  
  /**
   * Holds the current time of the simulation
   */
  private double currentTime;
  
  /**
   * This array stores for every event an object of EventInProcess that is used
   * to handel event processing during simulation
   */
  private EventInProcess events[];
  
  /**
   * An array, which stores all computed initial values of the model. If this
   * model does not contain initial assignments, the initial values will only be
   * taken once from the information stored in the model. Otherwise they have to
   * be computed again as soon as the parameter values of this model are
   * changed, because the parameters may influence the return values of the
   * initial assignments.
   */
  protected double[] initialValues;
  
  /**
   * An array, which stores for each constraint the list of times, in which the
   * constraint was violated during the simulation.
   */
  protected List<Double>[] listOfContraintsViolations;
  
  /**
   * The model to be simulated.
   */
  protected Model model;
  
  /**
   * Hashes the id of all {@link Compartment}s, {@link Species}, global
   * {@link Parameter}s, and, if necessary, {@link SpeciesReference}s in
   * {@link RateRule}s to an value object which contains the position in the
   * {@link #Y} vector
   */
  private Map<String, Integer> symbolHash;
  
  /**
   * An array of strings that memorizes at each position the identifier of the
   * corresponding element in the Y array.
   */
  private String[] symbolIdentifiers;
  
  /**
   * An array of the velocities of each reaction within the model system.
   * Holding this globally saves many new memory allocations during simulation
   * time.
   */
  protected double[] v;
  
  /**
   * This {@link Map} saves the current stoichiometric coefficients for those
   * {@link SpeciesReference} objects that are a target to an {@link Assignment}
   * .
   */
  protected Map<String, Double> stoichiometricCoefHash;
  
  /**
   * An array of the current concentration of each species within the model
   * system.
   */
  protected double[] Y;
  
  /**
   * A boolean indicating whether the solver is currently processing fast
   * reactions or not
   */
  private boolean isProcessingFastReactions = false;
  
  /**
   * A boolean indicating whether a model has fast reactions or not.
   */
  private boolean hasFastReactions = false;
  
  /**
   * Stores the indices of the events triggered for the current point in time.
   */
  private List<Integer> runningEvents;
  
  /**
   * Stores the indices of the events triggered for a future point in time.
   */
  private List<Integer> delayedEvents;
  /**
	 * 
	 */
  private EfficientASTNodeInterpreter nodeInterpreter;
  
  /**
   * 
   */
  private Map<String, Species> speciesMap;
  
  /**
   * 
   */
  private Set<String> inConcentration;
  
  /**
   * 
   */
  private int level;
  
  /**
   * 
   */
  private List<ASTNodeObject> kineticLawRoots;
  
  /**
   * 
   */
  private List<ASTNode> nodes;
  
  /**
   * 
   */
  private ASTNodeInterpreterWithTime nodeInterpreterWithTime;
  
  /**
   * 
   */
  private List<StoichiometryObject> stoichiometries;
  
  /**
   * 
   */
  private boolean[] reactionFast;

  /**
   * 
   */
  private List<AssignmentRuleObject> assignmentRulesRoots;

  /**
   * 
   */
  private List<RateRuleObject> rateRulesRoots;
  
  /**
   * 
   */
  private double astNodeTime;
  
  /**
   * <p>
   * This constructs a new {@link DESystem} for the given SBML {@link Model}.
   * Note that only a maximum of {@link Integer#MAX_VALUE} {@link Species} can
   * be simulated. If the model contains more {@link Species}, this class is not
   * applicable.
   * </p>
   * <p>
   * Note that currently, units are not considered.
   * </p>
   * 
   * @param model
   * @throws ModelOverdeterminedException
   * @throws SBMLException
   */
  public SBMLinterpreter(Model model) throws ModelOverdeterminedException,
    SBMLException {
    this.model = model;
    this.v = new double[this.model.getListOfReactions().size()];
    this.symbolHash = new HashMap<String, Integer>();
    this.compartmentHash = new HashMap<String, Integer>();
    this.stoichiometricCoefHash = new HashMap<String, Double>();
    this.nodeInterpreter = new EfficientASTNodeInterpreter(this);
    this.nodeInterpreterWithTime = new ASTNodeInterpreterWithTime(this);
    this.level = model.getLevel();
    this.astNodeTime=0d;
    
    Map<String, Integer> speciesReferenceToRateRule = new HashMap<String, Integer>();
    int speciesReferencesInRateRules = 0;
    for (int k = 0; k < model.getNumRules(); k++) {
      Rule rule = model.getRule(k);
      if (rule.isRate()) {
        RateRule rr = (RateRule) rule;
        SpeciesReference sr = model.findSpeciesReference(rr.getVariable());
        if (sr != null && sr.getConstant() == false) {
          speciesReferencesInRateRules++;
          speciesReferenceToRateRule.put(sr.getId(), k);
        }
      }
    }
    this.Y = new double[model.getNumCompartments() + model.getNumSpecies()
        + model.getNumParameters() + speciesReferencesInRateRules];
    this.symbolIdentifiers = new String[Y.length];
    
    speciesMap = new HashMap<String, Species>();
    inConcentration = new HashSet<String>();
    reactionFast = new boolean[model.getNumReactions()];
    initialValues = new double[Y.length];
    nodes = new LinkedList<ASTNode>();
    kineticLawRoots = new ArrayList<ASTNodeObject>();
    stoichiometries = new ArrayList<StoichiometryObject>();
    this.init();
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.apache.commons.math.ode.FirstOrderDifferentialEquations#
   * computeDerivatives(double, double[], double[])
   */
  public void computeDerivatives(double t, double[] y, double[] yDot)
    throws DerivativeException {
    try {
      getValue(t, y, yDot);
    } catch (IntegrationException exc) {
      throw new DerivativeException(exc);
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.simulator.math.odes.FastProcessDESystem#containsFastProcesses()
   */
  public boolean containsFastProcesses() {
    return hasFastReactions;
  }
  
  /**
   * Due to missing information about the attributes of species set by initial
   * Assignments, a majority vote of all other species is performed to determine
   * the attributes.
   * 
   * @return
   */
  private Species determineMajorSpeciesAttributes() {
    Species majority = new Species(2, 4);
    int concentration = 0, amount = 0, substanceUnits = 0;
    
    for (Species species : model.getListOfSpecies()) {
      if (species.isSetInitialAmount()) {
        amount++;
      } else if (species.isSetInitialConcentration()) {
        concentration++;
      }
      if (species.hasOnlySubstanceUnits()) {
        substanceUnits++;
      }
    }
    if (amount >= concentration) {
      majority.setInitialAmount(0.0);
    } else {
      majority.setInitialConcentration(0.0);
    }
    
    if (substanceUnits > (model.getNumSpecies() - substanceUnits)) {
      majority.setHasOnlySubstanceUnits(true);
    } else {
      majority.setHasOnlySubstanceUnits(false);
    }
    return majority;
    
  }
  
  /**
   * Evaluates the algebraic rules of the given model to assignment rules
   * 
   * @param ar
   * @param changeRate
   * @throws ModelOverdeterminedException
   */
  private void evaluateAlgebraicRule() throws ModelOverdeterminedException {
    OverdeterminationValidator odv = new OverdeterminationValidator(model);
    // model has not to be overdetermined (violation of the SBML
    // specifications)
    if (odv.isOverdetermined()) { throw new ModelOverdeterminedException(); }
    // create assignment rules out of the algebraic rules
    AlgebraicRuleConverter arc = new AlgebraicRuleConverter(odv.getMatching(),
      model);
    algebraicRules = arc.getAssignmentRules();
  }
  
  /**
   * Evaluates the assignment rules of the given model. This method is not to be
   * used at timepoints > 0 because the new value is directly written into the
   * changeRate array which is only valid at the starting point of the
   * simulation. At later time points, the solver takes care of assignment rules
   * with the help of the method processAssignmentRules
   * 
   * @param as
   * @param Y
   * @throws SBMLException
   */
  private void evaluateAssignmentRule(AssignmentRule as, double changeRate[])
    throws SBMLException {
    // get symbol and assign its new value
    Integer speciesIndex = symbolHash.get(as.getVariable());
    if (speciesIndex != null) {
      changeRate[speciesIndex.intValue()] = processAssignmentVaribale(
        as.getVariable(), as.getMath());
      
      if (compartmentHash.containsValue(speciesIndex)) {
        updateSpeciesConcentration(speciesIndex, changeRate, as);
      }
      
    } else if (model.findSpeciesReference(as.getVariable()) != null) {
      SpeciesReference sr = model.findSpeciesReference(as.getVariable());
      if (sr.getConstant() == false) {
        stoichiometricCoefHash.put(sr.getId(),
          as.getMath().compile(nodeInterpreter).toDouble());
      }
    }
  }
  
  /**
   * Evaluates the rate rules of the given model
   * 
   * @param rr
   * @param changeRate
   * @throws SBMLException
   */
  private void evaluateRateRule(RateRule rr, double changeRate[])
    throws SBMLException {
    
    // get symbol and assign its new rate
    Integer speciesIndex = symbolHash.get(rr.getVariable());
    changeRate[speciesIndex.intValue()] = processAssignmentVaribale(
      rr.getVariable(), rr.getMath());
    // when the size of a compartment changes, the concentrations of the
    // species located in this compartment have to change as well
    if (compartmentHash.containsValue(speciesIndex)) {
      updateSpeciesConcentration(speciesIndex, changeRate, rr);
    }
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eva2.tools.math.des.RichDESystem#getIntermediateIds()
   */
  public String[] getAdditionalValueIds() {
    String ids[] = new String[v.length];
    int i = 0;
    for (Reaction r : model.getListOfReactions()) {
      ids[i++] = r.getId();
    }
    return ids;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eva2.tools.math.des.RichDESystem#getIntermediates(double, double[])
   */
  public double[] getAdditionalValues(double t, double[] Y)
    throws IntegrationException {
    if ((t - currentTime > 1E-15)
        || ((Y != this.Y) && !Arrays.equals(Y, this.Y))) {
      /*
       * We have to compute the system for the given state. But we are not
       * interested in the rates of change, but only in the reaction velocities.
       * Therefore, we throw away the results into a senseless array.
       */
      getValue(t, Y);
    }
    return v;
  }
  
  /**
   * 
   * @return
   */
  public EfficientASTNodeInterpreter getASTNodeInterpreter() {
    return nodeInterpreter;
  }
  
  /**
   * Checks if the given symbol id refers to a species and returns the value of
   * its compartment or 1d otherwise
   * 
   * @param speciesId
   * @param val
   * @return
   */
  public double getCurrentCompartmentValueOf(String speciesId) {
    Integer compartmentIndex = compartmentHash.get(speciesId);
    
    if (compartmentIndex != null) {
      // Is species with compartment
      
      double value = Y[compartmentIndex.intValue()];
      if (value != 0d) { return value; }
      
      // Is compartment or parameter or there is no compartment for this
      // species
      // TODO: Replace by user-defined default value?
    }
    
    return 1d;
  }
  
  public double getCurrentCompartmentSize(String id) {
    return Y[symbolHash.get(id)];
  }
  
  public double getCurrentParameterValue(String id) {
    return Y[symbolHash.get(id)];
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.ValueHolder#getSpeciesValue()
   */
  public double getCurrentSpeciesValue(String id) {
    return Y[symbolHash.get(id)];
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.ValueHolder#getCurrentStoichiometry()
   */
  @SuppressWarnings("deprecation")
  public double getCurrentStoichiometry(String id) {
    Integer pos = symbolHash.get(id);
    if (pos != null) { return Y[pos]; }
    Double value = stoichiometricCoefHash.get(id);
    if (value != null) { return value; }
    
    // TODO: What happens if a species reference does not have an id? 
    SpeciesReference sr = model.findSpeciesReference(id);
    
    if (sr != null && sr.isSetStoichiometryMath()) {
      try {
        return sr.getStoichiometryMath().getMath().compile(nodeInterpreter)
            .toDouble();
      } catch (SBMLException exc) {
        logger.log(Level.WARNING, String.format(
          "Could not compile stoichiometry math of species reference %s.", id),
          exc);
      }
    } else if (sr != null) { return sr.getStoichiometry(); }
    return 1d;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eva2.tools.math.des.DESystem#getDESystemDimension()
   */
  public int getDESystemDimension() {
    return this.initialValues.length;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see
   * org.apache.commons.math.ode.FirstOrderDifferentialEquations#getDimension ()
   */
  public int getDimension() {
    return this.getDESystemDimension();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.odes.EventDESystem#getEventAssignments(double,
   * double[])
   */
  public List<DESAssignment> getEventAssignments(double t, double previousTime, double[] Y)
    throws IntegrationException {
    
    if (model.getNumEvents() == 0) { return null; }
    
    // change Y because of different priorities and reevaluation of
    // trigger/priority after the execution of events
    System.arraycopy(Y, 0, this.Y, 0, Y.length);
    this.currentTime = t;
    Double priority, execTime = 0d;
    Double triggerTimeValues[];
    Event ev;
    int i = 0, index;
    Boolean persistent, aborted;
    HashSet<Double> priorities = new HashSet<Double>();
    boolean hasNewDelayedEvents=false;
    
    try {
      
      // recheck trigger of events that have fired for this point in time
      // but have not been executed yet
      priorities.clear();
      while (i < runningEvents.size()) {
        index = runningEvents.get(i);
        ev = model.getEvent(index);
        persistent = ev.getTrigger().getPersistent();
        if (!persistent) {
          if (!ev.getTrigger().getMath().compile(nodeInterpreter).toBoolean()) {
            runningEvents.remove(i);
            events[index].aborted(currentTime);
            i--;
          } else {
            if (ev.getPriority() != null) {
              events[index].changePriority(ev.getPriority().getMath()
                  .compile(nodeInterpreter).toDouble());
              priorities.add(events[index].getPriority());
            }
          }
        } else {
          if (ev.getPriority() != null) {
            events[index].changePriority(ev.getPriority().getMath()
                .compile(nodeInterpreter).toDouble());
            priorities.add(events[index].getPriority());
          }
        }
        i++;
      }
      
      i = 0;
      // check events that have fired at an earlier point in time but have
      // not been executed yet due to a delay
      while (i < delayedEvents.size()) {
        index = delayedEvents.get(i);
        ev = model.getEvent(index);
        aborted = false;
        
        if(events[index].getLastTimeFired()>currentTime) {
          delayedEvents.remove(i);
          events[index].aborted(currentTime);
          i--;
          aborted = true;
        }
        
        if((events[index].getLastTimeFired()<=currentTime) && (events[index].getLastTimeExecuted()>previousTime)) {
          events[index].refresh(previousTime);
        }
        
        persistent = ev.getTrigger().getPersistent();
        if (!persistent && !aborted) {
          if (!ev.getTrigger().getMath().compile(nodeInterpreter).toBoolean()) {
            //delayedEvents.remove(i);
            events[index].aborted(currentTime);
            //i--;
            aborted = true;
          }
        }
        
        if ((events[index].hasExecutionTime()) && (events[index].getTime() <= currentTime) && !aborted) {
          if (ev.getPriority() != null) {
            priority = ev.getPriority().getMath().compile(nodeInterpreter)
                .toDouble();
            if (!priorities.contains(priority)) {
              priorities.add(priority);
            }
            events[index].changePriority(priority);
          }
          runningEvents.add(index);
          //delayedEvents.remove(i);
          //i--;
          
        }
        i++;
      }
      
      // check the trigger of all events in the model
      for (i = 0; i < model.getNumEvents(); i++) {
        ev = model.getEvent(i);
        if (ev.getTrigger().getMath().compile(nodeInterpreter).toBoolean()) {
          // event has not fired recently -> can fire
          if (!events[i].getFireStatus(currentTime)) {
            execTime = currentTime;
            // event has a delay
            if (ev.getDelay() != null) {
              execTime += ev.getDelay().getMath().compile(nodeInterpreter)
                  .toDouble();
              delayedEvents.add(i);
              hasNewDelayedEvents=true;
            } else {
              if (ev.getPriority() != null) {
                priority = ev.getPriority().getMath().compile(nodeInterpreter)
                    .toDouble();
                if (!priorities.contains(priority)) {
                  priorities.add(priority);
                }
                events[i].changePriority(priority);
              }
              runningEvents.add(i);
            }
            triggerTimeValues = null;
            if (ev.getUseValuesFromTriggerTime()) {
              triggerTimeValues = new Double[ev.getNumEventAssignments()];
              // store values from trigger time for later
              // execution
              for (int j = 0; j < ev.getNumEventAssignments(); j++) {
                triggerTimeValues[j] = processAssignmentVaribale(ev
                    .getEventAssignment(j).getVariable(), ev
                    .getEventAssignment(j).getMath());
              }
              
            }
            
            events[i].addValues(triggerTimeValues, execTime);
            
            events[i].fired(currentTime);
          }
          
        }
        // event has fired recently -> can not fire
        else {
          if(events[i].getFireStatus(currentTime)) {
            events[i].recovered(currentTime);
          }
        }
        
      }
      // there are events to fire
      if (runningEvents.size() > 0) {
        return processEvents(priorities);
      }
      // nothing to do
      else if(hasNewDelayedEvents) {
        return new LinkedList<DESAssignment>();
      }
      else {
        return null;
      }
      
    } catch (SBMLException exc) {
      throw new IntegrationException(exc);
    }
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eva2.tools.math.des.DESystem#getIdentifiers()
   */
  public String[] getIdentifiers() {
    return symbolIdentifiers;
  }
  
  /**
   * Returns the initial values of the model to be simulated.
   * 
   * @return Returns the initial values of the model to be simulated.
   */
  public double[] getInitialValues() {
    return this.initialValues;
  }
  
  /**
   * Returns the model that is used by this object.
   * 
   * @return Returns the model that is used by this object.
   */
  public Model getModel() {
    return model;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eva2.tools.math.des.RichDESystem#getNumIntermediates()
   */
  public int getNumAdditionalValues() {
    return v.length;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eva2.tools.math.des.EventDESystem#getNumEvents()
   */
  public int getNumEvents() {
    return model.getNumEvents();
  }
  
  /**
   * This method tells you the complete number of parameters within the model.
   * It counts the global model parameters and all local parameters (parameters
   * within a kinetic law).
   * 
   * @return The total number of model parameters. Note that this number is
   *         limited to an <code>int</code> value, whereas the SBML model may
   *         contain <code>int</code> values.
   */
  public int getNumParameters() {
    int p = (int) model.getNumParameters();
    for (int i = 0; i < model.getNumReactions(); i++) {
      KineticLaw k = model.getReaction(i).getKineticLaw();
      if (k != null) {
        p += k.getLocalParameterCount();
      }
    }
    return p;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eva2.tools.math.des.EventDESystem#getNumRules()
   */
  public int getNumRules() {
    return model.getNumRules();
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eva2.tools.math.des.EventDESystem#getPositionOfParameters()
   */
  public int getPositionOfParameters() {
    return Y.length-model.getNumParameters();
  }
  
  /**
   * Returns the timepoint where the simulation is currently situated
   * 
   * @return
   */
  public double getCurrentTime() {
    return currentTime;
  }
  
  /**
   * Returns the value of the ODE system at the time t given the current values
   * of Y
   * 
   * @param t
   * @param Y
   * @return
   * @throws IntegrationException
   * 
   */
  private double[] getValue(double time, double[] Y)
    throws IntegrationException {
    // create a new array with the same size of Y where the rate of change
    // is stored for every symbol in the simulation
    double changeRate[] = new double[Y.length];
    getValue(time, Y, changeRate);
    return changeRate;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eva2.tools.math.des.DESystem#getValue(double, double[], double[])
   */
  public void getValue(double time, double[] Y, double[] changeRate)
    throws IntegrationException {
    this.currentTime = time;
    System.arraycopy(Y, 0, this.Y, 0, Y.length);
    if (model.getNumEvents() > 0) {
      this.runningEvents.clear();
    }
    
    // make sure not to have invalid older values in the change rate
    Arrays.fill(changeRate, 0d);
    
    try {
      //Always call the compile functions with a new time
      astNodeTime += 0.01;
      
      /*
       * Compute changes due to rules
       */
      int nRateRules = rateRulesRoots.size();
      int nAssignmentRules = assignmentRulesRoots.size();
      
      for(int i=0;i!=nRateRules;i++) {
        rateRulesRoots.get(i).processRule(changeRate, this.Y, astNodeTime);
      }
      
      for(int i=0;i!=nAssignmentRules;i++) {
        assignmentRulesRoots.get(i).processRule(this.Y, astNodeTime);
      }
      
      /*
       * Compute changes due to reactions
       */
      processVelocities(changeRate, astNodeTime);
      
      
      
      /*
       * Check the model's constraints
       */
      for (int i = 0; i < (int) model.getNumConstraints(); i++) {
        if (model.getConstraint(i).getMath().compile(nodeInterpreter)
            .toBoolean()) {
          listOfContraintsViolations[i].add(Double.valueOf(time));
        }
      }
      
    } catch (SBMLException exc) {
      throw new IntegrationException(exc);
    }
    
  }
  
  /**
   * @param EDES
   * @param time
   * @param Ytemp
   * @return
   * @throws IntegrationException
   */
  public void processRules(double time, double[] Ytemp)
      throws IntegrationException {
    for (DESAssignment assignment : processAssignmentRules(time, Ytemp)) {
      Ytemp[assignment.getIndex()] = assignment.getValue();
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.ValueHolder#getSpeciesValue()
   */
  public double getCurrentValueOf(String id) {
    Integer symbolIndex = symbolHash.get(id);
    if (symbolIndex != null) {
      return Y[symbolIndex];
    } else {
      return Double.NaN;
    }
  }
  
  /**
   * <p>
   * This method initializes the differential equation system for simulation. In
   * more detail: the initial amounts or concentration will be assigned to every
   * species or initialAssignments if any are executed.
   * </p>
   * <p>
   * To save computation time the results of this method should be stored in an
   * array. Hence this method must only be called once. However, if the SBML
   * model to be simulated contains initial assignments, this can lead to wrong
   * simulation results because initial assignments may depend on current
   * parameter values.
   * </p>
   * 
   * 
   * @throws ModelOverdeterminedException
   * @throws SBMLException
   */
  public void init() throws ModelOverdeterminedException, SBMLException {
    init(true);
  }
  
  /**
   * 
   * @param refreshTree
   * @throws ModelOverdeterminedException
   * @throws SBMLException
   */
  @SuppressWarnings("unchecked")
  public void init(boolean refreshTree) throws ModelOverdeterminedException, SBMLException {
    int i;
    symbolHash.clear();
    compartmentHash.clear();
    Integer compartmentIndex, yIndex = 0;
    currentTime = 0d;
    astNodeTime=0d;
    
    Map<String, Integer> speciesReferenceToRateRule = new HashMap<String, Integer>();
    int speciesReferencesInRateRules = 0;
    for (int k = 0; k < model.getNumRules(); k++) {
      Rule rule = model.getRule(k);
      if (rule.isRate()) {
        RateRule rr = (RateRule) rule;
        SpeciesReference sr = model.findSpeciesReference(rr.getVariable());
        if (sr != null && sr.getConstant() == false) {
          speciesReferencesInRateRules++;
          speciesReferenceToRateRule.put(sr.getId(), k);
        }
      }
    }
    
    int sizeY = model.getNumCompartments() + model.getNumSpecies()
        + model.getNumParameters() + speciesReferencesInRateRules;
    if(sizeY != this.Y.length) {
      this.Y = new double[sizeY];
      this.symbolIdentifiers = new String[Y.length];
    }
    
    
    /*
     * Save starting values of the model's compartment in Y
     */
    for (i = 0; i < model.getNumCompartments(); i++) {
      Compartment c = model.getCompartment(i);
      if (Double.isNaN(c.getSize())) {
        Y[yIndex] = 0;
      } else {
        Y[yIndex] = c.getSize();
      }
      
      symbolHash.put(c.getId(), yIndex);
      symbolIdentifiers[yIndex] = c.getId();
      yIndex++;
    }
    
    // Due to unset initial amount or concentration of species try to set
    // one of them
    Species majority = determineMajorSpeciesAttributes();
    
    speciesMap.clear();
    /*
     * Save starting values of the model's species in Y and link them with their
     * compartment
     */
    for (i = 0; i < model.getNumSpecies(); i++) {
      Species s = model.getSpecies(i);
      if (!s.getBoundaryCondition() && !s.getConstant()) {
        speciesMap.put(s.getId(), s);
      }
      compartmentIndex = symbolHash.get(s.getCompartment());
      
      // Set initial amount or concentration when not already done
      if (!s.isSetInitialAmount() && !s.isSetInitialConcentration()) {
        if (majority.isSetInitialAmount()) {
          s.setInitialAmount(0d);
        } else {
          s.setInitialConcentration(0d);
        }
        
        s.setHasOnlySubstanceUnits(majority.getHasOnlySubstanceUnits());
      }
      
      if (s.isSetInitialAmount()) {
        Y[yIndex] = s.getInitialAmount();
      } else {
        Y[yIndex] = s.getInitialConcentration();
      }
      symbolHash.put(s.getId(), yIndex);
      compartmentHash.put(s.getId(), compartmentIndex);
      symbolIdentifiers[yIndex] = s.getId();
      yIndex++;
      
    }
    
    /*
     * Save starting values of the stoichiometries
     */
    for (String id : speciesReferenceToRateRule.keySet()) {
      SpeciesReference sr = model.findSpeciesReference(id);
      Y[yIndex] = sr.getStoichiometry();
      symbolHash.put(id, yIndex);
      symbolIdentifiers[yIndex] = id;
      yIndex++;
    }
    
    /*
     * Save starting values of the model's parameter in Y
     */
    for (i = 0; i < model.getNumParameters(); i++) {
      Parameter p = model.getParameter(i);
      Y[yIndex] = p.getValue();
      symbolHash.put(p.getId(), yIndex);
      symbolIdentifiers[yIndex] = p.getId();
      yIndex++;
    }
    
    
    
    /*
     * Initial assignments
     */
    processInitialAssignments();
    
    //updateValues();
    /*
     * Evaluate Constraints
     */
    if (model.getNumConstraints() > 0) {
      this.listOfContraintsViolations = (List<Double>[]) new LinkedList<?>[(int) model
          .getNumConstraints()];
      for (i = 0; i < (int) model.getNumConstraints(); i++) {
        if (listOfContraintsViolations[i] == null) {
          this.listOfContraintsViolations[i] = new LinkedList<Double>();
        }
        if (model.getConstraint(i).getMath().compile(nodeInterpreter)
            .toBoolean()) {
          this.listOfContraintsViolations[i].add(Double.valueOf(0d));
        }
      }
    }
    
    /*
     * Initialize Events
     */
    if (model.getNumEvents() > 0) {
      // this.events = new ArrayList<EventWithPriority>();
      this.events = new EventInProcess[model.getNumEvents()];
      this.runningEvents = new LinkedList<Integer>();
      this.delayedEvents = new LinkedList<Integer>();
      initEvents();
    }
    
    /*
     * Algebraic Rules
     */
    for (i = 0; i < (int) model.getNumRules(); i++) {
      if (model.getRule(i).isAlgebraic()) {
        evaluateAlgebraicRule();
        break;
      }
    }
    
    /*
     * Check for fast reactions & update math of kinetic law to avoid wrong
     * links concerning local parameters
     */
    inConcentration.clear();
    if(reactionFast.length != model.getNumReactions()) {
      reactionFast = new boolean[model.getNumReactions()];
    }
    int reactionIndex = 0;
    for (Reaction r : model.getListOfReactions()) {
      reactionFast[reactionIndex] = r.isFast();
      if (r.isFast() && !hasFastReactions) {
        hasFastReactions = true;
      }
      if (r.getKineticLaw() != null) {
        if (r.getKineticLaw().getListOfLocalParameters().size() > 0) {
          r.getKineticLaw().getMath().updateVariables();
        }
      }
      
      Species species;
      String speciesID;
      for (SpeciesReference speciesRef : r.getListOfReactants()) {
        speciesID = speciesRef.getSpecies();
        species = speciesMap.get(speciesID);
        
        if (species != null) {
          if (species.isSetInitialConcentration()
              && !species.hasOnlySubstanceUnits()) {
            inConcentration.add(speciesID);
          }
        }
      }
      for (SpeciesReference speciesRef : r.getListOfProducts()) {
        speciesID = speciesRef.getSpecies();
        species = speciesMap.get(speciesID);
        if (species != null) {
          if (species.isSetInitialConcentration()
              && !species.hasOnlySubstanceUnits()) {
            inConcentration.add(speciesID);
          }
        }
        
      }
      reactionIndex++;
    }
    
    /*
     * All other rules
     */
    processRules(Y);
    
    /*
     * Process initial assignments a 2nd time because there can be rules
     * dependent on initial assignments and vice versa, so one of both has to be
     * evaluated twice at the start
     */
    processInitialAssignments();
    
    // save the initial values of this system
    if(initialValues.length!=Y.length) {
      initialValues = new double[Y.length];
    }
    System.arraycopy(Y, 0, initialValues, 0, initialValues.length);
    
    if(refreshTree) {
      createSimplifiedSyntaxTree();
    }
    
  }
  
  /**
   * 
   */
  private void createSimplifiedSyntaxTree() {
    nodes.clear();
    kineticLawRoots.clear();
    stoichiometries.clear();
    
    initializeKineticLaws();
    initializeRules();
  }
  
  /**
   * 
   */
  private void initializeKineticLaws() {
    int reactionIndex = 0;
    for (Reaction r : model.getListOfReactions()) {
      KineticLaw kl = r.getKineticLaw();
      if (kl != null) {
        ASTNodeObject currentLaw = (ASTNodeObject) copyAST(kl.getMath(),true, null,null)
            .getUserObject();
        kineticLawRoots.add(currentLaw);
        for (SpeciesReference speciesRef : r.getListOfReactants()) {
          String speciesID = speciesRef.getSpecies();
          int speciesIndex = symbolHash.get(speciesID);
          
          int compartmentIndex = -1;
          Species sp = speciesRef.getSpeciesInstance();
          if (sp != null) {
            String compartmentID = sp.getCompartment();
            if (this.symbolHash.containsKey(compartmentID)) {
              compartmentIndex = this.symbolHash.get(compartmentID);
            }
          }
          int srIndex = -1;
          if (level >= 3) {
            String id = speciesRef.getId();
            if (id != null) {
              if (this.symbolHash.containsKey(id)) {
                srIndex = this.symbolHash.get(id);
              }
              
            }
            
          }
          stoichiometries.add(new StoichiometryObject(speciesRef, speciesIndex,
            srIndex, compartmentIndex, stoichiometricCoefHash, this, Y,
            nodeInterpreter, reactionIndex, inConcentration, true));
          
        }
        for (SpeciesReference speciesRef : r.getListOfProducts()) {
          String speciesID = speciesRef.getSpecies();
          int speciesIndex = symbolHash.get(speciesID);
          
          int compartmentIndex = -1;
          Species sp = speciesRef.getSpeciesInstance();
          if (sp != null) {
            String compartmentID = sp.getCompartment();
            if (this.symbolHash.containsKey(compartmentID)) {
              compartmentIndex = this.symbolHash.get(compartmentID);
            }
          }
          int srIndex = -1;
          if (level >= 3) {
            String id = speciesRef.getId();
            if (id != null) {
              if (this.symbolHash.containsKey(id)) {
                srIndex = this.symbolHash.get(id);
              }
              
            }
            
          }
          stoichiometries.add(new StoichiometryObject(speciesRef, speciesIndex,
            srIndex, compartmentIndex, stoichiometricCoefHash, this, Y,
            nodeInterpreter, reactionIndex, inConcentration, false));
          
        }
        
      } else {
        kineticLawRoots.add(new ASTNodeObject(nodeInterpreterWithTime,
          new ASTNode(0d)));
      }
      reactionIndex++;
    }
  }
  
  private void initializeRules() {
    assignmentRulesRoots = new ArrayList<AssignmentRuleObject>();
    rateRulesRoots = new ArrayList<RateRuleObject>();
    Integer symbolIndex;
    
    for (int i = 0; i < model.getNumRules(); i++) {
      Rule rule = model.getRule(i);
      if (rule.isAssignment()) {
        AssignmentRule as = (AssignmentRule) rule;
        symbolIndex = symbolHash.get(as.getVariable());
        if (symbolIndex != null) {
          Species sp = model.getSpecies(as.getVariable());
          if (sp != null) {
            Compartment c = sp.getCompartmentInstance();
            boolean hasZeroSpatialDimensions = true;
            if((c!=null) && (c.getSpatialDimensions()>0)) {
              hasZeroSpatialDimensions=false;
            }
            assignmentRulesRoots.add(new AssignmentRuleObject(
              (ASTNodeObject) copyAST(as.getMath(), true, null, null)
                  .getUserObject(), symbolIndex, sp, compartmentHash.get(sp
                  .getId()), hasZeroSpatialDimensions, this));
          } else {
            assignmentRulesRoots.add(new AssignmentRuleObject(
              (ASTNodeObject) copyAST(as.getMath(), true, null, null)
                  .getUserObject(), symbolIndex));
          }
        } else if (model.findSpeciesReference(as.getVariable()) != null) {
          SpeciesReference sr = model.findSpeciesReference(as.getVariable());
          if (sr.getConstant() == false) {
            assignmentRulesRoots.add(new AssignmentRuleObject(
              (ASTNodeObject) copyAST(as.getMath(), true, null, null)
                  .getUserObject(), sr.getId(), stoichiometricCoefHash));
          }
        }
      }
      else if(rule.isRate()) {
        RateRule rr = (RateRule) rule;
        symbolIndex = symbolHash.get(rr.getVariable());
        if (symbolIndex != null) {
          Species sp = model.getSpecies(rr.getVariable());
          if (sp != null) {
            Compartment c = sp.getCompartmentInstance();
            boolean hasZeroSpatialDimensions = true;
            if((c!=null) && (c.getSpatialDimensions()>0)) {
              hasZeroSpatialDimensions=false;
            }
            rateRulesRoots.add(new RateRuleObject(
              (ASTNodeObject) copyAST(rr.getMath(), true, null, null)
                  .getUserObject(), symbolIndex, sp, compartmentHash.get(sp
                  .getId()), hasZeroSpatialDimensions, this));
          }
          else if (compartmentHash.containsValue(symbolIndex)) {
            List<Integer> speciesIndices = new LinkedList<Integer>();
            for (Entry<String, Integer> entry : compartmentHash.entrySet()) {
              if (entry.getValue() == symbolIndex) {
                Species s = model.getSpecies(entry.getKey());
                if (s.isSetInitialConcentration()) {
                  int speciesIndex = symbolHash.get(entry.getKey());
                  speciesIndices.add(speciesIndex);
                }
              }
            }
            rateRulesRoots.add(new RateRuleObject(
              (ASTNodeObject) copyAST(rr.getMath(), true, null, null)
                  .getUserObject(), symbolIndex, speciesIndices, this));
          }
          
          else {
            rateRulesRoots.add(new RateRuleObject(
              (ASTNodeObject) copyAST(rr.getMath(), true, null, null)
                  .getUserObject(), symbolIndex));
          }
        }
      }
    }
    if (algebraicRules != null) {
      for (AssignmentRule as : algebraicRules) {
        symbolIndex = symbolHash.get(as.getVariable());
        if (symbolIndex != null) {
          Species sp = model.getSpecies(as.getVariable());
          if (sp != null) {
            Compartment c = sp.getCompartmentInstance();
            boolean hasZeroSpatialDimensions = true;
            if((c!=null) && (c.getSpatialDimensions()>0)) {
              hasZeroSpatialDimensions=false;
            }
            assignmentRulesRoots.add(new AssignmentRuleObject(
              (ASTNodeObject) copyAST(as.getMath(), true, null, null)
                  .getUserObject(), symbolIndex, sp, compartmentHash.get(sp
                  .getId()), hasZeroSpatialDimensions, this));
          } else {
            assignmentRulesRoots.add(new AssignmentRuleObject(
              (ASTNodeObject) copyAST(as.getMath(), true, null, null)
                  .getUserObject(), symbolIndex));
          }
        } else if (model.findSpeciesReference(as.getVariable()) != null) {
          SpeciesReference sr = model.findSpeciesReference(as.getVariable());
          if (sr.getConstant() == false) {
            assignmentRulesRoots.add(new AssignmentRuleObject(
              (ASTNodeObject) copyAST(as.getMath(), true, null, null)
                  .getUserObject(), sr.getId(), stoichiometricCoefHash));
          }
        }
      }
    }
  }
  
  
  
  /**
   * 
   * @param node
   * @return
   */
  private ASTNode copyAST(ASTNode node, boolean mergingPossible, FunctionValue function, List<ASTNode> inFunctionNodes) {
    String nodeString = node.toString();
    ASTNode copiedAST = null;
    if (mergingPossible) {
      //Be careful with local parameters!
      if (!(node.isName()) || (node.getType() == ASTNode.Type.NAME_TIME)
          || !((node.getVariable() != null) && (node.getVariable() instanceof LocalParameter))) {
        List<ASTNode> nodesToLookAt=null;
        if(function!=null) {
          nodesToLookAt=inFunctionNodes;
        }
        else {
          nodesToLookAt=nodes;
        }
        for (ASTNode current : nodesToLookAt) {
          if (!(current.isName()) || (current.getType() == ASTNode.Type.NAME_TIME)
              || ((current.isName()) && !(current.getVariable() instanceof LocalParameter))) {
            if ((current.toString().equals(nodeString)) && (!containUnequalLocalParameters(current,node))) {
              copiedAST = current;
              break;
            }
          }
        }
      }
    }
    
    if (copiedAST == null) {
      copiedAST = new ASTNode(node.getType());
      
      for (ASTNode child : node.getChildren()) {
        if(function!=null) {
          copiedAST.addChild(copyAST(child, true, function, inFunctionNodes));
        }
        else {
          copiedAST.addChild(copyAST(child, mergingPossible, function, inFunctionNodes));
        }
      }
      
      if(function!=null) {
        inFunctionNodes.add(copiedAST);
      }
      else {
        nodes.add(copiedAST);
      }
      
      if (node.isSetUnits()) {
        copiedAST.setUnits(node.getUnits());
      }
      switch (node.getType()) {
        case REAL:
          copiedAST.setValue(node.getReal());
          copiedAST.setUserObject(new ASTNodeObject(nodeInterpreterWithTime,
            copiedAST));
          break;
        case INTEGER:
          copiedAST.setValue(node.getInteger());
          copiedAST.setUserObject(new ASTNodeObject(nodeInterpreterWithTime,
            copiedAST));
          break;
        
        case RATIONAL:
          copiedAST.setValue(node.getNumerator(), node.getDenominator());
          copiedAST.setUserObject(new ASTNodeObject(nodeInterpreterWithTime,
            copiedAST));
          break;
        case NAME_TIME:
          copiedAST.setName(node.getName());
          copiedAST.setUserObject(new ASTNodeObject(nodeInterpreterWithTime,
            copiedAST));
          break;
        case FUNCTION_DELAY:
          copiedAST.setName(node.getName());
          copiedAST.setUserObject(new ASTNodeObject(nodeInterpreterWithTime,
            copiedAST));
          break;
        /*
         * Names of identifiers: parameters, functions, species etc.
         */
        case NAME:
          copiedAST.setName(node.getName());
          CallableSBase variable = node.getVariable();
          if (variable != null) {
            copiedAST.setVariable(variable);
            if (variable instanceof FunctionDefinition) {
              List<ASTNode> arguments=new LinkedList<ASTNode>();
              ASTNode lambda=((FunctionDefinition) variable).getMath();
              for(int i=0;i!=lambda.getChildren().size()-1;i++) {
                arguments.add(lambda.getChild(i));
              }
              FunctionValue functionValue=new FunctionValue(nodeInterpreterWithTime,
                copiedAST,arguments);
              copiedAST.setUserObject(functionValue);
              ASTNode mathAST = copyAST(lambda,
                false,functionValue,new LinkedList<ASTNode>());
              functionValue.setMath(mathAST);
            } else if (variable instanceof Species) {
              boolean hasZeroSpatialDimensions = true;
              Species sp = (Species) variable;
              Compartment c = sp.getCompartmentInstance();
              if((c!=null) && c.getSpatialDimensions() > 0) {
                hasZeroSpatialDimensions = false;
              }
              copiedAST.setUserObject(new SpeciesValue(nodeInterpreterWithTime,
                copiedAST, sp, this, symbolHash.get(variable
                    .getId()), compartmentHash.get(variable.getId()), hasZeroSpatialDimensions));
            } else if ((variable instanceof Compartment)
                || (variable instanceof Parameter)) {
              copiedAST.setUserObject(new CompartmentOrParameterValue(
                nodeInterpreterWithTime, copiedAST, variable, this, symbolHash
                    .get(variable.getId())));
            } else if (variable instanceof LocalParameter) {
              copiedAST.setUserObject(new LocalParameterValue(
                nodeInterpreterWithTime, copiedAST, (LocalParameter) variable));
            } else if (variable instanceof SpeciesReference) {
              copiedAST.setUserObject(new SpeciesReferenceValue(
                nodeInterpreterWithTime, copiedAST,
                (SpeciesReference) variable, this));
            } else if (variable instanceof Reaction) {
              copiedAST.setUserObject(new ReactionValue(
                nodeInterpreterWithTime, copiedAST, (Reaction) variable));
            } 
          } else {
            copiedAST.setUserObject(new NamedValue(
              nodeInterpreterWithTime, copiedAST, function));
          }
          break;
        
        case NAME_AVOGADRO:
          copiedAST.setUserObject(new ASTNodeObject(nodeInterpreterWithTime,
            copiedAST));
          copiedAST.setName(node.getName());
          break;
        case REAL_E:
          copiedAST.setValue(node.getMantissa(), node.getExponent());
          copiedAST.setUserObject(new ASTNodeObject(nodeInterpreterWithTime,
            copiedAST));
          
          break;
        case FUNCTION: {
          copiedAST.setName(node.getName());
          variable = node.getVariable();
          if (variable != null) {
            copiedAST.setVariable(variable);
            if (variable instanceof FunctionDefinition) {
              List<ASTNode> arguments=new LinkedList<ASTNode>();
              ASTNode lambda=((FunctionDefinition) variable).getMath();
              for(int i=0;i!=lambda.getChildren().size()-1;i++) {
                arguments.add(lambda.getChild(i));
              }
              FunctionValue functionValue=new FunctionValue(nodeInterpreterWithTime,
                copiedAST,arguments);
              copiedAST.setUserObject(functionValue);
              ASTNode mathAST = copyAST(lambda,
                false,functionValue,new LinkedList<ASTNode>());
              functionValue.setMath(mathAST);
            }
          }
          break;
        }
        case FUNCTION_PIECEWISE:
          copiedAST.setUserObject(new ASTNodeObject(nodeInterpreterWithTime,
            copiedAST));
          break;
        case FUNCTION_ROOT:
          copiedAST.setUserObject(new RootFunctionValue(nodeInterpreterWithTime,copiedAST));
        case LAMBDA:
          copiedAST.setUserObject(new ASTNodeObject(nodeInterpreterWithTime,
            copiedAST));
          break;
        default:
          copiedAST.setUserObject(new ASTNodeObject(nodeInterpreterWithTime,
            copiedAST));
          break;
      }
      
    }
    
    return copiedAST;
  }
  
  /**
   * 
   * @param current
   * @param node
   * @return
   */
  private boolean containUnequalLocalParameters(ASTNode node1, ASTNode node2) {
    if((node1.getType() == ASTNode.Type.NAME) && (node2.getType() == ASTNode.Type.NAME) &&
      (node1.getVariable() instanceof LocalParameter) && (node2.getVariable() instanceof LocalParameter)) {
        LocalParameter lp1 = (LocalParameter) node1.getVariable();
        LocalParameter lp2 = (LocalParameter) node2.getVariable();
        if((lp1.getId().equals(lp2.getId())) && (!lp1.equals(lp2))) {
          return true;
        }
        else {
          return false;
      }
    }
    else {
      boolean result=false;
      for (int i=0;i!=node1.getChildCount();i++) {
        result=result || containUnequalLocalParameters(node1.getChild(i),node2.getChild(i));
      }
      return result;
    }
  }

  /**
   * Initializes the events of the given model. An Event that triggers at t = 0
   * must not fire. Only when it triggers at t > 0
   * 
   * @throws SBMLException
   */
  private void initEvents() throws SBMLException {
    for (int i = 0; i < model.getNumEvents(); i++) {
      
      if (model.getEvent(i).getDelay() == null) {
        events[i] = new EventInProcess(model.getEvent(i).getTrigger()
            .getInitialValue());
      } else {
        events[i] = new EventInProcessWithDelay(model.getEvent(i).getTrigger()
            .getInitialValue());
      }
    }
  }
  
  private void pickRandomEvent(List<Integer> highOrderEvents) {
    int length = highOrderEvents.size();
    int random = RNG.randomInt(0, length - 1);
    
    Integer winner = highOrderEvents.get(random);
    highOrderEvents.clear();
    highOrderEvents.add(winner);
    
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.odes.EventDESystem#processAssignmentRules(double, double[])
   */
  public List<DESAssignment> processAssignmentRules(double t, double Y[])
    throws IntegrationException {
    ArrayList<DESAssignment> assignmentRules = new ArrayList<DESAssignment>();
    Integer symbolIndex;
    
    try {
      for (int i = 0; i < model.getNumRules(); i++) {
        Rule rule = model.getRule(i);
        if (rule.isAssignment()) {
          AssignmentRule as = (AssignmentRule) rule;
          symbolIndex = symbolHash.get(as.getVariable());
          if (symbolIndex != null) {
            assignmentRules.add(new DESAssignment(t, symbolIndex,
              processAssignmentVaribale(as.getVariable(), as.getMath())));
          } else if (model.findSpeciesReference(as.getVariable()) != null) {
            SpeciesReference sr = model.findSpeciesReference(as.getVariable());
            if (sr.getConstant() == false) {
              stoichiometricCoefHash.put(sr.getId(),
                processAssignmentVaribale(as.getVariable(), as.getMath()));
            }
          }
        }
      }
      if (algebraicRules != null) {
        for (AssignmentRule as : algebraicRules) {
          symbolIndex = symbolHash.get(as.getVariable());
          if (symbolIndex != null) {
            assignmentRules.add(new DESAssignment(t, symbolIndex,
              processAssignmentVaribale(as.getVariable(), as.getMath())));
          } else if (model.findSpeciesReference(as.getVariable()) != null) {
            SpeciesReference sr = model.findSpeciesReference(as.getVariable());
            if (sr.getConstant() == false) {
              stoichiometricCoefHash.put(sr.getId(),
                processAssignmentVaribale(as.getVariable(), as.getMath()));
            }
          }
        }
      }
    } catch (SBMLException exc) {
      throw new IntegrationException(exc);
    }
    
    return assignmentRules;
  }
  
  /**
   * Processes the variable of an assignment in terms of determining whether the
   * variable references to a species or not and if so accounts the compartment
   * in an appropriate way.
   * 
   * @param variable
   * @param math
   * @return
   * @throws SBMLException
   */
  private double processAssignmentVaribale(String variable, ASTNode math)
    throws SBMLException {
    double compartmentValue, result = 0d;
    Species s;
    if (compartmentHash.containsKey(variable)) {
      s = model.getSpecies(variable);
      if (s.isSetInitialAmount() && !s.getHasOnlySubstanceUnits()) {
        compartmentValue = getCurrentCompartmentValueOf(s.getId());
        result = math.compile(nodeInterpreter).toDouble() * compartmentValue;
      } else if (s.isSetInitialConcentration() && s.getHasOnlySubstanceUnits()) {
        compartmentValue = getCurrentCompartmentValueOf(s.getId());
        result = math.compile(nodeInterpreter).toDouble() / compartmentValue;
      } else {
        result = math.compile(nodeInterpreter).toDouble();
      }
      
    } else {
      result = math.compile(nodeInterpreter).toDouble();
    }
    
    return result;
  }
  
  /**
   * This method creates assignments from the events currently stored in the
   * associated HashMap with respect to their priority.
   * 
   * @param priorities
   * @return
   */
  private List<DESAssignment> processEvents(HashSet<Double> priorities)
    throws IntegrationException {
    List<DESAssignment> assignments = new LinkedList<DESAssignment>();
    List<Integer> highOrderEvents, events;
    Integer symbolIndex;
    ASTNode assignment_math;
    Event event;
    Variable variable;
    double newVal, highestPriority;
    Double[] array;
    int index;
    // check if more than one event has a priority set at this point in time
    if (!priorities.isEmpty()) {
      highOrderEvents = new LinkedList<Integer>();
      array = priorities.toArray(new Double[priorities.size()]);
      Arrays.sort(array);
      highestPriority = array[array.length - 1];
      // get event with the current highest priority
      for (int i = 0; i < this.runningEvents.size(); i++) {
        if (this.events[runningEvents.get(i)].getPriority() == highestPriority) {
          highOrderEvents.add(runningEvents.get(i));
        }
      }
      // pick one event randomly, as a matter of fact remove all event
      // except the picked one
      if (highOrderEvents.size() > 1) {
        pickRandomEvent(highOrderEvents);
      }
      
      events = highOrderEvents;
      this.runningEvents.removeAll(events);
    } else {
      events = this.runningEvents;
    }
    
    try {
      // execute the events chosen for execution
      while (events.size() > 0) {
        index = events.get(0);
        event = model.getEvent(index);
        while (this.events[index].hasMoreAssignments(currentTime)) {
          
          // event does not use values from trigger time
          if (!event.getUseValuesFromTriggerTime()) {
            for (int j = 0; j < event.getNumEventAssignments(); j++) {
              assignment_math = event.getEventAssignment(j).getMath();
              variable = event.getEventAssignment(j).getVariableInstance();
              
              if (model.findSpeciesReference(variable.getId()) != null) {
                String id = variable.getId();
                SpeciesReference sr = model.findSpeciesReference(id);
                newVal = assignment_math.compile(nodeInterpreter).toDouble();
                if (sr.getConstant() == false) {
                  stoichiometricCoefHash.put(id, newVal);
                }
                
              } else {
                symbolIndex = symbolHash.get(variable.getId());
                newVal = processAssignmentVaribale(variable.getId(),
                  assignment_math);
                this.Y[symbolIndex] = newVal;
                assignments.add(new DESAssignment(currentTime, symbolIndex,
                  newVal));
                
              }
              
            }
          } else {
            // event uses values from trigger time -> get stored values
            // from the HashMap
            Double[] triggerTimeValues = this.events[index].getValues();
            
            for (int j = 0; j < event.getNumEventAssignments(); j++) {
              assignment_math = event.getEventAssignment(j).getMath();
              variable = event.getEventAssignment(j).getVariableInstance();
              newVal = triggerTimeValues[j];
              
              if (model.findSpeciesReference(variable.getId()) != null) {
                String id = variable.getId();
                SpeciesReference sr = model.findSpeciesReference(id);
                if (sr.getConstant() == false) {
                  stoichiometricCoefHash.put(id, newVal);
                }
              } else {
                symbolIndex = symbolHash.get(variable.getId());
                this.Y[symbolIndex] = newVal;
                assignments.add(new DESAssignment(currentTime, symbolIndex,
                  newVal));
              }
            }
          }
          this.events[index].executed(currentTime);
        }
        events.remove(0);
      }
      
    } catch (SBMLException exc) {
      throw new IntegrationException(exc);
    }
    
    return assignments;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.ValueHolder#getCurrentCompartmentSize()
   */

  /**
   * Processes the initial assignments of the model
   * 
   * @throws SBMLException
   */
  private void processInitialAssignments() throws SBMLException {
    for (int i = 0; i < model.getNumInitialAssignments(); i++) {
      InitialAssignment iA = model.getInitialAssignment(i);
      Integer index = null;
      if (iA.isSetMath() && iA.isSetVariable()) {
        if (model.getSpecies(iA.getVariable()) != null) {
          Species s = model.getSpecies(iA.getVariable());
          double compartmentValue;
          String id = s.getId();
          index = symbolHash.get(id);
          
          if (compartmentHash.containsKey(id)) {
            if (s.isSetInitialAmount() && !s.getHasOnlySubstanceUnits()) {
              compartmentValue = getCurrentCompartmentValueOf(id);
              this.Y[index] = iA.getMath().compile(nodeInterpreter).toDouble()
                  * compartmentValue;
            } else if (s.isSetInitialConcentration()
                && s.getHasOnlySubstanceUnits()) {
              compartmentValue = getCurrentCompartmentValueOf(id);
              this.Y[index] = iA.getMath().compile(nodeInterpreter).toDouble()
                  / compartmentValue;
            } else {
              this.Y[index] = iA.getMath().compile(nodeInterpreter).toDouble();
            }
            
          } else {
            this.Y[index] = iA.getMath().compile(nodeInterpreter).toDouble();
          }
          
        } else if (model.getCompartment(iA.getVariable()) != null) {
          Compartment c = model.getCompartment(iA.getVariable());
          index = symbolHash.get(c.getId());
          this.Y[index] = iA.getMath().compile(nodeInterpreter).toDouble();
        } else if (model.getParameter(iA.getVariable()) != null) {
          Parameter p = model.getParameter(iA.getVariable());
          index = symbolHash.get(p.getId());
          this.Y[index] = iA.getMath().compile(nodeInterpreter).toDouble();
        } else if (model.findSpeciesReference(iA.getVariable()) != null) {
          SpeciesReference sr = model.findSpeciesReference(iA.getVariable());
          double assignment = iA.getMath().compile(nodeInterpreter).toDouble();
          stoichiometricCoefHash.put(sr.getId(), assignment);
          index = symbolHash.get(sr.getId());
          if (index != null) {
            this.Y[index] = assignment;
          }
        } else {
          System.err
              .println("The model contains an initial assignment for a component other than species, compartment, parameter or species reference.");
        }
      }
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.ValueHolder#getCurrentParameterValue()
   */

  /**
   * @param changeRate
   * @throws SBMLException
   */
  private void processRules(double[] changeRate) throws SBMLException {
    // evaluation of assignment rules through the DESystem itself
    // only at time point 0d, at time points >=0d the solver carries on
    // with this task. Assignment rules are only processed during
    // initialization in this class. During this process the passed array
    // changerate is not the actual changerate but the Y vector since there
    // is no change at timepoint zero
    
    for (int i = 0; i < model.getNumRules(); i++) {
      Rule rule = model.getRule(i);
      if (rule.isRate() && currentTime > 0d) {
        RateRule rr = (RateRule) rule;
        evaluateRateRule(rr, changeRate);
      } else if (rule.isAssignment() && currentTime == 0d) {
        AssignmentRule as = (AssignmentRule) rule;
        evaluateAssignmentRule(as, changeRate);
      } else /* if (rule.isScalar()) */{
        // a rule is scalar if it is an assignment rule.
      }
    }
    // process list of algebraic rules
    if (algebraicRules != null && currentTime == 0d) {
      for (AssignmentRule as : algebraicRules) {
        evaluateAssignmentRule(as, changeRate);
      }
      
    }
    
  }
  
  /**
   * This method computes the multiplication of the stoichiometric matrix of the
   * given model system with the reaction velocities vector passed to this
   * method. Note, the stoichiometric matrix is only constructed implicitly by
   * running over all reactions and considering all participating reactants and
   * products with their according stoichiometry or stoichiometric math.
   * 
   * @param velocities
   *        An array of reaction velocities at the current time.
   * @param Y
   * @return An array containing the rates of change for each species in the
   *         model system of this class.
   * @throws SBMLException
   */
  protected void processVelocities(double[] changeRate, double time)
    throws SBMLException {
    
    // Velocities of each reaction.
    for (int reactionIndex = 0; reactionIndex != v.length; reactionIndex++) {
      if (hasFastReactions) {
        if (isProcessingFastReactions == reactionFast[reactionIndex]) {
          v[reactionIndex] = kineticLawRoots.get(reactionIndex).compileDouble(
            time);
        } else {
          v[reactionIndex] = 0;
        }
      } else {
        v[reactionIndex] = kineticLawRoots.get(reactionIndex).compileDouble(
          time);
        
      }
    }
    
    int size = stoichiometries.size();
    for (int i = 0; i != size; i++) {
      stoichiometries.get(i).computeChange(currentTime, changeRate, v);
    }
    
  }
  
  /*
   * protected void processVelocities(double[] changeRate) throws SBMLException
   * { int reactionIndex, speciesIndex; String speciesID; String id; Species
   * species; SpeciesReference speciesRef; KineticLaw kin; // Velocities of each
   * reaction. reactionIndex = 0; for (Reaction r : model.getListOfReactions())
   * { kin = r.getKineticLaw(); if (hasFastReactions) { if ((kin != null) &&
   * (isProcessingFastReactions == currentReaction.isFast())) { v[reactionIndex]
   * = nodeInterpreter.compileDouble(kin.getMath()); } else { v[reactionIndex] =
   * 0; } } else { if (kin != null) { v[reactionIndex] =
   * nodeInterpreter.compileDouble(kin.getMath()); } else { v[reactionIndex] =
   * 0; } }
   * 
   * int numProducts = r.getNumProducts(); int numReactants =
   * r.getNumReactants(); for (int i = 0; i != numReactants; i++) { speciesRef =
   * r.getReactant(i); speciesID = speciesRef.getSpecies(); species =
   * speciesMap.get(speciesID);
   * 
   * if (species != null) { speciesIndex = symbolHash.get(speciesID); if (level
   * >= 3) { id = speciesRef.getId(); if (id != null) { if
   * (this.symbolHash.containsKey(id)) { double currentStoichiometry =
   * this.Y[this.symbolHash.get(id)]; changeRate[speciesIndex] -=
   * currentStoichiometry v[reactionIndex]; this.stoichiometricCoefHash.put(id,
   * currentStoichiometry); } else if
   * (this.stoichiometricCoefHash.containsKey(id)) { changeRate[speciesIndex] -=
   * this.stoichiometricCoefHash.get(id) v[reactionIndex]; } else { if
   * (speciesRef.isSetStoichiometryMath()) { changeRate[speciesIndex] -=
   * speciesRef.getStoichiometryMath()
   * .getMath().compile(nodeInterpreter).toDouble() v[reactionIndex]; } else {
   * changeRate[speciesIndex] -= speciesRef .getCalculatedStoichiometry() *
   * v[reactionIndex]; } } } else { if (speciesRef.isSetStoichiometryMath()) {
   * changeRate[speciesIndex] -= speciesRef.getStoichiometryMath()
   * .getMath().compile(nodeInterpreter).toDouble() v[reactionIndex]; } else {
   * changeRate[speciesIndex] -= speciesRef .getCalculatedStoichiometry() *
   * v[reactionIndex]; } }
   * 
   * } else { if (speciesRef.isSetStoichiometryMath()) {
   * changeRate[speciesIndex] -= speciesRef.getStoichiometryMath()
   * .getMath().compile(nodeInterpreter).toDouble() v[reactionIndex]; } else {
   * changeRate[speciesIndex] -= speciesRef .getCalculatedStoichiometry() *
   * v[reactionIndex]; } } } } for (int i = 0; i != numProducts; i++) {
   * speciesRef = r.getProduct(i); speciesID = speciesRef.getSpecies(); species
   * = speciesMap.get(speciesID); if (species != null) { speciesIndex =
   * symbolHash.get(speciesID); if (level >= 3) { id = speciesRef.getId(); if
   * (id != null) { if (this.symbolHash.containsKey(id)) { double
   * currentStoichiometry = this.Y[this.symbolHash.get(id)];
   * changeRate[speciesIndex] += currentStoichiometry v[reactionIndex];
   * this.stoichiometricCoefHash.put(id, currentStoichiometry); } else if
   * (this.stoichiometricCoefHash.containsKey(id)) { changeRate[speciesIndex] +=
   * this.stoichiometricCoefHash.get(id) v[reactionIndex]; } else { if
   * (speciesRef.isSetStoichiometryMath()) { changeRate[speciesIndex] +=
   * speciesRef.getStoichiometryMath()
   * .getMath().compile(nodeInterpreter).toDouble() v[reactionIndex]; } else {
   * changeRate[speciesIndex] += speciesRef .getCalculatedStoichiometry() *
   * v[reactionIndex]; } } } else { if (speciesRef.isSetStoichiometryMath()) {
   * changeRate[speciesIndex] += speciesRef.getStoichiometryMath()
   * .getMath().compile(nodeInterpreter).toDouble() v[reactionIndex]; } else {
   * changeRate[speciesIndex] += speciesRef .getCalculatedStoichiometry() *
   * v[reactionIndex]; } }
   * 
   * } else { if (speciesRef.isSetStoichiometryMath()) {
   * changeRate[speciesIndex] += speciesRef.getStoichiometryMath()
   * .getMath().compile(nodeInterpreter).toDouble() v[reactionIndex]; } else {
   * changeRate[speciesIndex] += speciesRef .getCalculatedStoichiometry() *
   * v[reactionIndex]; } }
   * 
   * } }
   * 
   * reactionIndex++; } // When the unit of reacting species is given mol/volume
   * // then it has to be considered in the change rate that should // always be
   * only in mol/time
   * 
   * for (String s : inConcentration) { speciesIndex = symbolHash.get(s);
   * changeRate[speciesIndex] = changeRate[speciesIndex] /
   * getCurrentCompartmentValueOf(s); } }
   */

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.sbml.simulator.math.odes.FastProcessDESystem#setFastProcessComputation
   * (boolean)
   */
  public void setFastProcessComputation(boolean isProcessing) {
    isProcessingFastReactions = isProcessing;
  }
  
  /**
   * This method allows us to set the parameters of the model to the specified
   * values in the given array.
   * 
   * @param params
   *        An array of parameter values to be set for this model. If the number
   *        of given parameters does not match the number of model parameters,
   *        an exception will be thrown.
   */
  // TODO changing the model directly not allowed / does this method still
  // make sense?
  public void setParameters(double[] params) {
    // TODO consider local parameters as well.
    // if (params.length != model.getNumParameters())
    // throw new IllegalArgumentException(
    // "The number of parameters passed to this method must "
    // + "match the number of parameters in the model.");
    int paramNum, reactionNum, localPnum;
    for (paramNum = 0; paramNum < model.getNumParameters(); paramNum++) {
      model.getParameter(paramNum).setValue(params[paramNum]);
    }
    for (reactionNum = 0; reactionNum < model.getNumReactions(); reactionNum++) {
      KineticLaw law = model.getReaction(reactionNum).getKineticLaw();
      for (localPnum = 0; localPnum < law.getLocalParameterCount(); localPnum++) {
        law.getLocalParameter(localPnum).setValue(params[paramNum++]);
      }
    }
    if (model.getNumInitialAssignments() > 0 || model.getNumEvents() > 0) {
      try {
        init();
      } catch (Exception exc) {
        // This can never happen
        logger.log(Level.WARNING,
          "Could not re-initialize the model with the new parameter values.",
          exc);
      }
    }
  }
  
  /**
   * Updates the concentration of species due to a change in the size of their
   * compartment
   * 
   * @param compartmentIndex
   */
  private void updateSpeciesConcentration(int compartmentIndex,
    double changeRate[], Rule r) {
    int speciesIndex;
    Species s;
    for (Entry<String, Integer> entry : compartmentHash.entrySet()) {
      if (entry.getValue() == compartmentIndex) {
        s = model.getSpecies(entry.getKey());
        if (s.isSetInitialConcentration()) {
          speciesIndex = symbolHash.get(entry.getKey());
          if (r instanceof RateRule) {
            changeRate[speciesIndex] = -changeRate[compartmentIndex]
                * Y[speciesIndex] / Y[compartmentIndex];
          } else {
            changeRate[speciesIndex] = Y[speciesIndex] / Y[compartmentIndex];
          }
          
        }
        
      }
    }
    
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.odes.DESystem#containsEventsOrRules()
   */
  public boolean containsEventsOrRules() {
    if ((model.getNumRules() != 0) || (model.getNumEvents() != 0)) {
      return true;
    } else {
      return false;
    }
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.ValueHolder#getCurrentValueOf(int)
   */
  public double getCurrentValueOf(int position) {
    return Y[position];
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.odes.DESystem#getNumPositiveValues()
   */
  public int getNumPositiveValues() {
    //return numPositives;
    return 0;
  }
  
  
}