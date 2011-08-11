/*
 * $Id: ValueHolder.java 15:57:31 draeger$
 * $URL: ValueHolder.java $
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

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;

/**
 * A {@link ValueHolder} is necessary to provide the current values for
 * {@link Compartment}s, {@link Species}, {@link Parameter}s, and
 * {@link SpeciesReference}s at simulation time. It also grants access
 * to the current simulation time. In this way, it is possible to separate
 * the interpretation and simulation of a {@link Model} from the pure evaluation
 * of {@link ASTNode}s.
 * 
 * @author Andreas Dr&auml;ger
 * @author Roland Keller
 * @version $Rev$
 * @since 1.0
 */
public interface ValueHolder {

    /**
     * @param id
     * @return
     */
    public double getCurrentCompartmentSize(String id);

    /**
     * @param speciesId
     * @return
     */
    public double getCurrentCompartmentValueOf(String speciesId);

    /**
     * @param id
     * @return
     */
    public double getCurrentParameterValue(String id);

    /**
     * @param id
     * @return
     */
    public double getCurrentSpeciesValue(String id);

    /**
     * @param id
     * @return
     */
    public double getCurrentStoichiometry(String id);

    /**
     * @param time
     * @return
     */
    public double getCurrentTime();

    /**
     * @param id
     * @return
     */
    public double getCurrentValueOf(String id);

}
