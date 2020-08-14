package fern.simulation.observer;

import fern.simulation.Simulator;


public abstract class TriggerObserver extends Observer {

  public TriggerObserver(Simulator sim) {
    super(sim);
    // TODO Auto-generated constructor stub
  }

  /**
   * Is executed, whenever a event can trigger (before step and during a theta event).
   *
   * @return whether or not the trigger has fired
   */
  public abstract boolean trigger();
}
