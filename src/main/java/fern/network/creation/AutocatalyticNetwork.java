package fern.network.creation;


import java.util.HashMap;
import java.util.LinkedList;

import cern.colt.bitvector.BitVector;
import fern.network.AnnotationManagerImpl;
import fern.network.AbstractKineticConstantPropensityCalculator;
import fern.network.AbstractNetworkImpl;
import fern.network.AmountManager;
import fern.network.AnnotationManager;
import fern.network.DefaultAmountManager;
import fern.network.PropensityCalculator;
import fern.network.modification.ReversibleNetwork;
import fern.tools.NumberTools;
import fern.tools.Stochastics;
import fern.tools.functions.Probability;

/**
 * Evolve an autocatalytic network. The evolution starts at some monomers, let them aggregate (e.g.
 * A+B -> AB) by a given probability and up to a given length. Then, each reaction is catalyzed by a
 * given probability by some molecule species (the catalysts are stored as fields in the net's
 * annotation). Since the reactions are only unidirectional you have to create a {@link
 * ReversibleNetwork} out of it. Because of this, also catalysts for the reverse reactions are
 * stored in the corresponding fields.
 * <p>
 * The advantage of the unidirectional reactions is space efficiency since the
 * <code>ReversibleNetwork</code> does not copy the reactions but redirects the indices.
 * <p>
 * This network can of course be used for stochastic simulations. If it is just converted into a
 * <code>ReversibleNetwork</code>, there are just different kinetic constants used for catalyzed
 * and
 * not catalyzed reactions ({@link AutocatalyticNetwork#getCatalyzedKineticConstant} and {@link
 * AutocatalyticNetwork#getUncatalyzedKineticConstant}).
 *
 * @author Florian Erhard
 */
public class AutocatalyticNetwork extends AbstractNetworkImpl implements CatalystIterator {

  /**
   * Name of the field where catalysts are stored.
   */
  public static final String CATALYSTS_FIELD = "Catalysts";
  /**
   * Name of the field where catalysts for the reverse reactions are stored.
   */
  public static final String CATALYSTS_FIELD_REVERSIBLE =
      CATALYSTS_FIELD + ReversibleNetwork.REVERSIBLE_SUFFIX;

  private char[] monomers;
  private Probability createProb;
  private Probability catProb;
  private int maxLength;

  private int[][] shellCutPosSize = null;
  private int[] shellSize = null;
  private int[] shellSizeCumSum = null;

  private long monomerAmount = 1;
  private long otherAmount = 1;
  private double catalyzedKineticConstant = 1;
  private double uncatalyzedKineticConstant = 0.001;

  private boolean useFastMethod = true;


  /**
   * Creates the autocatalytic network from given monomers, reaction probability, catalysis
   * probability up to a given polymer length. By default, the fast (but memory consuming) method of
   * creating / catalyzing is beeing used.
   *
   * @param monomers   the monomers to start the network evolution with
   * @param createProb the reaction probability
   * @param catProb    the catalyzation probability
   * @param maxLength  the maximal polymer length
   * @see Probability
   */
  public AutocatalyticNetwork(char[] monomers, Probability createProb, Probability catProb,
      int maxLength) {
    this(monomers, createProb, catProb, maxLength, true);
  }

  /**
   * Creates the autocatalytic network from given monomers, reaction probability, catalysis
   * probability up to a given polymer length. useDefault should usually set to true unless you want
   * to evolve really huge networks. The slower method only needs O(log(V)) extra space where the
   * faster method needs O(V).
   *
   * @param monomers      the monomers to start the network evolution with
   * @param createProb    the reaction probability
   * @param catProb       the catalysis probability
   * @param maxLength     the maximal polymer length
   * @param useFastMethod what method is going to be used for creating / catalyzing
   * @see Probability
   */
  public AutocatalyticNetwork(char[] monomers, Probability createProb, Probability catProb,
      int maxLength, boolean useFastMethod) {
    super("Autocatalytic network");
    this.monomers = monomers;
    this.catProb = catProb;
    this.createProb = createProb;
    this.maxLength = maxLength;
    this.useFastMethod = useFastMethod;
    init();
  }

  private void init() {
    createAnnotationManager();
    createSpeciesMapping();
    createAdjacencyLists();
    createAmountManager();
    createPropensityCalculator();
  }

  /**
   * Creates the adjacency lists for this network.
   */
  @Override
  protected void createAdjacencyLists() {
    createShellSizes();
    int size = NumberTools.sum(shellSize);
    adjListPro = new int[size - monomers.length][];
    adjListRea = new int[size - monomers.length][];
    indexToSpeciesId = new String[size];
    speciesIdToIndex = new HashMap<String, Integer>(size);
    for (int i = 0; i < monomers.length; i++) {
      indexToSpeciesId[i] = String.valueOf(monomers[i]);
      speciesIdToIndex.put(String.valueOf(monomers[i]), i);
    }

    Offset offset = new Offset();
    offset.Species = monomers.length;

    //shells
    for (int shell = 2; shell <= maxLength; shell++) {
      //cutpos
      for (int c = 1; c < shell; c++) {
        //obtain number random molecules created by substrates of length c and shell-c
        if (useFastMethod) {
          createRandomProductsFromShellsFast(shellCutPosSize[shell][c], c, shell - c, offset);
        } else {
          createRandomProductsFromShells(shellCutPosSize[shell][c], c, shell - c, offset);
        }
      }
    }

    // catalyze
    for (int i = 0; i < getNumReactions(); i++) {
      int count = Stochastics.getInstance().getBinom(getNumSpecies(), catProb.getProb(0, 0));
      if (useFastMethod) {
        catalyzeRandomFast(count, i, CATALYSTS_FIELD_REVERSIBLE);
      } else {
        catalyzeRandom(count, i, CATALYSTS_FIELD_REVERSIBLE);
      }
      count = Stochastics.getInstance().getBinom(getNumSpecies(), catProb.getProb(0, 0));
      if (useFastMethod) {
        catalyzeRandomFast(count, i, CATALYSTS_FIELD);
      } else {
        catalyzeRandom(count, i, CATALYSTS_FIELD);
      }
    }

  }

  private void createRandomProductsFromShells(int count, int s1, int s2, Offset offset) {
    BitVector used = new BitVector(shellSize[s1] * shellSize[s2]);

    // obtain count new reactions
    for (int i = 0; i < count; i++) {
      int i1 = 0, i2 = 0;
      int productValue = -1;
      // obtain reactants
      while (productValue == -1 || used.get(productValue)) {
        i1 = Stochastics.getInstance().getUnif(0, shellSize[s1]);
        i2 = Stochastics.getInstance().getUnif(0, shellSize[s2]);
        productValue = i1 * shellSize[s2] + i2;
      }
      used.set(productValue);
      // now we have our two reactants / their creating reactions
      int r1 = s1 == 1 ? i1 : adjListPro[i1 + shellSizeCumSum[s1 - 1] - monomers.length][0];
      int r2 = s2 == 1 ? i2 : adjListPro[i2 + shellSizeCumSum[s2 - 1] - monomers.length][0];
      String product = getSpeciesName(r1) + getSpeciesName(r2);
      // maybe the product is already present
      if (!speciesIdToIndex.containsKey(product)) {
        indexToSpeciesId[offset.Species] = product;
        speciesIdToIndex.put(product, offset.Species);
        offset.Species++;
      }
      // create the reaction
      adjListRea[offset.Reaction] = new int[]{r1, r2};
      adjListPro[offset.Reaction] = new int[]{speciesIdToIndex.get(product)};
      offset.Reaction++;
    }
  }

  private void createRandomProductsFromShellsFast(int count, int s1, int s2, Offset offset) {
    int[] r = NumberTools.getNumbersTo(shellSize[s1] * shellSize[s2] - 1);
    NumberTools.shuffle(r);

    // obtain count new reactions
    for (int i = 0; i < count; i++) {
      int i1 = r[i] / shellSize[s2], i2 = r[i] % shellSize[s2];
      // now we have our two reactants / their creating reactions
      int r1 = s1 == 1 ? i1 : adjListPro[i1 + shellSizeCumSum[s1 - 1] - monomers.length][0];
      int r2 = s2 == 1 ? i2 : adjListPro[i2 + shellSizeCumSum[s2 - 1] - monomers.length][0];
      String product = getSpeciesName(r1) + getSpeciesName(r2);
      // maybe the product is already present
      if (!speciesIdToIndex.containsKey(product)) {
        indexToSpeciesId[offset.Species] = product;
        speciesIdToIndex.put(product, offset.Species);
        offset.Species++;
      }
      // create the reaction
      adjListRea[offset.Reaction] = new int[]{r1, r2};
      adjListPro[offset.Reaction] = new int[]{speciesIdToIndex.get(product)};
      offset.Reaction++;
    }
  }

  private void catalyzeRandom(int count, int reaction, String field) {
    BitVector used = new BitVector(getNumSpecies());
    StringBuilder sb = new StringBuilder(count * (maxLength / 2 + 1));
    // obtain count catalysts
    for (int i = 0; i < count; i++) {
      int cat = -1;

      while (cat == -1 || used.get(cat)) {
        cat = Stochastics.getInstance().getUnif(0, getNumSpecies());
      }

      used.set(cat);
      sb.append(getSpeciesName(cat));
      sb.append(" ");
    }
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
      annotationManager.setReactionAnnotation(reaction, field, sb.toString());
    }
  }

  private void catalyzeRandomFast(int count, int reaction, String field) {
    int[] r = NumberTools.getNumbersTo(getNumSpecies());
    NumberTools.shuffle(r);

    StringBuilder sb = new StringBuilder(count * (maxLength / 2 + 1));
    // obtain count catalysts
    for (int i = 0; i < count; i++) {
      int cat = r[i];

      sb.append(getSpeciesName(cat));
      sb.append(" ");
    }
    if (sb.length() > 0) {
      sb.deleteCharAt(sb.length() - 1);
      annotationManager.setReactionAnnotation(reaction, field, sb.toString());
    }
  }

  private void createShellSizes() {
    shellSize = new int[maxLength + 1];
    shellCutPosSize = new int[maxLength + 1][];
    shellSize[1] = monomers.length;

    //shells
    for (int shell = 2; shell <= maxLength; shell++) {
      shellCutPosSize[shell] = new int[shell];
      //cutpos
      for (int c = 1; c < shell; c++) {
        shellCutPosSize[shell][c] = Stochastics.getInstance().getBinom(
            shellSize[c] * shellSize[shell - c],
            createProb.getProb(c, shell - c));
      }
      shellSize[shell] = NumberTools.sum(shellCutPosSize[shell]);
    }

    shellSizeCumSum = new int[shellSize.length];
    System.arraycopy(shellSize, 0, shellSizeCumSum, 0, shellSize.length);
    NumberTools.cumSum(shellSizeCumSum);
  }

  /**
   * Creates the {@link AmountManager} for this network.
   */
  @Override
  protected void createAmountManager() {
    amountManager = new DefaultAmountManager(this);
  }

  /**
   * Creates the {@link AnnotationManager} for this network.
   */
  @Override
  protected void createAnnotationManager() {
    annotationManager = new AnnotationManagerImpl() {


    };
  }

  /**
   * Creates the {@link PropensityCalculator} for this network.
   */
  @Override
  protected void createPropensityCalculator() {
    propensitiyCalculator = new AbstractKineticConstantPropensityCalculator(adjListRea) {
      public double getConstant(int i) {
        return (getCatalystsPopulation(i, CATALYSTS_FIELD)) * catalyzedKineticConstant
            + uncatalyzedKineticConstant;
      }
    };
  }

  /**
   * Gets the <code>PropensityCalculator</code> which has to be used for instantiation of the {@link
   * ReversibleNetwork}.
   *
   * @return the <code>PropensityCalculator</code> for the <code>ReversibleNetwork</code>
   */
  public PropensityCalculator getReversePropensityCalculator() {
    return new AbstractKineticConstantPropensityCalculator(adjListPro) {
      public double getConstant(int i) {
        return (getCatalystsPopulation(i, CATALYSTS_FIELD_REVERSIBLE)) * catalyzedKineticConstant
            + uncatalyzedKineticConstant;
      }
    };
  }

  private double getCatalystsPopulation(int reaction, String field) {
    if (!annotationManager.containsReactionAnnotation(reaction, field)) {
      return 0;
    }
    String[] catas = annotationManager.getReactionAnnotation(reaction, field).split(" ");
    double re = 0;
    for (String cata : catas) {
      re += getAmountManager().getAmount(getSpeciesByName(cata));
    }
    return re;
  }

  @Override
  protected void createSpeciesMapping() {
    // done in createAdjacencyLists
  }

  /**
   * Implementation for the {@link CatalystIterator}. Returns the indices of catalysts for the given
   * reaction. By using <code>getAnnotationManager</code> it returns the correct catalysts even if a
   * {@link ReversibleNetwork} is used.
   *
   * @param reaction index of the reaction for which the catalysts have to be returned
   * @return the catalysts of the reaction
   */
  public Iterable<Integer> getCatalysts(int reaction) {
    String c = getAnnotationManager().getReactionAnnotation(reaction % getNumReactions(),
        reaction < getNumReactions() ? CATALYSTS_FIELD : CATALYSTS_FIELD_REVERSIBLE);
//		String c = getAnnotationManager().getReactionAnnotation(reaction,CATALYSTS_FIELD);
    LinkedList<Integer> re = new LinkedList<Integer>();
    if (c == null) {
      return re;
    }
    String[] catas = c.split(" ");
    for (int i = 0; i < catas.length; i++) {
      int s = getSpeciesByName(catas[i]);
      if (s >= 0) {
        re.add(s);
      }
    }
    return re;
  }


  private static class Offset {

    public int Reaction = 0;
    public int Species = 0;

    @Override
    public String toString() {
      return "R=" + Reaction + " S=" + Species;
    }
  }


  public long getInitialAmount(int species) {
    return (species < monomers.length) ? monomerAmount : otherAmount;
  }

  public void setInitialAmount(int species, long value) {
    if (species < monomers.length) {
      monomerAmount = value;
    } else {
      otherAmount = (int) value;
    }
  }

  /**
   * Gets if the number of monomers
   *
   * @return number of monomers
   */
  public int getNumMonomers() {
    return monomers.length;
  }

  /**
   * Gets the initial amount of the monomers for a simulation algorithm. The default is 1000.
   *
   * @return initial amount of the monomers
   */
  public long getMonomerAmount() {
    return monomerAmount;
  }

  /**
   * Sets the initial amount of the monomers for a simulation algorithm. The default is 1000.
   *
   * @param monomerAmount initial amount of the monomers
   */
  public void setMonomerAmount(long monomerAmount) {
    this.monomerAmount = monomerAmount;
  }

  /**
   * Gets the initial amount of the not-monomers for a simulation algorithm. The default is 1.
   *
   * @return initial amount of the not-monomers
   */
  public long getOtherAmount() {
    return otherAmount;
  }

  /**
   * Sets the initial amount of the not-monomers for a simulation algorithm. The default is 1.
   *
   * @param otherAmount initial amount of the not-monomers
   */
  public void setOtherAmount(long otherAmount) {
    this.otherAmount = otherAmount;
  }

  /**
   * Gets the kinetic constant for catalyzed reactions. The default is 1.
   *
   * @return the catalyzedKineticConstant
   */
  public double getCatalyzedKineticConstant() {
    return catalyzedKineticConstant;
  }

  /**
   * Sets the kinetic constant for catalyzed reactions. The default is 1.
   *
   * @param catalyzedKineticConstant the catalyzedKineticConstant to set
   */
  public void setCatalyzedKineticConstant(double catalyzedKineticConstant) {
    this.catalyzedKineticConstant = catalyzedKineticConstant;
  }

  /**
   * Gets the kinetic constant for not catalyzed reactions. The default is 0.001.
   *
   * @return the uncatalyzedKineticConstant
   */
  public double getUncatalyzedKineticConstant() {
    return uncatalyzedKineticConstant;
  }

  /**
   * Sets the kinetic constant for not catalyzed reactions. The default is 0.001.
   *
   * @param uncatalyzedKineticConstant the uncatalyzedKineticConstant to set
   */
  public void setUncatalyzedKineticConstant(double uncatalyzedKineticConstant) {
    this.uncatalyzedKineticConstant = uncatalyzedKineticConstant;
  }

}
