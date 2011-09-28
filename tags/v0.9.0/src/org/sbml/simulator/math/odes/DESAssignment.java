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

/**
 * 
 * @author Alexander D&ouml;rr
 * @version $Rev$
 * @since 1.0
 */
public class DESAssignment {
	private int index;
	private double processTime;
	private Double value;
	private int eventNumber;

	/**
	 * 
	 * @param processTime
	 * @param index
	 * @param eventNumber
	 * @param value
	 */
	public DESAssignment(double processTime, int index, int eventNumber,
			Double value) {
		this.processTime = processTime;
		this.value = value;
		this.eventNumber = eventNumber;
		this.index = index;
	}

	/**
	 * 
	 * @param processTime
	 * @param index
	 * @param eventNumber
	 */
	public DESAssignment(double processTime, int index, int eventNumber) {
		this.processTime = processTime;
		this.eventNumber = eventNumber;
		this.index = index;
	}

	/**
	 * 
	 * @param processTime
	 * @param index
	 * @param value
	 */
	public DESAssignment(double processTime, int index, double value) {
		this.processTime = processTime;
		this.value = value;
		this.index = index;
	}

	/**
	 * 
	 * @return
	 */
	public int getIndex() {
		return index;
	}

	/**
	 * 
	 * @return
	 */
	public double getProcessTime() {
		return processTime;
	}

	/**
	 * 
	 * @return
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * 
	 * @return
	 */
	public int getEventNumber() {
		return eventNumber;
	}

	/**
	 * 
	 * @param value
	 */
	public void setValue(Double value) {
		this.value = value;
	}

}
