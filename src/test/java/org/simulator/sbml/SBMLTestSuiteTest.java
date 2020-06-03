package org.simulator.sbml;

import org.apache.commons.math.ode.DerivativeException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.TestUtils;
import org.simulator.comp.CompSimulator;
import org.simulator.io.CSVImporter;
import org.simulator.math.MaxDivergenceTolerance;
import org.simulator.math.QualityMeasure;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.*;

/**
 * Run full sbml-test-suite
 */
@RunWith(value = Parameterized.class)
public class SBMLTestSuiteTest {
    private String path;
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
    private static final String SBML_TEST_SUITE_PATH = "SBML_TEST_SUITE_PATH";
    private static final double THRESHOLD = 0.001;

    @Before
    public void setUp(){ }

    @After
    public void tearDown(){ }

    public SBMLTestSuiteTest(String path) {
        this.path = path;
    }

    /**
     * Test cases.
     * @return
     */
    @Parameters(name= "{index}: {0}")
    public static Iterable<Object[]> data(){

        // environment variable for semantic test case folder
        String testsuite_path = TestUtils.getPathForTestResource("/sbml-test-suite/cases/semantic/");
        System.out.println(SBML_TEST_SUITE_PATH + ": " + testsuite_path);

        if (testsuite_path.length() == 0){
            Object[][] resources = new String[0][1];
            logger.warn(String.format("%s environment variable not set.", SBML_TEST_SUITE_PATH));
            return Arrays.asList(resources);
        }

        int N = 1809;
        Object[][] resources = new String[N][1];
        for (int model_number = 1; model_number <= N; model_number++){

            // System.out.println("model " + model_number);

            StringBuilder modelFile = new StringBuilder();
            modelFile.append(model_number);
            while (modelFile.length() < 5) {
                modelFile.insert(0, '0');
            }
            String path = modelFile.toString();
            modelFile.append('/');
            modelFile.append(path);
            modelFile.insert(0, testsuite_path);
            path = modelFile.toString();

            resources[(model_number-1)][0] = path;

        }
        return Arrays.asList(resources);
    }


    @Test
    public void testModel() throws FileNotFoundException, IOException, XMLStreamException {

        if (path.contains("01592")){
            // FIXME: skipping test which takes 20-30 minutes to run (see https://github.com/draeger-lab/SBSCL/issues/39)
            assert(false);
        }


        //System.out.println(path);
        String sbmlfile, csvfile, configfile;
        csvfile = path + "-results.csv";
        configfile = path + "-settings.txt";

        Properties props = new Properties();
        props.load(new BufferedReader(new FileReader(configfile)));
        // int start = Integer.valueOf(props.getProperty("start"));
        double duration;
        double steps = (!props.getProperty("steps").isEmpty()) ? Double.parseDouble(props.getProperty("steps")) : 0d;
        Map<String, Boolean> amountHash = new HashMap<String, Boolean>();
        String[] amounts = String.valueOf(props.getProperty("amount")).split(",");
        String[] concentrations = String.valueOf(
                props.getProperty("concentration")).split(",");
         double absolute = (!props.getProperty("absolute").isEmpty()) ? Double.parseDouble(props.getProperty("absolute")) : 0d;
         double relative = (!props.getProperty("relative").isEmpty()) ? Double.parseDouble(props.getProperty("relative")) : 0d;

        for (String s : amounts) {
            s = s.trim();
            if (!s.isEmpty()) {
                amountHash.put(s, true);
            }
        }

        for (String s : concentrations) {
            s = s.trim();
            if (!s.isEmpty()) {
                amountHash.put(s, false);
            }
        }

        // Test all the SBML versions of test file
        String[] sbmlFileTypes = {"-sbml-l1v2.xml", "-sbml-l2v1.xml",
                "-sbml-l2v2.xml", "-sbml-l2v3.xml", "-sbml-l2v4.xml",
                "-sbml-l3v1.xml"};

        for (String sbmlFileType : sbmlFileTypes) {
            sbmlfile = path + sbmlFileType;
            File sbmlFile = new File(sbmlfile);

            if ((sbmlFile != null) && (sbmlFile.exists())) {
                // read model
                Model model = null;
                boolean errorInModelReading = false;
                try {
                    model = (new SBMLReader()).readSBML(sbmlFile)
                            .getModel();
                } catch (Exception e) {
                    errorInModelReading = true;
                }
                Assert.assertNotNull(model);
                Assert.assertFalse(errorInModelReading);

                // get timepoints
                CSVImporter csvimporter = new CSVImporter();
                MultiTable inputData = csvimporter.convert(model, csvfile);
                double[] timepoints = inputData.getTimePoints();
                duration = timepoints[timepoints.length - 1]
                        - timepoints[0];

                if (model.getExtension(CompConstants.shortLabel) == null){

                    AbstractDESSolver solver = new RosenbrockSolver();
                    // initialize interpreter
                    SBMLinterpreter interpreter = null;
                    boolean exceptionInInterpreter = false;
                    try {
                        interpreter = new SBMLinterpreter(model, 0, 0, 1,
                                amountHash);
                    } catch (Exception e) {
                        exceptionInInterpreter = true;
                    }
                    Assert.assertNotNull(interpreter);
                    Assert.assertFalse(exceptionInInterpreter);

                    if ((solver != null) && (interpreter != null)) {

                        solver.setStepSize(duration / steps);

                        // solve
                        MultiTable solution = null;
                        boolean errorInSolve = false;
                        try {
                            solution = solver.solve(interpreter,
                                    interpreter.getInitialValues(), timepoints);
                        } catch (DerivativeException e) {
                            errorInSolve = true;
                        }
                        Assert.assertNotNull(solution);
                        Assert.assertFalse(errorInSolve);
                        Assert.assertFalse(solver.isUnstable());

                        MultiTable left = solution;
                        MultiTable right = inputData;
                        if (solution.isSetTimePoints() && inputData.isSetTimePoints()) {
                            left = solution.filter(inputData.getTimePoints());
                            right = inputData.filter(solution.getTimePoints());
                        }

                        // compute the maximum divergence from the pre-defined results
                        QualityMeasure distance = new MaxDivergenceTolerance(absolute, relative);
                        List<Double> maxDivTolerances = distance.getColumnDistances(left, right);
                        for (Double maxDivTolerance: maxDivTolerances) {
                            Assert.assertTrue(maxDivTolerance <= 1d);
                        }

                    }
                }else {

                    // initialize simulator
                    CompSimulator compSimulator = null;
                    boolean errorInCompSimulator = false;
                    try {
                        compSimulator = new CompSimulator(sbmlFile);
                    }catch (Exception e) {
                        errorInCompSimulator = true;
                    }
                    Assert.assertNotNull(compSimulator);
                    Assert.assertFalse(errorInCompSimulator);

                    //solve
                    MultiTable solution = null;
                    boolean errorInSolve = false;
                    try {
                        double stepSize = (duration / steps);
                        solution = compSimulator.solve(stepSize, duration);
                    } catch (Exception e) {
                        errorInSolve = true;
                    }
                    Assert.assertNotNull(solution);
                    Assert.assertFalse(errorInSolve);

                    //TODO: Add quality measure to check whether solution meets the correct results
                }
            }
        }
    }
}