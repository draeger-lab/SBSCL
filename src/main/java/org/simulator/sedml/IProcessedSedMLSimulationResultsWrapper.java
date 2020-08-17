package org.simulator.sedml;

import org.jlibsedml.execution.IProcessedSedMLSimulationResults;

/**
 * Non-API Implementation of {@link IProcessedSedMLSimulationResults} Because this class is non-API
 * we don't have exhaustive arg checking etc., Borrowed from jlibsedml
 *
 * @author Shalin Shah
 */
class IProcessedSedMLSimulationResultsWrapper
    implements IProcessedSedMLSimulationResults {

  private double[][] data;
  private String[] headers;

  IProcessedSedMLSimulationResultsWrapper(double[][] data, String[] headers) {
    this.headers = new String[headers.length];
    System.arraycopy(headers, 0, this.headers, 0, headers.length);
    this.data = new double[data.length][];
    copyDataFromTo(data, this.data);
  }

  public String[] getColumnHeaders() {
    String[] rc = new String[headers.length];
    System.arraycopy(headers, 0, rc, 0, headers.length);
    return rc;
  }

  public double[][] getData() {
    double[][] copy = new double[data.length][];
    copyDataFromTo(data, copy);
    return copy;
  }

  private void copyDataFromTo(double[][] data2, double[][] copy) {
    int i = 0;
    for (double[] row : data2) {
      double[] copyRow = new double[row.length];
      System.arraycopy(row, 0, copyRow, 0, row.length);
      copy[i++] = copyRow;
    }
  }

  public int getNumColumns() {
    return headers.length;
  }

  public int getNumDataRows() {
    return data.length;
  }

  public Double[] getDataByColumnId(String colID) {
    int colInd = getIndexByColumnID(colID);
    if (colInd == -1) {
      return null;
    }
    Double[] rc = new Double[data.length];
    for (int i = 0; i < data.length; i++) {
      rc[i] = data[i][colInd];
    }
    return rc;
  }

  public int getIndexByColumnID(String colID) {
    int colInd = -1;
    for (int i = 0; i < headers.length; i++) {
      if (headers[i].equals(colID)) {
        colInd = i;
      }
    }
    return colInd;
  }

  public Double[] getDataByColumnIndex(int index) {
    Double[] rc = new Double[data.length];
    for (int i = 0; i < data.length; i++) {
      rc[i] = data[i][index];
    }
    return rc;
  }
}
