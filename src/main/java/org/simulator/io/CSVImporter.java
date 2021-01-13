/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2016 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
 * 5. EMBL European Bioinformatics Institute (EBML-EBI), Hinxton, UK
 * 6. The University of California, San Diego, La Jolla, CA, USA
 * 7. The Babraham Institute, Cambridge, UK
 *
 * This library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation. A copy of the license
 * agreement is provided in the file named "LICENSE.txt" included with
 * this software distribution and also available online as
 * <http://www.gnu.org/licenses/lgpl-3.0-standalone.html>.
 * ---------------------------------------------------------------------
 */
package org.simulator.io;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

import org.apache.log4j.Logger;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.UniqueNamedSBase;
import org.simulator.math.odes.MultiTable;

/**
 * This class is for importing CSV files.
 *
 * @author Roland Keller
 * @version $Rev$
 */
public class CSVImporter {

  private static final String TIME = "time";

  private static final Logger logger = Logger.getLogger(CSVImporter.class.getName());

  /**
   * This method reads the data from the CSV file and converts it into the map with key as the
   * column name and value as the array of amounts for the particular column.
   *
   * @param pathname The path of the CSV file
   * @return columnsMap
   */
  private Map<String, double[]> readDataFromCSV(String pathname, String separator)
      throws IOException {
    Map<String, double[]> columnsMap = new LinkedHashMap<>();
    BufferedReader reader = new BufferedReader(new FileReader(pathname));
    String line = reader.readLine();
    line = line.replaceAll("\\s", "");
    String[] identifiers = line.split(separator);
    if (line != null) {
      List<String> lines = getLinesFromCSV(reader);

      if (identifiers[0].equalsIgnoreCase(TIME)) {
        identifiers[0] = identifiers[0].toLowerCase();
      }
      for (String s : identifiers) {
        columnsMap.put(s, new double[lines.size()]);
      }

      for (int i = 0; i < lines.size(); i++) {
        String[] column = lines.get(i).split(separator);
        for (int j = 0; j < column.length; j++) {
          if ((column[j] != null) && (column[j].length() > 0)) {
            if (column[j].equalsIgnoreCase("INF")) {
              columnsMap.get(identifiers[j])[i] = Double.POSITIVE_INFINITY;
            } else if (column[j].equalsIgnoreCase("-INF")) {
              columnsMap.get(identifiers[j])[i] = Double.NEGATIVE_INFINITY;
            } else {
              columnsMap.get(identifiers[j])[i] = Double.parseDouble(column[j]);
            }
          }
        }
      }
    }

    return columnsMap;
  }

  /**
   * Converts the content of the CSV file in the form of the MultiTable.
   *
   * @param model    the SBML {@link Model}
   * @param pathname path of the CSV file
   * @return the results from the CSV file in MultiTable (null can be returned on exception)
   * @throws IOException
   */
  public MultiTable readMultiTableFromCSV(Model model, String pathname) throws IOException {
    MultiTable data = new MultiTable();
    Map<String, double[]> columnsMap = readDataFromCSV(pathname, ",");

    Map.Entry<String, double[]> timePoints = columnsMap.entrySet().iterator().next();
    data.setTimePoints(timePoints.getValue());
    columnsMap.remove(timePoints.getKey());

    try {
      data.addBlock(columnsMap.keySet().toArray(new String[0]));

      for (int i = 1; i < data.getColumnCount(); i++) {
        double[] colValues = columnsMap.get(data.getColumnName(i));
        for (int j = 0; j < data.getColumn(i).getRowCount(); j++) {
          data.setValueAt(colValues[j], j, i);
        }
      }

      if (model != null) {
        String[] colNames = new String[columnsMap.size()];
        UniqueNamedSBase sbase;
        int j = 0;
        for (Map.Entry<String, double[]> entry : columnsMap.entrySet()) {
          sbase = model.findUniqueNamedSBase(entry.getKey());
          colNames[j++] = (sbase != null) && (sbase.isSetName()) ? sbase.getName() : null;
        }
        data.getBlock(0).setColumnNames(colNames);
      }
      data.setTimeName(TIME);

      return data;
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Exception in converting the CSV file data to MultiTable.");
    }
    return null;
  }

  /**
   * Read all the values from the CSV file and stores them in an List of String.
   *
   * @param reader
   * @return
   * @throws IOException
   */
  private List<String> getLinesFromCSV(BufferedReader reader) throws IOException {
    List<String> lines = new LinkedList<>();
    String line = reader.readLine();
    while ((line != null) && !line.isEmpty()) {
      line = line.replaceAll("\\s", "");
      lines.add(line);
      line = reader.readLine();
    }
    return lines;
  }

  /**
   * @param model
   * @return
   */
  private String[] expectedTableHead(Model model) {
    List<String> modelSymbols = gatherSymbolIds(model);
    return modelSymbols.toArray(new String[0]);
  }

  /**
   * @param model
   * @return
   */
  private List<String> gatherSymbolIds(final Model model) {
    return new AbstractList<String>() {

      /**
       * {@inheritDoc}
       */
      @Override
      public String get(int index) {
        if (index < model.getCompartmentCount()) {
          return model.getCompartment(index).getId();
        }
        index -= model.getCompartmentCount();
        if (index < model.getSpeciesCount()) {
          return model.getSpecies(index).getId();
        }
        index -= model.getSpeciesCount();
        return model.getParameter(index).getId();
      }

      /**
       * {@inheritDoc}
       */
      @Override
      public int size() {
        return model.getCompartmentCount() + model.getSpeciesCount() + model.getParameterCount();
      }
    };
  }
}
