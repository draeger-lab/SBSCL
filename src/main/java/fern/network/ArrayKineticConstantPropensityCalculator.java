package fern.network;

/**
 * Implementation of an <code>AbstractKineticConstantPropensityCalculator</code> which uses an array
 * to store the constants for each reaction.
 *
 * @author Florian Erhard
 */
public class ArrayKineticConstantPropensityCalculator extends
    AbstractKineticConstantPropensityCalculator {

  private double[] constants = null;

  /**
   * Create the propensity calculator with given constants and given reactant adjacency arrays
   *
   * @param reactants array of adjacency arrays
   * @param constants array of kinetic constants
   */
  public ArrayKineticConstantPropensityCalculator(int[][] reactants, double[] constants) {
    super(reactants);

    for (int i = 0; i < constants.length; i++) {
      if (constants[i] <= 0) {
        throw new IllegalArgumentException("There is a non positive constant!");
      }
    }
    this.constants = constants;
  }

  public double getConstant(int i) {
    return constants[i];
  }

  public void setConstant(int i, double value) {
    constants[i] = value;
  }
}
