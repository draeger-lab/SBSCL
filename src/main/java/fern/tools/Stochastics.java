/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.tools;

import java.util.Date;

import cern.jet.random.Binomial;
import cern.jet.random.Exponential;
import cern.jet.random.Normal;
import cern.jet.random.Poisson;
import cern.jet.random.Uniform;
import cern.jet.random.engine.MersenneTwister;

/**
 * Manages a random number generators for various distributions. By only using this class for every
 * random number drawing in the whole framework, some advantages emerge:
 * <ul><li>it is guaranteed that the most quickest possible generator is used for each drawing</li>
 * <li>one can derandomize the algorithms by using {@link Stochastics#setSeed(Date)}</li>
 * <li>the number of random number generations can easily be counted</li></ul>
 *
 * @author Florian Erhard
 */
public class Stochastics {


  private static Stochastics inst = null;

  /**
   * Singleton pattern method.
   *
   * @return the singleton object
   */
  public static Stochastics getInstance() {
    if (inst == null) {
      inst = new Stochastics();
    }
    return inst;
  }

  private Date seed = null;
  private Uniform unif = null;
  private Exponential exponential = null;
  private Binomial binom = null;
  private Poisson poisson = null;
  private Normal normal = null;
  private int unifCount = 0;
  private int exponentialCount = 0;
  private int binomCount = 0;
  private int poissonCount = 0;
  private int normalCount = 0;


  private boolean countGenerations = false;

  private Stochastics() {
    resetSeed();
  }

  /**
   * Sets the seed to the current date
   */
  public void resetSeed() {
    setSeed(new Date());
  }

  /**
   * Gets the actual seed of the random number generator.
   *
   * @return seed
   */
  public long getSeed() {
    return seed.getTime();
  }

  /**
   * Sets a new seed for the random number generator.
   *
   * @param seed the seed
   */
  public void setSeed(long seed) {
    setSeed(new Date(seed));
  }

  /**
   * Sets a new seed for the random number generator.
   *
   * @param seed the seed
   */
  public void setSeed(Date seed) {
    unif = new Uniform(new MersenneTwister(seed));
    exponential = new Exponential(0, new MersenneTwister(seed));
    binom = new Binomial(10, 0.5, new MersenneTwister(seed));
    poisson = new Poisson(1, new MersenneTwister(seed));
    normal = new Normal(0, 1, new MersenneTwister(seed));
    this.seed = seed;
  }


  /**
   * Gets whether or not to count the number of random number generations
   *
   * @return whether or not to count
   */
  public boolean isCountGenerations() {
    return countGenerations;
  }


  /**
   * Sets whether or not to count the number of random number generations
   *
   * @param countGenerations whether or not to count
   */
  public void setCountGenerations(boolean countGenerations) {
    this.countGenerations = countGenerations;
  }


  /**
   * Gets the number of random number generations for each distribution (same order as in {@link
   * Stochastics#getNames()} since the last call of <code>resetCounts</code>.
   *
   * @return number of random number generations
   */
  public int[] getCounts() {
    return new int[]{unifCount, binomCount, exponentialCount, normalCount, poissonCount};
  }

  /**
   * Gets the names of the built-in distributions.
   *
   * @return names of the distributions
   */
  public String[] getNames() {
    return new String[]{"Uniform", "Binomial", "Exponential", "Normal", "Poisson"};
  }

  /**
   * Resets the counts for each distribution to zero.
   */
  public void resetCounts() {
    unifCount = binomCount = exponentialCount = normalCount = poissonCount = 0;
  }

  /**
   * Gets a random number from the uniform distribution between 0 and 1.
   *
   * @return random number between 0 and 1
   * @see Uniform#nextDouble()
   */
  public double getUnif() {
    if (countGenerations) {
      unifCount++;
    }
    return unif.nextDouble();
  }

  /**
   * Gets a random number from the exponential distribution.
   *
   * @param d mean
   * @return random number
   * @see Exponential#nextDouble()
   */
  public double getExponential(double d) {
    if (Double.isInfinite(d)) {
      return d;
    }
    if (countGenerations) {
      exponentialCount++;
    }

    return exponential.nextDouble(d);
  }

  /**
   * Gets a random number from the binomial distribution.
   *
   * @param i    size
   * @param prob probability
   * @return random number
   * @see Binomial#nextInt(int, double)
   */
  public int getBinom(int i, double prob) {
    if (countGenerations) {
      binomCount++;
    }
    if (prob == 0 || i == 0) {
      return 0;
    } else if (prob == 1) {
      return i;
    } else {
      return binom.nextInt(i, prob);
    }
  }

  /**
   * Gets a uniformly chosen random number between <code>min</code> (inclusive) and <code>max</code>
   * (exclusive)
   *
   * @param min inclusive minimum
   * @param max exclusive maximum
   * @return uniformly chosen random number
   * @see Uniform#nextIntFromTo(int, int)
   */
  public int getUnif(int min, int max) {
    if (countGenerations) {
      unifCount++;
    }
    return unif.nextIntFromTo(min, max - 1);
  }

  /**
   * Gets a random number from the Poisson distribution.
   *
   * @param mean mean
   * @return random number
   * @see Poisson#nextInt(double)
   */
  public int getPoisson(double mean) {
    if (mean == 0) {
      return 0;
    }
    if (countGenerations) {
      poissonCount++;
    }
    return poisson.nextInt(mean);
  }

  /**
   * Gets a random number from the Normal distribution
   *
   * @return random number
   * @see Normal#nextDouble()
   */
  public double getNormal() {
    if (countGenerations) {
      normalCount++;
    }
    return normal.nextDouble();
  }

  /**
   * Gets a random number from the Normal distribution with given mean and stddev.
   *
   * @param mean   the mean
   * @param stddev the stddev
   * @return random number
   * @see Normal#nextDouble(double, double)
   */
  public double getNormal(double mean, double stddev) {
    if (countGenerations) {
      normalCount++;
    }
    return normal.nextDouble(mean, stddev);
  }


}
