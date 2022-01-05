/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2022 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.sbml;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import fern.network.AmountManager;
import org.apache.commons.math.ode.DerivativeException;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Event;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.KineticLaw;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DESystem;
import org.simulator.math.odes.EventInProgress;
import org.simulator.sbml.astnode.ASTNodeValue;
import org.simulator.sbml.astnode.AssignmentRuleValue;

/**
 * <p>
 * This differential equation system ({@link DESystem}) takes a model in
 * <a href="http://sbml.org" target="_blank">SBML</a>
 * format and maps it to a data structure that is understood by the {@link AbstractDESSolver}.
 * Therefore, this class implements all necessary functions expected by <a href="http://sbml.org"
 * target="_blank">SBML</a>.
 * </p>
 *
 * @author Alexander D&ouml;rr
 * @author Andreas Dr&auml;ger
 * @author Roland Keller
 * @author Dieudonn&eacute; Motsou Wouamba
 * @version $Rev$
 * @since 0.9
 */
public class SBMLinterpreter extends EquationSystem {

  /**
   * A {@link Logger}.
   */
  private static final transient Logger logger = Logger.getLogger(SBMLinterpreter.class.getName());

  /**
   * Generated serial version UID
   */
  private static final long serialVersionUID = 3453063382705340995L;

  /**
   * <p>
   * This constructs a new {@link DESystem} for the given SBML {@link Model}. Note that only a
   * maximum of {@link Integer#MAX_VALUE} {@link Species} can be simulated. If the model contains
   * more {@link Species}, this class is not applicable.
   * </p>
   * <p>
   * Note that currently, units are not considered.
   * </p>
   *
   * @param model
   * @throws ModelOverdeterminedException
   * @throws SBMLException
   */
  public SBMLinterpreter(Model model)
      throws ModelOverdeterminedException, SBMLException {
    this(model, 0d, 1d, 1d);
  }


  /**
   * @param model
   * @param defaultSpeciesValue
   * @param defaultParameterValue
   * @param defaultCompartmentValue
   * @throws SBMLException
   * @throws ModelOverdeterminedException
   */
  public SBMLinterpreter(Model model, double defaultSpeciesValue, double defaultParameterValue,
      double defaultCompartmentValue)
      throws SBMLException, ModelOverdeterminedException {
    this(model, defaultSpeciesValue, defaultParameterValue, defaultCompartmentValue, null);
  }


  /**
   * Creates a new {@link SBMLinterpreter}
   *
   * @param model                   the model to interpret
   * @param defaultSpeciesValue     the default value for species, if no value is given
   * @param defaultParameterValue   the default value for parameters, if no value is given
   * @param defaultCompartmentValue the default value for compartments, if no value is given
   * @param amountHash              a hash that states for the species in the model, whether their
   *                                amount or their concentration should be computed
   * @throws SBMLException
   * @throws ModelOverdeterminedException
   */
  public SBMLinterpreter(Model model, double defaultSpeciesValue, double defaultParameterValue,
      double defaultCompartmentValue, Map<String, Boolean> amountHash)
      throws SBMLException, ModelOverdeterminedException {
    super(model);
    init(true, defaultSpeciesValue, defaultParameterValue, defaultCompartmentValue, amountHash);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double[] getAdditionalValues(double t, double[] Y)
      throws DerivativeException {
    if ((t - currentTime > 1E-15) || ((Y != this.Y) && !Arrays.equals(Y, this.Y)) || (t == 0)) {
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
   * {@inheritDoc}
   */
  @Override
  public EventInProgress getNextEventAssignments(double t, double previousTime, double[] Y)
      throws DerivativeException {
    if (!modelHasEvents) {
      return null;
    }
    // change Y because of different priorities and reevaluation of
    // trigger/priority after the execution of events
    System.arraycopy(Y, 0, this.Y, 0, Y.length);
    currentTime = t;
    Double priority, execTime = 0d;
    astNodeTime += 0.01;
    Double[] triggerTimeValues;
    Event ev;
    int i = 0, index;
    Boolean persistent, aborted;
    boolean hasNewDelayedEvents = false;
    try {
      // recheck trigger of events that have fired for this point in time
      // but have not been executed yet
      priorities.clear();
      while (i < runningEvents.size()) {
        index = runningEvents.get(i);
        //ev = model.getEvent(index);
        if (!events[index].hasMoreAssignments(currentTime)) {
          runningEvents.remove(i);
          continue;
        }
        persistent = events[index].getPersistent();
        if (!persistent) {
          if (events[index].getTriggerObject().compileDouble(astNodeTime, 0d) == 0d) {
            runningEvents.remove(i);
            events[index].aborted(currentTime);
            i--;
          } else {
            ASTNodeValue priorityObject = events[index].getPriorityObject();
            if (priorityObject != null) {
              events[index].changePriority(priorityObject.compileDouble(astNodeTime, 0d));
              priorities.add(events[index].getPriority());
            }
          }
        } else {
          ASTNodeValue priorityObject = events[index].getPriorityObject();
          if (priorityObject != null) {
            events[index].changePriority(priorityObject.compileDouble(astNodeTime, 0d));
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
        if (events[index].getLastTimeFired() > currentTime) {
          delayedEvents.remove(i);
          events[index].refresh(currentTime);
          i--;
          aborted = true;
        } else if ((events[index].getLastTimeFired() <= currentTime) && (
            events[index].getLastTimeExecuted() > previousTime) && (
            events[index].getLastTimeExecuted() != currentTime)) {
          events[index].refresh(previousTime);
        }
        persistent = ev.getTrigger().getPersistent();
        if (!persistent && !aborted) {
          if (events[index].getTriggerObject().compileDouble(astNodeTime, 0d) == 0d) {
            //delayedEvents.remove(i);
            events[index].aborted(currentTime);
            //i--;
            aborted = true;
          }
        }
        if ((events[index].hasExecutionTime()) && (events[index].getTime() <= currentTime)
            && !aborted) {
          if (ev.getPriority() != null) {
            priority = events[index].getPriorityObject().compileDouble(astNodeTime, 0d);
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
        if (events[i] != null) {
          if (events[i].getTriggerObject().compileDouble(astNodeTime, 0d) != 0d) {
            // event has not fired recently -> can fire
            if (!events[i].getFireStatus(currentTime)) {
              execTime = currentTime;
              // event has a delay
              ASTNodeValue delayObject = events[i].getDelayObject();
              if (delayObject != null) {
                execTime += delayObject.compileDouble(astNodeTime, 0d);
                if (!delayedEvents.contains(i)) {
                  delayedEvents.add(i);
                }
                hasNewDelayedEvents = true;
              } else {
                ASTNodeValue priorityObject = events[i].getPriorityObject();
                if (priorityObject != null) {
                  priority = events[i].getPriorityObject().compileDouble(astNodeTime, 0d);
                  priorities.add(priority);
                  events[i].changePriority(priority);
                }
                runningEvents.add(i);
              }
              triggerTimeValues = null;
              if (events[i].getUseValuesFromTriggerTime()) {
                // store values from trigger time for later
                // execution
                List<AssignmentRuleValue> ruleObjects = events[i].getRuleObjects();
                if (ruleObjects != null) {
                  triggerTimeValues = new Double[ruleObjects.size()];
                  int j = 0;
                  for (AssignmentRuleValue obj : ruleObjects) {
                    obj.processRule(Y, astNodeTime, false);
                    triggerTimeValues[j] = obj.getValue();
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
            if (events[i].getFireStatus(currentTime)) {
              events[i].recovered(currentTime);
            }
          }
        }
      }
      // there are events to fire
      if (runningEvents.size() > 0) {
        return processNextEvent(priorities, this.Y);
      }
      // return empty event, so the solver knows that a event with delay has been triggered
      else if (hasNewDelayedEvents) {
        events[0].clearAssignments();
        return events[0];
      } else {
        return null;
      }
    } catch (SBMLException exc) {
      throw new DerivativeException(exc);
    }
  }


  /**
   * Returns the value of the ODE system at the time t given the current values of Y
   *
   * @param time
   * @param Y
   * @return
   * @throws DerivativeException
   */
  private double[] computeDerivatives(double time, double[] Y)
      throws DerivativeException {
    // create a new array with the same size of Y where the rate of change
    // is stored for every symbol in the simulation
    double[] changeRate = new double[Y.length];
    computeDerivatives(time, Y, changeRate);
    return changeRate;
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public void computeDerivatives(double time, double[] Y, double[] changeRate)
      throws DerivativeException {
    currentTime = time;
    // make sure not to have invalid older values in the change rate
    //Arrays.fill(changeRate, 0d);
    Arrays.fill(changeRate, 0d);
    if (noDerivatives) {
      return;
    }
    System.arraycopy(Y, 0, this.Y, 0, Y.length);
    if (modelHasEvents) {
      runningEvents.clear();
    }
    try {
      //Always call the compile functions with a new time
      astNodeTime += 0.01d;

      /*
       * Compute changes due to rules
       */
      processRules(astNodeTime, changeRate, this.Y, false);

      /*
       * Compute changes due to reactions
       */
      processVelocities(changeRate, astNodeTime);

      /*
       * Check the model's constraints
       */
      checkConstraints(time);
    } catch (SBMLException exc) {
      throw new DerivativeException(exc);
    }
    this.changeRate = changeRate;
  }


  /**
   * <p>
   * This method initializes the differential equation system for simulation. In more detail: the
   * initial amounts or concentration will be assigned to every {@link Species} or {@link
   * InitialAssignment}s if any are executed.
   * </p>
   * <p>
   * To save computation time the results of this method should be stored in an array. Hence this
   * method must only be called once. However, if the SBML model to be simulated contains initial
   * assignments, this can lead to wrong simulation results because initial assignments may depend
   * on current parameter values.
   * </p>
   *
   * @throws ModelOverdeterminedException
   * @throws SBMLException
   * @see #init(boolean, double, double, double)
   */
  public void init() throws ModelOverdeterminedException, SBMLException {
    init(true, 0d, 1d, 1d);
  }


  /**
   * This method initializes the differential equation system for simulation. The user can tell
   * whether the tree of {@link ASTNode}s has to be refreshed.
   *
   * @param refreshTree
   * @throws ModelOverdeterminedException
   * @throws SBMLException
   */
  public void init(boolean refreshTree)
      throws ModelOverdeterminedException, SBMLException {
    init(refreshTree, 0d, 1d, 1d);
  }


  /**
   * This method initializes the differential equation system for simulation. The user can tell
   * whether the tree of {@link ASTNode}s has to be refreshed and give some default values.
   *
   * @param renewTree
   * @param defaultSpeciesValue
   * @param defaultParameterValue
   * @param defaultCompartmentValue
   * @throws ModelOverdeterminedException
   * @throws SBMLException
   */
  public void init(boolean renewTree, double defaultSpeciesValue, double defaultParameterValue,
      double defaultCompartmentValue)
      throws ModelOverdeterminedException, SBMLException {
    init(renewTree, defaultSpeciesValue, defaultParameterValue, defaultCompartmentValue, null);
  }


  /**
   * This method initializes the differential equation system for simulation. The user can tell
   * whether the tree of {@link ASTNode}s has to be refreshed, give some default values and state
   * whether a {@link Species} is seen as an amount or a concentration.
   *
   * @param renewTree
   * @param defaultSpeciesValue
   * @param defaultParameterValue
   * @param defaultCompartmentValue
   * @param amountHash
   * @throws ModelOverdeterminedException
   * @throws SBMLException
   */
  public void init(boolean renewTree, double defaultSpeciesValue, double defaultParameterValue,
      double defaultCompartmentValue, Map<String, Boolean> amountHash)
      throws ModelOverdeterminedException, SBMLException {
    super.init(renewTree, defaultSpeciesValue, defaultParameterValue, defaultCompartmentValue,
        amountHash);
    /*
     * Initial assignments
     */
    astNodeTime += 0.01d;
    processInitialAssignments(astNodeTime, Y);

    /*
     * Sometimes conversion factors are assigned values in the
     * initialAssignments. So, updating the conversion factors
     * after processing the initialAssignments.
     */
    for (int pp = 0; pp < model.getSpeciesCount(); pp++) {
      Species sp = model.getSpecies(pp);
      String conversionFactor = sp.getConversionFactor();
      if (conversionFactor == null) {
        conversionFactor = model.getConversionFactor();
      }
      if (!conversionFactor.equals("")) {
        conversionFactors[symbolHash.get(sp.getId())] = Y[symbolHash.get(conversionFactor)];
      }
    }

    /*
     * Compute changes due to reactions
     */
    processVelocities(changeRate, astNodeTime);

    /*
     * All other rules
     */
    astNodeTime += 0.01d;
    processRules(astNodeTime, null, Y, true);

    /*
     * Process initial assignments and rules till the Y array
     * becomes unchanged on running further initial assignments and rules.
     *
     * Reason: Initial assignments and rules can be dependent on each other.
     */
    double[] check;
    do {
      check = Y.clone();
      astNodeTime += 0.01d;
      processInitialAssignments(astNodeTime, Y);
      astNodeTime += 0.01d;
      processRules(astNodeTime, null, Y, true);
    } while (!Arrays.equals(check, Y));
    // save the initial values of this system
    System.arraycopy(Y, 0, initialValues, 0, initialValues.length);
  }


  /**
   * {@inheritDoc}
   */
  @Override
  public boolean processAssignmentRules(double t, double[] Y)
      throws DerivativeException {
    currentTime = t;
    astNodeTime += 0.01d;
    System.arraycopy(Y, 0, this.Y, 0, Y.length);
    boolean changed = processRules(t, null, this.Y, false);
    System.arraycopy(this.Y, 0, Y, 0, Y.length);
    return changed;
  }


  /**
   * This method creates assignments from the events currently stored in the associated HashMap with
   * respect to their priority.
   *
   * @param priorities the priorities
   * @param Y          the Y vector
   * @return the event with assignments
   */
  private SBMLEventInProgress processNextEvent(HashSet<Double> priorities, double[] Y)
      throws DerivativeException {
    Integer symbolIndex;
    double newVal, highestPriority = -1;
    int index;
    // check if more than one event has a priority set at this point in time
    highOrderEvents.clear();
    if (!priorities.isEmpty()) {
      boolean first = true;
      for (double priority : priorities) {
        if (first) {
          first = false;
          highestPriority = priority;
        } else {
          highestPriority = Math.max(highestPriority, priority);
        }
      }
      //      array = priorities.toArray(new Double[priorities.size()]);
      //      Arrays.sort(array);
      //      highestPriority = array[array.length - 1];
      // get event with the current highest priority
      for (int i = 0; i < runningEvents.size(); i++) {
        if (events[runningEvents.get(i)].getPriority() == highestPriority) {
          highOrderEvents.add(runningEvents.get(i));
        }
      }
      // pick one event randomly, as a matter of fact remove all event
      // except the picked one
      if (highOrderEvents.size() > 1) {
        pickRandomEvent(highOrderEvents);
      }
    } else {
      highOrderEvents.addAll(runningEvents);
      if (highOrderEvents.size() > 1) {
        pickRandomEvent(highOrderEvents);
      }
    }
    runningEvents.remove(highOrderEvents.get(0));
    try {
      // execute the events chosen for execution
      index = highOrderEvents.get(0);
      events[index].clearAssignments();
      // event does not use values from trigger time
      if (!events[index].getUseValuesFromTriggerTime()) {
        for (AssignmentRuleValue obj : events[index].getRuleObjects()) {
          obj.processRule(Y, astNodeTime, false);
          newVal = obj.getValue();
          symbolIndex = obj.getIndex();

          if (symbolIndex >= 0) {
            if (compartmentHash.containsValue(symbolIndex)) {
              updateSpeciesConcentrationByCompartmentChange(symbolIndex, Y, Y[symbolIndex], newVal,
                  -1);
            }
            events[index].addAssignment(symbolIndex, newVal);
          }
        }
      } else {
        // event uses values from trigger time -> get stored values
        // from the HashMap
        Double[] triggerTimeValues = events[index].getValues();
        if (events[index].getRuleObjects() != null) {
          int j = 0;
          for (AssignmentRuleValue obj : events[index].getRuleObjects()) {
            newVal = triggerTimeValues[j];
            symbolIndex = obj.getIndex();
            if (symbolIndex >= 0) {
              if (compartmentHash.containsValue(symbolIndex)) {
                updateSpeciesConcentrationByCompartmentChange(symbolIndex, Y, Y[symbolIndex],
                    newVal, index);
              }
              events[index].addAssignment(symbolIndex, newVal);
            } else {
              String id = obj.getSpeciesReferenceID();
              if (id != null) {
                stoichiometricCoefHash.put(id, newVal);
              }
            }
            j++;
          }
        }
      }
      events[index].executed(currentTime);
    } catch (SBMLException exc) {
      throw new DerivativeException(exc);
    }
    return events[index];
  }


  /**
   * Processes the initial assignments
   *
   * @param time the {@link ASTNode} time
   * @param Y    the Y vector
   * @throws SBMLException
   */
  public void processInitialAssignments(double time, double[] Y)
      throws SBMLException {
    if (Y != null) {
      for (int i = 0; i != initialAssignmentRoots.size(); i++) {
        initialAssignmentRoots.get(i).processRule(Y, time, true);
      }
    }
  }


  /**
   * Processes the rules
   *
   * @param time                the current time
   * @param changeRate          the changeRate vector
   * @param Y                   the Y vector
   * @param initialCalculations
   * @return flag that is true if there has been some change caused by any rule
   * @throws SBMLException
   */
  public boolean processRules(double time, double[] changeRate, double[] Y,
      boolean initialCalculations)
      throws SBMLException {
    boolean changeByAssignmentRules = false;
    double intermediateASTNodeTime = -astNodeTime;
    double oldTime = currentTime;
    if (Y != null) {
      for (int n = 0; n != numberOfAssignmentRulesLoops; n++) {
        if (!delaysIncluded) {
          System.arraycopy(Y, 0, oldY2, 0, Y.length);
        } else {
          System.arraycopy(Y, 0, oldY, 0, Y.length);
        }
        intermediateASTNodeTime = -intermediateASTNodeTime;
        for (int i = 0; i != nAssignmentRules; i++) {
          AssignmentRuleValue currentRuleObject = assignmentRulesRoots.get(i);
          double oldValue = Double.NaN, newValue = Double.NaN;
          if (!delaysIncluded) {
            System.arraycopy(Y, 0, oldY2, 0, Y.length);
          } else if (containsDelays) {
            System.arraycopy(Y, 0, oldY, 0, Y.length);
          }
          int index = currentRuleObject.getIndex();
          if (index >= 0) {
            oldValue = Y[index];
          }
          boolean currentChange = currentRuleObject.processRule(Y, intermediateASTNodeTime, true);
          currentTime = oldTime;
          if (index >= 0) {
            newValue = Y[index];
          }
          if (!delaysIncluded) {
            System.arraycopy(oldY2, 0, Y, 0, Y.length);
          } else if (containsDelays) {
            System.arraycopy(oldY, 0, Y, 0, Y.length);
          }
          if (index != -1) {
            Y[index] = newValue;
          }
          if (currentChange && (!initialCalculations) && (index >= 0) && (compartmentHash
              .containsValue(index))) {
            updateSpeciesConcentrationByCompartmentChange(index, Y, oldValue, newValue, -1);
          } else if (currentChange && initialCalculations && (index >= 0) && (compartmentHash
              .containsValue(index))) {
            refreshSpeciesAmount(index, Y, oldValue, newValue);
          }
          changeByAssignmentRules = changeByAssignmentRules || currentChange;
        }
      }
    }
    /*
     * Compute changes due to rules
     */
    if (changeRate != null) {
      for (int i = 0; i != nRateRules; i++) {
        rateRulesRoots.get(i).processRule(changeRate, this.Y, astNodeTime);
      }
    }
    return changeByAssignmentRules;
  }

  public void computeDerivativeWithChangingCompartment(Species sp, double[] changeRate) {

    double latestSpeciesValue = latestTimePointResult[symbolHash.get(sp.getId())];
    double latestCompartmentValue = latestTimePointResult[symbolHash.get(sp.getCompartment())];

    String speciesId = sp.getId();
    String compartmentId = sp.getCompartment();

    changeRate[symbolHash.get(compartmentId)] = rateRulesRoots.get(rateRuleHash.get(compartmentId))
        .getNodeObject().compileDouble(astNodeTime, 0d);
    latestCompartmentValue =
        latestCompartmentValue + (latestTimePoint - previousTimePoint) * changeRate[symbolHash
            .get(sp.getCompartment())];

    changeRate[symbolHash.get(speciesId)] = rateRulesRoots.get(rateRuleHash.get(speciesId))
        .getNodeObject().compileDouble(astNodeTime, 0d);

    double a1 =
        (latestSpeciesValue / latestCompartmentValue) * changeRate[symbolHash.get(compartmentId)];
    double a2 = latestCompartmentValue * changeRate[symbolHash.get(speciesId)];

    changeRate[symbolHash.get(speciesId)] = a1 + a2;

  }


  /**
   * This method computes the multiplication of the stoichiometric matrix of the given model system
   * with the reaction velocities vector passed to this method. Note, the stoichiometric matrix is
   * only constructed implicitly by running over all reactions and considering all participating
   * reactants and products with their according stoichiometry or stoichiometric math.
   *
   * @param changeRate An array containing the rates of change for each species in the model system
   *                   of this class.
   * @param time
   * @throws SBMLException
   */
  protected void processVelocities(double[] changeRate, double time)
      throws SBMLException {
    // Velocities of each reaction.
    for (int reactionIndex = 0; reactionIndex != v.length; reactionIndex++) {
      if (hasFastReactions) {
        if (isProcessingFastReactions == reactionFast[reactionIndex]) {
          v[reactionIndex] = kineticLawRoots[reactionIndex].compileDouble(time, 0d);
        } else {
          v[reactionIndex] = 0;
        }
      } else {
        v[reactionIndex] = kineticLawRoots[reactionIndex].compileDouble(time, 0d);
      }
    }
    for (int i = 0; i != stoichiometryValues.length; i++) {
      if (!constantStoichiometry[i] || !stoichiometrySet[i]) {
        stoichiometry[i] = stoichiometryValues[i].compileDouble(time);
        stoichiometrySet[i] = stoichiometryValues[i].getStoichiometrySet();
      }
      double value;
      if (zeroChange[i]) {
        value = 0;
      } else if (isReactant[i]) {
        value = -1 * stoichiometry[i] * v[reactionIndex[i]];
      } else {
        value = stoichiometry[i] * v[reactionIndex[i]];
      }
      changeRate[speciesIndex[i]] += value;
    }
    for (int i = 0; i != changeRate.length; i++) {
      // When the unit of reacting species is given mol/volume
      // then it has to be considered in the change rate that should
      // always be only in mol/time
      if (inConcentrationValues[i]) {
        changeRate[i] = changeRate[i] / Y[compartmentIndexes[i]];
      }
      changeRate[i] *= conversionFactors[i];
    }
    for (int i = 0; i < nRateRules; i++) {
      changeRate[symbolHash
          .get(rateRulesRoots.get(i).getVariable())] /= conversionFactors[symbolHash
          .get(rateRulesRoots.get(i).getVariable())];
    }
  }


  /**
   * This method allows us to set the parameters of the model to the specified values in the given
   * array.
   *
   * @param params An array of parameter values to be set for this model. If the number of given
   *               parameters does not match the number of model parameters, an exception will be
   *               thrown.
   */
  // TODO changing the model directly not allowed / does this method still
  // make sense?
  public void setParameters(double[] params) {
    // TODO consider local parameters as well.
    // if (params.length != model.getParameterCount())
    // throw new IllegalArgumentException(
    // "The number of parameters passed to this method must "
    // + "match the number of parameters in the model.");
    int paramNum, reactionNum, localPnum;
    for (paramNum = 0; paramNum < model.getParameterCount(); paramNum++) {
      model.getParameter(paramNum).setValue(params[paramNum]);
    }
    boolean updateSyntaxGraph = false;
    for (reactionNum = 0; (reactionNum < model.getReactionCount()) && (paramNum < params.length);
        reactionNum++) {
      KineticLaw law = model.getReaction(reactionNum).getKineticLaw();
      if (law != null) {
        for (localPnum = 0;
            (localPnum < law.getLocalParameterCount()) && (paramNum < params.length); localPnum++) {
          law.getLocalParameter(localPnum).setValue(params[paramNum++]);
          updateSyntaxGraph = true;
        }
        law.getMath()
            .updateVariables(); // make sure references to local parameter values are reflected in the ASTNode
      } else {
        logger.log(Level.FINE,
            "Cannot set local parameters for reaction {0} because of missing kinetic law.",
            model.getReaction(reactionNum).getId());
      }
    }
    if ((model.getInitialAssignmentCount() > 0) || (model.getEventCount() > 0)) {
      try {
        init();
      } catch (Exception exc) {
        // This can never happen
        logger
            .log(Level.WARNING, "Could not re-initialize the model with the new parameter values.",
                exc);
      }
    } else {
      int nCompPlusSpec = model.getCompartmentCount() + model.getSpeciesCount();
      for (int i = nCompPlusSpec; i < nCompPlusSpec + model.getParameterCount(); i++) {
        initialValues[i] = params[i - nCompPlusSpec];
      }
    }
    if (updateSyntaxGraph) {
      refreshSyntaxTree();
    }
  }

  /**
   * Updates the concentration of species due to a change in the size of their compartment (also at
   * events)
   *
   * @param compartmentIndex    the index of the compartment
   * @param Y                   the Y vector
   * @param oldCompartmentValue the old value of the compartment
   * @param newCompartmentValue the new value of the compartment
   * @param eventIndex
   */
  private void updateSpeciesConcentrationByCompartmentChange(int compartmentIndex, double[] Y,
      double oldCompartmentValue, double newCompartmentValue, int eventIndex) {
    int speciesIndex;
    for (Entry<String, Integer> entry : compartmentHash.entrySet()) {
      if (entry.getValue() == compartmentIndex) {
        speciesIndex = symbolHash.get(entry.getKey());
        if ((!isAmount[speciesIndex]) && (!speciesMap.get(symbolIdentifiers[speciesIndex])
            .getConstant())) {
          Y[speciesIndex] = (Y[speciesIndex] * oldCompartmentValue) / newCompartmentValue;
          if (eventIndex != -1) {
            events[eventIndex].addAssignment(speciesIndex, Y[speciesIndex]);
          }
        }
      }
    }
  }

  /**
   * Updates the changeRate of species due to a change in the size of their compartment (by
   * RateRule)
   *
   * @param compartmentIndex
   * @param changeRate
   */
  private void updateSpeciesConcentrationByCompartmentRateRule(int compartmentIndex,
      double[] changeRate) {
    int speciesIndex;
    for (Entry<String, Integer> entry : compartmentHash.entrySet()) {
      if (entry.getValue() == compartmentIndex) {
        speciesIndex = symbolHash.get(entry.getKey());
        if ((!isAmount[speciesIndex]) && (!speciesMap.get(symbolIdentifiers[speciesIndex])
            .getConstant())) {
          changeRate[speciesIndex] =
              -changeRate[compartmentIndex] * Y[speciesIndex] / Y[compartmentIndex];
        }
      }
    }
  }

  /**
   * Updates the species concentration as per the updated values in the AmountManager.
   *
   * @param amountManager
   */
  public void updateSpeciesConcentration(AmountManager amountManager) {
    for (int i = 0; i < model.getSpeciesCount(); i++) {
      Y[symbolHash.get(model.getSpecies(i).getId())] = amountManager.getAmount(i);
    }
  }

  /**
   * Updates the amount of a species due to a change in the size of their compartment caused by an
   * assignment rule overwriting the initial value
   *
   * @param compartmentIndex
   * @param Y
   * @param oldCompartmentValue
   * @param newCompartmentValue
   */
  private void refreshSpeciesAmount(int compartmentIndex, double[] Y, double oldCompartmentValue,
      double newCompartmentValue) {
    int speciesIndex;
    for (Entry<String, Integer> entry : compartmentHash.entrySet()) {
      if (entry.getValue() == compartmentIndex) {
        speciesIndex = symbolHash.get(entry.getKey());
        if ((isAmount[speciesIndex]) && (speciesMap.get(symbolIdentifiers[speciesIndex])
            .isSetInitialConcentration())) {
          Y[speciesIndex] = (Y[speciesIndex] / oldCompartmentValue) * newCompartmentValue;
        }
      }
    }
  }

  /**
   * @param reactionIndex index of the reaction
   * @return the current reaction velocity of a specific reaction
   */
  public double compileReaction(int reactionIndex) {
    astNodeTime += 0.01d;
    return kineticLawRoots[reactionIndex].compileDouble(astNodeTime, 0d);
  }

}
