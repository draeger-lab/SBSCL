/*
 * $Id$
 * $URL$
 * ---------------------------------------------------------------------
 * This file is part of Simulation Core Library, a Java-based library
 * for efficient numerical simulation of biological models.
 *
 * Copyright (C) 2007-2012 jointly by the following organizations:
 * 1. University of Tuebingen, Germany
 * 2. Keio University, Japan
 * 3. Harvard University, USA
 * 4. The University of Edinburgh, UK
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
package org.simulator.io;

import java.io.IOException;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;


import org.sbml.jsbml.Model;
import org.sbml.jsbml.UniqueNamedSBase;
import org.simulator.math.odes.MultiTable;

import de.zbit.gui.csv.CSVImporterV2;
import de.zbit.gui.csv.ExpectedColumn;
import de.zbit.io.csv.CSVReader;

/**
 * @author Roland Keller
 * @version $Rev$
 */
public class CSVImporter {

	/**
	   * A {@link java.util.logging.Logger} for this class.
	   */
	  private static final transient Logger logger = Logger.getLogger(CSVImporter.class.getName());
	  
	
	/**
	 * @param model
	 * @param csvfile
	 * @return
	 * @throws IOException 
	 */
	public MultiTable convert(Model model, String pathname) throws IOException {
		MultiTable data = new MultiTable();
		List<ExpectedColumn> cols;
		String expectedHeader[];

		if (model != null) {
			expectedHeader = expectedTableHead(model); // According to the
														// model: which symbols
			cols = new ArrayList<ExpectedColumn>(expectedHeader.length + 1);
			for (String head : expectedHeader) {
				cols.add(new ExpectedColumn(head, false));
			}
		} else {
			expectedHeader = new String[0];
			cols = new ArrayList<ExpectedColumn>(1);
		}
		cols.add(new ExpectedColumn(data.getTimeName(), true));

		CSVImporterV2 converter = new CSVImporterV2(pathname, cols);

		int i, j, timeColumn;
		CSVReader reader = converter.getCSVReader();
		String stringData[][] = reader.getData();
		timeColumn = reader.getColumn(data.getTimeName());
		if (timeColumn > -1) {
			double timePoints[] = new double[stringData.length];
			for (i = 0; i < stringData.length; i++) {
				timePoints[i] = Double.parseDouble(stringData[i][timeColumn]);
			}
			data.setTimePoints(timePoints);
			// exclude time column

			String newHead[] = new String[(int) Math.max(0,
					reader.getHeader().length - 1)];

			Map<String, Integer> nameToColumn = new HashMap<String, Integer>();
			i = 0;
			for (String head : reader.getHeader()) {
				if (!head.equalsIgnoreCase(data.getTimeName())) {
					newHead[i++] = head.trim();
					nameToColumn.put(newHead[i - 1],
							reader.getColumnSensitive(head));
				}
			}
			data.addBlock(newHead); // alphabetically sorted

			double dataBlock[][] = data.getBlock(0).getData();

			for (i = 0; i < dataBlock.length; i++) {
				j = 0; // timeCorrection(j, timeColumn)
				for (String head : newHead) {
					String s = stringData[i][nameToColumn.get(head)];
					if ((s != null) && (s.length() > 0)) {
						if (s.equalsIgnoreCase("INF")) {
							dataBlock[i][j] = Double.POSITIVE_INFINITY;
						} else if (s.equalsIgnoreCase("-INF")) {
							dataBlock[i][j] = Double.NEGATIVE_INFINITY;
						} else if (s.equalsIgnoreCase("NAN")) {
							dataBlock[i][j] = Double.NaN;
						} else {
							dataBlock[i][j] = Double.parseDouble(s);
						}
					}
					j++;
				}
			}

			if (model != null) {
				String colNames[] = new String[newHead.length];
				UniqueNamedSBase sbase;
				j = 0;
				for (String head : newHead) {
					sbase = model.findUniqueNamedSBase(head);
					colNames[j++] = (sbase != null) && (sbase.isSetName()) ? sbase
							.getName() : null;
				}
				data.getBlock(0).setColumnNames(colNames);
			}
			data.setTimeName("time");

			return data;

		} else {
			logger.fine("The file is not correctly formatted!");
		}
		return null;
	}

	/**
	 * @param model
	 * @param timeName
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

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.AbstractList#get(int)
			 */
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

			/*
			 * (non-Javadoc)
			 * 
			 * @see java.util.AbstractCollection#size()
			 */
			public int size() {
				return model.getCompartmentCount() + model.getSpeciesCount()
						+ model.getParameterCount();
			}
		};
	}

}
