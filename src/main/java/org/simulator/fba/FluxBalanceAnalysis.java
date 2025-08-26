/*
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

import static java.text.MessageFormat.format;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.ext.fbc.FBCModelPlugin;
import org.sbml.jsbml.ext.fbc.Objective;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

import org.optsolvx.model.AbstractLPModel;
import org.optsolvx.solver.LPSolution;
import org.optsolvx.solver.LPSolverAdapter;

import org.simulator.optsolvx.FbaToOptSolvX;
import org.simulator.optsolvx.OptSolvXSolverAdapter;







/**
 * Support for Flux Balance Analysis (FBA).
 * <p>
 * This class provides implementation of fba using the information from the SBML fbc packages. This
 * solver implementation supports SBML models with FBC package versions 1 or 2.
 *
 * @author Andreas Dr&auml;ger
 * @author Ali Ebrahim
 * @author Shalin Shah
 * @author Matthias König
 * @since 1.5
 */


/**
 * Flux Balance Analysis (FBA) solved via OptSolvX (SCPSolver-free).
 *
 * <p>This implementation builds a pure LP using {@link FbaToOptSolvX}
 * and solves it through an OptSolvX backend (default resolved via ServiceLoader
 * with a reflective fallback to a CommonsMath-based backend).</p>
 *
 * <p>Public API is preserved: {@code solve()}, {@code getObjectiveValue()},
 * {@code getValue(String)}, {@code getValues()}, {@code getSolution()}.</p>
 */
public class FluxBalanceAnalysis {

  private static final transient Logger logger =
          Logger.getLogger(FluxBalanceAnalysis.class.getName());

  /** OptSolvX LP model built from SBML/FBC. */
  private final AbstractLPModel lpModel;

  /** Solution returned by the backend. */
  private LPSolution lpSolution;

  /** reaction id -> index (kept for array-based getters in original API). */
  private final Map<String, Integer> reaction2Index = new LinkedHashMap<>();

  /** Reactions in stable SBML order. */
  private final List<String> reactionsOrdered = new ArrayList<>();

  /** Active FBC objective id (key used in {@link #getSolution()}). */
  private final String activeObjective;

  public FluxBalanceAnalysis(SBMLDocument doc)
          throws SBMLException, ModelOverdeterminedException {
    if (doc == null || !doc.isSetModel()) {
      throw new IllegalArgumentException(
              "Could not find a model definition in the given SBML document.");
    }
    final Model model = doc.getModel();

    // Determine FBC plugin and active objective (kept for API parity).
    final String fbcNSv1 = FBCConstants.getNamespaceURI(doc.getLevel(), doc.getVersion(), 1);
    final String fbcNSv2 = FBCConstants.getNamespaceURI(doc.getLevel(), doc.getVersion(), 2);
    final int fbcVersion = model.getExtension(FBCConstants.shortLabel).getPackageVersion();

    final FBCModelPlugin mPlug;
    if (fbcVersion == 2) {
      mPlug = (FBCModelPlugin) model.getPlugin(fbcNSv2);
    } else if (fbcVersion == 1) {
      mPlug = (FBCModelPlugin) model.getPlugin(fbcNSv1);
    } else {
      throw new IllegalArgumentException(format(
              "Cannot conduct flux balance analysis without FBC package in model ''{0}''.",
              model.getId()));
    }

    final Objective objective = mPlug.getActiveObjectiveInstance();
    if (objective == null) {
      throw new IllegalArgumentException(format(
              "Cannot conduct FBA without defined objective function in model ''{0}''.",
              model.getId()));
    }
    this.activeObjective = objective.getId();

    // Preserve reaction order and index mapping for API methods.
    for (int i = 0; i < model.getReactionCount(); i++) {
      Reaction r = model.getReaction(i);
      reactionsOrdered.add(r.getId());
      reaction2Index.put(r.getId(), i);
    }

    // SBML/FBC -> OptSolvX LP model (bridge keeps all biological semantics out of this class).
    this.lpModel = FbaToOptSolvX.fromSBML(doc);
    logger.info(format(
            "FBA: built OptSolvX model (vars={0}, cons={1})",
            lpModel.getVariables().size(), lpModel.getConstraints().size()));
  }

  /**
   * Solve the built LP via OptSolvX.
   * <p>Backend is resolved via ServiceLoader; if none is found, a reflective
   * fallback tries a CommonsMath-based backend without adding a hard compile-time dependency.</p>
   *
   * @return true if a feasible solution is available.
   * @throws NullPointerException if something goes wrong internally.
   */
  public boolean solve() throws NullPointerException {
    final LPSolverAdapter backend = resolveDefaultBackend();
    final OptSolvXSolverAdapter adapter = new OptSolvXSolverAdapter(backend, true);
    this.lpSolution = adapter.solve(lpModel);
    return lpSolution != null && lpSolution.isFeasible();
  }

  /** Objective value of the current solution. */
  public double getObjectiveValue() throws NullPointerException {
    if (lpSolution == null) {
      throw new NullPointerException("No solution available; call solve() first.");
    }
    return lpSolution.getObjectiveValue();
  }

  /** Flux value for a given reaction id. */
  public double getValue(String reactionId)
          throws NullPointerException, ArrayIndexOutOfBoundsException {
    if (lpSolution == null) {
      throw new NullPointerException("No solution available; call solve() first.");
    }
    final Double v = lpSolution.getVariableValues().get(reactionId);
    if (v == null) {
      // Preserve robustness: unknown id -> 0.0 (keeps original API tolerant).
      return 0.0d;
    }
    return v.doubleValue();
  }

  /** All flux values in SBML order (array shape preserved from original API). */
  public double[] getValues() throws NullPointerException {
    if (lpSolution == null) {
      throw new NullPointerException("No solution available; call solve() first.");
    }
    final double[] vals = new double[reactionsOrdered.size()];
    for (int i = 0; i < reactionsOrdered.size(); i++) {
      final Double v = lpSolution.getVariableValues().get(reactionsOrdered.get(i));
      vals[i] = (v == null) ? 0.0d : v.doubleValue();
    }
    return vals;
  }

  /** Map with objective (under activeObjective id) plus all reaction fluxes. */
  public Map<String, Double> getSolution() {
    if (lpSolution == null) {
      throw new NullPointerException("No solution available; call solve() first.");
    }
    final Map<String, Double> map = new LinkedHashMap<>();
    map.put(activeObjective, getObjectiveValue());
    for (String rid : reactionsOrdered) {
      final Double v = lpSolution.getVariableValues().get(rid);
      map.put(rid, (v == null) ? 0.0d : v.doubleValue());
    }
    return map;
  }

  /** Public accessor for the active FBC objective id (API/test compatibility). */
  public String getActiveObjective() { // simple O(0) getter; not performance-critical
    return activeObjective;
    }

  // ---- Backend resolution helpers ----
  /**
   * Resolves a default OptSolvX backend without introducing a hard compile-time dependency.
   * Strategy:
   *  0) explicit override via -Doptsolvx.backend or OPTSOLVX_BACKEND
   *  1) ServiceLoader (preferred)
   *  2) Reflective fallback with common class names (no-arg ctor assumed)
   */
  private LPSolverAdapter resolveDefaultBackend() {
    // 0) explicit override
    String cn = System.getProperty("optsolvx.backend");
    if (cn == null || cn.isEmpty()) cn = System.getenv("OPTSOLVX_BACKEND");
    if (cn != null && !cn.isEmpty()) {
      try {
        return (LPSolverAdapter) Class.forName(cn).getDeclaredConstructor().newInstance();
      } catch (Throwable t) {
        throw new IllegalStateException("Configured backend class not usable: " + cn, t);
      }
    }
    // 1) ServiceLoader
    for (LPSolverAdapter a : ServiceLoader.load(LPSolverAdapter.class)) return a;

    // 2) Reflective fallbacks (adjust if your package differs)
    final String[] candidates = {
            "org.optsolvx.solver.commonsmath.CommonsMathSolver",
            "org.optsolvx.backends.commonsmath.CommonsMathSolver"
    };
    for (String c : candidates) {
      try {
        return (LPSolverAdapter) Class.forName(c).getDeclaredConstructor().newInstance();
      } catch (Throwable ignored) {}
    }
    throw new IllegalStateException(
            "No OptSolvX LPSolverAdapter found on classpath (ServiceLoader and reflective fallback failed).");
  }

}
