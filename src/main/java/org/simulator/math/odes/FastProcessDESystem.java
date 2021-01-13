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
package org.simulator.math.odes;

/**
 * This interface describes differential equation systems with fast processes.
 *
 * @author Andreas Dr&auml;ger
 * @author Alexander D&ouml;rr
 * @version $Rev$
 * @since 0.9
 */
public interface FastProcessDESystem extends DESystem {

  /**
   * @return flag that is true if fast processes are contained.
   */
  boolean containsFastProcesses();

  /**
   * @param isProcessing Should there be a splitting of fast and slow reactions in the simulation?
   */
  void setFastProcessComputation(boolean isProcessing);
}
