package fern.example;

import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.observer.Observer;
import fern.tools.Stochastics;


/**
 * Does not observe anything, but it controls the reaction networks cell. At the beginning the
 * volume is set to 1 and then linearly increased to 2 until the generation time is reached. Then a
 * cell division is simulated by dividing each population by 2 and restart the volume at 1.
 * <p>
 * This class is used for the LacZ examples, so at a cell division the amount of PLac (the promoter)
 * is set to 1.
 *
 * @author Florian Erhard
 */
public class CellGrowthObserver extends Observer {


  private double generationTime;
  private double timeOffset;
  private double initialTimeOffset;
  private double recentTime;
  private long numSteps;

  /**
   * Creates the observer for given simulator, generation time and a time offset (because the
   * simulator starts at time 0 and for one example it actually starts at time 1000)
   *
   * @param sim            the simulator
   * @param generationTime generation time
   * @param timeOffset     time offset
   */
  public CellGrowthObserver(Simulator sim, double generationTime, double timeOffset) {
    super(sim);

    this.generationTime = generationTime;
    this.initialTimeOffset = timeOffset;
  }

  @Override
  public void activateReaction(int mu, double tau, FireType fireType, int times) {
  }

  @Override
  public void finished() {
  }

  @Override
  public void started() {
    getSimulator().setVolume(1);
    recentTime = Double.POSITIVE_INFINITY;
    numSteps = 0;
    timeOffset = initialTimeOffset;
  }

  @Override
  public void step() {

    if ((int) (getSimulator().getTime() / generationTime) > (int) (recentTime / generationTime)) {
      for (int i = 0; i < getSimulator().getNet().getNumSpecies(); i++) {
				if (getSimulator().getNet().getSpeciesByName("PLac") != i) {
					getSimulator().setAmount(i, (long) getSimulator().getAmount(i) / 2);
				} else {
					getSimulator().setAmount(i, 1);
				}
      }
      timeOffset -= generationTime;
    }

    double vol = 1.0 + (getSimulator().getTime() + timeOffset) / generationTime;
    int rnap = getSimulator().getNet().getSpeciesByName("RNAP");
    int ribosome = getSimulator().getNet().getSpeciesByName("Ribosome");
    getSimulator().setAmount(rnap, (long) Stochastics.getInstance().getNormal(35.0 * vol, 3.5));
    getSimulator().setAmount(ribosome, (long) Stochastics.getInstance().getNormal(350 * vol, 35));
    getSimulator().setVolume(vol);

    recentTime = getSimulator().getTime();
    numSteps++;
  }

  @Override
  public void theta(double theta) {
    // TODO Auto-generated method stub

  }

  /**
   * Gets the number of steps taken in one simulation
   *
   * @return number of steps
   */
  public long getNumSteps() {
    return numSteps;
  }

}
