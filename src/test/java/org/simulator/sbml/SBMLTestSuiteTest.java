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
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.TestUtils;
import org.simulator.comp.CompSimulator;
import org.simulator.fba.FluxBalanceAnalysis;
import org.simulator.io.CSVImporter;
import org.simulator.math.MaxDivergenceTolerance;
import org.simulator.math.QualityMeasure;
import org.simulator.math.odes.*;
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
    public static final String STEPS = "steps";
    public static final String AMOUNT = "amount";
    public static final String CONCENTRATION = "concentration";
    public static final String ABSOLUTE = "absolute";
    public static final String RELATIVE = "relative";
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
    private static final String SBML_TEST_SUITE_PATH = "SBML_TEST_SUITE_PATH";
    private static final double THRESHOLD = 0.001;

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    public SBMLTestSuiteTest(String path) {
        this.path = path;
    }

    /**
     * Test cases.
     *
     * @return
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {

        // environment variable for semantic test case folder
        String testsuite_path = TestUtils.getPathForTestResource(File.separator + "sbml-test-suite" + File.separator + "cases" + File.separator + "semantic" + File.separator);
        System.out.println(SBML_TEST_SUITE_PATH + ": " + testsuite_path);

        if (testsuite_path.length() == 0) {
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
            modelFile.append(File.separator);
            modelFile.append(path);
            modelFile.insert(0, testsuite_path);
            path = modelFile.toString();

            resources[(model_number - 1)][0] = path;

        }
        return Arrays.asList(resources);
    }


    @Test
    public void testModel() throws FileNotFoundException, IOException, XMLStreamException {

        if (path.contains("01592")) {
            // FIXME: skipping test which takes 20-30 minutes to run (see https://github.com/draeger-lab/SBSCL/issues/39)
            assert (false);
        }

        System.out.println("Testing test case: " + path);

        //System.out.println(path);
        String sbmlfile, csvfile, configfile;
        csvfile = path + "-results.csv";
        configfile = path + "-settings.txt";

        Properties props = new Properties();
        props.load(new BufferedReader(new FileReader(configfile)));
        // int start = Integer.valueOf(props.getProperty("start"));
        double duration;
        double steps = (!props.getProperty(STEPS).isEmpty()) ? Double.parseDouble(props.getProperty(STEPS)) : 0d;
        Map<String, Boolean> amountHash = new HashMap<String, Boolean>();
        String[] amounts = String.valueOf(props.getProperty(AMOUNT)).split(",");
        String[] concentrations = String.valueOf(
                props.getProperty(CONCENTRATION)).split(",");
        double absolute = (!props.getProperty(ABSOLUTE).isEmpty()) ? Double.parseDouble(props.getProperty(ABSOLUTE)) : 0d;
        double relative = (!props.getProperty(RELATIVE).isEmpty()) ? Double.parseDouble(props.getProperty(RELATIVE)) : 0d;

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
                "-sbml-l3v1.xml", "-sbml-l3v2.xml"};

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

                if (model.getExtension(CompConstants.shortLabel) != null) {

                    // initialize simulator
                    CompSimulator compSimulator = null;
                    boolean errorInCompSimulator = false;
                    try {
                        compSimulator = new CompSimulator(sbmlFile);
                    } catch (Exception e) {
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

                } else if (model.getExtension(FBCConstants.shortLabel) != null) {

                    FluxBalanceAnalysis solver = null;
                    boolean errorInFBASimulator = false;
                    try {
                        solver = new FluxBalanceAnalysis(org.sbml.jsbml.SBMLReader.read(sbmlFile));
                    } catch (Exception e) {
                        errorInFBASimulator = true;
                    }
                    Assert.assertNotNull(solver);
                    Assert.assertFalse(errorInFBASimulator);

                    boolean errorInSolve = false;
                    try {
                        solver.solve();
                    } catch (Exception e) {
                        errorInSolve = true;
                    }
                    Assert.assertFalse(errorInSolve);

                    Map<String, Double> solution = solver.getSolution();
                    solution.put("OBJF", solver.getObjectiveValue());

                    System.out.println(solution);

                    BufferedReader reader = new BufferedReader(new FileReader(csvfile));
                    String[] keys = reader.readLine().trim().split(",");
                    String[] values = reader.readLine().trim().split(",");

                    Map<String, Double> inputSolution = new HashMap<>();
                    for (int i = 0; i < keys.length; i++) {
                        inputSolution.put(keys[i], Double.valueOf(values[i]));
                    }

                    for (Map.Entry<String, Double> mapElement : inputSolution.entrySet()) {
                        if (solution.containsKey(mapElement.getKey())) {
                            Assert.assertEquals(mapElement.getValue(), solution.get(mapElement.getKey()), 0.0002);
                        }
                    }

                } else {

                    DESSolver solver = new RosenbrockSolver();
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

                        if (solver instanceof AbstractDESSolver) {
                            ((AbstractDESSolver) solver).setIncludeIntermediates(false);
                        }

                        if (solver instanceof AdaptiveStepsizeIntegrator) {
                            ((AdaptiveStepsizeIntegrator) solver).setAbsTol(1E-12);
                            ((AdaptiveStepsizeIntegrator) solver).setRelTol(1E-12);
                        }

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
                        for (Double maxDivTolerance : maxDivTolerances) {
                            Assert.assertTrue(maxDivTolerance <= 1d);
                        }

                    }

                }
            }
        }
    }
}