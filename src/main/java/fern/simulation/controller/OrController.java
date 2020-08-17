package fern.simulation.controller;

import fern.simulation.Simulator;

/**
 * Implements a {@link SimulationController} composed of many <code>SimulationController</code>. If
 * one controller tells to go on, the simulation will go on.
 *
 * @author Florian Erhard
 * @see AndController
 */
public class OrController implements SimulationController {

  private SimulationController[] controllers;

  public OrController(SimulationController... controllers) {
    this.controllers = controllers;
  }

  public boolean goOn(Simulator sim) {
    for (int i = 0; i < controllers.length; i++) {
      if (controllers[i].goOn(sim)) {
        return true;
      }
    }
    return false;
  }

}
