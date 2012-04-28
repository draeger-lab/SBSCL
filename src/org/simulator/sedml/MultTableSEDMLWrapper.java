package org.simulator.sedml;

import org.jlibsedml.execution.IModel2DataMappings;
import org.jlibsedml.execution.IRawSedmlSimulationResults;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.MultiTable.Block.Column;

/**
 * This class adapts the native results to an interface that the SEDML processor can use
 *  to post-process results.
 * 
 */
public class MultTableSEDMLWrapper implements IRawSedmlSimulationResults {
	public MultTableSEDMLWrapper(MultiTable mTable) {
		super();
		this.mTable = mTable;
	}

	public MultiTable getmTable() {
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