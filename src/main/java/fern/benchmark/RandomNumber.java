package fern.benchmark;

import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;
import fern.tools.Stochastics;

/**
 * Benchmarking of the time needed for different random number generations.
 *
 * @author Florian Erhard
 */
public class RandomNumber extends Benchmark {

  Stochastics stoch;

  public RandomNumber() {
    stoch = Stochastics.getInstance();
  }


  /**
   * Benchmark for the different computation of random numbers in the Langevin (normal distributed)
   * and tau leap (poisson distributed) method for double values at
   *
   * @param at the products of arbitraty propensites a with arbitrary taus
   */
  public void benchmarkLangevinAgainstTauLeapNumberGeneration(double[] at) {
    @SuppressWarnings("unused")
    int r;

    // langevin
    start();
		for (int i = 0; i < at.length; i++) {
			r = (int) (at[i] + Math.sqrt(at[i]) * stoch.getNormal());
		}

    System.out.println(end() + " nanoseconds for langevin");

    // tauleap
    start();
		for (int i = 0; i < at.length; i++) {
			r = stoch.getPoisson(at[i]);
		}
    System.out.println(end() + " nanoseconds for tau leap");
  }

  /**
   * @param args
   */
  public static void main(String[] args) {
    RandomNumber bench = new RandomNumber();

    double[] testset = bench
        .createRandomDoubleArray(1000000, new Uniform(1000, 5000, new MersenneTwister()));

    bench.benchmarkLangevinAgainstTauLeapNumberGeneration(testset);


  }

}
