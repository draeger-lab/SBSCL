import org.simulator.math.odes.RungeKutta_EventSolver;
import org.simulator.math.odes.DESystem;
import org.junit.jupiter.api.Test;
import org.apache.commons.math.ode.DerivativeException;

import static org.junit.jupiter.api.Assertions.*;

public class RungeKutta_EventSolverTest {

    @Test
    public void testComputeRK2_ExponentialDecay() throws DerivativeException {

        // A system where dy/dt = -y
        DESystem exponentialDecaySystem = new DESystem() {

            public int getDimension() { 
                return 1; 
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = -y[0];
            }

            @Override
            public String[] getIdentifiers() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
        };

        RungeKutta_EventSolver solver = new RungeKutta_EventSolver("RK2");
        double[] yTemp = {1.0}; // initial value y(0) = 1
        double t = 0.0;
        double h = 0.1;
        double[] change = new double[1];

        double[] result = solver.computeRK2(exponentialDecaySystem, yTemp, t, h, change);

        // RK2 (Heun's method) estimate:
        // k0 = h * f(t, yTemp)
        // k0 = h * -1 = -0.1
        // y_temp + k0 = 0.9
        // k1 = h * f(t + h, yTemp + k0)
        // k1 = h * -0.9 = -0.09
        // RK2: change = 0.5 * (k0 + k1) = 0.5 * (-0.1 -0.09) = -0.095
        double expectedChange = -0.095;

        assertEquals(expectedChange, result[0], 1e-12);
    }

    @Test
    public void testComputeRK2_ZeroDerivative() throws DerivativeException {

        // A system where dy/dt = 0
        DESystem zeroSystem = new DESystem() {

            public int getDimension() {
                return 1; 
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = 0.0;
            }

            @Override
            public String[] getIdentifiers() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
        };

        RungeKutta_EventSolver solver = new RungeKutta_EventSolver("RK2");
        double[] yTemp = {42.0}; // random value
        double t = 0.0;
        double h = 0.1;
        double[] change = new double[1];

        double[] result = solver.computeRK2(zeroSystem, yTemp, t, h, change);

        assertEquals(0.0, result[0], 1e-12);
    }

    @Test
    public void testComputeRK2_InvalidDimensionMismatch() {
        DESystem system = new DESystem() {
            public int getDimension() {
                return 2;
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = y[0];
                yDot[1] = y[1];
            }

            @Override
            public String[] getIdentifiers() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
        };

        RungeKutta_EventSolver solver = new RungeKutta_EventSolver("RK2");
        double[] yTemp = {1.0};  // Mismatch: only 1 element
        double[] change = new double[2];  // Proper size here

        assertThrows(ArrayIndexOutOfBoundsException.class, () -> {
            solver.computeRK2(system, yTemp, 0.0, 0.1, change);
        });
    }

    @Test
    public void testComputeRK2_NullSystem() {
        RungeKutta_EventSolver solver = new RungeKutta_EventSolver("RK2");

        double[] yTemp = {1.0};
        double[] change = new double[1];

        assertThrows(NullPointerException.class, () -> {
            solver.computeRK2(null, yTemp, 0.0, 0.1, change);
        });
    }

    @Test
    public void testComputeRK2_ExponentialDecay_FailsOnWrongValue() throws DerivativeException {
        DESystem exponentialDecaySystem = new DESystem() {
            public int getDimension() {
                return 1;
            }

            public void computeDerivatives(double t, double[] y, double[] yDot) {
                yDot[0] = -y[0];
            }

            @Override
            public String[] getIdentifiers() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                // TODO Auto-generated method stub
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
        };

        RungeKutta_EventSolver solver = new RungeKutta_EventSolver("RK2");
        double[] yTemp = {1.0};
        double t = 0.0;
        double h = 0.1;
        double[] change = new double[1];

        solver.computeRK2(exponentialDecaySystem, yTemp, t, h, change);

        // Suppose someone writes wrong test assuming change to be -0.1, actual change = -0.095
        assertNotEquals(-0.1, change[0], 1e-12);
    }

    @Test
    public void testDefaultsToRK4() {
        RungeKutta_EventSolver solver = new RungeKutta_EventSolver();

        assertEquals("Runge-Kutta event solver (RK4)", solver.getName());
    }

}

