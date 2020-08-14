package fern.simulation.observer;

import java.io.PrintWriter;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;


/**
 * Simply prints out every event that the simulator reports to the observer.
 *
 * @author Florian Erhard
 */
public class InstantOutputObserver extends Observer {

  private PrintWriter pw;

  /**
   * Creates the observer for a given simulator and a given {@link PrintWriter}
   *
   * @param sim simulator
   * @param pw  <code>PrintWriter</code>
   */
  public InstantOutputObserver(Simulator sim, PrintWriter pw) {
    super(sim);
    this.pw = pw;
  }

  /**
   * Prints event.
   */
  @Override
  public void activateReaction(int mu, double tau, FireType fireType, int times) {
    pw.append(
        "Fired " + getSimulator().getNet().getReactionName(mu) + " at " + tau + " by " + fireType
            .toString() + "\n");
    pw.flush();
  }

  /**
   * Prints event.
   */
  @Override
  public void finished() {
    pw.append("finished\n");
    pw.flush();
  }

  /**
   * Prints event.
   */
  @Override
  public void started() {
    pw.append("started\n");
    pw.flush();
  }

  /**
   * Prints event.
   */
  @Override
  public void step() {
    pw.append("step\n");
		for (int i = 0; i < getSimulator().getNet().getNumReactions(); i++) {
			pw.append(String.format("a[%d]=%e\n", i, getSimulator().getPropensity(i)));
		}
    pw.flush();
  }

  /**
   * Prints event.
   */
  @Override
  public void theta(double theta) {
    pw.append("theta: ");
    pw.append(theta + "\n");
    pw.flush();
  }

}
