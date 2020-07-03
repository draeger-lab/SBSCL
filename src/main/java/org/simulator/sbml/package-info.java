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
/**
 * Classes for storing and interpreting an
 * <a href="http://sbml.org" target="_blank">SBML</a>
 * model. The most important class is {@link org.simulator.sbml.SBMLinterpreter}
 * that can return the current vector of derivatives to the solver.
 * <p>
 * One important special case of during the simulation of SBML models is dealing
 * with {@link org.sbml.jsbml.Constraint} violation. To this end, this package
 * also provides the interface {@link org.simulator.sbml.ConstraintListener},
 * which takes a special {@link java.util.EventObject}, namely the
 * {@link org.simulator.sbml.ConstraintEvent} as an argument. The method
 * {@link org.simulator.sbml.ConstraintListener#processViolation(ConstraintEvent)}
 * can then be used to deal with the {@link org.sbml.jsbml.Constraint}.
 * It receives the time, when the
 * {@link org.sbml.jsbml.Constraint}'s condition has been violated together with
 * a reference of the {@link org.sbml.jsbml.Constraint} itself. By default, the
 * {@link org.simulator.sbml.SBMLinterpreter} adds an instance of
 * {@link org.simulator.sbml.SimpleConstraintListener}
 * to its list of {@link org.simulator.sbml.ConstraintListener}s. You can remove
 * this element by calling
 * {@link org.simulator.sbml.SBMLinterpreter#removeConstraintListener(int)}
 * with the argument 0 and add an arbitrary number of user-specific listeners
 * instead. The {@link org.simulator.sbml.SimpleConstraintListener} uses the
 * standard Java {@link java.util.logging.Logger} and displays the time,
 * condition (as formula {@link java.lang.String}),
 * and the message of the {@link org.sbml.jsbml.Constraint} on the log-level
 * {@link java.util.logging.Level#WARNING}. A recommended practice would be
 * maintain a list for each {@link org.sbml.jsbml.Constraint} that gathers all
 * points in time, when it was violated. This list could at the end of the
 * simulation be presented to the user via a graphical user interface together
 * with a rendered version of the message (note that the message element in
 * the {@link org.sbml.jsbml.Constraint} is not a simple text, but an arbitrary
 * XHTML document and therefore not always suitable to be displayed on the
 * console).
 *
 * @version $Rev$
 */
package org.simulator.sbml;
