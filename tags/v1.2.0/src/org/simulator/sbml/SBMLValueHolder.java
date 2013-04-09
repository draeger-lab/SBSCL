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
package org.simulator.sbml;

import java.io.Serializable;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Compartment;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Parameter;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.SpeciesReference;
import org.simulator.math.odes.DelayValueHolder;

/**
 * A {@link SBMLValueHolder} is necessary to provide the current values for
 * {@link Compartment}s, {@link Species}, {@link Parameter}s, and
 * {@link SpeciesReference}s at simulation time. It also grants access
 * to the current simulation time. In this way, it is possible to separate
 * the interpretation and simulation of a {@link Model} from the pure evaluation
 * of {@link ASTNode}s.
 * 
 * @author Andreas Dr&auml;ger
 * @author Roland Keller
 * @version $Rev$
 * @since 0.9
 */
public interface SBMLValueHolder extends DelayValueHolder, Serializable {

    /**
     * Returns the size of the compartment with the given id.
     * @param id
     * @return compartmentSize
     */
    public double getCurrentCompartmentSize(String id);

    /**
     * Returns the size of the compartment of the species with the given id.
     * @param speciesId
     * @return compartmentValue
     */
    public double getCurrentCompartmentValueOf(String speciesId);

    /**
     * Returns the value of the parameter with the given id.
     * @param id
     * @return parameterValue
     */
    public double getCurrentParameterValue(String id);

    /**
     * Returns the value of the species with the given id.
     * @param id
     * @return speciesValue
     */
    public double getCurrentSpeciesValue(String id);

    /**
     * Returns the value of the stoichiometry of the species reference with the given id.
     * @param id
     * @return stoichiometry
     */
    public double getCurrentStoichiometry(String id);

    /**
     * Returns the current simulation time.
     * @return time
     */
    public double getCurrentTime();

    /**
     * Returns the current value of the SBase with the given id.
     * @param id
     * @return value
     */
    public double getCurrentValueOf(String id);
    
    /**
     * Returns the current value of the Y vector at the given position.
     * @param position
     * @return value
     */
    public double getCurrentValueOf(int position);

}
