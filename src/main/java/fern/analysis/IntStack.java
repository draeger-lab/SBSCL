package fern.analysis;

import cern.colt.list.IntArrayList;

/**
 * IntStack is an search structure for {@link AnalysisBase} representing an lifo stack for a depth
 * first search.
 *
 * @author Florian Erhard
 */
public class IntStack extends IntArrayList implements IntSearchStructure {

  private static final long serialVersionUID = 1L;

  public IntStack() {
    super();
  }

  public IntStack(int initialSize) {
    super(initialSize);
  }

  public int get() {
    int re = super.get(size() - 1);
    remove(size() - 1);
    return re;
  }

}
