package fern.network;

import java.util.Map;

import fern.simulation.Simulator;
import fern.tools.NumberTools;


/**
 * Base implementation of a {@code PropensityCalculator}. The propensity is simply a product of
 * the populations of its reactant species and a specific reaction probability rate constant (which
 * is related to the conventional deterministic rate constant and can be calculated by
 * {@code getConstantFromDeterministicRateConstant}.
 * <p>
 * Some of the tau leap procedures need partial derivatives of the propensity function (and use
 * therefore the method {@code calculatePartialDerivative}) so the use of this procedures is
 * only possible when the network's {@code PropensityCalculator} is a
 * {@code AbstractKineticConstantPropensityCalculator}.
 *
 * @author Florian Erhard
 */
public abstract class AbstractKineticConstantPropensityCalculator implements
KineticConstantPropensityCalculator, PartialDerivativePropensityCalculator {

  //	private Map<Integer,Integer>[] reactantHistos = null;
  private int[][] reactantHistosKeys = null;
  private int[][] reactantHistosVals = null;
  private int[][] reactants;

  /**
   * Creates a {@code AbstractKineticConstantPropensityCalculator} by an array of adjacency
   * arrays for the reaction's reactant species (which are the only one needed for the propensity
   * calculation).
   *
   * @param reactants array of adjacency arrays
   */
  @SuppressWarnings("unchecked")
  public AbstractKineticConstantPropensityCalculator(int[][] reactants) {
    reactantHistosKeys = new int[reactants.length][];
    reactantHistosVals = new int[reactants.length][];

    for (int i = 0; i < reactants.length; i++) {
      Map<Integer, Integer> reactantHisto = NumberTools.createHistogramAsMap(reactants[i]);
      reactantHistosKeys[i] = new int[reactantHisto.size()];
      reactantHistosVals[i] = new int[reactantHisto.size()];
      int index = 0;
      for (int r : reactantHisto.keySet()) {
        reactantHistosKeys[i][index] = r;
        reactantHistosVals[i][index] = reactantHisto.get(r);
        index++;
      }
    }
    this.reactants = reactants;
  }

  /**
   * Calculates the propensity for {@code reaction} by the formula h*c, where c is the kinetic
   * constant for {@code reaction} and h is the number of distinct molecular reactant
   * combinations for {@code reaction}. If a positive value for volume is given, it is assumed
   * that the constants are deterministic rate constants and are hence transformed to specific
   * reaction rate constants.
   *
   * @param reaction the index of the reaction
   * @param amount   the {@code AmountManager}
   * @param sim      the simulator
   * @return the propensity for the reaction
   */
  @Override
  public double calculatePropensity(int reaction, AmountManager amount, Simulator sim) {
    double re = getConstant(reaction);
    double volume = sim.getVolume();
    if (volume > 0) {
      re = getConstantFromDeterministicRateConstant(re, reaction, volume);
    }
    for (int i = 0; i < reactantHistosKeys[reaction].length;
        i++) { //int r : reactantHistos[reaction].keySet()) {
      int freq = reactantHistosVals[reaction][i];
      int r = reactantHistosKeys[reaction][i];
      for (int f = 0; f < freq; f++) {
        re *= ((double) amount.getAmount(r) - f);
      }
      re /= NumberTools.faculty(freq);
    }
    if (re < 0) {
      throw new RuntimeException("Propensity < 0");
    }
    return Math.abs(re);
  }


  /**
   * Calculates partial differentials of the propensity functions for the tau leaping methods. If a
   * positive value for volume is given, it is assumed that the constants are deterministic rate
   * constants and are hence transformed to specific reaction rate constants.
   *
   * @param reaction      the reaction index
   * @param amount        the {@code AmountManager}
   * @param reactantIndex the network index of the reactant to calculate the partial differential
   *                      for
   * @param volume        the volume of the reaction space
   * @return partial differential
   */
  @Override
  public double calculatePartialDerivative(int reaction, AmountManager amount, int reactantIndex,
    double volume) {
    int speciesIndex = reactantIndex;

    //		if (!reactantHistos[reaction].containsKey(speciesIndex))
    //			return 0;
    int histoIndex = -1;

    double re = getConstant(reaction);
    if (volume > 0) {
      re = getConstantFromDeterministicRateConstant(re, reaction, volume);
    }

    for (int i = 0; i < reactantHistosKeys[reaction].length;
        i++) { //int r : reactantHistos[reaction].keySet()) {
      int freq = reactantHistosVals[reaction][i];
      int r = reactantHistosKeys[reaction][i];
      if (speciesIndex == r) {
        histoIndex = i;
      } else {
        for (int f = 0; f < freq; f++) {
          re *= (amount.getAmount(r) - f);
        }
        re /= NumberTools.faculty(freq);
      }
    }

    double x = amount.getAmount(speciesIndex);
    switch (reactantHistosVals[reaction][histoIndex]) {
    case 1:
      re *= 1;
      break;
    case 2:
      re *= (x - 0.5);
      break;
    case 3:
      re *= (((0.5 * x * x) - x) + (1.0 / 3.0));
      break;
      //		case 4:
      //			re*=(1.0/6.0*x*x*x-3.0/4.0*x*x+11.0/12.0*x-1.0/4.0);
      //			break;
      //		case 5:
      //			re*=(1.0/24.0*x*x*x*x-1.0/3.0*x*x*x+7.0/8.0*x*x-5.0/6.0*x+1.0/5.0);
      //			break;
      //		case 6:
      //			re*=(-1.0/6.0+137.0/180.0*x+17.0/36.0*x*x*x-5.0/48.0*x*x*x*x+1.0/120.0*x*x*x*x*x-15.0/16.0*x*x);
      //			break;
    default:
      throw new RuntimeException(
          "Cannot calculate partial differentiale for a reaction with >3 reactants of the same species!");
    }

    return re;
  }


  /**
   * Calculates the specific reaction probability rate constant <i>c</i> from the conventional
   * deterministic rate constant <i>k</i> in some fixed volume <i>v</i> by the formula <i>c</i> =
   * |reactants| ! &#8901; <i>k</i> / <i>V</i><sup>(|reactants|-1)</sup>
   * <br><br>
   * For references see Daniel T. Gillespie, A General Method for Numerically Simulating the
   * Stochastic Time Evolution of Coupled Chemical Reactions, Journal of Computational Physics 22,
   * 403-434 (1976) <a href="https://doi.org/10.1016/0021-9991(76)90041-3">doi: 10.1016/0021-9991(76)90041-3</a>
   *
   * @param k        deterministic rate constant
   * @param reaction the index of the constant's reaction
   * @param V        the fixed volume
   * @return the specific reaction probability rate constant
   */
  @Override
  public double getConstantFromDeterministicRateConstant(double k, int reaction, double V) {
    double re = k / Math.pow(V, reactants[reaction].length - 1);
    for (int i = 0; i < reactantHistosKeys[reaction].length;
        i++) { //int r : reactantHistos[reaction].keySet()) {
      int freq = reactantHistosVals[reaction][i];
      re *= NumberTools.faculty(freq);
    }
    return re;
  }


}
