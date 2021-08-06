package org.simulator.sbml;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.AssignmentRule;
import org.sbml.jsbml.CallableSBase;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Constraint;
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
import org.sbml.jsbml.Symbol;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.validator.OverdeterminationValidator;
import org.simulator.math.odes.DESystem;
import org.simulator.math.odes.DelayValueHolder;
import org.simulator.math.odes.DelayedDESystem;
import org.simulator.math.odes.EventDESystem;
import org.simulator.math.odes.FastProcessDESystem;
import org.simulator.math.odes.RichDESystem;
import org.simulator.sbml.astnode.ASTNodeInterpreter;
import org.simulator.sbml.astnode.ASTNodeValue;
import org.simulator.sbml.astnode.AssignmentRuleValue;
import org.simulator.sbml.astnode.CompartmentOrParameterValue;
import org.simulator.sbml.astnode.DivideValue;
import org.simulator.sbml.astnode.FunctionValue;
import org.simulator.sbml.astnode.IntegerValue;
import org.simulator.sbml.astnode.LocalParameterValue;
import org.simulator.sbml.astnode.MinusValue;
import org.simulator.sbml.astnode.NamedValue;
import org.simulator.sbml.astnode.PlusValue;
import org.simulator.sbml.astnode.PowerValue;
import org.simulator.sbml.astnode.RateRuleValue;
import org.simulator.sbml.astnode.ReactionValue;
import org.simulator.sbml.astnode.RootFunctionValue;
import org.simulator.sbml.astnode.SpeciesReferenceValue;
import org.simulator.sbml.astnode.SpeciesValue;
import org.simulator.sbml.astnode.StoichiometryValue;
import org.simulator.sbml.astnode.TimesValue;

/**
 *
 * @author Hemil Panchiwala
 * @author Andreas Dr&auml;ger
 *
 */
public abstract class EquationSystem
implements SBMLValueHolder, DelayedDESystem, EventDESystem,
FastProcessDESystem, RichDESystem, PropertyChangeListener {

  /**
   * Generated serial version identifier.
   */
  private static final long serialVersionUID = -5059388953358396837L;

  /**
   * A {@link Logger}.
   */
  private static final transient Logger logger = Logger.getLogger(EquationSystem.class.getName());

  /**
   * Key to memorize user objects in {@link ASTNode}
   */
  public static final String TEMP_VALUE = "SBML_SIMULATION_TEMP_VALUE";

  /**
   * Contains a list of all algebraic rules transformed to assignment rules for further processing
   */
  protected List<AssignmentRule> algebraicRules;

  /**
   * Hashes the id of all species located in a compartment to the position of their compartment in
   * the Y vector. When a species has no compartment, it is hashed to null.
   */
  protected Map<String, Integer> compartmentHash;

  /**
   * This field is necessary to also consider local parameters of the current reaction because it is
   * not possible to access these parameters from the model. Hence we have to memorize an additional
   * reference to the Reaction and thus to the list of these parameters.
   */
  protected Reaction currentReaction;

  /**
   * Holds the current time of the simulation
   */
  protected double currentTime;

  /**
   * This array stores for every event an object of {@link SBMLEventInProgress} that is used to
   * handle event processing during simulation
   */
  protected SBMLEventInProgress[] events;

  /**
   * This set stores the priorities of the currently processed events.
   */
  protected HashSet<Double> priorities;

  /**
   * An array, which stores all computed initial values of the model. If this model does not contain
   * initial assignments, the initial values will only be taken once from the information stored in
   * the model. Otherwise they have to be computed again as soon as the parameter values of this
   * model are changed, because the parameters may influence the return values of the initial
   * assignments.
   */
  protected double[] initialValues;

  /**
   * A {@link List} of {@link ConstraintListener}, which deal with violation of {@link Constraint}s
   * during simulation.
   */
  protected List<ConstraintListener> listOfConstraintListeners;

  /**
   * An array that stores derivatives of each species in the model system at current time.
   */
  public double[] changeRate;

  /**
   * The model to be simulated.
   */
  protected Model model;

  /**
   * Hashes the id of all {@link Compartment}s, {@link Species}, global {@link Parameter}s, and, if
   * necessary, {@link SpeciesReference}s in {@link RateRule}s to an value object which contains the
   * position in the {@link #Y} vector
   */
  protected Map<String, Integer> symbolHash;

  /**
   * Hashes the id of all {@link Compartment}s, {@link Species}, global {@link Parameter}s, and, if
   * necessary, {@link SpeciesReference}s in {@link RateRule}s to an boolean object which contains
   * whether it is constant or not
   */
  protected Map<String, Boolean> constantHash;

  /**
   * An array of strings that memorizes at each position the identifier of the corresponding element
   * in the Y array.
   */
  protected String[] symbolIdentifiers;

  /**
   * An array of the velocities of each reaction within the model system. Holding this globally
   * saves many new memory allocations during simulation time.
   */
  protected double[] v;

  /**
   * This {@link Map} saves the current stoichiometric coefficients for those {@link
   * SpeciesReference} objects that are a target to an Assignment .
   */
  protected Map<String, Double> stoichiometricCoefHash;

  /**
   * An array of the state variables within the model including species and parameters.
   */
  protected double[] Y;

  /**
   * A boolean indicating whether the solver is currently processing fast reactions or not
   */
  protected boolean isProcessingFastReactions = false;

  /**
   * A boolean indicating whether a model has fast reactions or not.
   */
  protected boolean hasFastReactions = false;

  /**
   * Stores the indices of the {@link Event}s triggered for the current point in time.
   */
  protected List<Integer> runningEvents;

  /**
   * Stores the indices of the events triggered for a future point in time.
   */
  protected List<Integer> delayedEvents;

  /**
   * Map for faster access to species.
   */
  protected Map<String, Species> speciesMap;

  /**
   * {@link Species} with the unit given in mol/volume for which it has to be considered that the
   * change rate should always be only in mol/time
   */
  protected Set<String> inConcentration;

  /**
   * List of kinetic laws given as ASTNodeObjects
   */
  protected ASTNodeValue[] kineticLawRoots;

  /**
   * List of constraints given as {@link ASTNodeValue} objects
   */
  protected List<ASTNodeValue> constraintRoots;

  /**
   * List of all occurring {@link ASTNode}s
   */
  protected List<ASTNode> nodes;

  /**
   * Node interpreter taking the time into consideration
   */
  protected ASTNodeInterpreter nodeInterpreter;

  /**
   * List of all occuring stoichiometries
   */
  protected StoichiometryValue[] stoichiometryValues;

  /**
   * Array that stores which reactions are fast
   */
  protected boolean[] reactionFast;

  /**
   * Array that stores which reactions are reversible
   */
  protected boolean[] reactionReversible;

  /**
   * List of the assignment rules (as AssignmentRuleObjects)
   */
  protected List<AssignmentRuleValue> assignmentRulesRoots;

  /**
   * List of the rate rules (as RateRuleObjects)
   */
  protected List<RateRuleValue> rateRulesRoots;

  /**
   * Map for getting the raterule index (in the rateRulesRoots ArrayList) of particular id
   */
  protected Map<String, Integer> rateRuleHash;

  /**
   * Current time for the ASTNode processing (not equal to the simulation time!)
   */
  protected double astNodeTime;

  /**
   * Value holder for computation of delayed values
   */
  protected DelayValueHolder delayValueHolder;

  /**
   * List which is used for choosing the next event to process
   */
  protected List<Integer> highOrderEvents;

  /**
   * Array of the conversionFactors given (default value: 1)
   */
  protected double[] conversionFactors;

  /**
   * Flag which stores whether the model contains any events
   */
  protected boolean modelHasEvents;

  /**
   * Number of rate rules
   */
  protected int nRateRules;

  /**
   * Number of assignment rules
   */
  protected int nAssignmentRules;

  /**
   * List of the {@link InitialAssignment} (as {@link AssignmentRuleValue} objects)
   */
  protected List<AssignmentRuleValue> initialAssignmentRoots;

  /**
   * Flag which is true if no changes (in rate rules and kinetic laws) occur in the model
   */
  protected boolean noDerivatives;

  /**
   * Array that shows whether a division by the compartment size is necessary after computation of
   * the derivatives.
   */
  protected boolean[] inConcentrationValues;

  /**
   * Contains the compartment indexes of the species in the Y vector.
   */
  protected int[] compartmentIndexes;

  /**
   * Are the stoichiometries in the stoichiometry values set?
   */
  protected boolean[] stoichiometrySet;

  /**
   * Are the stoichiometries in the stoichiometry values constant?
   */
  protected boolean[] constantStoichiometry;

  /**
   * Is the stoichiometry value referring to a species whose value does not change?
   */
  protected boolean[] zeroChange;

  /**
   * Is the stoichiometry value referring to a reactant?
   */
  protected boolean[] isReactant;

  /**
   * The indices of the reactions the stoichiometry values are referring to
   */
  protected int[] reactionIndex;

  /**
   * The species indices of the stoichiometry values
   */
  protected int[] speciesIndex;

  /**
   * The current stoichiometries of the stoichiometry values
   */
  protected double[] stoichiometry;

  /**
   * Is the SBase in the Y vector an amount?
   */
  protected boolean[] isAmount;

  /**
   * Are delays included in the computation?
   */
  protected boolean delaysIncluded;

  /**
   * The number of repetitions for the processing of assignment rules
   */
  protected int numberOfAssignmentRulesLoops;

  /**
   * Array for saving older Y values
   */
  protected double[] oldY;

  /**
   * Array for saving older Y values (when computing delayed values)
   */
  protected double[] oldY2;

  protected boolean containsDelays;

  /**
   * The value of the latest time point
   */
  protected double latestTimePoint;

  /**
   * The value of the previous time point
   */
  protected double previousTimePoint;

  /**
   * An array of the concentration of each species at latest processed time point within the model
   * system.
   */
  protected double[] latestTimePointResult;

  /**
   * Property name for getting the latest result processed.
   */
  private static final String RESULT = "result";

  public EquationSystem(Model model) {
    this.model = model;
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
        throws ModelOverdeterminedException {

    v = new double[this.model.getNumReactions()];
    symbolHash = new HashMap<>();
    constantHash = new HashMap<>();
    compartmentHash = new HashMap<>();
    stoichiometricCoefHash = new HashMap<>();
    speciesMap = new HashMap<>();
    rateRuleHash = new HashMap<>();

    priorities = new HashSet<>();
    inConcentration = new HashSet<>();

    currentTime = 0d;
    astNodeTime = 0d;
    latestTimePoint = 0d;

    reactionFast = new boolean[this.model.getReactionCount()];
    reactionReversible = new boolean[this.model.getReactionCount()];
    highOrderEvents = new LinkedList<>();
    listOfConstraintListeners = new LinkedList<>();
    nodes = new LinkedList<>();

    delaysIncluded = true;
    containsDelays = false;
    noDerivatives = false;

    nodeInterpreter = new ASTNodeInterpreter(this);

    int i;
    int compartmentIndex, yIndex = 0;

    if ((model.getReactionCount() == 0) && (constraintRoots == null)) {
      noDerivatives = true;
      for (int k = 0; k < model.getRuleCount(); k++) {
        Rule rule = model.getRule(k);
        if (rule.isRate()) {
          noDerivatives = false;
        }
      }
    }

    Map<String, Integer> speciesReferenceToRateRule = new HashMap<>();
    int speciesReferencesInRateRules = 0;
    for (int k = 0; k < model.getRuleCount(); k++) {
      Rule rule = model.getRule(k);
      if (rule.isRate()) {
        RateRule rr = (RateRule) rule;
        SpeciesReference sr = model.findSpeciesReference(rr.getVariable());
        if ((sr != null) && !sr.isConstant()) {
          speciesReferencesInRateRules++;
          speciesReferenceToRateRule.put(sr.getId(), k);
        }
      }
    }

    int sizeY = model.getCompartmentCount() + model.getSpeciesCount() + model.getParameterCount()
    + speciesReferencesInRateRules;
    Y = new double[sizeY];
    oldY = new double[sizeY];
    oldY2 = new double[sizeY];
    changeRate = new double[sizeY];
    isAmount = new boolean[sizeY];
    compartmentIndexes = new int[sizeY];
    conversionFactors = new double[sizeY];
    inConcentrationValues = new boolean[sizeY];
    Arrays.fill(conversionFactors, 1d);
    symbolIdentifiers = new String[sizeY];
    initialValues = new double[sizeY];
    latestTimePointResult = new double[sizeY];

    /*
     * Save starting values of the model's compartment in Y
     */
    for (i = 0; i < model.getCompartmentCount(); i++) {
      Compartment c = model.getCompartment(i);
      if (!c.isSetValue()) {
        Y[yIndex] = defaultCompartmentValue;
      } else {
        Y[yIndex] = c.getSize();
      }
      symbolHash.put(c.getId(), yIndex);
      constantHash.put(c.getId(), c.isConstant());
      symbolIdentifiers[yIndex] = c.getId();
      yIndex++;
    }

    // Due to unset initial amount or concentration of species try to set
    // one of them
    Species majority = determineMajorSpeciesAttributes();

    /*
     * Save starting values of the model's species in Y and link them with their
     * compartment
     */
    for (i = 0; i < model.getSpeciesCount(); i++) {
      Species s = model.getSpecies(i);
      speciesMap.put(s.getId(), s);
      if (!s.getBoundaryCondition() && !s.isConstant()) {
        Parameter convParameter = s.getConversionFactorInstance();
        if (convParameter == null) {
          convParameter = model.getConversionFactorInstance();
        }
        if (convParameter != null) {
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
      //determine whether amount or concentration is set
      if ((amountHash != null)) {
        if (amountHash.containsKey(s.getId())) {
          isAmount[yIndex] = amountHash.get(s.getId());
        } else {
          isAmount[yIndex] = s.isSetInitialAmount();
        }
      } else {
        isAmount[yIndex] = s.isSetInitialAmount();
      }
      if (!s.isSetValue()) {
        Y[yIndex] = defaultSpeciesValue;
      } else {
        if (s.isSetInitialAmount()) {
          if (isAmount[yIndex]) {
            Y[yIndex] = s.getInitialAmount();
          } else {
            Y[yIndex] = s.getInitialAmount() / Y[compartmentIndex];
          }
        } else {
          if (!isAmount[yIndex]) {
            Y[yIndex] = s.getInitialConcentration();
          } else {
            Y[yIndex] = s.getInitialConcentration() * Y[compartmentIndex];
          }
        }
      }
      symbolHash.put(s.getId(), yIndex);
      constantHash.put(s.getId(), s.isConstant());
      compartmentHash.put(s.getId(), compartmentIndex);
      compartmentIndexes[yIndex] = compartmentIndex;
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
      constantHash.put(id, sr.isConstant());
      symbolIdentifiers[yIndex] = id;
      yIndex++;
    }

    /*
     * Save starting values of the model's parameter in Y
     */
    for (i = 0; i < model.getParameterCount(); i++) {
      Parameter p = model.getParameter(i);
      if (!p.isSetValue()) {
        Y[yIndex] = defaultParameterValue;
      } else {
        Y[yIndex] = p.getValue();
      }
      symbolHash.put(p.getId(), yIndex);
      constantHash.put(p.getId(), p.isConstant());
      symbolIdentifiers[yIndex] = p.getId();
      yIndex++;
    }

    /*
     * Check for fast reactions & update math of kinetic law to avoid wrong
     * links concerning local parameters
     */
    if (reactionFast.length != model.getReactionCount()) {
      reactionFast = new boolean[model.getReactionCount()];
    }
    int reactionIndex = 0;
    boolean fastReactions = false;
    for (Reaction r : model.getListOfReactions()) {
      if (r.isSetFast()) {
        reactionFast[reactionIndex] = r.getFast();
      } else {
        reactionFast[reactionIndex] = false;
      }
      reactionReversible[reactionIndex] = r.isReversible();
      if (r.isSetFast() && r.getFast()) {
        fastReactions = true;
      }
      if (r.getKineticLaw() != null) {
        if ((r.getKineticLaw().getListOfLocalParameters().size() > 0) && r.getKineticLaw()
            .isSetMath()) {
          r.getKineticLaw().getMath().updateVariables();
        }
      }
      Species species;
      String speciesID;
      for (SpeciesReference speciesRef : r.getListOfReactants()) {
        speciesID = speciesRef.getSpecies();
        species = speciesMap.get(speciesID);
        if (species != null) {
          if (!isAmount[symbolHash.get(speciesID)]) {
            inConcentration.add(speciesID);
          }
        }
      }
      for (SpeciesReference speciesRef : r.getListOfProducts()) {
        speciesID = speciesRef.getSpecies();
        species = speciesMap.get(speciesID);
        if (species != null) {
          if (!isAmount[symbolHash.get(speciesID)]) {
            inConcentration.add(speciesID);
          }
        }
      }
      reactionIndex++;
    }
    if (fastReactions) {
      hasFastReactions = true;
    }
    for (i = 0; i != inConcentrationValues.length; i++) {
      if (inConcentration.contains(symbolIdentifiers[i])) {
        inConcentrationValues[i] = true;
      } else {
        inConcentrationValues[i] = false;
      }
    }

    /*
     * Algebraic Rules
     */
    boolean containsAlgebraicRules = false;
    for (i = 0; i < model.getRuleCount(); i++) {
      if (model.getRule(i).isAlgebraic() && model.getRule(i).isSetMath()) {
        containsAlgebraicRules = true;
        break;
      }
    }
    if (containsAlgebraicRules) {
      evaluateAlgebraicRules();
    }

    /*
     * Initialize Events
     */
    if (model.getEventCount() > 0) {
      // this.events = new ArrayList<EventWithPriority>();
      if (events == null) {
        events = new SBMLEventInProgress[model.getEventCount()];
      }
      runningEvents = new ArrayList<>();
      delayedEvents = new ArrayList<>();
      initEvents();
      modelHasEvents = true;
    } else {
      modelHasEvents = false;
    }
    if (renewTree) {
      createSimplifiedSyntaxTree();
    } else {
      refreshSyntaxTree();
    }
    // save the initial values of this system, necessary at this point for the delay function
    if (initialValues.length != Y.length) {
      initialValues = new double[Y.length];
    }
    System.arraycopy(Y, 0, initialValues, 0, initialValues.length);

    /*
     * Evaluate Constraints
     */
    if (model.getConstraintCount() > 0) {
      // TODO: This is maybe not the best solution because callers can hardly influence that because this init method is called upon creation of this object.
      if (getConstraintListenerCount() == 0) {
        addConstraintListener(new SimpleConstraintListener());
      }
      checkConstraints(0d);
    }

  }

  /**
   * Creates the syntax tree and simplifies it.
   */
  private void createSimplifiedSyntaxTree() {
    nodes.clear();
    initializeKineticLaws();
    initializeRules();
    initializeConstraints();
    initializeEvents();
  }

  /**
   * Refreshes the syntax tree (e.g., resets the ASTNode time)
   */
  protected void refreshSyntaxTree() {
    for (ASTNode node : nodes) {
      ((ASTNodeValue) node.getUserObject(TEMP_VALUE)).reset();
    }
    for (int i = 0; i != stoichiometryValues.length; i++) {
      stoichiometryValues[i].refresh();
      stoichiometrySet[i] = stoichiometryValues[i].getStoichiometrySet();
    }
  }

  /**
   * Includes the math of the kinetic laws in the syntax tree.
   */
  private void initializeKineticLaws() {
    int reaction = 0;
    List<Boolean> isReactantList = new ArrayList<>();
    List<Integer> speciesIndexList = new ArrayList<>();
    List<Integer> reactionIndexList = new ArrayList<>();
    List<Boolean> zeroChangeList = new ArrayList<>();
    List<Boolean> constantStoichiometryList = new ArrayList<>();
    List<StoichiometryValue> stoichiometriesList = new ArrayList<>();
    List<ASTNodeValue> kineticLawRootsList = new ArrayList<>();
    for (Reaction r : model.getListOfReactions()) {
      KineticLaw kl = r.getKineticLaw();
      if ((kl != null) && kl.isSetMath()) {
        ASTNodeValue currentLaw = (ASTNodeValue) copyAST(kl.getMath(), true, null, null)
            .getUserObject(TEMP_VALUE);
        kineticLawRootsList.add(currentLaw);
        kl.getMath().putUserObject(TEMP_VALUE, currentLaw);
        for (SpeciesReference speciesRef : r.getListOfReactants()) {
          String speciesID = speciesRef.getSpecies();
          int speciesIndex = symbolHash.get(speciesID);
          int srIndex = -1;
          if (model.getLevel() >= 3) {
            String id = speciesRef.getId();
            if (id != null) {
              if (symbolHash.containsKey(id)) {
                srIndex = symbolHash.get(id);
              }
            }
          }
          //Value for stoichiometry math
          ASTNodeValue currentMathValue = null;
          if (speciesRef.isSetStoichiometryMath() && speciesRef.getStoichiometryMath()
              .isSetMath()) {
            @SuppressWarnings("deprecation") ASTNode currentMath = speciesRef.getStoichiometryMath()
                .getMath();
            currentMathValue = (ASTNodeValue) copyAST(currentMath, true, null, null)
                .getUserObject(TEMP_VALUE);
            currentMath.putUserObject(TEMP_VALUE, currentMathValue);
          }
          boolean constantStoichiometry = false;
          if (speciesRef.isSetConstant()) {
            constantStoichiometry = speciesRef.getConstant();
          } else if ((!speciesRef.isSetId()) && (!speciesRef.isSetStoichiometryMath())) {
            constantStoichiometry = true;
          }
          boolean zeroChange = false;
          Species s = speciesRef.getSpeciesInstance();
          if (s != null) {
            if (s.getBoundaryCondition()) {
              zeroChange = true;
            }
            if (s.getConstant()) {
              zeroChange = true;
            }
          }
          zeroChangeList.add(zeroChange);
          constantStoichiometryList.add(constantStoichiometry);
          reactionIndexList.add(reaction);
          speciesIndexList.add(speciesIndex);
          isReactantList.add(true);
          stoichiometriesList.add(
            new StoichiometryValue(speciesRef, srIndex, stoichiometricCoefHash, Y,
              currentMathValue));
        }
        for (SpeciesReference speciesRef : r.getListOfProducts()) {
          String speciesID = speciesRef.getSpecies();
          int speciesIndex = symbolHash.get(speciesID);
          int srIndex = -1;
          if (model.getLevel() >= 3) {
            String id = speciesRef.getId();
            if (id != null) {
              if (symbolHash.containsKey(id)) {
                srIndex = symbolHash.get(id);
              }
            }
          }
          //Value for stoichiometry math
          ASTNodeValue currentMathValue = null;
          if (speciesRef.isSetStoichiometryMath() && speciesRef.getStoichiometryMath()
              .isSetMath()) {
            @SuppressWarnings("deprecation") ASTNode currentMath = speciesRef.getStoichiometryMath()
                .getMath();
            currentMathValue = (ASTNodeValue) copyAST(currentMath, true, null, null)
                .getUserObject(TEMP_VALUE);
            currentMath.putUserObject(TEMP_VALUE, currentMathValue);
          }
          boolean constantStoichiometry = false;
          if (speciesRef.isSetConstant()) {
            constantStoichiometry = speciesRef.getConstant();
          } else if ((!speciesRef.isSetId()) && (!speciesRef.isSetStoichiometryMath())) {
            constantStoichiometry = true;
          }
          boolean zeroChange = false;
          Species s = speciesRef.getSpeciesInstance();
          if (s != null) {
            if (s.getBoundaryCondition()) {
              zeroChange = true;
            }
            if (s.getConstant()) {
              zeroChange = true;
            }
          }
          zeroChangeList.add(zeroChange);
          constantStoichiometryList.add(constantStoichiometry);
          reactionIndexList.add(reaction);
          speciesIndexList.add(speciesIndex);
          isReactantList.add(false);
          stoichiometriesList.add(
            new StoichiometryValue(speciesRef, srIndex, stoichiometricCoefHash, Y,
              currentMathValue));
        }
      } else {
        kineticLawRootsList.add(new ASTNodeValue(nodeInterpreter, new ASTNode(0d)));
      }
      reaction++;
    }
    int stoichiometriesSize = stoichiometriesList.size();
    stoichiometryValues = stoichiometriesList.toArray(new StoichiometryValue[stoichiometriesSize]);
    kineticLawRoots = kineticLawRootsList.toArray(new ASTNodeValue[v.length]);
    isReactant = new boolean[stoichiometriesSize];
    speciesIndex = new int[stoichiometriesSize];
    reactionIndex = new int[stoichiometriesSize];
    zeroChange = new boolean[stoichiometriesSize];
    constantStoichiometry = new boolean[stoichiometriesSize];
    stoichiometrySet = new boolean[stoichiometriesSize];
    stoichiometry = new double[stoichiometriesSize];
    for (int i = 0; i != stoichiometriesSize; i++) {
      isReactant[i] = isReactantList.get(i);
      speciesIndex[i] = speciesIndexList.get(i);
      reactionIndex[i] = reactionIndexList.get(i);
      zeroChange[i] = zeroChangeList.get(i);
      constantStoichiometry[i] = constantStoichiometryList.get(i);
      stoichiometrySet[i] = stoichiometryValues[i].getStoichiometrySet();
      stoichiometry[i] = stoichiometryValues[i].getStoichiometry();
    }
  }

  /**
   * Includes the math of the rules in the syntax tree.
   */
  private void initializeRules() {
    Set<AssignmentRuleValue> assignmentRulesRootsInit = new HashSet<>();
    initialAssignmentRoots = new ArrayList<>();
    rateRulesRoots = new ArrayList<>();
    Integer symbolIndex;
    for (int i = 0; i < model.getRuleCount(); i++) {
      Rule rule = model.getRule(i);
      if (rule.isAssignment()) {
        AssignmentRule as = (AssignmentRule) rule;
        symbolIndex = symbolHash.get(as.getVariable());
        if (symbolIndex != null) {
          Species sp = model.getSpecies(as.getVariable());
          if (sp != null) {
            Compartment c = sp.getCompartmentInstance();
            boolean hasZeroSpatialDimensions = true;
            if ((c != null) && (c.getSpatialDimensions() > 0)) {
              hasZeroSpatialDimensions = false;
            }
            if (as.isSetMath()) {
              assignmentRulesRootsInit.add(new AssignmentRuleValue(
                (ASTNodeValue) copyAST(as.getMath(), true, null, null).getUserObject(TEMP_VALUE),
                symbolIndex, sp, compartmentHash.get(sp.getId()), hasZeroSpatialDimensions, this,
                isAmount[symbolIndex]));
            }
          } else {
            if (as.isSetMath()) {
              assignmentRulesRootsInit.add(new AssignmentRuleValue(
                (ASTNodeValue) copyAST(as.getMath(), true, null, null).getUserObject(TEMP_VALUE),
                symbolIndex));
            }
          }
        } else if (model.findSpeciesReference(as.getVariable()) != null) {
          SpeciesReference sr = model.findSpeciesReference(as.getVariable());
          if (!sr.isConstant() && as.isSetMath()) {
            assignmentRulesRootsInit.add(new AssignmentRuleValue(
              (ASTNodeValue) copyAST(as.getMath(), true, null, null).getUserObject(TEMP_VALUE),
              sr.getId(), stoichiometricCoefHash));
          }
        }
      } else if (rule.isRate()) {
        RateRule rr = (RateRule) rule;
        symbolIndex = symbolHash.get(rr.getVariable());
        if (symbolIndex != null) {
          Species sp = model.getSpecies(rr.getVariable());
          if (sp != null) {
            Compartment c = sp.getCompartmentInstance();
            boolean hasZeroSpatialDimensions = true;
            if ((c != null) && (c.getSpatialDimensions() > 0)) {
              hasZeroSpatialDimensions = false;
            }
            if (rr.isSetMath()) {
              rateRulesRoots.add(new RateRuleValue(
                (ASTNodeValue) copyAST(rr.getMath(), true, null, null).getUserObject(TEMP_VALUE),
                symbolIndex, sp, compartmentHash.get(sp.getId()), hasZeroSpatialDimensions, this,
                rr.getVariable(), isAmount[symbolIndex]));
              rateRuleHash.put(rr.getVariable(), rateRulesRoots.size() - 1);
            }
          } else if (compartmentHash.containsValue(symbolIndex)) {
            List<Integer> speciesIndices = new LinkedList<>();
            for (Map.Entry<String, Integer> entry : compartmentHash.entrySet()) {
              if (entry.getValue().equals(symbolIndex)) {
                Species s = model.getSpecies(entry.getKey());
                int speciesIndex = symbolHash.get(entry.getKey());
                if ((!isAmount[speciesIndex]) && (!s.isConstant())) {
                  speciesIndices.add(speciesIndex);
                }
              }
            }
            if (rr.isSetMath()) {
              rateRulesRoots.add(new RateRuleValue(
                (ASTNodeValue) copyAST(rr.getMath(), true, null, null).getUserObject(TEMP_VALUE),
                symbolIndex, speciesIndices, this, rr.getVariable()));
              rateRuleHash.put(rr.getVariable(), rateRulesRoots.size() - 1);
            }
          } else {
            if (rr.isSetMath()) {
              rateRulesRoots.add(new RateRuleValue(
                (ASTNodeValue) copyAST(rr.getMath(), true, null, null).getUserObject(TEMP_VALUE),
                symbolIndex, rr.getVariable()));
              rateRuleHash.put(rr.getVariable(), rateRulesRoots.size() - 1);
            }
          }
        }
      }
    }

    /*
     * Traversing through all the rate rules for finding if species
     * are present in changing compartment. Traversing is done again as the
     * the compartment rate rules in the SBML models can be declared after the
     * species rate rule.
     */
    for (int i = 0; i < model.getRuleCount(); i++) {
      Rule rr = model.getRule(i);
      if (rr.isRate()) {
        RateRule rateRule = (RateRule) rr;
        symbolIndex = symbolHash.get(rateRule.getVariable());
        if (symbolIndex != null) {
          Species sp = model.getSpecies(rateRule.getVariable());
          if (sp != null) {
            Compartment c = sp.getCompartmentInstance();

            if ((c != null) && (rateRuleHash.get(c.getId()) != null)) {
              rateRulesRoots.get(rateRuleHash.get(sp.getId()))
              .setCompartmentRateRule(rateRulesRoots.get(rateRuleHash.get(c.getId())));
            }
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
            if ((c != null) && (c.getSpatialDimensions() > 0)) {
              hasZeroSpatialDimensions = false;
            }
            if (as.isSetMath()) {
              assignmentRulesRootsInit.add(new AssignmentRuleValue(
                (ASTNodeValue) copyAST(as.getMath(), true, null, null).getUserObject(TEMP_VALUE),
                symbolIndex, sp, compartmentHash.get(sp.getId()), hasZeroSpatialDimensions, this,
                isAmount[symbolIndex]));
            }
          } else {
            if (as.isSetMath()) {
              assignmentRulesRootsInit.add(new AssignmentRuleValue(
                (ASTNodeValue) copyAST(as.getMath(), true, null, null).getUserObject(TEMP_VALUE),
                symbolIndex));
            }
          }
        } else if (model.findSpeciesReference(as.getVariable()) != null) {
          SpeciesReference sr = model.findSpeciesReference(as.getVariable());
          if (!sr.isConstant() && as.isSetMath()) {
            assignmentRulesRootsInit.add(new AssignmentRuleValue(
              (ASTNodeValue) copyAST(as.getMath(), true, null, null).getUserObject(TEMP_VALUE),
              sr.getId(), stoichiometricCoefHash));
          }
        }
      }
    }
    assignmentRulesRoots = new ArrayList<>();
    if (assignmentRulesRootsInit.size() <= 1) {
      for (AssignmentRuleValue rv : assignmentRulesRootsInit) {
        assignmentRulesRoots.add(rv);
        numberOfAssignmentRulesLoops = 1;
      }
    } else {
      // Determine best order of assignment rule roots
      Map<String, Set<String>> neededRules = new HashMap<>();
      Map<String, AssignmentRuleValue> sBaseMap = new HashMap<>();
      Set<String> variables = new HashSet<>();
      for (AssignmentRuleValue rv : assignmentRulesRootsInit) {
        assignmentRulesRoots.add(rv);
        if (rv.getIndex() != -1) {
          variables.add(symbolIdentifiers[rv.getIndex()]);
          sBaseMap.put(symbolIdentifiers[rv.getIndex()], rv);
        } else if (rv.getSpeciesReferenceID() != null) {
          variables.add(rv.getSpeciesReferenceID());
          sBaseMap.put(rv.getSpeciesReferenceID(), rv);
        }
      }
      for (String variable : variables) {
        for (String dependentVariable : getSetOfVariables(sBaseMap.get(variable).getMath(),
          variables, new HashSet<>())) {
          Set<String> currentSet = neededRules.get(dependentVariable);
          if (currentSet == null) {
            currentSet = new HashSet<>();
            neededRules.put(dependentVariable, currentSet);
          }
          currentSet.add(variable);
        }
      }
      int currentPosition = assignmentRulesRootsInit.size() - 1;
      Set<String> toRemove = new HashSet<>();
      Set<String> keysToRemove = new HashSet<>();
      boolean toContinue = variables.size() > 0;
      while (toContinue) {
        toContinue = false;
        toRemove.clear();
        keysToRemove.clear();
        for (String variable : variables) {
          if (!neededRules.containsKey(variable)) {
            toRemove.add(variable);
            assignmentRulesRoots.set(currentPosition, sBaseMap.get(variable));
            currentPosition--;
          }
        }
        for (String key : neededRules.keySet()) {
          Set<String> currentSet = neededRules.get(key);
          currentSet.removeAll(toRemove);
          if (currentSet.size() == 0) {
            keysToRemove.add(key);
          }
        }
        for (String keyToRemove : keysToRemove) {
          neededRules.remove(keyToRemove);
        }
        variables.removeAll(toRemove);
        if ((toRemove.size() > 0) || (keysToRemove.size() > 0)) {
          toContinue = true;
        }
      }
      for (String variable : variables) {
        assignmentRulesRoots.set(currentPosition, sBaseMap.get(variable));
        currentPosition--;
      }
      numberOfAssignmentRulesLoops = Math.max(variables.size(), 1);
    }
    for (int i = 0; i < model.getInitialAssignmentCount(); i++) {
      InitialAssignment iA = model.getInitialAssignment(i);
      symbolIndex = symbolHash.get(iA.getVariable());
      if (symbolIndex != null) {
        Species sp = model.getSpecies(iA.getVariable());
        if (sp != null) {
          Compartment c = sp.getCompartmentInstance();
          boolean hasZeroSpatialDimensions = true;
          if ((c != null) && (c.getSpatialDimensions() > 0)) {
            hasZeroSpatialDimensions = false;
          }
          if (iA.isSetMath()) {
            initialAssignmentRoots.add(new AssignmentRuleValue(
              (ASTNodeValue) copyAST(iA.getMath(), true, null, null).getUserObject(TEMP_VALUE),
              symbolIndex, sp, compartmentHash.get(sp.getId()), hasZeroSpatialDimensions, this,
              isAmount[symbolIndex]));
          }
        } else {
          if (iA.isSetMath()) {
            initialAssignmentRoots.add(new AssignmentRuleValue(
              (ASTNodeValue) copyAST(iA.getMath(), true, null, null).getUserObject(TEMP_VALUE),
              symbolIndex));
          }
        }
      } else if (model.findSpeciesReference(iA.getVariable()) != null) {
        SpeciesReference sr = model.findSpeciesReference(iA.getVariable());
        if (iA.isSetMath()) {
          initialAssignmentRoots.add(new AssignmentRuleValue(
            (ASTNodeValue) copyAST(iA.getMath(), true, null, null).getUserObject(TEMP_VALUE),
            sr.getId(), stoichiometricCoefHash));
        }
      }
    }
    nRateRules = rateRulesRoots.size();
    nAssignmentRules = assignmentRulesRoots.size();
  }

  /**
   * Includes the math of the {@link Constraint}s in the syntax tree.
   */
  private void initializeConstraints() {
    constraintRoots = new ArrayList<>();
    for (Constraint c : model.getListOfConstraints()) {
      if (c.isSetMath()) {
        ASTNodeValue currentConstraint = (ASTNodeValue) copyAST(c.getMath(), true, null, null)
            .getUserObject(TEMP_VALUE);
        constraintRoots.add(currentConstraint);
        c.getMath().putUserObject(TEMP_VALUE, currentConstraint);
      }
    }
  }

  /**
   * Includes the math of the events in the syntax tree.
   */
  private void initializeEvents() {
    if (events != null) {
      for (int i = 0; i != events.length; i++) {
        Event e = model.getEvent(i);
        if (e.isSetTrigger() && e.getTrigger().isSetMath()) {
          events[i].setUseValuesFromTriggerTime(e.getUseValuesFromTriggerTime());
          events[i].setPersistent(e.getTrigger().getPersistent());
          events[i].setTriggerObject(
            (ASTNodeValue) copyAST(e.getTrigger().getMath(), true, null, null)
            .getUserObject(TEMP_VALUE));
          if ((e.getPriority() != null) && e.getPriority().isSetMath()) {
            events[i].setPriorityObject(
              (ASTNodeValue) copyAST(e.getPriority().getMath(), true, null, null)
              .getUserObject(TEMP_VALUE));
          }
          if ((e.getDelay() != null) && e.getDelay().isSetMath()) {
            events[i].setDelayObject(
              (ASTNodeValue) copyAST(e.getDelay().getMath(), true, null, null)
              .getUserObject(TEMP_VALUE));
          }
          events[i].clearRuleObjects();
          for (EventAssignment as : e.getListOfEventAssignments()) {
            Integer symbolIndex = symbolHash.get(as.getVariable());
            if (symbolIndex != null) {
              Species sp = model.getSpecies(as.getVariable());
              if (sp != null) {
                Compartment c = sp.getCompartmentInstance();
                boolean hasZeroSpatialDimensions = true;
                if ((c != null) && (c.getSpatialDimensions() > 0)) {
                  hasZeroSpatialDimensions = false;
                }
                if (as.isSetMath()) {
                  events[i].addRuleObject(new AssignmentRuleValue(
                    (ASTNodeValue) copyAST(as.getMath(), true, null, null)
                    .getUserObject(TEMP_VALUE), symbolIndex, sp,
                    compartmentHash.get(sp.getId()), hasZeroSpatialDimensions, this,
                    isAmount[symbolIndex]));
                }
              } else {
                if (as.isSetMath()) {
                  events[i].addRuleObject(new AssignmentRuleValue(
                    (ASTNodeValue) copyAST(as.getMath(), true, null, null)
                    .getUserObject(TEMP_VALUE), symbolIndex));
                }
              }
            } else if (model.findSpeciesReference(as.getVariable()) != null) {
              SpeciesReference sr = model.findSpeciesReference(as.getVariable());
              if (!sr.isConstant() && as.isSetMath()) {
                events[i].addRuleObject(new AssignmentRuleValue(
                  (ASTNodeValue) copyAST(as.getMath(), true, null, null)
                  .getUserObject(TEMP_VALUE), sr.getId(), stoichiometricCoefHash));
              }
            }
          }
          events[i].setUseValuesFromTriggerTime(e.getUseValuesFromTriggerTime());
          if (events[i].getRuleObjects() == null) {
            events[i] = null;
          }
        } else {
          events[i] = null;
        }
      }
    }
  }

  /**
   * Initializes the events of the given model. An Event that triggers at t = 0 must not fire. Only
   * when it triggers at t > 0
   *
   * @throws SBMLException
   */
  private void initEvents() throws SBMLException {
    for (int i = 0; i < model.getEventCount(); i++) {
      if (model.getEvent(i).isSetTrigger()) {
        if (model.getEvent(i).getDelay() == null) {
          if (events[i] != null) {
            events[i].refresh(model.getEvent(i).getTrigger().getInitialValue());
          } else {
            events[i] = new SBMLEventInProgress(model.getEvent(i).getTrigger().getInitialValue());
          }
        } else {
          if (events[i] != null) {
            events[i].refresh(model.getEvent(i).getTrigger().getInitialValue());
          } else {
            events[i] = new SBMLEventInProgressWithDelay(
              model.getEvent(i).getTrigger().getInitialValue());
          }
        }
      } else {
        events[i] = null;
      }
    }
  }

  /**
   * Evaluates the algebraic rules of the given model to assignment rules
   *
   * @throws ModelOverdeterminedException
   */
  private void evaluateAlgebraicRules() throws ModelOverdeterminedException {
    OverdeterminationValidator odv = new OverdeterminationValidator(model);
    // model must not be overdetermined (violation of the SBML specifications)
    if (odv.isOverdetermined()) {
      throw new ModelOverdeterminedException();
    }
    // create assignment rules out of the algebraic rules
    AlgebraicRuleConverter arc = new AlgebraicRuleConverter(odv.getMatching(), model);
    algebraicRules = arc.getAssignmentRules();
  }

  /**
   * Due to missing information about the attributes of {@link Species} set by {@link
   * InitialAssignment}s, a majority vote of all other species is performed to determine the
   * attributes.
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
      majority.setInitialAmount(0.0d);
    } else {
      majority.setInitialConcentration(0.0d);
    }
    if (substanceUnits > (model.getSpeciesCount() - substanceUnits)) {
      majority.setHasOnlySubstanceUnits(true);
    } else {
      majority.setHasOnlySubstanceUnits(false);
    }
    return majority;
  }

  /**
   * Creates a copy of an {@link ASTNode} or returns an {@link ASTNode} that is equal to the
   * presented node.
   *
   * @param node            the node to copy
   * @param mergingPossible flag that is true if it is allowed to return a node that is equal to the
   *                        given node
   * @param function        the function that is currently processed (if any) or null
   * @param inFunctionNodes the nodes that already belong to the function
   * @return the found node
   */
  public ASTNode copyAST(ASTNode node, boolean mergingPossible, FunctionValue function,
    List<ASTNode> inFunctionNodes) {
    String nodeString = node.toString();
    ASTNode copiedAST = null;
    if (mergingPossible && !nodeString.equals("") && !nodeString.contains("")) {
      //Be careful with local parameters!
      if (!(node.isName()) || (node.getType() == ASTNode.Type.NAME_TIME) || (node.getType()
          == ASTNode.Type.NAME_AVOGADRO) || !((node.getVariable() != null) && (node
              .getVariable() instanceof LocalParameter))) {
        List<ASTNode> nodesToLookAt = null;
        if (function != null) {
          nodesToLookAt = inFunctionNodes;
        } else {
          nodesToLookAt = nodes;
        }
        for (ASTNode current : nodesToLookAt) {
          if (!(current.isName()) || (current.getType() == ASTNode.Type.NAME_TIME) || (
              current.getType() == ASTNode.Type.NAME_AVOGADRO) || ((current.isName()) && !(current
                  .getVariable() instanceof LocalParameter))) {
            if ((current.toString().equals(nodeString)) && (!containUnequalLocalParameters(current,
              node))) {
              copiedAST = current;
              break;
            }
          }
        }
      }
    }
    if (copiedAST == null) {
      copiedAST = new ASTNode(node.getType());
      copiedAST.setParentSBMLObject(
        node.getParentSBMLObject()); // The variable is not stored any more directly in the ASTNode2
      for (ASTNode child : node.getChildren()) {
        if (function != null) {
          copiedAST.addChild(copyAST(child, true, function, inFunctionNodes));
        } else {
          copiedAST.addChild(copyAST(child, mergingPossible, function, inFunctionNodes));
        }
      }
      if (function != null) {
        inFunctionNodes.add(copiedAST);
      } else {
        nodes.add(copiedAST);
      }
      if (node.isSetUnits()) {
        copiedAST.setUnits(node.getUnits());
      }
      switch (node.getType()) {
      case REAL:
        double value = node.getReal();
        int integerValue = (int) value;
        if ((value - integerValue) == 0.0d) {
          copiedAST.setValue(integerValue);
          copiedAST.putUserObject(TEMP_VALUE, new IntegerValue(nodeInterpreter, copiedAST));
        } else {
          copiedAST.setValue(value);
          copiedAST.putUserObject(TEMP_VALUE, new ASTNodeValue(nodeInterpreter, copiedAST));
        }
        break;
      case FUNCTION_POWER:
        copiedAST.putUserObject(TEMP_VALUE, new PowerValue(nodeInterpreter, copiedAST));
        break;
      case POWER:
        copiedAST.putUserObject(TEMP_VALUE, new PowerValue(nodeInterpreter, copiedAST));
        break;
      case PLUS:
        copiedAST.putUserObject(TEMP_VALUE, new PlusValue(nodeInterpreter, copiedAST));
        break;
      case TIMES:
        copiedAST.putUserObject(TEMP_VALUE, new TimesValue(nodeInterpreter, copiedAST));
        break;
      case DIVIDE:
        copiedAST.putUserObject(TEMP_VALUE, new DivideValue(nodeInterpreter, copiedAST));
        break;
      case MINUS:
        copiedAST.putUserObject(TEMP_VALUE, new MinusValue(nodeInterpreter, copiedAST));
        break;
      case INTEGER:
        copiedAST.setValue(node.getInteger());
        copiedAST.putUserObject(TEMP_VALUE, new IntegerValue(nodeInterpreter, copiedAST));
        break;
      case RATIONAL:
        copiedAST.setValue(node.getNumerator(), node.getDenominator());
        copiedAST.putUserObject(TEMP_VALUE, new ASTNodeValue(nodeInterpreter, copiedAST));
        break;
      case NAME_TIME:
        copiedAST.setName(node.getName());
        copiedAST.putUserObject(TEMP_VALUE, new ASTNodeValue(nodeInterpreter, copiedAST));
        break;
      case FUNCTION_DELAY:
        copiedAST.setName(node.getName());
        copiedAST.putUserObject(TEMP_VALUE, new ASTNodeValue(nodeInterpreter, copiedAST));
        break;
        /*
         * Names of identifiers: parameters, functions, species etc.
         */
      case NAME:
        copiedAST.setName(node.getName());
        CallableSBase variable = node.getVariable();
        if ((variable == null) && (function == null)) {
          variable = model.findQuantity(node.getName());
          if ((variable == null) && (function == null)) {
            String id = node.getName();
            for (Reaction r : model.getListOfReactions()) {
              KineticLaw kl = r.getKineticLaw();
              for (LocalParameter lp : kl.getListOfLocalParameters()) {
                if (lp.getId().equals(id)) {
                  variable = lp;
                  break;
                }
              }
            }
          }
        }
        if (variable != null) {
          copiedAST.setVariable(variable);
          if (variable instanceof FunctionDefinition) {
            List<ASTNode> arguments = new LinkedList<>();
            ASTNode lambda = ((FunctionDefinition) variable).getMath();
            for (int i = 0; i != (lambda.getChildren().size() - 1); i++) {
              arguments.add(lambda.getChild(i));
            }
            FunctionValue functionValue = new FunctionValue(nodeInterpreter, copiedAST,
              arguments);
            copiedAST.putUserObject(TEMP_VALUE, functionValue);
            ASTNode mathAST = copyAST(lambda, false, functionValue, new LinkedList<>());
            functionValue.setMath(mathAST);
          } else if (variable instanceof Species) {
            boolean hasZeroSpatialDimensions = true;
            Species sp = (Species) variable;
            Compartment c = sp.getCompartmentInstance();
            if ((c != null) && (c.getSpatialDimensions() > 0)) {
              hasZeroSpatialDimensions = false;
            }
            copiedAST.putUserObject(TEMP_VALUE,
              new SpeciesValue(nodeInterpreter, copiedAST, sp, this,
                symbolHash.get(variable.getId()), compartmentHash.get(variable.getId()),
                sp.getCompartment(), hasZeroSpatialDimensions,
                isAmount[symbolHash.get(variable.getId())]));
          } else if ((variable instanceof Compartment) || (variable instanceof Parameter)) {
            copiedAST.putUserObject(TEMP_VALUE,
              new CompartmentOrParameterValue(nodeInterpreter, copiedAST, (Symbol) variable,
                this, symbolHash.get(variable.getId())));
          } else if (variable instanceof LocalParameter) {
            copiedAST.putUserObject(TEMP_VALUE,
              new LocalParameterValue(nodeInterpreter, copiedAST, (LocalParameter) variable));
          } else if (variable instanceof SpeciesReference) {
            copiedAST.putUserObject(TEMP_VALUE,
              new SpeciesReferenceValue(nodeInterpreter, copiedAST, (SpeciesReference) variable,
                this));
          } else if (variable instanceof Reaction) {
            copiedAST.putUserObject(TEMP_VALUE,
              new ReactionValue(nodeInterpreter, copiedAST, (Reaction) variable));
          }
        } else {
          copiedAST
          .putUserObject(TEMP_VALUE, new NamedValue(nodeInterpreter, copiedAST, function));
        }
        break;
      case NAME_AVOGADRO:
        copiedAST.putUserObject(TEMP_VALUE, new ASTNodeValue(nodeInterpreter, copiedAST));
        copiedAST.setName(node.getName());
        break;
      case REAL_E:
        copiedAST.setValue(node.getMantissa(), node.getExponent());
        copiedAST.putUserObject(TEMP_VALUE, new ASTNodeValue(nodeInterpreter, copiedAST));
        break;
      case FUNCTION: {
        copiedAST.setName(node.getName());
        variable = node.getVariable();
        if (variable != null) {
          copiedAST.setVariable(variable);
          if (variable instanceof FunctionDefinition) {
            List<ASTNode> arguments = new LinkedList<>();
            ASTNode lambda = ((FunctionDefinition) variable).getMath();
            for (int i = 0; i != (lambda.getChildren().size() - 1); i++) {
              arguments.add(lambda.getChild(i));
            }
            FunctionValue functionValue = new FunctionValue(nodeInterpreter, copiedAST,
              arguments);
            copiedAST.putUserObject(TEMP_VALUE, functionValue);
            ASTNode mathAST = copyAST(lambda, false, functionValue, new LinkedList<>());
            functionValue.setMath(mathAST);
          }
        }
        break;
      }
      case FUNCTION_PIECEWISE:
        copiedAST.putUserObject(TEMP_VALUE, new ASTNodeValue(nodeInterpreter, copiedAST));
        break;
      case FUNCTION_ROOT:
        copiedAST.putUserObject(TEMP_VALUE, new RootFunctionValue(nodeInterpreter, copiedAST));
        break;
      case LAMBDA:
        copiedAST.putUserObject(TEMP_VALUE, new ASTNodeValue(nodeInterpreter, copiedAST));
        break;
      default:
        copiedAST.putUserObject(TEMP_VALUE, new ASTNodeValue(this, nodeInterpreter, copiedAST));
        break;
      }
    }
    return copiedAST;
  }

  /**
   * Checks whether the two given nodes are equal to each other (especially regarding local
   * parameters contained).
   *
   * @param node1 the first node
   * @param node2 the second node
   * @return
   */
  private boolean containUnequalLocalParameters(ASTNode node1, ASTNode node2) {
    if (node1.getChildCount() != node2.getChildCount()) {
      return true;
    }
    if ((node1.getType() == ASTNode.Type.NAME) && (node2.getType() == ASTNode.Type.NAME) && (node1
        .getVariable() instanceof LocalParameter) && (node2
            .getVariable() instanceof LocalParameter)) {
      LocalParameter lp1 = (LocalParameter) node1.getVariable();
      LocalParameter lp2 = (LocalParameter) node2.getVariable();
      if ((lp1.getId().equals(lp2.getId())) && (!lp1.equals(lp2))) {
        return true;
      } else {
        return false;
      }
    } else if ((node1.getType() == ASTNode.Type.NAME) && (node2.getType() == ASTNode.Type.NAME) && (
        ((node1.getVariable() instanceof LocalParameter) && !(node2
            .getVariable() instanceof LocalParameter)) || (
                !(node1.getVariable() instanceof LocalParameter) && (node2
                    .getVariable() instanceof LocalParameter)))) {
      return true;
    } else {
      boolean result = false;
      for (int i = 0; i != node1.getChildCount(); i++) {
        result = result || containUnequalLocalParameters(node1.getChild(i), node2.getChild(i));
      }
      return result;
    }
  }

  /**
   * @param math
   * @param variables
   * @param current
   * @return
   */
  private Set<String> getSetOfVariables(ASTNode math, Set<String> variables, Set<String> current) {
    if ((math.isVariable()) && (math.getVariable() != null) && (variables
        .contains(math.getVariable().getId()))) {
      current.add(math.getVariable().getId());
    }
    for (ASTNode node : math.getChildren()) {
      getSetOfVariables(node, variables, current);
    }
    return current;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCurrentCompartmentSize(String id) {
    return Y[symbolHash.get(id)];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCurrentCompartmentValueOf(String speciesId) {
    Integer compartmentIndex = compartmentHash.get(speciesId);
    if (compartmentIndex != null) {
      // Is species with compartment
      double value = Y[compartmentIndex];
      if (value != 0d) {
        return value;
      }
      // Is compartment or parameter or there is no compartment for this
      // species
      // TODO: Replace by user-defined default value?
    }
    return 1d;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCurrentParameterValue(String id) {
    return Y[symbolHash.get(id)];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCurrentSpeciesValue(String id) {
    return Y[symbolHash.get(id)];
  }

  /**
   * {@inheritDoc}
   */
  @Override
  @SuppressWarnings("deprecation")
  public double getCurrentStoichiometry(String id) {
    Integer pos = symbolHash.get(id);
    if (pos != null) {
      return Y[pos];
    }
    Double value = stoichiometricCoefHash.get(id);
    if (value != null) {
      return value;
    }
    // TODO: What happens if a species reference does not have an id?
    SpeciesReference sr = model.findSpeciesReference(id);
    if ((sr != null) && sr.isSetStoichiometryMath()) {
      try {
        return ((ASTNodeValue) sr.getStoichiometryMath().getMath().getUserObject(TEMP_VALUE))
            .compileDouble(astNodeTime, 0d);
      } catch (SBMLException exc) {
        // TODO: Localize
        logger.log(Level.WARNING, MessageFormat
          .format("Could not compile stoichiometry math of species reference {0}.", id), exc);
      }
    } else if (sr != null) {
      return sr.getStoichiometry();
    }
    return 1d;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCurrentTime() {
    return currentTime;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCurrentValueOf(String id) {
    Integer symbolIndex = symbolHash.get(id);
    if (symbolIndex != null) {
      return Y[symbolIndex];
    } else {
      return Double.NaN;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public double getCurrentValueOf(int position) {
    return Y[position];
  }

  @Override
  public double computeDelayedValue(double time, String id, DESystem DES, double[] initialValues,
    int yIndex) {
    containsDelays = true;
    if (!delaysIncluded) {
      return Y[symbolHash.get(id)];
    }
    if ((time < 0d) || ((time >= 0d) && (delayValueHolder == null))) {
      int index = symbolHash.get(id);
      double oldTime = currentTime;
      currentTime = time;
      double value = Double.NaN;
      for (AssignmentRuleValue r : assignmentRulesRoots) {
        if (r.getIndex() == index) {
          r.processRule(Y, -astNodeTime - 0.01d, false);
          value = r.getValue();
          break;
        }
      }
      if (Double.isNaN(value)) {
        for (AssignmentRuleValue i : initialAssignmentRoots) {
          if (i.getIndex() == index) {
            i.processRule(Y, -astNodeTime - 0.01d, false);
            value = i.getValue();
            break;
          }
        }
      }
      if (Double.isNaN(value)) {
        value = this.initialValues[index];
      }
      currentTime = oldTime;
      return value;
    } else if (delayValueHolder == null) {
      // TODO: Localize
      logger.warning(MessageFormat
        .format("Cannot access delayed value at time {0,number} for {1}.", time, id));
      return Double.NaN;
    }
    return delayValueHolder
        .computeDelayedValue(time, id, this, this.initialValues, symbolHash.get(id));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void registerDelayValueHolder(DelayValueHolder dvh) {
    delayValueHolder = dvh;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getIdentifiers() {
    return symbolIdentifiers;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsEventsOrRules() {
    if ((model.getRuleCount() != 0) || (model.getEventCount() != 0)) {
      return true;
    } else {
      return false;
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getPositiveValueCount() {
    //return numPositives;
    return 0;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setDelaysIncluded(boolean delaysIncluded) {
    this.delaysIncluded = delaysIncluded;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getDimension() {
    return initialValues.length;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getEventCount() {
    return model.getEventCount();
  }

  /**
   * Returns the model that is used by this object.
   *
   * @return Returns the model that is used by this object.
   */
  public Model getModel() {
    return model;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getRuleCount() {
    return model.getRuleCount();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean getNoDerivatives() {
    return noDerivatives;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean containsFastProcesses() {
    return hasFastReactions;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String[] getAdditionalValueIds() {
    String[] ids = new String[v.length];
    int i = 0;
    for (Reaction r : model.getListOfReactions()) {
      ids[i++] = r.getId();
    }
    return ids;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int getAdditionalValueCount() {
    return v.length;
  }

  /**
   * Returns the initial values of the model to be simulated.
   *
   * @return Returns the initial values of the model to be simulated.
   */
  public double[] getInitialValues() {
    return initialValues;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void propertyChange(PropertyChangeEvent propertyChangeEvent) {

    if (propertyChangeEvent.getPropertyName().equals(RESULT)) {
      setLatestTimePointResult((double[]) propertyChangeEvent.getNewValue());
    } else {
      setPreviousTimePoint((Double) propertyChangeEvent.getOldValue());
      setLatestTimePoint((Double) propertyChangeEvent.getNewValue());
    }

  }

  /**
   * Chooses an event of a list randomly.
   *
   * @param highOrderEvents
   */
  protected void pickRandomEvent(List<Integer> highOrderEvents) {
    int randomIndex = ThreadLocalRandom.current().nextInt(highOrderEvents.size());
    Integer winner = highOrderEvents.get(randomIndex);
    highOrderEvents.clear();
    highOrderEvents.add(winner);
  }


  /**
   * Adds the given {@link ConstraintListener} to this interpreter's list of listeners.
   *
   * @param listener the element to be added.
   * @return {@code true} if this operation was successful, {@code false} otherwise.
   * @throws NullPointerException
   * @see List#add(Object)
   */
  public boolean addConstraintListener(ConstraintListener listener) {
    return listOfConstraintListeners.add(listener);
  }


  /**
   * Removes the given {@link ConstraintListener} from this interpreter.
   *
   * @param listener the element to be removed.
   * @return {@code true} if this operation was successful, {@code false} otherwise.
   * @throws NullPointerException
   * @see List#remove(Object)
   */
  public boolean removeConstraintListener(ConstraintListener listener) {
    return listOfConstraintListeners.remove(listener);
  }


  /**
   * Removes the {@link ConstraintListener} with the given index from this interpreter.
   *
   * @param index of the {@link ConstraintListener} to be removed.
   * @return {@code true} if this operation was successful, {@code false} otherwise.
   * @throws IndexOutOfBoundsException
   * @see List#remove(int)
   */
  public ConstraintListener removeConstraintListener(int index) {
    return listOfConstraintListeners.remove(index);
  }


  /**
   * @return the number of {@link ConstraintListener}s currently assigned to this interpreter.
   */
  public int getConstraintListenerCount() {
    return listOfConstraintListeners.size();
  }

  public void setPreviousTimePoint(double previousTimePoint) {
    this.previousTimePoint = previousTimePoint;
  }

  private void setLatestTimePoint(double latestTimePoint) {
    this.latestTimePoint = latestTimePoint;
  }

  private void setLatestTimePointResult(double[] latestTimePointResult) {
    this.latestTimePointResult = latestTimePointResult;
  }

  public void setCurrentTime(double currentTime) {
    this.currentTime = currentTime;
  }

  public List<RateRuleValue> getRateRulesRoots() {
    return rateRulesRoots;
  }


  public Map<String, Integer> getSymbolHash() {
    return symbolHash;
  }


  public Map<String, Boolean> getConstantHash() {
    return constantHash;
  }

  /**
   * Get state array.
   *
   * @return the state array
   */
  public double[] getY() {
    return Y;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setFastProcessComputation(boolean isProcessing) {
    isProcessingFastReactions = isProcessing;
  }

  /**
   * @return the array of derivatives of each species of the model system at the current time.
   */
  public double[] getChangeRate() {
    return changeRate;
  }


  /**
   * {@inheritDoc}
   */
  public int getPositionOfParameters() {
    return Y.length - model.getParameterCount();
  }

  /**
   * This method tells you the complete number of parameters within the model. It counts the global
   * model parameters and all local parameters (parameters within a kinetic law).
   *
   * @return The total number of model parameters. Note that this number is limited to an {@code
   * int} value, whereas the SBML model may contain {@code int} values.
   */
  public int getParameterCount() {
    int p = model.getParameterCount();
    for (int i = 0; i < model.getReactionCount(); i++) {
      KineticLaw k = model.getReaction(i).getKineticLaw();
      if (k != null) {
        p += k.getLocalParameterCount();
      }
    }
    return p;
  }

  /**
   * Checks the model's constraint and logs a warning if any constraint is violated.
   *
   * @param time
   */
  protected void checkConstraints(double time) {
    for (int i = 0; i < model.getConstraintCount(); i++) {
      Constraint constraint = model.getConstraint(i);
      if (constraint.isSetMath()) {
        boolean violation = constraintRoots.get(i).compileBoolean(time);

        if (constraint.getUserObject(ConstraintListener.CONSTRAINT_VIOLATION_LOG) == null) {
          constraint.putUserObject(ConstraintListener.CONSTRAINT_VIOLATION_LOG, Boolean.FALSE);
        }

        if (violation && (constraint.getUserObject(ConstraintListener.CONSTRAINT_VIOLATION_LOG)
            == Boolean.FALSE)) {
          ConstraintEvent evt = new ConstraintEvent(constraint, time);
          for (ConstraintListener listener : listOfConstraintListeners) {
            listener.processViolation(evt);
          }
          constraint.putUserObject(ConstraintListener.CONSTRAINT_VIOLATION_LOG, Boolean.TRUE);
        } else if (!violation && (
            constraint.getUserObject(ConstraintListener.CONSTRAINT_VIOLATION_LOG)
            == Boolean.TRUE)) {
          ConstraintEvent evt = new ConstraintEvent(constraint, time);
          for (ConstraintListener listener : listOfConstraintListeners) {
            listener.processSatisfiedAgain(evt);
          }
          constraint.putUserObject(ConstraintListener.CONSTRAINT_VIOLATION_LOG, Boolean.FALSE);
        }
      }
    }
  }
}
