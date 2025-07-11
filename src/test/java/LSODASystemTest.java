import static org.junit.jupiter.api.Assertions.*;
import org.apache.commons.math.ode.DerivativeException;
import org.junit.jupiter.api.Test;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DESystem;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.LSODA.LSODACommon;
import org.simulator.math.odes.LSODA.LSODAContext;
import org.simulator.math.odes.LSODA.LSODAIntegrator;
import org.simulator.math.odes.LSODA.LSODAOptions;
import org.simulator.math.odes.exception.IllegalInputException;
import org.simulator.math.odes.exception.MaxStepExceededException;
import org.simulator.math.odes.exception.MembersNotInitializedException;
import org.simulator.math.odes.exception.TooMuchAccuracyException;
import org.simulator.sbml.SBMLinterpreter;
import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.stream.XMLStreamException;

public class LSODASystemTest {
    
    private static final Logger logger = Logger.getLogger(LSODASystemTest.class.getName());

    LSODACommon common = new LSODACommon();
    LSODAOptions opt = new LSODAOptions();
    LSODAContext ctx = new LSODAContext(common, opt);
    LSODAIntegrator integrator = new LSODAIntegrator();

    @Test
    void linearSystem() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 2;
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12};
        double[] rtol = {1e-12};
        double[] y = {15d};
        double[] t = {5d};
        double tout = 7d;
        double result = 2*tout + 5;

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext();
        ctx.setNeq(system.getDimension());
        ctx.setOdeSystem(system);
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);
        solver.lsoda(ctx, y, t, tout);
        double[] res = solver.getResult();

        // System.out.println("output = " + res[0] + ", expected = " + result);
        assertTrue(Math.abs(result-res[0]) < 1e-8);
    }

    @Test
    void linearSystemComputeChangeTest() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 2;
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12};
        double[] rtol = {1e-12};
        double[] y = {15d};
        double t = 5d;
        double tout = 7d;
        double result = 2*tout + 5;

        LSODAIntegrator solver = new LSODAIntegrator(atol, rtol);
        solver.prepare(system, 1, 1, 1);
        double[] change = new double[system.getDimension()];
        solver.computeChange(system, y, t, tout - t, change, false);
        double res = (change[0]+y[0]);

        // System.out.println("output = " + res + ", expected = " + result);
        assertTrue(Math.abs(result-res) < 1e-8);
    }

    @Test
    void linearSystemWithVeryHighSlope() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 1e6;
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12};
        double[] rtol = {1e-12};
        double[] y = {5d};
        double[] t = {0d};
        double tout = 7d;
        double result = 1e6*tout + 5;

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext();
        ctx.setNeq(system.getDimension());
        ctx.setOdeSystem(system);
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);
        solver.lsoda(ctx, y, t, tout);
        double[] res = solver.getResult();

        // System.out.println("output = " + res[0] + ", expected = " + result);
        assertTrue(Math.abs(result-res[0]) < 1e-8);
    }

    @Test
    void linearSystemWithVerySmallSlope() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 1e-8;
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12};
        double[] rtol = {1e-12};
        double[] y = {1e-10d};
        double[] t = {0};
        double tout = 7e10d;
        double result = (1e-8)*tout + (1e-10);

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext();
        ctx.setNeq(system.getDimension());
        ctx.setOdeSystem(system);
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);
        solver.lsoda(ctx, y, t, tout);
        double[] res = solver.getResult();

        // System.out.println("output = " + res[0] + ", expected = " + result);
        assertTrue(Math.abs(result-res[0]) < 1e-8);
    }

    @Test
    void exponentialSystem() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = y[0];
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12};
        double[] rtol = {1e-12};
        double[] y = {5d};
        double[] t = {0d};
        double tout = 4d;
        double result = 5*(Math.exp(tout));

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext();
        ctx.setNeq(system.getDimension());
        ctx.setOdeSystem(system);
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);
        solver.lsoda(ctx, y, t, tout);
        double[] res = solver.getResult();

        // System.out.println("output = " + res[0] + ", expected = " + result + ", diff = " + Math.abs(result-res[0]));
        assertTrue(Math.abs(result-res[0]) < 1e-8);
    }

    @Test
    void exponentialSystem2() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = -3 * y[0];
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12};
        double[] rtol = {1e-12};
        double[] y = {1};
        double[] t = {0};
        double tout = 1;
        double result;

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext(opt, system);
        ctx.setNeq(system.getDimension());
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);

        while(tout<=5){
            result = Math.exp(-3 * tout);
            solver.lsoda(ctx, y, t, tout);
            double[] res = solver.getResult();

            // System.out.println("output at " + tout + " = " + res[0] + ", expected = " + result);
            assertTrue(Math.abs(result-res[0]) < 1e-6);
            tout+=1;
        }

    }

    @Test
    void stiffSystem1() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 3;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 1.0E4 * y[1] * y[2] - .04E0 * y[0];
                yDot[2] = 3.0E7 * y[1] * y[1];
                yDot[1] = -1.0 * (yDot[0] + yDot[2]);
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = new double[3];
        double[] rtol = new double[3];
        double[] y = new double[3];
        y[0] = 1.0E0;
        y[1] = 0.0E0;
        y[2] = 0.0E0;
        double[] t = {0d};
        double tout = 0.4;

        rtol[0] = 1.0E-4;
        rtol[2] = 1.0E-4;
        rtol[1] = 1.0E-4;
        atol[0] = 1.0E-6;
        atol[1] = 1.0E-10;
        atol[2] = 1.0E-6;

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(1);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext(opt, system);
        ctx.setNeq(system.getDimension());
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);

        double[][] res = {{9.851712e-01,   3.386380e-05,   1.479493e-02},
                          {9.055333e-01,   2.240655e-05,   9.444430e-02},
                          {7.158403e-01,   9.186334e-06,   2.841505e-01},
                          {4.505250e-01,   3.222964e-06,   5.494717e-01},
                          {1.831976e-01,   8.941773e-07,   8.168015e-01},
                          {3.898729e-02,   1.621940e-07,   9.610125e-01},
                          {4.936362e-03,   1.984221e-08,   9.950636e-01},
                          {5.161833e-04,   2.065787e-09,   9.994838e-01},
                          {5.179804e-05,   2.072027e-10,   9.999482e-01},
                          {5.283675e-06,   2.113481e-11,   9.999947e-01},
                          {4.658667e-07,   1.863468e-12,   9.999995e-01},
                          {1.431100e-08,   5.724404e-14,   1.000000e+00}
                        }; // result taken from liblsoda

        for(int i=1; i<=12; i++) {

            solver.lsoda(ctx, y, t, tout);
            double[] result = solver.getResult();

            // System.out.printf(" at t= %12.4e  ->  y= %14.6e %14.6e %14.6e\n", tout, result[0], result[1], result[2]);
            assertTrue(Math.abs(result[0] - res[i-1][0]) < 1e-6);
            assertTrue(Math.abs(result[1] - res[i-1][1]) < 1e-8);
            assertTrue(Math.abs(result[2] - res[i-1][2]) < 1e-6);

            if(ctx.getState()==0) break;
            tout*=10;
        }
    }

    @Test
    void stiffSystem2WithHighDimension() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 12;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                for (int i=0; i<12; i++)
                    yDot[i] = -Math.pow(i+1,4)*y[i] + i;
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = new double[12];
        double[] rtol = new double[12];
        double[] y = {1d, 1d, 1d, 1d, 1d, 1d, 1d, 1d, 1d, 1d, 1d, 1d};
      
        double[] t = {0d};
        double tout = 1;

        for(int i=0; i<12; i++){
            rtol[i] = 1e-6;
            atol[i] = 1e-6;
        }

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(1);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext(opt, system);
        ctx.setNeq(system.getDimension());
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();

        solver.lsodaPrepare(ctx, opt);
        solver.lsoda(ctx, y, t, tout);

        double[] result = solver.getResult();

        double[] res = {3.6787944436997860e-01, 6.2500152626828648e-02, 2.4691357993737381e-02, 1.1718750006733666e-02, 6.3999999971913754e-03, 
                        3.8580246808020361e-03, 2.4989587671788559e-03, 1.7089843749999937e-03, 1.2193263222069807e-03, 9.0000000000000008e-04, 
                        6.8301345536507063e-04, 5.3047839506169931e-4}; // result taken from liblsoda

        for(int i=0; i<12; i++) {
            assertTrue(Math.abs(result[i]-res[i]) < 1e-6);
            // System.out.printf("%16.16e, ", result[i]);
        }
    
    }

    @Test
    void stiffSystem3() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = y[0]*y[0] - y[0]*y[0]*y[0];
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = new double[system.getDimension()];
        double[] rtol = new double[system.getDimension()];
        double[] y = {0.01d};
      
        double[] t = {0d};
        double tout = 100;

        for(int i=0; i<system.getDimension(); i++){
            rtol[i] = 1e-6;
            atol[i] = 1e-6;
        }

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(1);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext(opt, system);
        ctx.setNeq(system.getDimension());
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();

        solver.lsodaPrepare(ctx, opt);
        solver.lsoda(ctx, y, t, tout);

        double[] result = solver.getResult();

        double[] res = {2.7773928727749259e-01}; // result taken from liblsoda

        assertTrue(Math.abs(result[0]-res[0]) < 1e-8);
        // System.out.printf("y = %16.16e\n", result[0]);

    }
    
    @Test
    void illegalInputExceptionTest() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 2;
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12, 1e-12};
        double[] rtol = {1e-12};
        double[] y = {15d};
        double[] t = {5d};
        double tout = 7d;

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext();
        ctx.setNeq(system.getDimension());
        ctx.setOdeSystem(system);
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();

        assertThrows(IllegalInputException.class, () -> {
            solver.lsodaPrepare(ctx, opt);
        });
    }

    @Test
    void membersNotInitializedExceptionTest() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = 2;
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-12};
        double[] rtol = {1e-12};
        double[] y = {15d};
        double[] t = {5d};
        double tout = 7d;

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext();
        ctx.setNeq(system.getDimension());
        ctx.setOdeSystem(system);
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();

        assertThrows(MembersNotInitializedException.class, () -> {
            solver.lsoda(ctx, y, t, tout);
        });
    }

    @Test
    void tooMuchAccuracyDemandedErrorTest() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = y[0];
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = {1e-16};
        double[] rtol = {1e-16};
        double[] y = {5d};
        double[] t = {0d};
        double tout = 4d;
        double result = 5*(Math.exp(tout));

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(0);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);

        LSODAContext ctx = new LSODAContext();
        ctx.setNeq(system.getDimension());
        ctx.setOdeSystem(system);
        ctx.setState(1);

        LSODAIntegrator solver = new LSODAIntegrator();
        solver.lsodaPrepare(ctx, opt);

        assertThrows(TooMuchAccuracyException.class, () -> {
            solver.lsoda(ctx, y, t, tout);
        });
        
    }

    @Test
    void maxStepExceededErrorTest() throws DerivativeException {
        DESystem system = new DESystem() {

            @Override
            public int getDimension() {
                return 1;
            }

            @Override
            public void computeDerivatives(double t, double[] y, double[] yDot) throws DerivativeException {
                yDot[0] = y[0]*y[0] - y[0]*y[0]*y[0];
            }

            @Override
            public String[] getIdentifiers() {
                throw new UnsupportedOperationException("Unimplemented method 'getIdentifiers'");
            }

            @Override
            public boolean containsEventsOrRules() {
                throw new UnsupportedOperationException("Unimplemented method 'containsEventsOrRules'");
            }

            @Override
            public int getPositiveValueCount() {
                throw new UnsupportedOperationException("Unimplemented method 'getPositiveValueCount'");
            }

            @Override
            public void setDelaysIncluded(boolean delaysIncluded) {
                throw new UnsupportedOperationException("Unimplemented method 'setDelaysIncluded'");
            }
            
        };

        double[] atol = new double[system.getDimension()];
        double[] rtol = new double[system.getDimension()];
        double[] y = {0.01d};
      
        double[] t = {0d};
        double tout = 1000;

        for(int i=0; i<system.getDimension(); i++){
            rtol[i] = 1e-6;
            atol[i] = 1e-6;
        }

        LSODAOptions opt = new LSODAOptions();
        opt.setIxpr(1);
        opt.setRtol(rtol);
        opt.setAtol(atol);
        opt.setItask(1);
        opt.setMxstep(100);

        LSODAContext ctx = new LSODAContext(opt, system);
        ctx.setNeq(system.getDimension());
        ctx.setState(1);
        LSODAIntegrator solver = new LSODAIntegrator();

        solver.lsodaPrepare(ctx, opt);

        assertThrows(MaxStepExceededException.class, () -> {
            solver.lsoda(ctx, y, t, tout);
        });
        
    }

    // @Test
    void SBMLParsingPipelineTest00001() throws DerivativeException, SBMLException, ModelOverdeterminedException, XMLStreamException, IOException {

        String sbmlfile = "/home/sbml-semantic-test-cases-2017-12-12/cases/semantic/00001/00001-sbml-l1v2.xml";  // Change as per your system and uncomment the @Test decorator
        Model model = (new SBMLReader()).readSBML(sbmlfile).getModel();
        SBMLinterpreter interpreter = new SBMLinterpreter(model);

        double atol = 1e-12;
        double rtol = 1e-12;
        AbstractDESSolver solver = new LSODAIntegrator(atol, rtol);
        solver.prepare(interpreter, 1, 1, 1);
        solver.setStepSize(0.1);
        MultiTable solution = solver.solve(interpreter, interpreter.getInitialValues(), 0, 5);

        double[] res = {1.01069204986282E-06, 0.000148989307950137}; // True Result from SBMLTestSuite

        assertTrue(Math.abs(solution.getBlock(0).getRow(50)[1] - res[0]) < 1e-10);
        assertTrue(Math.abs(solution.getBlock(0).getRow(50)[2] - res[1]) < 1e-10);
        
        // System.out.println(solution.getColumnCount());
        // System.out.println(solution.getColumnName(0) + " " + solution.getColumnName(1) + " " + solution.getColumnName(2) + " " + solution.getColumnName(3) + " " + solution.getColumnName(4) + " " + solution.getColumnName(5));
        // int from = model.getNumCompartments();
        // int to = from + model.getNumSpecies();
        // double time = 0;
        // for (int i = 0; i < solution.getRowCount(); i++) {
        //   double[] symbol = solution.getBlock(0).getRow(i);
        //   System.out.println("time = " + time + " " + i);
        //   for (int j = from; j < to; j++) {

        //     double sym = symbol[j];
        //     System.out.print(sym + " ");
        //   }
        //   System.out.println();
        //   time += solver.getStepSize();
        // }


        // for t = 0.1
        // Actual ->                1.357256127053939E-4,    1.427438729460607E-5
        // LSODA (1e-8 tol) ->      1.3572073877119505E-4    1.4279261228804921E-5 
        // LSODA (1e-12 tol) ->     1.3572561295191373E-4    1.427438704808624E-5    
        // Rosenbrock ->            1.357255926050389E-4     1.4274407394961046E-5 

    }
    
}
