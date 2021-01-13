package fern.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cern.colt.bitvector.BitVector;

/**
 * Contains some common methods to handle number related data structures.
 *
 * @author Florian Erhard
 */
public class NumberTools {

  /**
   * Gets the faculty
   *
   * @param i the number
   * @return faculty(i)
   */
  public static int faculty(int i) {
    return i == 1 ? 1 : i * faculty(i - 1);
  }

  /**
   * Gets the maximal value within the given {@link Iterable}.
   *
   * @param l the iterable
   * @return the maximal number
   */
  public static int max(Iterable<Integer> l) {
    int max = Integer.MIN_VALUE;
    for (int i : l) {
      max = Math.max(max, i);
    }
    return max;
  }

  /**
   * Gets the minimal value within the given {@link Iterable}.
   *
   * @param l the iterable
   * @return the minimal number
   */
  public static int min(Iterable<Integer> l) {
    int min = Integer.MAX_VALUE;
    for (int i : l) {
      min = Math.min(min, i);
    }
    return min;
  }

  /**
   * Gets the argmax of the given map. The argmax is the key whose value is the maximum within the
   * values.
   *
   * @param l map
   * @return argmax(map)
   */
  public static int argMax(Map<Integer, Integer> l) {
    int max = Integer.MIN_VALUE;
    int arg = 0;
    for (Integer key : l.keySet()) {
      if (l.get(key) > max) {
        max = l.get(key);
        arg = key;
      }
    }
    return arg;
  }

  /**
   * Gets the sum of the values in the array.
   *
   * @param l the array
   * @return sum of the values
   */
  public static int sum(int[] l) {
    int sum = 0;
    for (int i = 0; i < l.length; i++) {
      sum += l[i];
    }
    return sum;
  }

  /**
   * Replaces the values in the given array with the cumulative sum of the array.
   *
   * @param l the array
   */
  public static void cumSum(int[] l) {
    for (int i = 1; i < l.length; i++) {
      l[i] += l[i - 1];
    }
  }

  /**
   * Copies the values of the given collection and unboxes them to an array.
   *
   * @param list collection
   * @return array with the same values
   */
  public static int[] toIntArray(Collection<Integer> list) {
    int[] re = new int[list.size()];
    int index = 0;
    for (Integer i : list) {
      re[index++] = i.intValue();
    }
    return re;
  }


  /**
   * Copies the values of the given collection and unboxes them to an array.
   *
   * @param list collection
   * @return array with the same values
   */
  public static double[] toDoubleArray(Collection<Double> list) {
    double[] re = new double[list.size()];
    int index = 0;
    for (Double i : list) {
      re[index++] = i.doubleValue();
    }
    return re;
  }

  /**
   * Creates the inverse array to the given array. In the inverse array a' of a following condition
   * holds:
   * <p>
   * <code> a[i]=j <=>  a'[j]=i<code>
   * <p>
   * Be careful with sparse arrays, the inverse array could be very large (prefer {@link
   * NumberTools#createInverseAsMap(int[])}. It should preferably be used with bijective arrays.
   *
   * @param a the array
   * @return the inverse
   */
  public static int[] createInverse(int[] a) {
    int max = 0;
    for (int i = 0; i < a.length; i++) {
      if (a[i] < 0) {
        throw new IllegalArgumentException("Each element in a has to be notnegative!");
      } else {
        max = Math.max(max, a[i]);
      }
    }
    int[] re = new int[max + 1];
    Arrays.fill(re, -1);
    for (int i = 0; i < a.length; i++) {
      if (re[a[i]] != -1) {
        throw new IllegalArgumentException("The values in a have to be unique!");
      } else {
        re[a[i]] = i;
      }
    }
    return re;
  }

  /**
   * Creates the inverse map to the given array. In the inverse map a' of a following condition
   * holds:
   * <p>
   * <code> a[i]=j <=>  a'.get(j)=i<code>
   *
   * @param a the array
   * @return the inverse map
   */
  public static Map<Integer, Integer> createInverseAsMap(int[] a) {
    Map<Integer, Integer> re = new HashMap<>();
    for (int i = 0; i < a.length; i++) {
      if (!re.containsKey(a[i])) {
        re.put(a[i], i);
      } else {
        throw new IllegalArgumentException("The values in a have to be unique!");
      }
    }
    return re;
  }

  /**
   * Creates a histogram from the given data. The returned array matrix contains in the first column
   * the x axis (if the first index of the array matrix denotes the column index) The other columns
   * contain the frequencies of values containing to the corresponding bin of the x axis.
   *
   * @param data    data to create the histogram for
   * @param numBins number of bins, the return array matrix will have numBins rows
   * @param indices the indices of the data to create the histogram for
   * @return histogram
   */
  public static double[][] createHistogram(Collection<double[]> data, int numBins, int... indices) {
    double min = Double.POSITIVE_INFINITY;
    double max = Double.NEGATIVE_INFINITY;
    for (double[] d : data) {
      if (indices.length > 0) {
        for (int index : indices) {
          min = Math.min(min, d[index]);
          max = Math.max(max, d[index]);
        }
      } else {
        for (int index = 0; index < d.length; index++) {
          min = Math.min(min, d[index]);
          max = Math.max(max, d[index]);
        }
      }
    }

    double binSize = (max - min) / (double) numBins;
    double[][] re = new double[numBins][indices.length > 0 ? indices.length + 1
        : data.iterator().next().length + 1];
    for (int i = 0; i < re.length; i++) {
      re[i][0] = min + (i + 1) * binSize;
    }

    for (double[] d : data) {
      if (indices.length > 0) {
        for (int i = 0; i < indices.length; i++) {
          re[Math.min((int) ((d[indices[i]] - min) / binSize), re.length - 1)][i + 1]++;
        }
      } else {
        for (int i = 0; i < d.length; i++) {
          re[Math.min((int) ((d[i] - min) / binSize), re.length - 1)][i + 1]++;
        }
      }
    }
    return re;
  }

  /**
   * Create a histogram for the given array. Be careful with sparse arrays, prefer {@link
   * NumberTools#createHistogramAsMap(int[])}.
   *
   * @param a the array to create a histogram for
   * @return histogram for the array
   */
  public static int[] createHistogram(int[] a) {
    if (a == null || a.length == 0) {
      return a;
    }

    int max = a[0];
    for (int i = 1; i < a.length; i++) {
      if (a[i] < 0) {
        throw new IllegalArgumentException("Negative numbers are not allowed!");
      } else {
        max = Math.max(max, a[i]);
      }
    }

    int[] re = new int[max + 1];
    for (int i = 0; i < a.length; i++) {
      re[a[i]]++;
    }
    return re;
  }

  /**
   * Create a histogram map for the given array.
   *
   * @param a the array to create a histogram for
   * @return histogram for the array
   */
  public static Map<Integer, Integer> createHistogramAsMap(int[] a) {
    Map<Integer, Integer> re = new HashMap<>();
    if (a == null || a.length == 0) {
      return re;
    }

    for (int i = 0; i < a.length; i++) {
      if (!re.containsKey(a[i])) {
        re.put(a[i], 0);
      }
      re.put(a[i], re.get(a[i]) + 1);
    }
    return re;
  }

  /**
   * Writes a histogram to a file. Then it can be loaded by <code> loadHistogram</code>.
   *
   * @param histo the histogram to save
   * @param file  the file
   * @throws IOException
   */
  public static void saveHistogram(Map<Integer, Integer> histo, File file) throws IOException {
    FileWriter fw = new FileWriter(file);
    fw.write(getHistogramAsString(histo));
    fw.close();
  }

  /**
   * Reads a previously saved histogram from a file.
   *
   * @param file the file
   * @return the histogram
   * @throws IOException
   */
  public static Map<Integer, Integer> loadHistogram(File file) throws IOException {
    BufferedReader bf = new BufferedReader(new FileReader(file));
    Map<Integer, Integer> re = new HashMap<>();
    String line;
    while ((line = bf.readLine()) != null) {
      String[] vals = line.split("\t");
      if (vals.length != 2) {
        throw new IOException("Illegal file format");
      }
      re.put(Integer.parseInt(vals[0]), Integer.parseInt(vals[1]));
    }
    return re;
  }


  /**
   * Gets a string representation of the histogram map.
   *
   * @param h histogram
   * @return string representation
   */
  public static String getHistogramAsString(Map<Integer, Integer> h) {
    StringBuilder sb = new StringBuilder();
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    for (int i : h.keySet()) {
      min = Math.min(min, i);
      max = Math.max(max, i);
    }

    for (int i = min; i <= max; i++) {
      sb.append(i + "\t" + (h.containsKey(i) ? h.get(i) : 0) + "\n");
    }
    return sb.toString();
  }

  /**
   * Gets the content of the given array as {@link BitVector}, which means, that the
   * <code>BitVector</code> will have a size equal to the maximal value in the array and contains a
   * 1 for each element which is contained at least once in the array
   *
   * @param a array
   * @return <code>BitVector> for the array
   */
  public static BitVector getContentAsBitVector(int[] a) {
    int max = a[0];
    for (int i = 1; i < a.length; i++) {
      max = Math.max(max, a[i]);
    }
    BitVector re = new BitVector(max + 1);
    for (int i = 0; i < a.length; i++) {
      re.set(a[i]);
    }
    return re;
  }

  /**
   * Gets the indices of the set bits of the given {@link BitVector} as array.
   *
   * @param bv the <code>BitVector</code>
   * @return array with the set indices
   */
  public static int[] getContentAsArray(BitVector bv) {
    int[] re = new int[bv.cardinality()];

    int bvIndex = 0;
    for (int i = 0; i < re.length; i++) {
      bvIndex = bv.indexOfFromTo(bvIndex, bv.size() - 1, true);
      re[i] = bvIndex++;
    }

    return re;
  }

  /**
   * Shuffles the content of the given array.
   *
   * @param a array
   */
  public static void shuffle(int[] a) {
    int d, j;
    for (int i = 0; i < a.length; i++) {
      j = Stochastics.getInstance().getUnif(0, a.length);
      d = a[i];
      a[i] = a[j];
      a[j] = d;
    }
  }

  /**
   * Performs a deep cast of an int[].
   *
   * @param a the array
   * @return the casted array
   */
  public static double[] convertIntToDouble(int[] a) {
    double[] re = new double[a.length];
    for (int i = 0; i < a.length; i++) {
      re[i] = a[i];
    }
    return re;
  }

  /**
   * Gets the numbers from 0 to n in an array.
   *
   * @param n maximal number
   * @return array of numbers
   */
  public static int[] getNumbersTo(int n) {
    int[] re = new int[n + 1];
    for (int i = 0; i < re.length; i++) {
      re[i] = i;
    }
    return re;
  }

  /**
   * Calculates the histogram distance of two histograms.
   * <p>
   * For reference see Cao & Petzold, Accuracy limitations and the measurement of errors in the
   * stochastic simulation of chemically reacting systems, Journal of Computational Physics 212
   * (2006) 6�24
   *
   * @param h1 first histogram
   * @param h2 second histogram
   * @return histogram distance
   */
  public static double calculateHistogramDistance(Map<Integer, Integer> h1,
      Map<Integer, Integer> h2) {
    int min = Integer.MAX_VALUE;
    int max = Integer.MIN_VALUE;
    double s1 = 0;
    double s2 = 0;

    for (int i : h1.keySet()) {
      min = Math.min(min, i);
      max = Math.max(max, i);
      s1 += h1.get(i);
    }
    for (int i : h2.keySet()) {
      min = Math.min(min, i);
      max = Math.max(max, i);
      s2 += h2.get(i);
    }

    double sum = 0;
    for (int i = min; i <= max; i++) {
      sum += Math
          .abs((h1.containsKey(i) ? h1.get(i) : 0) / s1 - (h2.containsKey(i) ? h2.get(i) : 0) / s2);
    }
    return sum;
  }

  /**
   * Gets the avg of the array
   *
   * @param a array
   * @return avg(array)
   */
  public static double avg(double[] a) {
    double s = 0;
    for (double d : a) {
      s += d;
    }
    return s / a.length;
  }

  /**
   * Gets the standard deviation of the array
   *
   * @param a array
   * @return stddev(array)
   */
  public static double stddev(double[] a) {
    double avg = avg(a);
    double s = 0;
    for (double d : a) {
      s += (d - avg) * (d - avg);
    }
    return Math.sqrt(s / (a.length - 1));
  }

  /**
   * Inverts the given array in place and returns it.
   *
   * @param a array
   * @return inverted array
   */
  public static String[] inverse(String[] a) {
    String d;
    for (int i = 0; i < a.length / 2; i++) {
      d = a[i];
      a[i] = a[a.length - 1 - i];
      a[a.length - 1 - i] = d;
    }
    return a;
  }

  /**
   * Joins the array with the given glue to one String.
   *
   * @param a    array
   * @param glue glue
   * @return joined array
   */
  public static String join(String[] a, String glue) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < a.length; i++) {
      sb.append(a[i]);
      if (i < a.length - 1) {
        sb.append(glue);
      }
    }
    return sb.toString();
  }

  /**
   * Performs a linear interpolation of the values. The array time and the array values must have
   * the same length which means that each element in time is associated with one element in values.
   * The time interval is rescaled to match the given interval and the corresponding values are
   * interpolated.
   *
   * @param interval the interval
   * @param time     time array
   * @param value    value array
   * @return interpolated value array
   */
  public static double[] interpolateLinear(double interval, double[] time, double[] value) {
    int firstIndex = getIntervalIndex(time[0], interval);
    int lastIndex = getIntervalIndex(time[time.length - 1], interval);

    double[] re = new double[lastIndex - firstIndex + 1];

    // search for the moments in time[] that fit best to the intervals
    // j points to the element in time[], which fits best to the corresponding time
    int j = 0;
    for (int i = 0; i < re.length; i++) {
      double desiredTime = (i + firstIndex) * interval;
      while (j + 1 < time.length && Math.abs(desiredTime - time[j]) > Math
          .abs(desiredTime - time[j + 1])) {
        j++;
      }

      int a, b;
      if (time[j] < desiredTime) {
        a = b = j;
        while (time[a] <= time[b] && a < time.length - 1) {
          a++;
        }
      } else {
        a = b = j;
        while (time[a] <= time[b] && b > 0) {
          b--;
        }
      }

      if (a == b || time[a] < desiredTime) {
        re[i] = value[a];
      } else {
        re[i] = NumberTools.interpolateLinear(desiredTime, time[b], time[a], value[b], value[a]);
      }
    }

    return re;

  }

  /**
   * Interpolates a value
   *
   * @param desiredTime
   * @param beforeTime
   * @param afterTime
   * @param beforeValue
   * @param afterValue
   * @return interpolated value
   */
  public static double interpolateLinear(double desiredTime, double beforeTime, double afterTime,
      double beforeValue, double afterValue) {
    if (desiredTime < beforeTime || desiredTime > afterTime) {
      throw new IllegalArgumentException(
          "Cannot interpolate " + desiredTime + " in [" + beforeTime + ":" + afterTime + "]");
    }

    double b = desiredTime - beforeTime;
    double a = afterTime - desiredTime;
    return beforeValue * a / (a + b) + afterValue * b / (a + b);
  }

  private static int getIntervalIndex(double time, double interval) {
    return (int) Math.round(time / interval);
  }


}
