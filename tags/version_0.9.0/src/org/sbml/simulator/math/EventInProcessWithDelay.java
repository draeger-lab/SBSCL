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
package org.sbml.simulator.math;

/**
 * <p>
 * This class represents a compilation of all information calculated during
 * simulation concerning events. An EventInProcessWithDelay especially stands
 * for an event with delay, so it can has multiple times of execution and
 * therefore multiple arrays of values from trigger time.
 * </p>
 * 
 * @author Alexander D&ouml;rr
 * @date 2011-03-04
 * @version $Rev$
 * @since 1.0
 */
public class EventInProcessWithDelay extends EventInProcess {
	
	/**
	 * Creates a new EventInProcessWithDelay with the given boolean value
	 * indicating whether or not it can fire at time point 0d.
	 * 
	 * @param fired
	 */
	EventInProcessWithDelay(boolean fired) {
		super(fired);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.simulator.math.EventInProcess#aborted()
	 */
	@Override
	public void aborted() {
		execTimes.poll();
		executed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.simulator.math.EventInProcess#addValues(java.lang.Double[],
	 * double)
	 */
	@Override
	public void addValues(Double[] values, double time) {
		int index;
		index = insertTime(time);
		this.execTimes.add(index, time);
		this.values.add(index, values);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sbml.simulator.math.EventInProcess#executed()
	 */
	@Override
	public void executed() {
		values.poll();
		execTimes.poll();
	}
	
	/**
	 * Due to the fact that events with delay can trigger multiple times before
	 * execution, the time of execution and the corresponding values have to be
	 * inserted at the chronological correct position in the list.
	 * 
	 * @param time
	 * @return the index where time has been inserted
	 */
	private int insertTime(double time) {
		if (execTimes.isEmpty()) {
			return 0;
		}

		for (int i = 0; i < execTimes.size(); i++) {
			if (time < execTimes.get(i)) {
				return i;
			}
		}
		
		return execTimes.size();

	}

}
