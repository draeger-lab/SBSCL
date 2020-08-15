package fern.tools.gnuplot;

import java.lang.reflect.Array;
import java.util.Iterator;


/**
 * Implementation of {@link Axes} to use with transposed array matrixes of any number type (the type
 * of the arrays is inferred by using the reflection api). An array matrix is just a two dimensional
 * rectangular array (<code>new double[20][3]</code>). The first index of the transposed matrix
 * gives the column, the second the row (3 rows and 20 columns).
 * <p>
 * If your array matrix is not transposed (your first index denotes the row, the second one the
 * column), just use {@link ArrayMatrixAxes}.
 *
 * @author Florian Erhard
 * @see Axes
 */
public class TransposedArrayMatrixAxes extends ArrayMatrixAxes {


  /**
   * Create a new <code>Axes</code> object containing the given array matrix without labels /
   * styles. If the passed object is not a valid array matrix, an {@link IllegalArgumentException}
   * will be thrown.
   *
   * @param matrix array matrix
   */
  public TransposedArrayMatrixAxes(Object matrix) {
    this(matrix, null, null);
  }

  /**
   * Create a new <code>Axes</code> object containing the given array matrix with the given labels
   * /styles (either can be <code>null</code>). If the passed object is not a valid array matrix, an
   * {@link IllegalArgumentException} will be thrown.
   *
   * @param matrix the array matrix
   * @param labels the labels
   * @param styles the styles
   */
  public TransposedArrayMatrixAxes(Object matrix, String[] labels, String[] styles) {
    super(matrix, labels, styles);

  }


  @Override
  public Number getNumber(int row, int col) {
    return super.getNumber(col + getNumLabelAxes(), row - getNumLabelAxes());
  }

  @Override
  public int getNumRows() {
    return Array.getLength(Array.get(array, 0));
  }

  @Override
  public int getNumColumns() {
    return Array.getLength(array) + getNumAdditionalColumns();
  }

  /**
   * Yields the tab separated columns row by row.
   */
  public Iterator<String> iterator() {
    return new Iterator<String>() {

      @SuppressWarnings("unchecked")
      int index = 0;
      Iterator<String> itAdd = getAdditionalAxesIterator();

      public boolean hasNext() {
        return index < getNumRows();
      }

      public String next() {
        String s = itAdd.hasNext() ? itAdd.next() : "";

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Array.getLength(array); i++) {
          sb.append(Array.get(Array.get(array, i), index).toString() + "\t");
        }
        if (sb.length() > 0) {
          sb.deleteCharAt(sb.length() - 1);
        }
        index++;
        return sb.toString() + s;

      }

      public void remove() {
      }

    };
  }


}
