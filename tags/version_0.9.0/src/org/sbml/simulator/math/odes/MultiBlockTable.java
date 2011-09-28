/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of SBMLsimulator, a Java-based simulator for models
 * of biochemical processes encoded in the modeling language SBML.
 *
 * Copyright (C) 2007-2011 by the University of Tuebingen, Germany.
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.sbml.simulator.math.odes;

import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import org.sbml.simulator.math.odes.MultiBlockTable.Block.Column;

/**
 * <p>
 * This data structure contains the an array of sorted time points and a matrix
 * organized in one column per quantity for which data (measurement or
 * simulation) are gathered. Each row in the matrix corresponds to one time
 * point. Hence, the array of time points must be equal to the number of rows in
 * the matrix. To be able to identify the content of each column, this data
 * structure also contains an array of identifiers for each column.
 * </p>
 * <p>
 * To be displayed in a graphical user interface, this object extends
 * {@link AbstractTableModel}. Note that the access to the elements in this
 * object therefore puts both elements together, i.e., the time column is
 * considered to be the first column in the table.
 * </p>
 * 
 * @author Andreas Dr&auml;ger
 * @date 2010-08-26
 * @version $Rev$
 * @since 1.0
 */
public class MultiBlockTable extends AbstractTableModel implements
		Iterable<Iterable<Double>> {

	/**
	 * A {@link Block} is a data structure with a two-dimensional double array
	 * of actual data together with identifiers for each column.
	 * 
	 * @author Andreas Dr&auml;ger
	 * @date 2010-08-31
	 */
	public class Block extends AbstractTableModel {
		
		/**
		 * A column of the {@link MultiBlockTable.Block} matrix.
		 * 
		 * @author Andreas Dr&aumlg;ger
		 * @date 2010-08-26
		 */
		public class Column implements Iterable<Double> {

			/**
			 * The index of this column.
			 */
			private int columnIndex;

			/**
			 * Creates a new {@link Column} object for the column with the given
			 * index.
			 * 
			 * @param columnIndex
			 */
			private Column(int columnIndex) {
				if ((columnIndex < 0) || (getColumnCount() <= columnIndex)) {
					throw new IndexOutOfBoundsException(Integer
							.toString(columnIndex));
				}
				this.columnIndex = columnIndex;
			}

			/**
			 * Delivers the {@link Column} identifier of this particular column.
			 * 
			 * @return The {@link String} that identifies this {@link Column}.
			 */
			public String getId() {
				return identifiers[columnIndex];
			}

			/**
			 * Gives the number of rows in this {@link Column}.
			 * 
			 * @return
			 */
			public int getRowCount() {
				return data.length;
			}

			/**
			 * Access to the given row in this column.
			 * 
			 * @param rowIndex
			 * @return
			 */
			public double getValue(int rowIndex) {
				return data[rowIndex][columnIndex];
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Iterable#iterator()
			 */
			public Iterator<Double> iterator() {
				return new Iterator<Double>() {

					/**
					 * To memorize the current position within the column.
					 */
					private int currRow = 0;

					/*
					 * (non-Javadoc)
					 * 
					 * @see java.util.Iterator#hasNext()
					 */
					public boolean hasNext() {
						return currRow < getRowCount();
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see java.util.Iterator#next()
					 */
					public Double next() {
						return Double.valueOf(getValue(currRow++));
					}

					/*
					 * (non-Javadoc)
					 * 
					 * @see java.util.Iterator#remove()
					 */
					public void remove() {
						// don't remove anything here!
						throw new UnsupportedOperationException(
								"cannot remove anything from the underlying object");
					}
				};
			}

			/**
			 * Change the entry at the given row in this {@link Column}.
			 * 
			 * @param rowIndex
			 *            The row where to change
			 * @param doubleValue
			 *            The new value.
			 * 
			 */
			public void setValue(double doubleValue, int rowIndex) {
				data[rowIndex][columnIndex] = ((Double) doubleValue)
						.doubleValue();
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Object#toString()
			 */
			@Override
			public String toString() {
				StringBuilder sb = new StringBuilder();
				sb.append('[');
				for (int i = 0; i < getRowCount(); i++) {
					sb.append(getValue(i));
					if (i < getRowCount() - 1) {
						sb.append(", ");
					}
				}
				sb.append(']');
				return sb.toString();
			}
		}

		/**
		 * Generated serial version identifier.
		 */
		private static final long serialVersionUID = -6797479340761921075L;

		/**
		 * The matrix of actual data. Must have an equal number of rows as the
		 * time points array.
		 */
		private double data[][];

		/**
		 * These are the column names for all columns in the data matrix. This
		 * array does not include the name for the time column.
		 */
		private String identifiers[];

		/**
		 * This {@link Hashtable} memorizes the column indices of all
		 * identifiers.
		 */
		private Hashtable<String, Integer> idHash;

		/**
		 * The optional name of the block.
		 */
		private String name;

		/**
		 * Pointer to the containing table.
		 */
		private MultiBlockTable parent;

		/**
		 * 
		 * @param data
		 * @param identifiers
		 */
		private Block(double[][] data, String[] identifiers,
				MultiBlockTable parent) {
			this(parent);
			setData(data);
			setIdentifiers(identifiers);
		}

		/**
		 * 
		 */
		private Block(MultiBlockTable parent) {
			this.parent = parent;
			idHash = new Hashtable<String, Integer>();
		}

		/**
		 * Checks whether or not this {@link Block} contains a {@link Column}
		 * with the given identifier.
		 * 
		 * @param id
		 * @return
		 */
		public boolean containsColumn(String id) {
			return idHash.containsKey(id);
		}

		/**
		 * Grants access to the specified column.
		 * 
		 * @param columnIndex
		 *            The index of the column (excluding the time column)
		 * @return Returns the {@link Column} with the given index in the data
		 *         matrix, i.e., the time column is excluded.
		 */
		public Column getColumn(int columnIndex) {
			return new Column(columnIndex);
		}

		/**
		 * Provides access to the {@link Column} corresponding to the given
		 * identifier.
		 * 
		 * @param identfier
		 *            The identifier of the {@link Column} to be queried.
		 * @return A {@link Column} object for convenient access to the data in
		 *         the desired table column.
		 */
		public Column getColumn(String identfier) {
			return new Column(idHash.get(identfier).intValue());
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getColumnCount()
		 */
		public int getColumnCount() {
			return identifiers.length;
		}

		/*
		 * (non-Javadoc)
		 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
		 */
		@Override
		public String getColumnName(int column) {
			return identifiers[column]; 
		}

		/**
		 * @return the data
		 */
		public double[][] getData() {
			return data;
		}

		/**
		 * @return the identifiers
		 */
		public String[] getIdentifiers() {
			return identifiers;
		}

		/**
		 * 
		 * @return
		 */
		public String getName() {
			return name;
		}

		/**
		 * Delivers the given row of the data matrix as an array of doubles
		 * only, i.e., no time points.
		 * 
		 * @param rowIndex
		 *            The index of the row to be delivered.
		 * @return An array of double values from the encapsulated data matrix.
		 */
		public double[] getRow(int rowIndex) {
			return data[rowIndex];
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getRowCount()
		 */
		public int getRowCount() {
			return isSetTimePoints() ? timePoints.length : 0;
		}

		/**
		 * Access to the time points of the overall table.
		 * 
		 * @return A pointer to the time points in the containing table.
		 */
		public double[] getTimePoints() {
			return parent.getTimePoints();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see javax.swing.table.TableModel#getValueAt(int, int)
		 */
		public Double getValueAt(int rowIndex, int columnIndex) {
			if (columnIndex == 0) {
				return getTimePoint(rowIndex);
			}
			return Double.valueOf(data[rowIndex][columnIndex - 1]);
		}

		/**
		 * Checks whether or not a data matrix has been defined in this object
		 * 
		 * @return
		 */
		public boolean isSetData() {
			return data != null;
		}

		/**
		 * @param data
		 *            the data to set
		 */
		public void setData(double[][] data) {
			if (isSetTimePoints() && (data.length != getRowCount())) {
				throw new IllegalArgumentException(String.format(
						UNEQUAL_DATA_AND_TIME_POINTS, data.length,
						timePoints.length));
			}
			this.data = data;
		}

		/**
		 * @param identifiers
		 *            the identifiers to set
		 */
		public void setIdentifiers(String[] identifiers) {
			if (isSetData() && (identifiers.length != data[0].length)) {
				throw new IllegalArgumentException(String.format(
						UNEQUAL_COLUMNS_AND_IDENTIFIERS, data[0].length,
						identifiers.length));
			}
			this.identifiers = identifiers;
			this.idHash.clear();
			for (int i = 0; i < this.identifiers.length; i++) {
				idHash.put(this.identifiers[i], Integer.valueOf(i));
			}
		}

		/**
		 * 
		 * @param name
		 */
		public void setName(String name) {
			this.name = name;
		}

		/**
		 * Sets the given array as the new row in the given position of the data
		 * matrix, but requires that the number of values in the array equal the
		 * number of columns in the matrix.
		 * 
		 * @param rowIndex
		 *            The index of the row to be replaced by the new array.
		 * @param array
		 *            An array of length {@link #getColumnCount()} - 1.
		 */
		public void setRowData(int rowIndex, double[] array) {
			if (array.length != getColumnCount()) {
				throw new IllegalArgumentException(String.format(
						UNEQUAL_COLUMNS_AND_IDENTIFIERS, array.length,
						identifiers.length));
			}
			data[rowIndex] = array;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
		 * int, int)
		 */
		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			if (!(aValue instanceof Double)) {
				throw new IllegalArgumentException(ONLY_DOUBLE_VALUES_ACCEPTED);
			}
			if (columnIndex == 0) {
				timePoints[rowIndex] = ((Double) aValue).doubleValue();
			} else {
				data[rowIndex][columnIndex - 1] = ((Double) aValue)
						.doubleValue();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(Arrays.toString(getIdentifiers()));
			if (isSetData()) {
				sb.append(", ");
				for (int i = 0; i < getRowCount(); i++) {
					sb.append(Arrays.toString(getRow(i)));
					if (i < getRowCount() - 1) {
						sb.append(", ");
					}
				}
			} else {
				sb.append("[]");
			}
			sb.append(']');
			return sb.toString();
		}
	}

	/**
	 * Error message
	 */
	private static final String ONLY_DOUBLE_VALUES_ACCEPTED = "only double values are accepted as arguments";

	/**
	 * Generated serial version identifier
	 */
	private static final long serialVersionUID = 1853070398348919488L;

	/**
	 * Error message
	 */
	private static final String UNEQUAL_COLUMNS_AND_IDENTIFIERS = "unequal number of %d columns and %d identifiers in the data matrix";

	/**
	 * Error message
	 */
	private static final String UNEQUAL_DATA_AND_TIME_POINTS = "unequal number of %d data rows and %d time points";

	/**
	 * @return the serialversionuid
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * A list of actual data
	 */
	private List<Block> listOfBlocks;

	/**
	 * The name of the time column. Default is the English word "Time".
	 */
	private String timeName = "Time";

	/**
	 * The array to gather the time points in this data structure. This array
	 * must be sorted.
	 */
	private double timePoints[];

	/**
	 * 
	 */
	private String name;

	/**
	 * Constructs an empty {@link MultiBlockTable} object.
	 */
	public MultiBlockTable() {
		listOfBlocks = new LinkedList<Block>();
	}

	/**
	 * Constructs a data object for the given values.
	 * 
	 * @param timePoints
	 * @param data
	 * @param identifiers
	 *            The first column in identifiers may be the name for the time
	 *            column.
	 */
	public MultiBlockTable(double timePoints[], double data[][],
			String identifiers[]) {
		this();
		setTimePoints(timePoints);
		String ids[];
		if (identifiers.length == data[0].length + 1) {
			setTimeName(identifiers[0]);
			ids = new String[identifiers.length - 1];
			System.arraycopy(identifiers, 1, ids, 0, ids.length);
		} else {
			ids = identifiers;
		}
		listOfBlocks.add(new Block(data, ids, this));
	}

	/**
	 * Creates a new {@link MultiBlockTable.Block} and adds it to this object.
	 * The number of rows will be equal to the number of time points of the
	 * overall data structure.
	 * 
	 * @param identifiers
	 *            The column identifiers of the new block.
	 */
	public void addBlock(String[] identifiers) {
		Block block = new Block(this);
		block.setIdentifiers(identifiers);
		if (isSetTimePoints()) {
			block.setData(new double[timePoints.length][identifiers.length]);
		}
		listOfBlocks.add(block);
	}

	/**
	 * 
	 * @param index
	 * @return
	 */
	public Block getBlock(int index) {
		return listOfBlocks.get(index);
	}

	/**
	 * 
	 * @return
	 */
	public int getBlockCount() {
		return getNumBlocks();
	}

	/**
	 * 
	 * @param column
	 * @return
	 */
	public Column getColumn(int column) {
		if (column > getColumnCount()) {
			throw new IndexOutOfBoundsException(Integer.toString(column));
		}
		if (column == 0) {
			throw new IllegalArgumentException(
					"no column 0: use getTimePoints()");
		}
		int index = column - 1;
		int bidx = 0;
		Block b = listOfBlocks.get(bidx);
		while (index >= b.getColumnCount()) {
			index -= b.getColumnCount();
			b = listOfBlocks.get(++bidx);
		}
		return b.getColumn(index);
	}

	/**
	 * Returns the column corresponding to the given identifier.
	 * 
	 * @param identifier
	 *            An identifier.
	 * @return A {@link Column} object for this identifier or null if no such
	 *         {@link Column} exists.
	 */
	public Column getColumn(String identifier) {
		for (int i = 1; i < getColumnCount(); i++) {
			if (getColumnName(i).equals(identifier)) {
				return getColumn(i);
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
	 */
	@Override
	public Class<Double> getColumnClass(int columnIndex) {
		return Double.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getColumnCount()
	 */
	public int getColumnCount() {
		int count = 0;
		if (isSetTimePoints()) {
			count++;
		}
		for (Block b : listOfBlocks) {
			count += b.getColumnCount();
		}
		return count;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#getColumnName(int)
	 */
	@Override
	public String getColumnName(int column) {
		if (column > getColumnCount()) {
			throw new IndexOutOfBoundsException(Integer.toString(column));
		}
		if (column == 0) {
			return getTimeName();
		}
		return getColumn(column).getId();
	}

	/**
	 * Gives this {@link MultiBlockTable}'s name.
	 * 
	 * @return
	 */
	public String getName() {
		return name;
	}

	/**
	 * 
	 * @return
	 */
	public int getNumBlocks() {
		return listOfBlocks.size();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getRowCount()
	 */
	public int getRowCount() {
		return isSetTimePoints() ? timePoints.length : 0;
	}

	/**
	 * The column identifier for the first column, i.e., the time column.
	 * 
	 * @return the timeName
	 */
	public String getTimeName() {
		return timeName;
	}

	/**
	 * Returns the time value at the given index position.
	 * 
	 * @param rowIndex
	 *            The index of the time value of interest.
	 * @return A double number representing the time at the given index.
	 */
	public double getTimePoint(int rowIndex) {
		return timePoints[rowIndex];
	}

	/**
	 * @return the timePoints
	 */
	public double[] getTimePoints() {
		return timePoints;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.TableModel#getValueAt(int, int)
	 */
	public Double getValueAt(int rowIndex, int columnIndex) {
		return Double.valueOf(columnIndex == 0 ? timePoints[rowIndex]
				: getColumn(columnIndex).getValue(rowIndex));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
	 */
	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// Maybe we want to change this later
		return false;
	}

	/**
	 * Checks whether an array of time points has been set for this object.
	 * 
	 * @return
	 */
	public boolean isSetTimePoints() {
		return timePoints != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Iterable<Double>> iterator() {
		return new Iterator<Iterable<Double>>() {

			private int currCol = 0;

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#hasNext()
			 */
			public boolean hasNext() {
				return currCol < getColumnCount() - 1;
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#next()
			 */
			public Iterable<Double> next() {
				return getColumn(currCol++);
			}

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.Iterator#remove()
			 */
			public void remove() {
				throw new UnsupportedOperationException(
						"this iterator does not remove anything.");
			}
		};
	}

	/**
	 * Removes the {@link Block} with the given index from this data structure.
	 * 
	 * @param index
	 *            The index of the block. Do not confuse with the index of the
	 *            column.
	 */
	public void removeBlock(int index) {
		listOfBlocks.remove(index);
	}

	/**
	 * Sets the name of this {@link MultiBlockTable}.
	 * 
	 * @param name
	 */
	public void setName(String name) {
		this.name = new String(name);
	}

	/**
	 * Set the name of the time column
	 * 
	 * @param timeName
	 *            the timeName to set
	 */
	public void setTimeName(String timeName) {
		this.timeName = timeName;
	}

	/**
	 * @param timePoints
	 *            the timePoints to set
	 */
	public void setTimePoints(double[] timePoints) {
		if ((listOfBlocks.size() > 0)
				&& (listOfBlocks.get(0).getRowCount() != timePoints.length)) {
			throw new IllegalArgumentException(String.format(
					UNEQUAL_DATA_AND_TIME_POINTS, listOfBlocks.get(0)
							.getRowCount(), timePoints.length));
		}
		this.timePoints = timePoints;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
	 * int, int)
	 */
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (!(aValue instanceof Double)) {
			throw new IllegalArgumentException(ONLY_DOUBLE_VALUES_ACCEPTED);
		}
		if (columnIndex == 0) {
			timePoints[rowIndex] = ((Double) aValue).doubleValue();
		} else {
			getColumn(columnIndex).setValue(((Double) aValue).doubleValue(),
					rowIndex);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('[');
		sb.append(getTimeName());
		sb.append("] [");
		for (int i = 0; i < timePoints.length; i++) {
			sb.append(timePoints[i]);
			if (i < timePoints.length - 1) {
				sb.append(", ");
			}
		}
		sb.append(']');
		for (Block b : listOfBlocks) {
			sb.append(", ");
			sb.append(b.toString());
		}
		return sb.toString();
	}
}
