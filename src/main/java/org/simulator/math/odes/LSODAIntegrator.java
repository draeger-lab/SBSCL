package org.simulator.math.odes;
import org.apache.commons.math.ode.AbstractIntegrator;
import org.apache.commons.math.ode.DerivativeException;
import org.apache.commons.math.ode.IntegratorException;
import org.apache.log4j.Logger;
import org.simulator.io.CSVImporter;

public class LSODAIntegrator extends AdaptiveStepsizeIntegrator {

    // Constants
    private static final long serialVersionUID = 1L;

    
    private int maxOrdN; // maximum order for nonstiff method
    private int maxOrdS; // maximum order for stiif moethod
    private boolean stiff; // indicates if the problem is stiff
    private static final Logger logger = Logger.getLogger(CSVImporter.class.getName());

    //Constructor
    public LSODAIntegrator() {
        super();
        maxOrdN = 12; // LSODA Documentation states "Should be <= 12 [...] to save storage space"
        maxOrdS = 5; // LSODA Docs state "Should be <= 5 [...] to save storage space"
        stiff = false; // stiffness by default set to false
    }

    public LSODAIntegrator(LSODAIntegrator integrator) {
        super(integrator);
        this.maxOrdN = integrator.maxOrdN;
        this.maxOrdS = integrator.maxOrdS;
        this.stiff = integrator.stiff;
    }

    @Override
    public AbstractDESSolver clone() {
        return new LSODAIntegrator(this);
    }

    @Override
    public int getKiSAOterm() {
        return 0;
    }

    @Override
    public double[] computeChange(DESystem DES, double[] y, double t, double stepSize, double[] change, boolean steadyState) throws DerivativeException {
       //compute derivatives at given time
       double[] yDot = new double[y.length];
       DES.computeDerivatives(t, y, yDot);

       //checks if system is stiff or non-stiff
       if(!stiff) {
        //for non-stiff method
        nonStiffStep(DES, t, y, yDot, t, yDot);
       } else {
        //for stiff method
        stiffStep(DES, t, y, yDot, t, yDot);
       }

       return change;
    }

    @Override
    public String getName() {
        return "LSODA Integrator";
    }

    @Override
    protected boolean hasSolverEventProcessing() {
        return false;
    }

    //helper functions for LSODA Integration
    private void nonStiffStep(DESystem DES, double t, double[] y, double[] yDot, double h, double[] error) {

        AdamsMoultonSolver adamsMoulton = new AdamsMoultonSolver();
        adamsMoulton.createIntegrator();
        AbstractIntegrator integrator = adamsMoulton.getIntegrator();
        try {
            integrator.integrate(DES, t, y, t+h, yDot);
        } catch(DerivativeException | IntegratorException e) {
            logger.error("Error caught during non-stiff integration.", e);
        }
    }

    // TODO needs to be changed, I misunderstood the documentation --> uses BDF for stiff steps
    // when implementing BDF (Gear's Solver), should I just add the solver directly into this function or should I create a new class and then call the class?
    private void stiffStep(DESystem DES, double t, double[] y, double[] yDot, double h, double[] error) {

        RosenbrockSolver rosenbrockSolver = new RosenbrockSolver();
        try {
            rosenbrockSolver.step(DES);
        } catch (DerivativeException e) {
            logger.error("Error caught during stiff integration.", e);

        }
        


    }

    //Getters and setters
    public int getMaxOrdN() {
        return this.maxOrdN;
    }

    public void setMaxOrdN(int maxOrdN) {
        this.maxOrdN = maxOrdN;
    }

    public int getMaxOrdS() {
        return this.maxOrdS;
    }

    public void setMaxOrdS(int maxOrdS) {
        this.maxOrdS = maxOrdS;
    }

    public boolean isStiff() {
        return this.stiff;
    }

    public void changeStiffness() {
        stiff = !stiff;
    }

}
































