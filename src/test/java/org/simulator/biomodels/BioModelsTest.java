package org.simulator.biomodels;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.TestUtils;
import org.simulator.math.odes.AdaptiveStepsizeIntegrator;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.sbml.SBMLinterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;

@RunWith(value = Parameterized.class)
public class BioModelsTest {

    private String resource;
    private static final String BIO_MODELS_RESOURCE_PATH = "/biomodels";
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);

    public static String getBiomodelsPath() {
        return TestUtils.getPathForTestResource(BIO_MODELS_RESOURCE_PATH);
    }

    public BioModelsTest(String resource) {
        this.resource = resource;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {
        HashSet<String> skip = null;
        String filter = null;
        Boolean mvnResource = false;

        String bioPath = getBiomodelsPath();
        logger.info("BioModels path: " + bioPath);
        return TestUtils.findResources(bioPath, ".xml", filter, skip, mvnResource);
    }

    @Test
    public void testBioModels() throws Exception {
        AdaptiveStepsizeIntegrator solver = new RosenbrockSolver();
        solver.setAbsTol(1E-12);
        solver.setRelTol(1E-6);
        String[] dirs = resource.split("/");
        int modelnr = Integer.parseInt(dirs[dirs.length - 1].substring(5, 14));
        boolean slowModel = false;

        String modelFile = resource;
        Model model = (new SBMLReader()).readSBML(modelFile).getModel();

        if (model != null) {
            solver.reset();
            try {
                double time1 = System.nanoTime();
                SBMLinterpreter interpreter = new SBMLinterpreter(model);
                if ((solver != null) && (interpreter != null)) {
                    solver.setStepSize(0.01);
                    solver.solve(interpreter, interpreter.getInitialValues(), 0, 10);
                    if (solver.isUnstable()) {
                        logger.error("unstable!");
                    }
                }
                double time2 = System.nanoTime();
                double runningTime = (time2 - time1) / 1E9;
                if (runningTime >= 300) {
                    slowModel = true;
                }
            } catch (Exception e) {
                logger.error("Exception in Biomodel " + modelnr);
            }
        }

        if (slowModel) {
            logger.warn("Slow running time in Biomodel " + modelnr);
        }
    }

}
