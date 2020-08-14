package fern.simulation.controller;

import fern.simulation.Simulator;

/**
 * Implements a {@link SimulationController} composed of many <code>SimulationController</code>. If
 * one controller tells to stop, the simulation will stop.
 *
 * @author Florian Erhard
 * @see OrController
 */
public class AndController implements SimulationController {

  private SimulationController[] controllers;

  public AndController(SimulationController... controllers) {
    this.controllers = controllers;
  }

  public boolean goOn(Simulator sim) {
		for (int i = 0; i < controllers.length; i++) {
			if (!controllers[i].goOn(sim)) {
				return false;
			}
		}
    return true;
  }

}
