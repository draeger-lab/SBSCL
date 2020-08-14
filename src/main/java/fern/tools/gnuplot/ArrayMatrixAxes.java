package fern.tools.gnuplot;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * Implementation of {@link Axes} to use with array matrixes of any number type (the type of the
 * arrays is inferred by using the reflection api). An array matrix is just a two dimensional
 * rectangular array (<code>new double[3][20]</code>). The first index of the matrix gives the row,
 * the second the column (3 rows and 20 columns).
 * <p>
 * If your array matrix is transposed (your first index denotes the column, the second one the row),
 * just use {@link TransposedArrayMatrixAxes}.
 *
 * @author Florian Erhard
 * @see Axes
 */
public class ArrayMatrixAxes extends Axes {

  /**
   * Contains the matrix array
   */
  protected Object array;

  /**
   * Create a new <code>Axes</code> object containing the given array matrix without labels /
   * styles. If the passed object is not a valid array matrix, an {@link IllegalArgumentException}
   * will be thrown.
   *
   * @param matrix array matrix
   */
  public ArrayMatrixAxes(Object matrix) {
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
  public ArrayMatrixAxes(Object matrix, String[] labels, String[] styles) {
    super();
    this.array = matrix;
    getNumColumns();
    getNumRows();
    checkMatrix();

    loadLabelsAndLineStyles(labels, styles);
  }

  /**
   * Checks if the passed object is a valid array matrix.
   */
  protected void checkMatrix() {
    int cols = Array.getLength(Array.get(array, 0));
		for (int i = 0; i < Array.getLength(array); i++) {
			if (Array.getLength(Array.get(array, i)) != cols) {
				throw new IllegalArgumentException("Obejct is not an array matrix");
			}
		}
  }

  @Override
  public int getNumColumns() {
    return Array.getLength(Array.get(array, 0)) + getNumAdditionalColumns();
  }

  @Override
  public int getNumRows() {
    return Array.getLength(array);
  }

  @Override
  protected Number getNumber_internal(int row, int col) {
    return (Number) Array.get(Array.get(array, row), col);
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
        Object o = Array.get(array, index++);
        String s = itAdd.hasNext() ? itAdd.next() : "";

        StringBuilder sb = new StringBuilder();
				for (int i = 0; i < Array.getLength(Array.get(array, 0)); i++) {
					sb.append(Array.get(o, i).toString() + "\t");
				}
				if (sb.length() > 0) {
					sb.deleteCharAt(sb.length() - 1);
				}
        return sb.toString() + s;

      }

      public void remove() {
      }

    };
  }

}
