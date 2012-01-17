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
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Assignment;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.EventAssignment;
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
import org.sbml.jsbml.util.StringTools;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.validator.OverdeterminationValidator;
import org.simulator.math.RNG;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DESystem;
import org.simulator.math.odes.DelayValueHolder;
import org.simulator.math.odes.DelayedDESystem;
import org.simulator.math.odes.EventDESystem;
import org.simulator.math.odes.FastProcessDESystem;
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
public class SBMLinterpreter implements DelayedDESystem, EventDESystem,
    FastProcessDESystem, RichDESystem, ValueHolder {
  
  /**
   * A logger.
   */
  private static final transient Logger logger = Logger.getLogger(SBMLinterpreter.class
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
   * 
   */
  private HashSet<Double> priorities;
  
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
  private int stoichiometriesSize;
  
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
   * 
   */
  private DelayValueHolder delayValueHolder;


  /**
   * 
   */
  private List<Integer> highOrderEvents;

  /**
   * 
   */
  private double[] conversionFactors;
  
  /**
   * 
   */
  private boolean modelHasEvents;

  /**
   * 
   */
  private int nRateRules;

  /**
   * 
   */
  private int nAssignmentRules;

  /**
   * 
   */
  private int nConstraints;

  
  /**
   * 
   */
  private List<AssignmentRuleObject> initialAssignmentRoots;
  
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
    this.nConstraints=this.model.getNumConstraints();
    this.symbolHash = new HashMap<String, Integer>();
    this.compartmentHash = new HashMap<String, Integer>();
    this.stoichiometricCoefHash = new HashMap<String, Double>();
    this.nodeInterpreter = new EfficientASTNodeInterpreter(this);
    this.nodeInterpreterWithTime = new ASTNodeInterpreterWithTime(this);
    this.level = model.getLevel();
    this.astNodeTime=0d;
    this.priorities = new HashSet<Double>();
    this.highOrderEvents = new LinkedList<Integer>();
    
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
    this.conversionFactors = new double[Y.length];
    Arrays.fill(conversionFactors, 1d);
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
  private void evaluateAlgebraicRules() throws ModelOverdeterminedException {
    OverdeterminationValidator odv = new OverdeterminationValidator(model);
    // model has not to be overdetermined (violation of the SBML
    // specifications)
    if (odv.isOverdetermined()) { throw new ModelOverdeterminedException(); }
    // create assignment rules out of the algebraic rules
    AlgebraicRuleConverter arc = new AlgebraicRuleConverter(odv.getMatching(),
      model);
    algebraicRules = arc.getAssignmentRules();
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
    throws DerivativeException {
    if ((t - currentTime > 1E-15)
        || ((Y != this.Y) && !Arrays.equals(Y, this.Y))) {
      /*
       * We have to compute the system for the given state. But we are not
       * interested in the rates of change, but only in the reaction velocities.
       * Therefore, we throw away the results into a senseless array.
       */
      computeDerivatives(t, Y);
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
   * @see
   * org.apache.commons.math.ode.FirstOrderDifferentialEquations#getDimension ()
   */
  public int getDimension() {
    return this.initialValues.length;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.odes.EventDESystem#getEventAssignments(double,
   * double[])
   */
  public EventInProcess getNextEventAssignments(double t, double previousTime, double[] Y)
    throws DerivativeException {
    
    if (!modelHasEvents) { return null; }
    
    // change Y because of different priorities and reevaluation of
    // trigger/priority after the execution of events
    System.arraycopy(Y, 0, this.Y, 0, Y.length);
    this.currentTime = t;

    Double priority, execTime = 0d;
    astNodeTime += 0.01;
    Double triggerTimeValues[];
    Event ev;
    int i = 0, index;
    Boolean persistent, aborted;
    boolean hasNewDelayedEvents=false;
    
    try {
      
      // recheck trigger of events that have fired for this point in time
      // but have not been executed yet
      priorities.clear();
      while (i < runningEvents.size()) {
        index = runningEvents.get(i);
        //ev = model.getEvent(index);
        if(!events[index].hasMoreAssignments(currentTime)) {
          runningEvents.remove(i);
          continue;
        }
        persistent = events[index].getPersistent();
        if (!persistent) {
          if (!events[index].getTriggerObject().compileBoolean(astNodeTime)) {
            runningEvents.remove(i);
            events[index].aborted(currentTime);
            i--;
          } else {
            ASTNodeObject priorityObject=events[index].getPriorityObject();
            if (priorityObject != null) {
              events[index].changePriority(priorityObject.compileDouble(astNodeTime));
              priorities.add(events[index].getPriority());
            }
          }
        } else {
          ASTNodeObject priorityObject=events[index].getPriorityObject();
          if (priorityObject != null) {
            events[index].changePriority(priorityObject.compileDouble(astNodeTime));
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
          events[index].refresh(currentTime);
          i--;
          aborted = true;
        }
        else if((events[index].getLastTimeFired()<=currentTime) && (events[index].getLastTimeExecuted()>previousTime) && (events[index].getLastTimeExecuted()!=currentTime)) {
          events[index].refresh(previousTime);
        }
        
        persistent = ev.getTrigger().getPersistent();
        if (!persistent && !aborted) {
          if (!events[index].getTriggerObject().compileBoolean(astNodeTime)) {
            //delayedEvents.remove(i);
            events[index].aborted(currentTime);
            //i--;
            aborted = true;
          }
        }
        
        if ((events[index].hasExecutionTime()) && (events[index].getTime() <= currentTime) && !aborted) {
          if (ev.getPriority() != null) {
            priority = events[index].getPriorityObject().compileDouble(astNodeTime);
            
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
      for (i = 0; i < events.length; i++) {
        if (events[i].getTriggerObject().compileBoolean(astNodeTime)) {
          // event has not fired recently -> can fire
          if (!events[i].getFireStatus(currentTime)) {
            execTime = currentTime;
            // event has a delay
            ASTNodeObject delayObject = events[i].getDelayObject();
            if (delayObject != null) {
              execTime += delayObject.compileDouble(astNodeTime);
              if(!delayedEvents.contains(i)) {
                delayedEvents.add(i);
              }
              hasNewDelayedEvents=true;
            } else {
              ASTNodeObject priorityObject = events[i].getPriorityObject();
              if (priorityObject != null) {
                priority = events[i].getPriorityObject().compileDouble(astNodeTime);
                priorities.add(priority);
                events[i].changePriority(priority);
              }
              runningEvents.add(i);
            }
            triggerTimeValues = null;
            if (events[i].getUseValuesFromTriggerTime()) {
              // store values from trigger time for later
              // execution
              List<AssignmentRuleObject> ruleObjects = events[i].getRuleObjects();
              triggerTimeValues = new Double[ruleObjects.size()];
              if(ruleObjects!=null) {
                int j=0;
                for (AssignmentRuleObject obj:ruleObjects) {
                  obj.processRule(Y, astNodeTime, false);
                  triggerTimeValues[j]=obj.getValue();
                  j++;
                }
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
        return processNextEvent(priorities,this.Y);
      }
      // nothing to do
      else if(hasNewDelayedEvents) {
        this.events[0].clearAssignments();
        return this.events[0];
      }
      else {
        return null;
      }
      
    } catch (SBMLException exc) {
      throw new DerivativeException(exc);
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
   * @throws DerivativeException
   * 
   */
  private double[] computeDerivatives(double time, double[] Y)
    throws DerivativeException {
    // create a new array with the same size of Y where the rate of change
    // is stored for every symbol in the simulation
    double changeRate[] = new double[Y.length];
    computeDerivatives(time, Y, changeRate);
    return changeRate;
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see eva2.tools.math.des.DESystem#getValue(double, double[], double[])
   */
  public void computeDerivatives(double time, double[] Y, double[] changeRate)
    throws DerivativeException {
    this.currentTime = time;
    if(Double.isNaN(time)) {
      throw new DerivativeException("Time should not be NaN!");
    }
    System.arraycopy(Y, 0, this.Y, 0, Y.length);
    if (modelHasEvents) {
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
      processRules(astNodeTime, changeRate, this.Y);
      
      /*
       * Compute changes due to reactions
       */
      processVelocities(changeRate, astNodeTime);
      
      
      
      /*
       * Check the model's constraints
       */
      for (int i = 0; i < nConstraints; i++) {
        if (model.getConstraint(i).getMath().compile(nodeInterpreter)
            .toBoolean()) {
          listOfContraintsViolations[i].add(Double.valueOf(time));
        }
      }
      
    } catch (SBMLException exc) {
      throw new DerivativeException(exc);
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
      this.conversionFactors = new double[Y.length];
      Arrays.fill(conversionFactors, 1d);
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
        Parameter convParameter = s.getConversionFactorInstance();
        if(convParameter == null) {
          convParameter = model.getConversionFactorInstance();
        }
        if(convParameter!=null) {
          conversionFactors[yIndex] = convParameter.getValue();
        }
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
     * Algebraic Rules
     */
    boolean containsAlgebraicRules=false;
    for (i = 0; i < (int) model.getNumRules(); i++) {
      if (model.getRule(i).isAlgebraic()) {
        containsAlgebraicRules=true;
        break;
      }
    }
    if(containsAlgebraicRules) {
      evaluateAlgebraicRules();
    }
    
    /*
     * Initialize Events
     */
    if (model.getNumEvents() > 0) {
      // this.events = new ArrayList<EventWithPriority>();
      if(this.events==null) {
        this.events = new EventInProcess[model.getNumEvents()];
      }
      this.runningEvents = new LinkedList<Integer>();
      this.delayedEvents = new LinkedList<Integer>();
      initEvents();
      modelHasEvents=true;
    }
    else {
      modelHasEvents=false;
    }
    
    
    if(refreshTree) {
      createSimplifiedSyntaxTree();
    
    }
    
    // save the initial values of this system, necessary at this point for the delay function
    if(initialValues.length!=Y.length) {
      initialValues = new double[Y.length];
    }
    System.arraycopy(Y, 0, initialValues, 0, initialValues.length);
    
    /*
     * Initial assignments
     */
    astNodeTime+=0.01;
    processInitialAssignments(astNodeTime,this.Y);
    
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
     * All other rules
     */
    astNodeTime+=0.01;
    processRules(astNodeTime, null, this.Y);
    
    /*
     * Process initial assignments a 2nd time because there can be rules
     * dependent on initial assignments and vice versa, so one of both has to be
     * evaluated twice at the start
     */
    
    astNodeTime+=0.01;
    processInitialAssignments(astNodeTime,this.Y);
    
    // save the initial values of this system
    System.arraycopy(Y, 0, initialValues, 0, initialValues.length);
    
    
    
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
    initializeEvents();
  }
  
  /**
   * 
   */
  private void initializeEvents() {
    if(events!=null) {
      for(int i=0;i!=events.length;i++) {
        Event e=model.getEvent(i);
        events[i].setUseValuesFromTriggerTime(e.getUseValuesFromTriggerTime());
        events[i].setPersistent(e.getTrigger().getPersistent());
        events[i].setTriggerObject((ASTNodeObject)copyAST(e.getTrigger().getMath(),true,null,null).getUserObject());
        if(e.getPriority()!=null) {
          events[i].setPriorityObject((ASTNodeObject)copyAST(e.getPriority().getMath(),true,null,null).getUserObject());
        }
        if(e.getDelay()!=null) {
          events[i].setDelayObject((ASTNodeObject)copyAST(e.getDelay().getMath(),true,null,null).getUserObject());
        }
        
        events[i].clearRuleObjects();
        for (EventAssignment as: e.getListOfEventAssignments()) {
          Integer symbolIndex = symbolHash.get(as.getVariable());
          if (symbolIndex != null) {
            Species sp = model.getSpecies(as.getVariable());
            if (sp != null) {
              Compartment c = sp.getCompartmentInstance();
              boolean hasZeroSpatialDimensions = true;
              if((c!=null) && (c.getSpatialDimensions()>0)) {
                hasZeroSpatialDimensions=false;
              }
              events[i].addRuleObject(new AssignmentRuleObject(
                (ASTNodeObject) copyAST(as.getMath(), true, null, null)
                    .getUserObject(), symbolIndex, sp, compartmentHash.get(sp
                    .getId()), hasZeroSpatialDimensions, this));
            } else {
              events[i].addRuleObject(new AssignmentRuleObject(
                (ASTNodeObject) copyAST(as.getMath(), true, null, null)
                    .getUserObject(), symbolIndex));
            }
          } else if (model.findSpeciesReference(as.getVariable()) != null) {
            SpeciesReference sr = model.findSpeciesReference(as.getVariable());
            if (sr.getConstant() == false) {
              events[i].addRuleObject(new AssignmentRuleObject(
                (ASTNodeObject) copyAST(as.getMath(), true, null, null)
                    .getUserObject(), sr.getId(), stoichiometricCoefHash));
            }
          }
        }
        events[i].setUseValuesFromTriggerTime(e.getUseValuesFromTriggerTime());
      }
      
    }
    
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
    stoichiometriesSize=stoichiometries.size();
  }
  
  private void initializeRules() {
    assignmentRulesRoots = new ArrayList<AssignmentRuleObject>();
    initialAssignmentRoots = new ArrayList<AssignmentRuleObject>();
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
    
    for(int i = 0; i < model.getNumInitialAssignments(); i++) {
      InitialAssignment iA = model.getInitialAssignment(i);
      symbolIndex = symbolHash.get(iA.getVariable());
      if (symbolIndex != null) {
        Species sp = model.getSpecies(iA.getVariable());
        if (sp != null) {
          Compartment c = sp.getCompartmentInstance();
          boolean hasZeroSpatialDimensions = true;
          if((c!=null) && (c.getSpatialDimensions()>0)) {
            hasZeroSpatialDimensions=false;
          }
          initialAssignmentRoots.add(new AssignmentRuleObject(
            (ASTNodeObject) copyAST(iA.getMath(), true, null, null)
                .getUserObject(), symbolIndex, sp, compartmentHash.get(sp
                .getId()), hasZeroSpatialDimensions, this));
        } else {
          initialAssignmentRoots.add(new AssignmentRuleObject(
            (ASTNodeObject) copyAST(iA.getMath(), true, null, null)
                .getUserObject(), symbolIndex));
        }
      } else if (model.findSpeciesReference(iA.getVariable()) != null) {
        SpeciesReference sr = model.findSpeciesReference(iA.getVariable());
        if (sr.getConstant() == false) {
          initialAssignmentRoots.add(new AssignmentRuleObject(
            (ASTNodeObject) copyAST(iA.getMath(), true, null, null)
                .getUserObject(), sr.getId(), stoichiometricCoefHash));
        }
      }
    }
    nRateRules = rateRulesRoots.size();
    nAssignmentRules = assignmentRulesRoots.size();
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
      if (!(node.isName()) || (node.getType() == ASTNode.Type.NAME_TIME) || (node.getType() == ASTNode.Type.NAME_AVOGADRO)
          || !((node.getVariable() != null) && (node.getVariable() instanceof LocalParameter))) {
        List<ASTNode> nodesToLookAt=null;
        if(function!=null) {
          nodesToLookAt=inFunctionNodes;
        }
        else {
          nodesToLookAt=nodes;
        }
        for (ASTNode current : nodesToLookAt) {
          if (!(current.isName()) || (current.getType() == ASTNode.Type.NAME_TIME) || (current.getType() == ASTNode.Type.NAME_AVOGADRO)
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
          if((variable==null) && (function==null)) {
            variable = model.findCallableSBase(node.getName());
          }
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
          break;
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
    if(node1.getChildCount()!=node2.getChildCount()) {
      return true;
    }
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
        if(events[i]!=null) {
          events[i].refresh(model.getEvent(i).getTrigger()
            .getInitialValue());
        }
        else {
          events[i] = new EventInProcess(model.getEvent(i).getTrigger()
            .getInitialValue());
        }
      } else {
        if(events[i]!=null) {
          events[i].refresh(model.getEvent(i).getTrigger()
            .getInitialValue());
        }
        else {
          events[i] = new EventInProcessWithDelay(model.getEvent(i).getTrigger()
            .getInitialValue());
        }
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
  public boolean processAssignmentRules(double t, double Y[])
    throws DerivativeException {
    this.currentTime=t;
    this.astNodeTime+=0.01;
    return processRules(t,null,Y);
  }
  
  
  /**
   * This method creates assignments from the events currently stored in the
   * associated HashMap with respect to their priority.
   * 
   * @param priorities
   * @return
   */
  private EventInProcess processNextEvent(HashSet<Double> priorities, double[] Y)
    throws DerivativeException {
    Integer symbolIndex;
    double newVal, highestPriority=-1;
    int index;
    // check if more than one event has a priority set at this point in time
    highOrderEvents.clear();
    
    if (!priorities.isEmpty()) {
      boolean first=true;
      for(double priority:priorities) {
        if(first) {
          first=false;
          highestPriority=priority;
        }
        else {
          highestPriority=Math.max(highestPriority, priority);
        }
      }
//      array = priorities.toArray(new Double[priorities.size()]);
//      Arrays.sort(array);
//      highestPriority = array[array.length - 1];
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
    } else {
      for (int i = 0; i < this.runningEvents.size(); i++) {
        highOrderEvents.add(runningEvents.get(i));
      }
      if (highOrderEvents.size() > 1) {
        pickRandomEvent(highOrderEvents);
      }
    }
    this.runningEvents.remove(highOrderEvents.get(0));
    try {
      // execute the events chosen for execution
      index = highOrderEvents.get(0);
      this.events[index].clearAssignments();
      
      // event does not use values from trigger time
      if (!events[index].getUseValuesFromTriggerTime()) {
        for(AssignmentRuleObject obj: events[index].getRuleObjects()) {
          obj.processRule(Y, astNodeTime, false);
          newVal = obj.getValue();
          symbolIndex = obj.getIndex();;
          if ((symbolIndex>=0) && (compartmentHash.containsValue(symbolIndex))) {
              updateSpeciesConcentration(symbolIndex, Y, false);
          }
          if(symbolIndex>=0) {
            this.events[index].addAssignment(symbolIndex, newVal);
          }
            
       }
          
      } else {
        // event uses values from trigger time -> get stored values
        // from the HashMap
        Double[] triggerTimeValues = this.events[index].getValues();
        
        int j=0;
        for(AssignmentRuleObject obj: events[index].getRuleObjects()) {
        //for (int j = 0; j < triggerTimeValues.length; j++) {
          newVal = triggerTimeValues[j];
          
          symbolIndex=obj.getIndex();
          if(symbolIndex>=0) {
            if (compartmentHash.containsValue(symbolIndex)) {
              updateSpeciesConcentration(symbolIndex, Y, false);
            }
            this.events[index].addAssignment(symbolIndex, newVal);
          }
          else {
            String id=obj.getSpeciesReferenceID();
            if(id!=null) {
              stoichiometricCoefHash.put(id, newVal);
            }
          } 
          j++;
        }
      }
      this.events[index].executed(currentTime);
    } catch (SBMLException exc) {
      throw new DerivativeException(exc);
    }
    
    return this.events[index];
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.sbml.simulator.math.ValueHolder#getCurrentCompartmentSize()
   */
  
  /**
   * 
   * @param time
   * @param Y
   * @throws SBMLException
   */
  public void processInitialAssignments(double time, double[] Y) throws SBMLException {
    if(Y != null) {
      for (int i = 0; i != initialAssignmentRoots.size(); i++) {
        initialAssignmentRoots.get(i).processRule(Y, time,true);
      }
    }
  }
  
  /**
   * 
   * @param time
   * @param changeRate
   * @param Y
   * @return
   * @throws SBMLException
   */
  public boolean processRules(double time, double[] changeRate, double[] Y) throws SBMLException {
    /*
     * Compute changes due to rules
     */
    
    if(changeRate!=null) {
      for(int i=0;i!=nRateRules;i++) {
        rateRulesRoots.get(i).processRule(changeRate, this.Y, time);
      }
    }
    
    boolean changeByAssignmentRules=false;
    if(Y != null) {
      for (int i = 0; i != nAssignmentRules; i++) {
        boolean currentChange=assignmentRulesRoots.get(i).processRule(Y, time,true);
        changeByAssignmentRules = changeByAssignmentRules || currentChange;
      }
    }
    return changeByAssignmentRules;
    
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
    
    for (int i = 0; i != stoichiometriesSize; i++) {
      stoichiometries.get(i).computeChange(astNodeTime, changeRate, v);
    }
    
    for(int i=0;i!=changeRate.length;i++) {
      changeRate[i]*=conversionFactors[i];
    }
    
  }
  

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
    double changeRate[], boolean causedByRateRule) {
    int speciesIndex;
    Species s;
    for (Entry<String, Integer> entry : compartmentHash.entrySet()) {
      if (entry.getValue() == compartmentIndex) {
        s = model.getSpecies(entry.getKey());
        if (s.isSetInitialConcentration()) {
          speciesIndex = symbolHash.get(entry.getKey());
          if (causedByRateRule) {
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

  /*
   * (non-Javadoc)
   * @see org.simulator.math.odes.DelayedDESSystem#registerDelayValueHolder(org.simulator.math.odes.DelayValueHolder)
   */
  public void registerDelayValueHolder(DelayValueHolder dvh) {
    this.delayValueHolder = dvh;
  }

  /*
   * (non-Javadoc)
   * @see org.simulator.math.odes.DelayValueHolder#computeDelayedValue(double, java.lang.String)
   */
  public double computeDelayedValue(double time, String id) throws DerivativeException {
    if(time<0) {
      int index=symbolHash.get(id);
      double oldTime=currentTime;
      this.currentTime=time;
      double value=Double.NaN;
      for(AssignmentRuleObject r: assignmentRulesRoots) {
        if(r.getIndex()==index) {
          r.processRule(Y, -astNodeTime, false);
          value=r.getValue();
          break;
        }
      }
      if(Double.isNaN(value)) {
        for(AssignmentRuleObject i: initialAssignmentRoots) {
          if(i.getIndex()==index) {
            i.processRule(Y, -astNodeTime, false);
            value=i.getValue();
            break;
          }
        }
      }
      if(Double.isNaN(value)) {
        value=initialValues[index]; 
      }
      this.currentTime=oldTime;
      return value;
    }
    else if (this.delayValueHolder == null) {
      logger.warning(String.format(
        "Cannot access delayed value at time %s for %s.", StringTools
            .toString(time), id));
      
    }
    return this.delayValueHolder.computeDelayedValue(time, id);
  }
  
}
