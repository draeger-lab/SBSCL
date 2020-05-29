package org.simulator.sbml;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.simulator.TestUtils;
import org.simulator.io.CSVImporter;
import org.simulator.math.MaxAbsDistance;
import org.simulator.math.QualityMeasure;
import org.simulator.math.RelativeMaxDistance;
import org.simulator.math.odes.MultiTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@RunWith(value = Parameterized.class)
public class MaxRelDistanceTest {

    private static final Logger logger = LoggerFactory.getLogger(MaxRelDistanceTest.class);
    private String resource;
    private static final String REL_DISTANCE_PATH = "DISTANCE_PATH";

    public MaxRelDistanceTest(String resource) {
        this.resource = resource;
    }

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> data() {

        String distance_path = TestUtils.getPathForTestResource("/distance/test");
        System.out.println(REL_DISTANCE_PATH + ": " + distance_path);

        if (distance_path.length() == 0){
            Object[][] resources = new String[0][1];
            logger.warn(String.format("%s environment variable not set.", REL_DISTANCE_PATH));
            return Arrays.asList(resources);
        }

        int N = 2;
        Object[][] resources = new String[N][1];
        for (int test_number = 1; test_number <= N; test_number++){

            StringBuilder modelFile = new StringBuilder();
            modelFile.append(test_number);
            modelFile.append('/');
            modelFile.insert(0, distance_path);
            String path = modelFile.toString();

            resources[(test_number-1)][0] = path;

        }
        return Arrays.asList(resources);

    }

    @Test
    public void testMaxRelDistance() throws IOException {

        // configuration
        String filePath = resource;

        String first = filePath + "a.csv";
        String second = filePath + "b.csv";
        String result = filePath + "rel_result.csv";

        // convert the test files to MultiTable
        CSVImporter csvImporter = new CSVImporter();
        MultiTable a = csvImporter.convert(null, first);
        MultiTable b = csvImporter.convert(null, second);

        // calculates max relative distance
        QualityMeasure distance = new RelativeMaxDistance();
        ArrayList<Double> relMaxDistances = distance.getColumnDistances(a, b);

        // get pre-defined results from the test case
        ArrayList<Double> inputData = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(result))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                inputData.add(Double.parseDouble(values[0]));
            }
        }

        System.out.println("Results: " + relMaxDistances);
        System.out.println("Pre-defined Results: " + inputData);

        for (int i = 0; i < relMaxDistances.size(); i++) {
            Assert.assertEquals(relMaxDistances.get(i), inputData.get(i), 0.02);
        }

    }

}
