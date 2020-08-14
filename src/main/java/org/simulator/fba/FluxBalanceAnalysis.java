/*
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2016 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
 * 8. Duke University, Durham, NC, US
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.fba;

import static org.sbml.jsbml.util.Pair.pairOf;
import static java.text.MessageFormat.format;

import java.util.*;
import java.util.logging.Logger;

import org.sbml.jsbml.AlgebraicRule;
import org.sbml.jsbml.InitialAssignment;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.FBCReactionPlugin;
import org.sbml.jsbml.ext.fbc.FluxBound;
import org.sbml.jsbml.ext.fbc.FluxObjective;
import org.sbml.jsbml.ext.fbc.Objective;
import org.sbml.jsbml.util.Pair;
import org.sbml.jsbml.util.SBMLtools;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.sbml.SBMLinterpreter;
import org.simulator.sbml.astnode.ASTNodeValue;
import scpsolver.constraints.LinearBiggerThanEqualsConstraint;
import scpsolver.constraints.LinearConstraint;
import scpsolver.constraints.LinearEqualsConstraint;
import scpsolver.constraints.LinearSmallerThanEqualsConstraint;
import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LinearProgram;

/**
 * Support for Flux Balance Analysis (FBA).
 *
 * This class provides implementation of fba using the information from the
 * SBML fbc packages. This solver implementation supports SBML models with
 * FBC package versions 1 or 2.
 *
 * @author Andreas Dr&auml;ger
 * @author Ali Ebrahim
 * @author Shalin Shah
 * @author Matthias König
 * @since 1.5
 */
@SuppressWarnings("deprecation")
public class FluxBalanceAnalysis {

  /**
   * A Logger for this class.
   */
  private static final transient Logger logger = Logger.getLogger(FluxBalanceAnalysis.class.getName());

  /**
   * The linear programming solver.
   */
  private LinearProgramSolver glpkSolver;

  private LinearProgram problem;

  private double[] solution;

  // SCPSolver does not allow same values for lower and upper bounds.
  // So, eps is used to add to one of the bounds when both the bounds have equal values.
  private double eps = 1E-10;

  /**
   * This interpreter is only used if the model contains
   * {@link InitialAssignment}s or {@link org.sbml.jsbml.StoichiometryMath}.
   * In all other situations, it will be {@code null}.
   */
  private SBMLinterpreter interpreter;

  /**
   * The variables of the linear program, i.e., the reactions.
   */

  /**
   * A dictionary to lookup the position of a {@link Reaction} in the list of
   * reactions of the {@link Model} based on the reaction's identifier.
   */
  private Map<String, Integer> reaction2Index;

  /**
   * A String that keeps track of id of the active objective function
   */
  private String activeObjective;

  /**
   * Initializes the linear program and all data structures based on the
   * definitions in the given {@link SBMLDocument}.
   * This implementation should work for diverse levels and versions of SBML
   * {@link Model}s given that the model contains an fbc package in version 1 or
   * 2.
   *
   * @param doc the SBML container from which the {@link Model} is taken. This
   *            implementation only understands SBML core (diverse levels and
   *            versions) in combination with fbc versions 1 and 2.
   * @throws ModelOverdeterminedException if the {@link Model} is over determined through
   *                                      {@link AlgebraicRule}s.
   * @throws SBMLException                if the {@link Model} is invalid or inappropriate for flux balance
   *                                      analysis.
   */
  public FluxBalanceAnalysis(SBMLDocument doc)
      throws SBMLException, ModelOverdeterminedException {
    super();
    if (!doc.isSetModel()) {
      throw new IllegalArgumentException("Could not find a model definition in the given SBML document.");
    }
    Model model = doc.getModel();
    interpreter = new SBMLinterpreter(model);
    int level = doc.getLevel(), version = doc.getVersion();
    String fbcNamespaceV1 = FBCConstants.getNamespaceURI(level, version, 1);
    String fbcNamespaceV2 = FBCConstants.getNamespaceURI(level, version, 2);
    reaction2Index = new HashMap<String, Integer>();

    // initialize upper and lower reaction bounds
    double lb[] = new double[model.getReactionCount()];
    double ub[] = new double[model.getReactionCount()];

    // Mapping from species id to reaction id and stoichiometric coefficient in that reaction.
    Map<String, Set<Pair<String, Double>>> species2Reaction = new HashMap<String, Set<Pair<String, Double>>>();
    for (int i = 0; i < model.getReactionCount(); i++) {
      Reaction r = model.getReaction(i);
      if (r.isSetPlugin(fbcNamespaceV2)) {
        FBCReactionPlugin rPlug = (FBCReactionPlugin) r.getPlugin(fbcNamespaceV2);
        Parameter upperBound = rPlug.getUpperFluxBoundInstance();
        Parameter lowerBound = rPlug.getLowerFluxBoundInstance();
        lb[i] = interpreter != null ?
            interpreter.getCurrentValueOf(lowerBound.getId()) :
            lowerBound.getValue();
        ub[i] = interpreter != null ?
            interpreter.getCurrentValueOf(upperBound.getId()) :
            upperBound.getValue();
        adjustBoundNumerics(lb, ub, i);
      }
      reaction2Index.put(r.getId(), i);
      buildSpeciesReactionMap(species2Reaction, r.getListOfReactants());
      buildSpeciesReactionMap(species2Reaction, r.getListOfProducts());
    }
    FBCModelPlugin mPlug = null;
    int fbcVersion = model.getExtension(FBCConstants.shortLabel).getPackageVersion();
    if (fbcVersion == 2) {
      mPlug = (FBCModelPlugin) model.getPlugin(fbcNamespaceV2);
    } else if (fbcVersion == 1) {
      mPlug = (FBCModelPlugin) model.getPlugin(fbcNamespaceV1);
      if (mPlug.isSetListOfFluxBounds()) {
        for (FluxBound fb : mPlug.getListOfFluxBounds()) {
          if (!fb.isSetReaction()) {
            logger.warning(format("Encountered fluxBound ''{0}'' without reaction identifier.", fb.getId()));
          } else {
            int index = reaction2Index.get(fb.getReaction());
            if (fb.isSetOperation()) {
              if (fb.getOperation() == FluxBound.Operation.GREATER_EQUAL) {
                lb[index] = fb.getValue();
              } else if (fb.getOperation() == FluxBound.Operation.LESS_EQUAL) {
                ub[index] = fb.getValue();
              } else if (fb.getOperation() == FluxBound.Operation.EQUAL) {
                lb[index] = ub[index] = fb.getValue();
              } else {
                logger.severe(format("Encountered fluxBound ''{0}'' with invalid operation.", fb.getId()));
              }
              adjustBoundNumerics(lb, ub, index);
            } else {
              logger.severe(format("Encountered fluxBound ''{0}'' without defined operation.", fb.getId()));
            }
          }
        }
      }
    } else {
      throw new IllegalArgumentException(format("Cannot conduct flux balance analysis without defined objective function in model ''{0}''.", model.getId()));
    }

    // define objective function
    double objvals[] = new double[model.getReactionCount()];
    Arrays.fill(objvals, 0d);
    Objective objective = mPlug.getActiveObjectiveInstance();
    Objective.Type type = objective.getType(); // max or min
    activeObjective = objective.getId();
    for (FluxObjective fo : objective.getListOfFluxObjectives()) {
      int rIndex = reaction2Index.get(fo.getReaction());
      objvals[rIndex] = fo.getCoefficient();
    }

    /*
     * Create linear solver
     */
    SolverFactory.newDefault();
    glpkSolver = new NewGLPKSolver();
    problem = new LinearProgram(objvals);
    problem.setLowerbound(lb);
    problem.setUpperbound(ub);
    switch (type) {
    case MAXIMIZE:
      problem.setMinProblem(false);
      break;
    case MINIMIZE:
      problem.setMinProblem(true);
      break;
    default:
      throw new SBMLException(format("Unspecified operation {0}", type));
    }

    // Add weighted constraints equations for each reaction.
    for (Species species : model.getListOfSpecies()) {
      double[] weights = new double[reaction2Index.size()];
      if (!species2Reaction.containsKey(species.getId())) {
        logger.warning(format("Species ''{0}'' does not participate in any reaction.", species.getId()));
      } else {
        for (Pair<String, Double> pair : species2Reaction.get(species.getId())) {
          weights[reaction2Index.get(pair.getKey())] = pair.getValue();
        }
        if (species.isSetBoundaryCondition() && !species.getBoundaryCondition()) {
          problem.addConstraint(new LinearEqualsConstraint(weights, 0d, "cnstrt_" + species.getId()));
        }
      }
    }
  }

  /**
   * This method updates the lower bounds and upper bounds
   * as per the standards of the SCPSolver.
   *
   * @param lowerBound the array of lower flux bounds
   * @param upperBound the array of the upper flux bounds
   * @param index      the index of lower bound and upper bound
   */
  void adjustBoundNumerics(double[] lowerBound, double[] upperBound, int index) {

  	// SCPSolver doesn't allow same values for upper bound and lower bound
    // therefore adding a small EPSILON
    if (lowerBound[index] == upperBound[index]) {
      upperBound[index] += eps;
    }

    // SCPSolver doesn't allow the bounds to be +Infinity or -Infinity
    // therefore changing them to +Double.MAX_VALUE and -Double.MAX_VALUE respectively
    // for both lower as well as upper bounds
    if (lowerBound[index] == Double.POSITIVE_INFINITY) {
      lowerBound[index] = Double.MAX_VALUE;
    }
    else if (lowerBound[index] == -Double.POSITIVE_INFINITY) {
      lowerBound[index] = -Double.MAX_VALUE;
    }

    if (upperBound[index] == Double.POSITIVE_INFINITY) {
      upperBound[index] = Double.MAX_VALUE;
    }
    else if (upperBound[index] == -Double.POSITIVE_INFINITY) {
      upperBound[index] = -Double.MAX_VALUE;
    }
  }

  /**
   * Helper function that fills a dictionary data structure that points from
   * {@link Species} identifiers to a {@link Set} of {@link Pair}s that again
   * consist of a {@link Reaction} identifier and the stoichiometric coefficient
   * that the {@link Species} has in this reaction.
   * In other words, this look-up table will provide for each {@link Species}
   * all {@link Reaction} ids and stoichiometric coefficients in that particular
   * reaction. If a species acts as reactant in a reaction, its stoichiometric
   * coefficient will be negative, otherwise it will be a positive value.
   *
   * @param species2Reaction   the dictionary data structure to be filled.
   * @param listOfParticipants the list of reaction participants that have the stoichiometry values
   *                           and links to {@link Species}
   * @throws ModelOverdeterminedException if the overall SBML {@link Model} is over determined through
   *                                      {@link AlgebraicRule}s
   * @throws SBMLException                if the {@link Model} is invalid or inappropriate for being solved.
   */
  private void buildSpeciesReactionMap(Map<String, Set<Pair<String, Double>>> species2Reaction, ListOf<SpeciesReference> listOfParticipants)
      throws SBMLException, ModelOverdeterminedException {
    String rId = ((Reaction) listOfParticipants.getParent()).getId();
    if ((rId == null) || (rId.length() == 0)) {
      Model model = listOfParticipants.getModel();
      Reaction r = (Reaction) listOfParticipants.getParent();
      String id = SBMLtools.getIdOrName(model);
      throw new SBMLException(format("Incomplete model{0}: encountered {1} without identifier.",
          (id.length() > 0) ? " '" + id + "'" : "", (r.isSetName() ?
              "reaction '" + r.getName() + "'" : "a reaction")));
    }
    double factor =
        listOfParticipants.getSBaseListType().equals(ListOf.Type.listOfReactants) ?
            -1d : 1d;
    for (SpeciesReference specRef : listOfParticipants) {
      if (!specRef.isSetSpecies()) {
        throw new SBMLException(format("Incomplete model: no species defined for a species reference in the {0} of reaction ''{1}''", listOfParticipants.getSBaseListType(), rId));
      }
      if (!species2Reaction.containsKey(specRef.getSpecies())) {
        species2Reaction.put(specRef.getSpecies(), new HashSet<Pair<String, Double>>());
      }
      species2Reaction.get(specRef.getSpecies()).add(pairOf(rId, factor * stoichiometry(specRef)));
    }
  }
  /**
   * This method writes the configuration of the linear program into an LP file
   * with the given path.
   *
   * @param path
   *        the path to a file, where the LP should be written to. This file
   *        must end with extension '.lp'.
   * @throws IloException if the path is invalid or the file cannot be written.
   */
  //	public void exportLP(String path) throws IloException {
  //		//cplex.exportModel(path);
  //	}

  /**
   * Solves the linear program that is defined in the {@link SBMLDocument} with
   * which this solver was initialized.
   *
   * @return A Boolean value reporting whether a feasible solution has been
   * found. This solution is not necessarily optimal. If false is
   * returned, a feasible solution may still be present, but IloCplex
   * has not been able to prove its feasibility.
   * @throws NullPointerException If the method fails, an exception of type NullPointerException, or one of
   *                              its derived classes, is thrown.
   */
  public boolean solve() throws NullPointerException {
    solution = glpkSolver.solve(problem);
    if (solution != null)
      return true;
    return false;
  }

  /**
   * Returns the objective value of the current solution.
   *
   * @return the objective value of the current solution.
   * @throws NullPointerException If the method fails, an exception of type IloException, or one of
   *                              its derived classes, is thrown.
   */
  public double getObjectiveValue() throws NullPointerException {
    return problem.evaluate(solution);
  }

  /**
   * Returns the solution value for the {@link Reaction} variable with the given
   * identifier.
   *
   * @param reactionId the identifier of the {@link Reaction} of interest.
   * @return The value the {@link Reaction} takes for the current solution.
   * @throws NullPointerException           If the {@link Reaction} identifier is not in the active model.
   * @throws ArrayIndexOutOfBoundsException If the method fails, an exception of type ArrayIndexOutOfBoundsException, or one of
   *                                        its derived classes, is thrown.
   */
  public double getValue(String reactionId)
      throws NullPointerException, ArrayIndexOutOfBoundsException {
    return solution[reaction2Index.get(reactionId)];
  }

  /**
   * Returns solution values for an array of {@link Reaction} variables.
   *
   * @return The solution values for the variables in the list of reactions.
   * @throws NullPointerException If the method fails, an exception of type NullPointerException, or one of
   *                              its derived classes, is thrown.
   */
  public double[] getValues() throws NullPointerException {
    return solution;
  }

  /**
   * Returns solution values as a HashMap with key as reaction Id and value as the flux.
   *
   * @return The flux values for the each of the reactions
   */
  public Map<String, Double> getSolution() {
    Map<String, Double> result = new HashMap<>();
    result.put(activeObjective, getObjectiveValue());
    for (Map.Entry<String, Integer> mapElement : reaction2Index.entrySet()) {
      result.put(mapElement.getKey(), solution[mapElement.getValue()]);
    }
    return result;
  }

  /**
   * Determines the stoichiometry value of a given {@link SpeciesReference}.
   * This might involve the evaluation of a
   * {@link org.sbml.jsbml.StoichiometryMath} or needs to lookup the current
   * stoichiometry value if it has been changed by an initial assignment.
   *
   * @param specRef the {@link SpeciesReference} whose stoichiometry value needs to be
   *                determined
   * @return a double value indicating the stoichiometry value of the given
   * {@link SpeciesReference}. This value can be directly specified by
   * the element, or needs to be calculated from its
   * {@link org.sbml.jsbml.StoichiometryMath} or through an
   * {@link InitialAssignment}.
   * @throws ModelOverdeterminedException if the model cannot be solved because too many equations over
   *                                      determine its solution space (this can happen if algebraic rules
   *                                      are used in the model).
   * @throws SBMLException                if the model has an invalid structure.
   */
  private double stoichiometry(SpeciesReference specRef)
      throws SBMLException, ModelOverdeterminedException {
    if ((interpreter != null) && specRef.isSetId()) {
      // There could be an initial assignment that has changed the value of this speciesReference.
      return interpreter.getCurrentStoichiometry(specRef.getId());
    } else if (specRef.isSetStoichiometry()) {
      return specRef.getStoichiometry();
    } else {
      if (interpreter == null) {
        interpreter = new SBMLinterpreter(specRef.getModel());
      }
      if (specRef.isSetStoichiometryMath()) {
        return ((ASTNodeValue) specRef.getStoichiometryMath().getMath().getUserObject(SBMLinterpreter.TEMP_VALUE)).compileDouble(interpreter.getCurrentTime(), 0d);
      } else if (specRef.isSetId()) {
        // Is there an initial assignment?
        interpreter.getCurrentStoichiometry(specRef.getId());
      } else {
        throw new SBMLException("Could not calculate the stoichiometry for a species reference because it was lacking an identifier.");
      }
    }
    return Double.NaN;
  }

  /**
   * Gets the value of the EPSILON
   *
   * @return the epsilon value
   */
  public double getEpsilon() {
    return eps;
  }

  /**
   * Set the value of the EPSILON specific to a particular FBC instance
   *
   * @param eps
   */
  public void setEpsilon(double eps) {
    this.eps = eps;
  }

  /**
   * Gets the id of the active objective function
   *
   * @return
   */
  public String getActiveObjective() {
    return activeObjective;
  }
}
