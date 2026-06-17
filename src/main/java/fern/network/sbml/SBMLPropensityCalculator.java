package fern.network.sbml;

import fern.network.AmountManager;
import fern.network.ComplexDependenciesPropensityCalculator;
import fern.simulation.Simulator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SpeciesReference;
import org.sbml.jsbml.ModifierSpeciesReference;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.sbml.SBMLinterpreter;

/**
 * Propensity calculator which is used for {@link SBMLNetwork}s. 
 * Refactored to use native SBSCL SBMLinterpreter for optimized stochastic simulation.
 */
public class SBMLPropensityCalculator implements ComplexDependenciesPropensityCalculator {

  private SBMLinterpreter interpreter;
  private Map<String, Double> globalParameter;
  private Model model;

  public SBMLPropensityCalculator(SBMLinterpreter interpreter) throws ModelOverdeterminedException {
    this.interpreter = interpreter;
    this.model = interpreter.getModel();
    
    globalParameter = new HashMap<>();
    for (int i = 0; i < model.getNumParameters(); i++) {
      globalParameter.put(model.getParameter(i).getId(), model.getParameter(i).getValue());
    }
    for (int i = 0; i < model.getNumCompartments(); i++) {
      globalParameter.put(model.getCompartment(i).getId(), model.getCompartment(i).getSize());
    }
    
    // Notice: We no longer create an array of MathTrees!
  }

  public Map<String, Double> getGlobalParameters() {
    return globalParameter;
  }

  public double calculatePropensity(int reactionIndex, AmountManager amount, Simulator sim) {
    // 1. Update the SBSCL interpreter with the current stochastic state
    interpreter.updateSpeciesConcentration(amount);
    interpreter.setCurrentTime(sim.getTime());
    
    // 2. Let the highly-optimized SBSCL engine calculate the velocity natively!
    double re = interpreter.compileReaction(reactionIndex);
    
    if (re < 0) {
      throw new RuntimeException(
          "The propensity of reaction " + sim.getNet().getReactionName(reactionIndex) + " is negative");
    }
    return Math.abs(re);
  }

  public List<Integer> getKineticLawSpecies(int reactionIndex) {
    // Instead of doing a manual DFS tree search, we just ask the SBML Reaction for its dependencies
    List<Integer> speciesIndices = new ArrayList<>();
    Reaction reaction = model.getReaction(reactionIndex);
    
    // Add Reactants
    for (SpeciesReference reactant : reaction.getListOfReactants()) {
       int index = getSpeciesIndex(reactant.getSpecies());
       if (index != -1 && !speciesIndices.contains(index)) speciesIndices.add(index);
    }
    
    // Add Modifiers
    for (ModifierSpeciesReference modifier : reaction.getListOfModifiers()) {
       int index = getSpeciesIndex(modifier.getSpecies());
       if (index != -1 && !speciesIndices.contains(index)) speciesIndices.add(index);
    }
    
    return speciesIndices;
  }

  // Helper method to resolve species IDs to array indices for FERN
  private int getSpeciesIndex(String speciesId) {
     if (interpreter.getSymbolHash().containsKey(speciesId)) {
        return interpreter.getSymbolHash().get(speciesId) - model.getCompartmentCount();
     }
     return -1;
  }
}