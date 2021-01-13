/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.network.sbml;

import fern.network.AmountManager;
import fern.network.ComplexDependenciesPropensityCalculator;
import fern.simulation.Simulator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.sbml.SBMLinterpreter;

/**
 * Propensity calculator which is used for {@link SBMLNetwork}s. The propensities are calculated by
 * using a {@link MathTree} derived by the MathML representation of the kinetic law for each
 * reaction.
 *
 * @author Florian Erhard
 */
public class SBMLPropensityCalculator implements ComplexDependenciesPropensityCalculator {

  private MathTree[] propensities;
  private Map<String, Double> globalParameter;

  /**
   * Creates the {@link MathTree}s and parses the parameters.
   *
   * @param interpreter instance of the SBMLinterpreter
   */
  public SBMLPropensityCalculator(SBMLinterpreter interpreter) throws ModelOverdeterminedException {

    Model model = interpreter.getModel();
    globalParameter = new HashMap<>();
    for (int i = 0; i < model.getNumParameters(); i++) {
      globalParameter.put(model.getParameter(i).getId(), model.getParameter(i).getValue());
    }
    for (int i = 0; i < model.getNumCompartments(); i++) {
      globalParameter.put(model.getCompartment(i).getId(), model.getCompartment(i).getSize());
    }

    propensities = new MathTree[model.getNumReactions()];

    for (int i = 0; i < model.getNumReactions(); i++) {
      Map<String, Double> localParameter = new HashMap<>();
      Reaction reaction = model.getReaction(i);
      for (int j = 0; j < reaction.getKineticLaw().getLocalParameterCount(); j++) {
        localParameter.put(reaction.getKineticLaw().getLocalParameter(j).getId(),
            reaction.getKineticLaw().getLocalParameter(j).getValue());
      }
      propensities[i] = new MathTree(interpreter, reaction.getKineticLaw().getMath());
    }
  }

  /**
   * Gets the global parameters.
   *
   * @return global parameters
   */
  public Map<String, Double> getGlobalParameters() {
    return globalParameter;
  }

  public double calculatePropensity(int reaction, AmountManager amount, Simulator sim) {
    double re = propensities[reaction].calculate(amount, sim);
    if (re < 0) {
      throw new RuntimeException(
          "The propensity of reaction " + sim.getNet().getReactionName(reaction) + " is negative");
    }
    return Math.abs(re);
  }

  public List<Integer> getKineticLawSpecies(int reaction) {
    return propensities[reaction].getSpecies();
  }


  /**
   * Gets the internal representation of the sbml kinetic law.
   *
   * @param reaction index of the reaction
   * @return a MathTree representation of the kinetic law
   */
  public MathTree getMathTree(int reaction) {
    return propensities[reaction];
  }


}
