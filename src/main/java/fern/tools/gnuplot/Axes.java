package fern.tools.gnuplot;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Contains the data columns for {@link GnuPlot}. <code>GnuPlot</code> has a list of
 * <code>Axes</code> objects and assumes that each of these <code>Axes</code> object has one
 * x column (the first one), optionally one y column and n value columns.
 * <p>
 * The main access point to the data is the {@link Iterable} interface which yields the tab
 * separated values of each columns row by row.
 * <p>
 * It is possible to add other <code>Axes</code> object to an <code>Axes</code> object. So If you
 * call the {@link Axes#iterator()}, all of the columns of each attached <code>Axes
 * </code> object will be yielded. Hence attached <code>Axes</code> objects must have the
 * same number of rows (if they don't, an {@link IllegalArgumentException} will be thrown).
 * <p>
 * This class also manages labels and styles for each column. If you specify either of them, then
 * either the length must be equal to the number of columns or 1 shorter (then the
 * <code>Axes</code> objects knows that its first column is an x column).
 * <p>
 * The content of labels is directly passed to the <code>plot</code> command as <code>title
 * "label"</code>, the content of styles also is attached to the corresponding place in the
 * <code>plot</code> command (e.g. styles[i]="with linespoints").
 *
 * @author Florian Erhard
 */
public abstract class Axes implements Iterable<String> {

  public static final int TwoD = 0;
  public static final int ThreeD = 1;
  public static final int Additional = 2;

  private Collection<Axes> additionalAxes = null;
  private String[] labels = null;
  private String[] styles = null;
  private boolean hasXAxis;
  private boolean hasYAxis;

  public Axes() {
    additionalAxes = new LinkedList<>();
  }

  /**
   * Adds new labels and styles to the axes object. If either of them is not <code>null</code> then
   * the length must be equal to the number of columns or 1 shorter.
   *
   * @param labels labels for the columns
   * @param styles styles for the columns
   */
  protected void loadLabelsAndLineStyles(String[] labels, String[] styles) {
    int numLabels = labels == null ? 0 : labels.length;
    int dif = getNumColumns() - numLabels;

    if (labels != null) {
      if (dif >= 0 && dif <= 2) {
        this.labels = labels;
      } else {
        throw new IllegalArgumentException(
            "Length of labels is not equal to the number of columns");
      }
    } else {
      this.labels = new String[getNumColumns()];
    }

    if (styles != null) {
      if (styles.length == this.labels.length) {
        this.styles = styles;
      } else {
        throw new IllegalArgumentException("Length of styles is not equal to the number of labels");
      }
    } else {
      this.styles = new String[this.labels.length];
    }

    hasXAxis = dif >= 1;
    hasYAxis = dif == 2;
  }

  /**
   * Gets if this Axes object contains 2 or 3 dimensional data. If it is an additional axes object
   * of another, it does not have a dimension. The dimension is infered by inspecting the number of
   * labels and styles given.
   *
   * @return
   * @see Axes#TwoD
   * @see Axes#ThreeD
   * @see Axes#Additional
   */
  public int getDimensionType() {
    if (hasXAxis && hasYAxis) {
      return ThreeD;
    } else if (hasXAxis) {
      return TwoD;
    } else {
      return Additional;
    }
  }

  /**
   * Attaches an <code>Axes</code> object to this one. It cannot have an <code>Axes</code> object
   * with an x axis (which means that the labels/styles array is 1 shorter then the number of
   * columns).
   * <p>
   * Additionally the numbers of rows must be equal.
   *
   * @param axes the <code>Axes</code> object to attach
   */
  public void addAxes(Axes axes) {
    if (axes.hasXAxis || axes.hasYAxis) {
      throw new IllegalArgumentException(
          "You cannot add an axes object with a x axis to another axes object!");
    }
    if (getNumRows() != axes.getNumRows()) {
      throw new IllegalArgumentException("Number of rows is not equal!");
    }
    additionalAxes.add(axes);

  }

  /**
   * Gets the number of columns. The number of the attached <code>Axes</code> object is also
   * included.
   *
   * @return number of columns.
   */
  public abstract int getNumColumns();

  /**
   * Gets the number of rows.
   *
   * @return number of rows.
   */
  public abstract int getNumRows();

  /**
   * Gets the entry of this axes at the specified position.
   *
   * @param row the row of the entry
   * @param col the col of the entry
   * @return the entry
   */
  protected abstract Number getNumber_internal(int row, int col);

  public Number getX(int row) {
    if (hasXAxis) {
      return getNumber(row, -getNumLabelAxes());
    } else {
      throw new RuntimeException("This axes object does not have an x axis!");
    }
  }


  /**
   * Gets the entry of this axes at the specified position.
   *
   * @param row the row of the entry
   * @param col the col of the entry
   * @return the entry
   */
  public Number getNumber(int row, int col) {
    col += getNumLabelAxes();
    return getNumber_internal(row, col);
  }

  /**
   * Gets the number of label axes (e.g. =2 if this object has a x and a y axis).
   *
   * @return
   */
  protected int getNumLabelAxes() {
    int re = 0;
    if (hasXAxis) {
      re++;
    }
    if (hasYAxis) {
      re++;
    }
    return re;
  }

  /**
   * Sets the label for a column. If this <code>Axes</code> object has a x axis, the label of the
   * 0th column cannot be set (because it is the x axis).
   *
   * @param col   zero based index of the column
   * @param label label for the column
   */
  public void setLabel(int col, String label) {
    col -= getNumLabelAxes();
    int column = col;
    if (col >= labels.length) {
      col -= labels.length;
      for (Axes a : additionalAxes) {
        if (col < a.getNumColumns()) {
          a.setLabel(col, label);
          return;
        }
        col -= a.getNumColumns();
      }
      throw new IllegalArgumentException(
          "There are only " + getNumColumns() + " columns, you can't set the " + column
              + "th column label!");
    } else {
      labels[col] = label;
    }
  }

  /**
   * Gets the label for a column. If this <code>Axes</code> object has a x axis, the label of the
   * 0th column cannot be retrieved (because it is the x axis).
   *
   * @param col zero based index of the column
   * @return label for the column
   */
  public String getLabel(int col) {
    if (hasXAxis) {
      col--;
    }
    if (hasYAxis) {
      col--;
    }
    int column = col;
    if (col >= labels.length) {
      col -= labels.length;
      for (Axes a : additionalAxes) {
        if (col < a.getNumColumns()) {
          return a.getLabel(col);
        }
        col -= a.getNumColumns();
      }
      throw new IllegalArgumentException(
          "There are only " + getNumColumns() + " columns, you can't get the " + column
              + "th column label!");
    } else {
      return labels == null || labels[col] == null ? "" : labels[col];
    }
  }

  /**
   * Sets the style for a column. If this <code>Axes</code> object has a x axis, the style of the
   * 0th column cannot be set (because it is the x axis).
   *
   * @param col   zero based index of the column
   * @param style style for the column
   */
  public void setStyle(int col, String style) {
    if (hasXAxis) {
      col--;
    }
    if (hasYAxis) {
      col--;
    }
    int column = col;
    if (col >= styles.length) {
      col -= styles.length;
      for (Axes a : additionalAxes) {
        if (col < a.getNumColumns()) {
          a.setStyle(col, style);
          return;
        }
        col -= a.getNumColumns();
      }
      throw new IllegalArgumentException(
          "There are only " + getNumColumns() + " columns, you can't set the " + column
              + "th column linestyle!");
    } else {
      styles[col] = style;
    }
  }

  /**
   * Gets the style for a column. If this <code>Axes</code> object has a x axis, the style of the
   * 0th column cannot be retrieved (because it is the x axis).
   *
   * @param col zero based index of the column style			style for the column
   */
  public String getStyle(int col) {
    if (hasXAxis) {
      col--;
    }
    if (hasYAxis) {
      col--;
    }
    int column = col;
    if (col >= styles.length) {
      col -= styles.length;
      for (Axes a : additionalAxes) {
        if (col < a.getNumColumns()) {
          return a.getStyle(col);
        }
        col -= a.getNumColumns();
      }
      throw new IllegalArgumentException(
          "There are only " + getNumColumns() + " columns, you can't get the " + column
              + "th column linestyle!");
    } else {
      return styles == null || styles[col] == null ? "" : styles[col];
    }
  }

  /**
   * Sets the style of each column to the default style except for columns that already have a
   * style.
   *
   * @param defaultStyle the default style to set
   */
  public void applyDefaultStyle(String defaultStyle) {
    for (int i = 0; i < styles.length; i++) {
      if (styles[i] == null || styles[i].length() == 0) {
        styles[i] = defaultStyle;
      }
    }
  }

  /**
   * Gets the number of columns of the attached <code>Axes</code> objects.
   *
   * @return number of attached columns
   */
  protected int getNumAdditionalColumns() {
    int re = 0;
    for (Axes a : additionalAxes) {
      re += a.getNumColumns();
    }
    return re;
  }

  /**
   * Iterator for the attached <code>Axes</code> objects.
   *
   * @return iterator
   */
  @SuppressWarnings("unchecked")
  protected Iterator<String> getAdditionalAxesIterator() {
    final Iterator<String>[] it = new Iterator[additionalAxes.size()];
    int index = 0;
    for (Axes a : additionalAxes) {
      it[index++] = a.iterator();
    }
    return new Iterator<String>() {

      public boolean hasNext() {
        for (int i = 0; i < it.length; i++) {
          if (it[i].hasNext()) {
            return true;
          }
        }
        return false;
      }

      public String next() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < it.length; i++) {
          if (it[i].hasNext()) {
            sb.append("\t" + it[i].next());
          }
        }
        return sb.toString();
      }

      public void remove() {
      }

    };
  }


}
