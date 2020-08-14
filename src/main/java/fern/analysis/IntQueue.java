package fern.analysis;

import cern.colt.list.IntArrayList;

/**
 * IntQueue is an search structure for {@link AnalysisBase} representing an fifo queue for a breadth
 * first search.
 *
 * @author Florian Erhard
 */
public class IntQueue extends IntArrayList implements IntSearchStructure {

  private static final long serialVersionUID = 1L;

  public IntQueue() {
    super();
  }

  public IntQueue(int initialCapacity) {
    super(initialCapacity);
  }

  public int get() {
    int re = super.get(0);
    remove(0);
    return re;
  }

}
