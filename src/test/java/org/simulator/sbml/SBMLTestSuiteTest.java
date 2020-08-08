package org.simulator.sbml;

import org.apache.commons.math.ode.DerivativeException;
import org.junit.Assert;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
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
    public static final String NAN = "NaN";
    private static final Logger logger = LoggerFactory.getLogger(TestUtils.class);
    private static final String SBML_TEST_SUITE_PATH = "SBML_TEST_SUITE_PATH";
    private static final double TOLERANCE_FACTOR = 1E-5;
    private static final double DELTA = 0.0002d;

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
    public void testModel() throws IOException {

        String[] failedTests = new String[]{
            "01126", "01127", "01129", "01130", "01131", "01132", "01133", "01134", "01140", "01142", "01143", "01144", "01145", "01148", "01149", "01151", "01154", "01155", "01156", "01157", "01158", "01159", "01160", "01161", "01162", "01164", "01166", "01169", "01170", "01174", "01175", "01176", "01178", "01180", "01181", "01182", "01183", "01344", "01345", "01346", "01347", "01348", "01349", "01350", "01351", "01352", "01355", "01356", "01358", "01359", "01360", "01364", "01365", "01367", "01368", "01369", "01371", "01372", "01373", "01374", "01375", "01376", "01377", "01378", "01379", "01380", "01381", "01382", "01383", "01384", "01385", "01386", "01387", "01388", "01389", "01468", "01469", "01470", "01474", // failed due to comp flattening (see issue #36)
            "01153", // (comp model) failing with IllegalArgumentException: Cannot set duplicate meta identifier
            "01165", "01167", "01168", "01471", "01472", "01473", "01475", "01476", "01477", "01778", // (comp model) failing with FileNotFoundException: enzyme_model.xml (No such file or directory)
            "01507", "01508", "01511", // sbml model with changing compartment size (see issue #50)
            "01287", "01592", // failing due to long run time (see issue #39)
            "01400", "01401", "01403", "01406", "01409", // failing due to delay in rateOf (see issue #46)
            "01444", "01445", "01446", "01447", "01448", // failing due to event triggers before mentioned condition (see issue #44)
            "01456",
            "01480",
            "01481", // model below sbml l3v1
            "01492", "01493", // failing due to misinterpretation of function variables with parameters (see issue #45)
            "01520",
            "01539",
            "01560", "01568", "01570", "01787", // models below sbml l3v2
            "01575",
            "01583",
            "01589",
            "01626",
            "01754",
            "01758", "01759", "01760", "01761"
        };

        boolean isFailedTest = false;
        for (String failedTest : failedTests) {
            isFailedTest = isFailedTest || path.contains(failedTest);
        }

        if (isFailedTest){
            logger.warn("Test case failed");
            return;
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
                    e.printStackTrace();
                }
                Assert.assertNotNull(model);
                Assert.assertFalse(errorInModelReading);

                // get timepoints
                CSVImporter csvimporter = new CSVImporter();
                MultiTable inputData = csvimporter.readMultiTableFromCSV(model, csvfile);
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
                        e.printStackTrace();
                    }
                    Assert.assertNotNull(compSimulator);
                    Assert.assertFalse(errorInCompSimulator);

                    MultiTable solution = null;
                    boolean errorInSolve = false;
                    try {
                        double stepSize = (duration / steps);
                        solution = compSimulator.solve(stepSize, duration);
                    } catch (Exception e) {
                        errorInSolve = true;
                        e.printStackTrace();
                    }
                    Assert.assertNotNull(solution);
                    Assert.assertFalse(errorInSolve);

                } else if (model.getExtension(FBCConstants.shortLabel) != null) {

                    FluxBalanceAnalysis solver = null;
                    boolean errorInFBASimulator = false;
                    try {
                        solver = new FluxBalanceAnalysis(org.sbml.jsbml.SBMLReader.read(sbmlFile));
                    } catch (Exception e) {
                        errorInFBASimulator = true;
                        e.printStackTrace();
                    }
                    Assert.assertNotNull(solver);
                    Assert.assertFalse(errorInFBASimulator);

                    boolean errorInSolve = false;
                    boolean isSolved = false;
                    try {
                        isSolved = solver.solve();
                    } catch (Exception e) {
                        errorInSolve = true;
                        e.printStackTrace();
                    }
                    Assert.assertFalse(errorInSolve);

                    BufferedReader reader = new BufferedReader(new FileReader(csvfile));
                    String[] keys = reader.readLine().trim().split(",");
                    String[] values = reader.readLine().trim().split(",");

                    if (isSolved) {
                        Map<String, Double> solution = solver.getSolution();
                        Map<String, Double> inputSolution = new HashMap<>();
                        for (int i = 0; i < keys.length; i++) {
                            inputSolution.put(keys[i], Double.valueOf(values[i]));
                        }

                        for (Map.Entry<String, Double> mapElement : inputSolution.entrySet()) {
                            if (solution.containsKey(mapElement.getKey())) {
                                Assert.assertEquals(mapElement.getValue(), solution.get(mapElement.getKey()), DELTA);
                            }
                        }
                    } else {
                        if ((keys[0].equals(solver.getActiveObjective())) && (values[0].equals(NAN))) {
                            Assert.assertTrue(true);
                        } else {
                            Assert.fail();
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
                        e.printStackTrace();
                    }
                    Assert.assertNotNull(interpreter);
                    Assert.assertFalse(exceptionInInterpreter);

                    if ((solver != null) && (interpreter != null)) {

                        solver.setStepSize(duration / steps);

                        if (solver instanceof AbstractDESSolver) {
                            ((AbstractDESSolver) solver).setIncludeIntermediates(false);
                        }

                        if (solver instanceof AdaptiveStepsizeIntegrator) {
                            ((AdaptiveStepsizeIntegrator) solver).setAbsTol(TOLERANCE_FACTOR * absolute);
                            ((AdaptiveStepsizeIntegrator) solver).setRelTol(TOLERANCE_FACTOR * relative);
                        }

                        // solve
                        MultiTable solution = null;
                        boolean errorInSolve = false;
                        try {
                            solution = solver.solve(interpreter,
                                    interpreter.getInitialValues(), timepoints, null);
                        } catch (DerivativeException e) {
                            errorInSolve = true;
                            e.printStackTrace();
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
