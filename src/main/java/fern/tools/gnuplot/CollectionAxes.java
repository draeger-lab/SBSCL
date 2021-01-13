package fern.tools.gnuplot;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Implementation of {@link Axes} to use with {@link Collection}s or {@link Iterable}s of any number
 * type or array of numbers (the type of the collection is inferred by using the reflection api).
 *
 * @author Florian Erhard
 * @see Axes
 */
@SuppressWarnings("unchecked")
public class CollectionAxes extends Axes {


  private Collection collection = null;

  /**
   * Create a new <code>Axes</code> object containing the given collection without labels /styles.
   *
   * @param collection the collection
   */
  public CollectionAxes(Collection collection) {
    this(collection, null, null);
  }

  /**
   * Create a new <code>Axes</code> object containing a collection (which is copied from the given
   * iterable) without labels /styles.
   *
   * @param iterable the iterable
   */
  public CollectionAxes(Iterable iterable) {
    this(iterable, null, null);
  }

  /**
   * Create a new <code>Axes</code> object containing the given collection with the given labels
   * /styles (either can be <code>null</code>).
   *
   * @param collection the collection
   * @param labels     labels for each column
   * @param styles     styles for each column
   */
  public CollectionAxes(Collection collection, String[] labels, String[] styles) {
    super();
    this.collection = collection;
    checkCollection();
    loadLabelsAndLineStyles(labels, styles);
  }

  /**
   * Create a new <code>Axes</code> object containing a collection (which is copied from the given
   * iterable) with the given labels /styles (either can be <code>null</code>).
   *
   * @param iterable the iterable
   * @param labels   labels for each column
   * @param styles   styles for each column
   */
  public CollectionAxes(Iterable iterable, String[] labels, String[] styles) {
    super();
    this.collection = new LinkedList();
    for (Object o : iterable) {
      collection.add(o);
    }
    checkCollection();
    loadLabelsAndLineStyles(labels, styles);
  }

  private void checkCollection() {
    if (collection.size() == 0) {
      return;
    }
    Object o = collection.iterator().next();
    if (o instanceof Number) {
      return;
    }
    try {
      if (!(Array.get(o, 0) instanceof Number)) {
        throw new Exception();
      }
    } catch (Exception e) {
      throw new IllegalArgumentException(
          o.toString() + " is not a number nor an array of numbers!");
    }
    // check if arrays in collection have same size
    int cols = getNumColumns();
    for (Object o2 : collection) {
      if (Array.getLength(o2) != cols) {
        throw new IllegalArgumentException(
            "The arrays in the collection do not have the same length!");
      }
    }
  }

  /**
   * Yields the tab separated columns row by row.
   */
  public Iterator<String> iterator() {
    return new Iterator<String>() {

      @SuppressWarnings("unchecked")
      Iterator it = collection.iterator();
      Iterator<String> itAdd = getAdditionalAxesIterator();

      public boolean hasNext() {
        return it.hasNext();
      }

      public String next() {
        Object o = it.next();
        String s = itAdd.hasNext() ? itAdd.next() : "";

        if (o instanceof Number) {
          return o.toString() + s;
        } else {
          StringBuilder sb = new StringBuilder();
          for (int i = 0; i < Array.getLength(o); i++) {
            sb.append(Array.get(o, i).toString() + "\t");
          }
          if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
          }
          return sb.toString() + s;
        }
      }

      public void remove() {
      }

    };
  }


  @Override
  public int getNumColumns() {
    if (collection.size() == 0) {
      return 0;
    }
    Object o = collection.iterator().next();
    if (o instanceof Number) {
      return 1 + getNumAdditionalColumns();
    } else {
      return Array.getLength(o) + getNumAdditionalColumns();
    }
  }


  @Override
  public int getNumRows() {
    return collection.size();
  }

  @Override
  protected Number getNumber_internal(int row, int col) {
    if (row >= getNumRows()) {
      throw new IllegalArgumentException("Axes does not contain enough rows.");
    }
    Object o = null;
    Iterator<Object> it = collection.iterator();
    for (int r = 0; r < row; r++) {
      o = it.next();
    }
    return (Number) Array.get(o, col);
  }

}
