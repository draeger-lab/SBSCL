import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.math.ode.DerivativeException;
import org.junit.jupiter.api.Test;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.AdaptiveStepsizeIntegrator;
import org.simulator.math.odes.AdamsMoultonSolver;
import org.simulator.math.odes.DESystem;
import org.simulator.math.odes.DormandPrince54Solver;
import org.simulator.math.odes.DormandPrince853Solver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.math.odes.LSODA.LSODAIntegrator;

/**
 * Every solver must be able to integrate a plain {@link DESystem} that is not an SBML model
 * (previously solve() cast the system to SBMLinterpreter and threw a ClassCastException).
 */
public class PlainDESystemSolveTest {

  /** y1' = -y1, y2' = -2 y2 with y(0) = (1, 1): exact solution (e^-t, e^-2t). */
  static class Decay implements DESystem {
    private static final long serialVersionUID = 1L;
    @Override public int getDimension() { return 2; }
    @Override public void computeDerivatives(double t, double[] y, double[] yDot) {
      yDot[0] = -y[0];
      yDot[1] = -2d * y[1];
    }
    @Override public String[] getIdentifiers() { return new String[] {"y1", "y2"}; }
    @Override public boolean containsEventsOrRules() { return false; }
    @Override public int getPositiveValueCount() { return 0; }
    @Override public void setDelaysIncluded(boolean delaysIncluded) { }
  }

  private static void check(AbstractDESSolver solver) throws DerivativeException {
    ((AdaptiveStepsizeIntegrator) solver).setAbsTol(1e-10);
    ((AdaptiveStepsizeIntegrator) solver).setRelTol(1e-8);   
    solver.setStepSize(0.5);
    double[] times = {0d, 0.5, 1d, 1.5, 2d};
    MultiTable result = solver.solve(new Decay(), new double[] {1d, 1d}, times);
    assertEquals(times.length, result.getRowCount(), solver.getName());
    int last = times.length - 1;
    assertEquals(Math.exp(-2d), result.getValueAt(last, 1), 1e-6, solver.getName() + " y1(2)");
    assertEquals(Math.exp(-4d), result.getValueAt(last, 2), 1e-6, solver.getName() + " y2(2)");
  }

  @Test void lsoda() throws DerivativeException { check(new LSODAIntegrator()); }
  @Test void rosenbrock() throws DerivativeException { check(new RosenbrockSolver()); }
  @Test void dormandPrince54() throws DerivativeException { check(new DormandPrince54Solver()); }
  @Test void dormandPrince853() throws DerivativeException { check(new DormandPrince853Solver()); }
  @Test void adamsMoulton() throws DerivativeException { check(new AdamsMoultonSolver()); }
}
