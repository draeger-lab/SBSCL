/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.simulation.controller;

import fern.simulation.Simulator;

/**
 * The probably most common implementation of an {@link SimulationController}. It causes the
 * simulation to stop, after a given moment in time is crossed.
 *
 * @author Florian Erhard
 */
public class DefaultController implements SimulationController {

  private double maxTime;

  /**
   * Creates the controller for a given time where the simulation has to stop.
   *
   * @param maxTime the moment in time where to stop the simulation
   */
  public DefaultController(double maxTime) {
    this.maxTime = maxTime;
  }

  public boolean goOn(Simulator sim) {
    return sim.getTime() < maxTime;
  }

  /**
   * Sets the time where the simulation has to stop.
   *
   * @param time the moment in time where to stop the simulation
   */
  public void setTime(double time) {
    maxTime = time;
  }

  /**
   * Gets the time where the simulation has to stop.
   *
   * @return the moment in time where to stop the simulation
   */
  public double getTime() {
    return maxTime;
  }

}
