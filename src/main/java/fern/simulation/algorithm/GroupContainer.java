package fern.simulation.algorithm;

import fern.network.AmountManager;
import fern.network.DefaultAmountManager;
import fern.network.Network;
import fern.network.PropensityCalculator;
import fern.simulation.Simulator;
import fern.tools.Stochastics;
import java.util.Arrays;


public class GroupContainer {

  private int[][] groups;
  private int[] groupSizes;
  private double[] groupPropensitySums;

  private double min;
  private double max;
  private int[] groupOfReaction;
  private int[] positionOfReaction;
  private int topGroup = 0;

  private double a_sum;

  public GroupContainer(Simulator sim) {
    determineMaxMin(sim);

    int numReactions = sim.getNet().getNumReactions();
    int numGroups = (int) (1 + Math.log(max / min) / Math.log(2));

    groupOfReaction = new int[numReactions];
    positionOfReaction = new int[numReactions];
    Arrays.fill(groupOfReaction, -1);
    Arrays.fill(positionOfReaction, -1);

    groupSizes = new int[numGroups];
    groups = new int[numGroups][numReactions];
    groupPropensitySums = new double[numGroups];
  }

  public double getA_sum() {
    return a_sum;
  }

  private void determineMaxMin(Simulator sim) {
    Network net = sim.getNet();
    PropensityCalculator prop = net.getPropensityCalculator();
    AmountManager amount = new DefaultAmountManager(net);

    min = Double.POSITIVE_INFINITY;
    max = Double.NEGATIVE_INFINITY;
    for (int i = 0; i < net.getNumReactions(); i++) {
      for (int r : net.getReactants(i)) {
        amount.setAmount(r, 0);
      }
      for (int r : net.getReactants(i)) {
        amount.setAmount(r, amount.getAmount(r) + 1);
      }
      min = Math.min(min, prop.calculatePropensity(i, amount, sim));
      for (int r : net.getReactants(i)) {
        amount.setAmount(r, amount.getAmount(r) * 10);
      }
      max = Math.max(max, prop.calculatePropensity(i, amount, sim));
    }
  }

  public void initialize(double[] a) {
    for (int g = 0; g < groupPropensitySums.length; g++) {
      groupPropensitySums[g] = 0;
      groupSizes[g] = 0;
    }
    a_sum = 0;
    for (int i = 0; i < groupOfReaction.length; i++) {
      if (a[i] > 0) {
        a_sum += a[i];
        checkMax(a[i]);

        int g = getGroup(a[i]);
        groupOfReaction[i] = g;
        positionOfReaction[i] = groupSizes[g];
        groups[g][groupSizes[g]++] = i;
        groupPropensitySums[g] += a[i];
        topGroup = Math.max(topGroup, g);
      } else {
        groupOfReaction[i] = -1;
        positionOfReaction[i] = -1;
      }
    }
  }

  private void checkMax(double a) {
    if (a > max) {
      int g = getGroup(a);
      if (g >= groups.length) {
        int numGroups = g + 1;
        int[] n_groupSizes = new int[numGroups];
        int[][] n_groups = new int[numGroups][groupOfReaction.length];
        double[] n_groupPropensityCumSums = new double[numGroups];
        System.arraycopy(groupSizes, 0, n_groupSizes, 0, groupSizes.length);
        System.arraycopy(groups, 0, n_groups, 0, groups.length);
        System.arraycopy(groupPropensitySums, 0, n_groupPropensityCumSums, 0,
            groupPropensitySums.length);
        groupSizes = n_groupSizes;
        groups = n_groups;
        groupPropensitySums = n_groupPropensityCumSums;
      }

      max = a;
    }
  }

  public int drawReaction(Stochastics s, double[] a) {
    double t = s.getUnif() * a_sum;

    double sum = 0;
    int group;
    for (group = topGroup; group >= 0; group--) {
      sum += groupPropensitySums[group];
      if (sum >= t) {
        break;
      }
    }

    int[] currentGroup = groups[group];
    int currentGroupSize = groupSizes[group];

    int reaction;
    double p;

    if (currentGroupSize == 1) {
      reaction = 0;
    } else {
      do {
        reaction = s.getUnif(0, currentGroupSize);
        p = s.getUnif() * min * (1 << group);
      } while (p > a[currentGroup[reaction]]);
    }

    return currentGroup[reaction];
  }


  private int getGroup(double a) {
    double t = a / min;
    return (int) (Math.log(t) / Math.log(2));
  }

  public void propensityChanged(int reaction, double a_old, double a_new) {
    checkMax(a_new);

    int g = groupOfReaction[reaction];
    int p = positionOfReaction[reaction];
    if (g > -1) {
      groupPropensitySums[g] -= a_old;
      a_sum -= a_old;
    }

    int ng = a_new == 0 ? -1 : getGroup(a_new);
    if (ng != g) {
      if (g > -1) {
        groups[g][p] = groups[g][--groupSizes[g]];
        positionOfReaction[groups[g][p]] = p;
      }
      if (ng > -1) {
        groupOfReaction[reaction] = ng;
        positionOfReaction[reaction] = groupSizes[ng];
        groups[ng][groupSizes[ng]++] = reaction;
      } else {
        groupOfReaction[reaction] = -1;
        positionOfReaction[reaction] = -1;
      }

    }
    if (ng > -1) {
      groupPropensitySums[ng] += a_new;
      a_sum += a_new;
    }
    if (ng > g && ng > topGroup) {
      topGroup = ng;
    } else if (ng < g) {
      for (; topGroup > 0 && groupSizes[topGroup] == 0; topGroup--) {
        ;
      }
    }
  }


}
