package fern.network;

public interface KineticConstantPropensityCalculator extends PropensityCalculator {

  /**
   * Gets the constant for a reaction
   *
   * @param reaction index of the reaction
   * @return constant for the reaction
   */
  double getConstant(int reaction);

  /**
   * Calculates the specific reaction probability rate constant c from the conventional
   * deterministic rate constant k in some fixed volume v by the formula c=|reactants| ! * k /
   * V^(|reactants|-1)
   * <br><br>
   * For references see Daniel T. Gillespie, A General Method for Numerically Simulating the
   * Stochastic Time Evolution of Coupled Chemical Reactions, Journal of Computational Physics 22,
   * 403-434 (1976)
   *
   * @param k        deterministic rate constant
   * @param reaction the index of the constant's reaction
   * @param V        the fixed volume
   * @return the specific reaction probability rate constant
   */
  double getConstantFromDeterministicRateConstant(double k, int reaction, double V);

}
