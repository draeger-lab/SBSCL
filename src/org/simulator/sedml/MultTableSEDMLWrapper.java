/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2012 jointly by the following organizations:
 * 1. The University of Tuebingen, Germany
 * 2. The Keio University, Japan
 * 3. The Harvard University, USA
 * 4. The University of Edinburgh
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.sedml;

import org.jlibsedml.execution.IModel2DataMappings;
import org.jlibsedml.execution.IRawSedmlSimulationResults;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.MultiTable.Block.Column;

/**
 * This class adapts the native results to an interface that the SEDML processor can use
 *  to post-process results.
 *  This class can be used to access the raw results via the getMultiTable() method.
 *  <p>
 *  All methods accessing data access the underlying {@link MultiTable} object. <br/>
 *  Changes to a {@link MultiTable} object will therefore be visible to this class,
 *  i.e., it does not make a separate copy of the data.
 * @author Richard Adams
 * @version $Rev$
 * @since 1.1
 */
public class MultTableSEDMLWrapper implements IRawSedmlSimulationResults {
	public MultTableSEDMLWrapper(MultiTable mTable) {
		super();
		this.mTable = mTable;
	}

	/**
	 * Gets the underlying {@link MultiTable} wrapped by this class.
	 * @return
	 */
	public MultiTable getMultiTable() {
		return mTable;
	}

	private MultiTable mTable;
	public String[] getColumnHeaders() {
		String [] hdrs = new String [mTable.getColumnCount()];
		for (int i=0; i < hdrs.length;i++){
			hdrs[i]=mTable.getColumnIdentifier(i);
		}
		return hdrs;
	}

	public double[][] getData() {
		double [][] data = new double [mTable.getRowCount()][mTable.getColumnCount()];
		for (int i = 0; i < mTable.getRowCount();i++){
			for (int j =0; j< mTable.getColumnCount();j++){
				data[i][j] = mTable.getValueAt(i, j);
			
			}
			
		}
		return data;
	}

	public Double[] getDataByColumnId(String id) {
		
		Double [] rc = new Double[mTable.getRowCount()];
		Column col = mTable.getColumn(id);
		for (int i =0; i< mTable.getRowCount();i++){
			rc[i]=col.getValue(i);
		}
		return rc;
	}

	public Double[] getDataByColumnIndex(int indx) {
		Double [] rc = new Double[mTable.getRowCount()];
		Column col = mTable.getColumn(indx);
		for (int i =0; i< mTable.getRowCount();i++){
			rc[i]=col.getValue(i);
		}
		return rc;
	}

	public int getIndexByColumnID(String colID) {
		return mTable.getColumnIndex(colID);
	}

	public int getNumColumns() {
		return mTable.getColumnCount();
	}

	public int getNumDataRows() {
		return mTable.getRowCount();
	}

	/*
	 * This class maps variable IDs to data column headers.
	 * In this class, data column headers are usually IDs, so no mapping is required
	 * (non-Javadoc)
	 * @see org.jlibsedml.execution.IRawSedmlSimulationResults#getMappings()
	 */
	public IModel2DataMappings getMappings() {
		return new IModel2DataMappings() {
			
			public boolean hasMappingFor(String id) {
				return mTable.getColumn(id)!=null;
			}
			
			public String getColumnTitleFor(String modelID) {
				return modelID;
			}
			
			public int getColumnIndexFor(String colID) {
				return mTable.getColumnIndex(colID);
			}
		};
	}
	
}