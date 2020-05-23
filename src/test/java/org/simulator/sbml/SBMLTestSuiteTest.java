package org.simulator.sbml;

import org.apache.commons.math.ode.DerivativeException;
import org.junit.*;
import org.sbml.jsbml.*;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.TestUtils;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.simulator.io.CSVImporter;
import org.simulator.math.QualityMeasure;
import org.simulator.math.RelativeEuclideanDistance;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

        int N = 1780;
        Object[][] resources = new String[N][1];
        for (int model_number = 1; model_number <= N; model_number++){
            if (model_number != 1592){
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
        }
        return Arrays.asList(resources);
    }


    @Test
    public void testModel() throws FileNotFoundException, IOException {

        //System.out.println(path);
        String sbmlfile, csvfile, configfile;
        csvfile = path + "-results.csv";
        configfile = path + "-settings.txt";

        Properties props = new Properties();
        props.load(new BufferedReader(new FileReader(configfile)));
        // int start = Integer.valueOf(props.getProperty("start"));
        double duration;
        double steps = (!props.getProperty("steps").equals("")) ? Double.parseDouble(props.getProperty("steps")) : 0;
        Map<String, Boolean> amountHash = new HashMap<String, Boolean>();
        String[] amounts = String.valueOf(props.getProperty("amount"))
                .trim().split(",");
        String[] concentrations = String.valueOf(
                props.getProperty("concentration")).split(",");
        // double absolute = Double.valueOf(props.getProperty("absolute"));
        // double relative = Double.valueOf(props.getProperty("relative"));

        for (String s : amounts) {
            s = s.trim();
            if (!s.equals("")) {
                amountHash.put(s, true);
            }
        }

        for (String s : concentrations) {
            s = s.trim();
            if (!s.equals("")) {
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

                AbstractDESSolver solver = new RosenbrockSolver();
                // initialize interpreter
                SBMLinterpreter interpreter = null;
                boolean exceptionInInterpreter = false;
                try {
                    interpreter = new SBMLinterpreter(model, 0, 0, 1,
                            amountHash);
                } catch (SBMLException e) {
                    exceptionInInterpreter = true;
                } catch (ModelOverdeterminedException e) {
                    exceptionInInterpreter = true;
                }
                Assert.assertNotNull(interpreter);
                Assert.assertFalse(exceptionInInterpreter);

                // get timepoints
                CSVImporter csvimporter = new CSVImporter();
                MultiTable inputData = csvimporter.convert(model, csvfile);
                double[] timepoints = inputData.getTimePoints();
                duration = timepoints[timepoints.length - 1]
                        - timepoints[0];

                if ((solver != null) && (interpreter != null)) {
                    // System.out.println(sbmlFileType + " " + solver.getName());
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

                    // compute distance
                    QualityMeasure distance = new RelativeEuclideanDistance();
                    double dist = distance.distance(solution, inputData);
                    Assert.assertTrue(dist <= 0.2);

                }
            }
        }
    }
}