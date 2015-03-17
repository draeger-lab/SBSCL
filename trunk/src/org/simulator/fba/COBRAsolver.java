/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2015 jointly by the following organizations:
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
package org.simulator.fba;

import static org.sbml.jsbml.util.Pair.pairOf;
import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
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
import org.sbml.jsbml.ext.fbc.FluxObjective;
import org.sbml.jsbml.ext.fbc.Objective;
import org.sbml.jsbml.util.Pair;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.sbml.SBMLinterpreter;
import org.simulator.sbml.astnode.ASTNodeValue;


/**
 * This solver implementation only accepts SBML models with FBC package version
 * 2.
 * 
 * @author Andreas Dr&auml;ger
 * @author Ali Ebrahim
 * @since 1.5
 */
public class COBRAsolver {

  /**
   * A Logger for this class.
   */
  private static final transient Logger logger = Logger.getLogger(COBRAsolver.class.getName());

  /**
   * 
   */
  private IloCplex cplex;
  /**
   * This interpreter is only used if the model contains
   * {@link InitialAssignment}s or {@link org.sbml.jsbml.StoichiometryMath}.
   * In all other situations, it will be {@code null}.
   */
  @SuppressWarnings("javadoc")
  private SBMLinterpreter interpreter;
  /**
   * The variables of the linear program, i.e., the reactions.
   */
  private IloNumVar x[];
  /**
   * A dictionary to lookup the position of a {@link Reaction} in the list of
   * reactions of the {@link Model} based on the reaction's identifier.
   */
  private HashMap<String, Integer> reaction2Index;

  /**
   * Initializes the linear program and all data structures based on the
   * definitions in the given {@link SBMLDocument}.
   * This implementation should work for diverse levels and versions of SBML,
   * but note that this implementation only accepts {@link Model}s with fbc
   * package in version 2.
   * 
   * @param doc
   *        the SBML container from which the {@link Model} is taken. This
   *        implementation only understands SBML core (diverse levels and
   *        versions) and fbc version 2.
   * @throws IloException
   *         if the construction of the linear program fails.
   * @throws ModelOverdeterminedException
   *         if the {@link Model} is over determined through
   *         {@link AlgebraicRule}s.
   * @throws SBMLException
   *         if the {@link Model} is invalid or inappropriate for flux balance
   *         analysis.
   */
  public COBRAsolver(SBMLDocument doc) throws IloException, SBMLException, ModelOverdeterminedException {
    super();

    cplex = new IloCplex();

    if (!doc.isSetModel()) {
      throw new IllegalArgumentException("Could not find a model definition in the given SBML document.");
    }

    Model model = doc.getModel();

    if (model.getInitialAssignmentCount() > 0) {
      interpreter = new SBMLinterpreter(model);
    }

    int level = doc.getLevel(), version = doc.getVersion();

    String fbcNamespace = FBCConstants.getNamespaceURI(level, version, 2);

    reaction2Index = new HashMap<String, Integer>();

    // initialize upper and lower reaction bounds
    double lb[] = new double[model.getReactionCount()];
    double ub[] = new double[model.getReactionCount()];

    // Mapping from species id to reaction id and stoichiometric coefficient in that reaction.
    Map<String, Set<Pair<String, Double>>> species2Reaction = new HashMap<String, Set<Pair<String, Double>>>();

    if (model.getReactionCount() > 0) {
      int i = 0;
      for (Reaction r : model.getListOfReactions()) {
        FBCReactionPlugin rPlug = (FBCReactionPlugin) r.getPlugin(fbcNamespace);
        Parameter upperBound = rPlug.getUpperFluxBoundInstance();
        Parameter lowerBound = rPlug.getLowerFluxBoundInstande();

        lb[i] = interpreter != null ? interpreter.getCurrentValueOf(lowerBound.getId()) : lowerBound.getValue();
        ub[i] = interpreter != null ? interpreter.getCurrentValueOf(upperBound.getId()) : upperBound.getValue();

        reaction2Index.put(r.getId(), i);

        buildSpeciesReactionMap(species2Reaction, r.getListOfReactants());
        buildSpeciesReactionMap(species2Reaction, r.getListOfProducts());

        i++;
      }
    }



    FBCModelPlugin mPlug = (FBCModelPlugin) model.getPlugin(fbcNamespace);

    // define objective function
    double objvals[] = new double[model.getReactionCount()];
    Arrays.fill(objvals, 0d);
    Objective objective = mPlug.getActiveObjectiveInstsance();
    Objective.Type type = objective.getType(); // max or min

    for (FluxObjective fo : objective.getListOfFluxObjectives()) {
      int rIndex = reaction2Index.get(fo.getReaction());
      objvals[rIndex] = fo.getCoefficient();
    }

    x = cplex.numVarArray(model.getReactionCount(), lb, ub);
    IloLinearNumExpr target = cplex.scalProd(x, objvals);
    switch (type) {
    case MAXIMIZE:
      cplex.addMaximize(target);
      break;
    case MINIMIZE:
      cplex.addMinimize(target);
      break;
    default:
      throw new SBMLException(MessageFormat.format("Unspecified operation {0}", type));
    }

    // Add constraints.
    for (Species species : model.getListOfSpecies()) {
      IloLinearNumExpr expr = cplex.linearNumExpr();

      if (!species2Reaction.containsKey(species.getId())) {
        logger.warning(MessageFormat.format(
          "Species ''{0}'' does not participate in any reaction.",
          species.getId()));
      } else {
        for (Pair<String, Double> pair : species2Reaction.get(species.getId())) {
          expr.addTerm(pair.getValue(), x[reaction2Index.get(pair.getKey())]);
        }
      }

      cplex.addEq(expr, 0d);
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
   * @param species2Reaction
   *        the dictionary data structure to be filled.
   * @param listOfParticipants
   *        the list of reaction participants that have the stoichiometry values
   *        and links to {@link Species}
   * @throws ModelOverdeterminedException
   *         if the overall SBML {@link Model} is over determined through
   *         {@link AlgebraicRule}s
   * @throws SBMLException
   *         if the {@link Model} is invalid or inappropriate for being solved.
   */
  private void buildSpeciesReactionMap(Map<String, Set<Pair<String, Double>>> species2Reaction, ListOf<SpeciesReference> listOfParticipants) throws SBMLException, ModelOverdeterminedException {
    String rId = ((Reaction) listOfParticipants.getParent()).getId();
    if ((rId == null) || (rId.length() == 0)) {
      throw new SBMLException("Incomplete model: encountered a reaction with undefined identifier.");
    }
    double factor = listOfParticipants.getSBaseListType().equals(ListOf.Type.listOfReactants) ? -1d : 1d;

    for (SpeciesReference specRef : listOfParticipants) {
      if (!specRef.isSetSpecies()) {
        throw new SBMLException(MessageFormat.format(
          "Incomplete model: no species defined for a species reference in reaction ''{0}''",
          rId));
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
  public void exportLP(String path) throws IloException {
    cplex.exportModel(path);
  }

  /**
   * Solves the linear program that is defined in the {@link SBMLDocument} with
   * which this solver was initialized.
   * 
   * @return A Boolean value reporting whether a feasible solution has been
   *         found. This solution is not necessarily optimal. If false is
   *         returned, a feasible solution may still be present, but IloCplex
   *         has not been able to prove its feasibility.
   * @throws IloException
   *         If the method fails, an exception of type IloException, or one of
   *         its derived classes, is thrown.
   */
  public boolean solve() throws IloException {
    return cplex.solve();
  }

  /**
   * Returns the objective value of the current solution.
   * 
   * @return the objective value of the current solution.
   * @throws IloException
   *         If the method fails, an exception of type IloException, or one of
   *         its derived classes, is thrown.
   */
  public double getObjetiveValue() throws IloException {
    return cplex.getObjValue();
  }

  /**
   * Returns the solution value for the {@link Reaction} variable with the given
   * identifier.
   * 
   * @param reactionId
   *        the identifier of the {@link Reaction} of interest.
   * @return The value the {@link Reaction} takes for the current solution.
   * @throws UnknownObjectException
   *         If the {@link Reaction} identifier is not in the active model.
   * @throws IloException
   *         If the method fails, an exception of type IloException, or one of
   *         its derived classes, is thrown.
   */
  public double getValue(String reactionId) throws UnknownObjectException, IloException {
    return cplex.getValue(x[reaction2Index.get(reactionId)]);
  }

  /**
   * Returns solution values for an array of {@link Reaction} variables.
   * 
   * @return The solution values for the variables in the list of reactions.
   * @throws IloException
   *         If the method fails, an exception of type IloException, or one of
   *         its derived classes, is thrown.
   */
  public double[] getValues() throws IloException {
    try {
      return cplex.getValues(x);
    } catch (UnknownObjectException exc) {
      // This should never happen because x is the vector of reactions.
      logger.warning(exc.getMessage());
      return null;
    }
  }

  /**
   * Determines the stoichiometry value of a given {@link SpeciesReference}.
   * This might involve the evaluation of a
   * {@link org.sbml.jsbml.StoichiometryMath} or needs to lookup the current
   * stoichiometry value if it has been changed by an initial assignment.
   * 
   * @param specRef
   *        the {@link SpeciesReference} whose stoichiometry value needs to be
   *        determined
   * @return a double value indicating the stoichiometry value of the given
   *         {@link SpeciesReference}. This value can be directly specified by
   *         the element, or needs to be calculated from its
   *         {@link org.sbml.jsbml.StoichiometryMath} or through an
   *         {@link InitialAssignment}.
   * @throws ModelOverdeterminedException
   *         if the model cannot be solved because too many equations over
   *         determine its solution space (this can happen if algebraic rules
   *         are used in the model).
   * @throws SBMLException
   *         if the model has an invalid structure.
   */
  @SuppressWarnings({"deprecation", "javadoc"})
  private double stoichiometry(SpeciesReference specRef) throws SBMLException, ModelOverdeterminedException {
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

}
