package org.simulator.examples;

import org.apache.commons.math.ode.DerivativeException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.ext.comp.CompConstants;
import org.sbml.jsbml.ext.fbc.FBCConstants;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.comp.CompSimulator;
import org.simulator.fba.FluxBalanceAnalysis;
import org.simulator.io.CSVImporter;
import org.simulator.math.odes.*;
import org.simulator.sbml.SBMLinterpreter;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * <p>A wrapper class used to verify tests in the SBML Test Runner</p>
 *
 * <p>
 * The results of the simulation by this class are stored as a CSV file in the specified output
 * directory in SBML Test Runner. Then the SBML Test Runner creates the distance plots comparing
 * this results with the pre-defined results from SBML Test Suite under the criteria given at this
 * <a href="http://sbml.org/Software/SBML_Test_Suite/Case_Descriptions#The_.22settings.22_file" target="_blank">link</a>
 * </p>
 */
public class SBMLTestSuiteRunnerWrapper {

    public static final String STEPS = "steps";
    public static final String AMOUNT = "amount";
    public static final String CONCENTRATION = "concentration";
    public static final String ABSOLUTE = "absolute";
    public static final String RELATIVE = "relative";
    private static final double TOLERANCE_FACTOR = 1E-3;

    /**
     * Runs a simulation of a SBML file and writes result to a specified CSV file
     *
     * @param args
     * @throws IOException
     * @throws XMLStreamException
     * @throws ModelOverdeterminedException
     * @throws DerivativeException
     */
    public static void main(String[] args) throws IOException, XMLStreamException, ModelOverdeterminedException, DerivativeException {

        // Configuration
        String dirPath = args[0];
        String currentCase = args[1];
        String outputDirPath = args[2];
        String level = args[3];
        String version = args[4];

        String filePath = dirPath + File.separator + currentCase + File.separator + currentCase + "-sbml-l" + level + 'v' + version + ".xml";
        String settingsPath = dirPath + File.separator + currentCase + File.separator + currentCase + "-settings.txt";
        String outputFilePath = outputDirPath + File.separator + currentCase + ".csv";
        String resultsPath = dirPath + File.separator + currentCase + File.separator + currentCase + "-results.csv";

        Properties properties = new Properties();
        properties.load(new BufferedReader(new FileReader(settingsPath)));
        double duration;
        double steps = (!properties.getProperty(STEPS).isEmpty()) ? Double.parseDouble(properties.getProperty(STEPS)) : 0d;
        Map<String, Boolean> amountHash = new HashMap<String, Boolean>();
        String[] amounts = String.valueOf(properties.getProperty(AMOUNT)).split(",");
        String[] concentrations = String.valueOf(
                properties.getProperty(CONCENTRATION)).split(",");
        double absolute = (!properties.getProperty(ABSOLUTE).isEmpty()) ? Double.parseDouble(properties.getProperty(ABSOLUTE)) : 0d;
        double relative = (!properties.getProperty(RELATIVE).isEmpty()) ? Double.parseDouble(properties.getProperty(RELATIVE)) : 0d;

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

        // Read the model and initialize solver
        File sbmlfile = new File(filePath);
        SBMLDocument document = (new SBMLReader()).readSBML(sbmlfile);
        Model model = document.getModel();

        // get timepoints
        CSVImporter csvimporter = new CSVImporter();
        MultiTable inputData = csvimporter.convert(model, resultsPath);
        double[] timepoints = inputData.getTimePoints();
        duration = timepoints[timepoints.length - 1]
                - timepoints[0];

        MultiTable solution;

        // writes results to the output file in CSV format
        File outputFile = new File(outputFilePath);
        outputFile.createNewFile();

        System.out.println(Paths.get(outputFilePath));
        FileWriter csvWriter = new FileWriter(outputFilePath);

        StringBuilder output = new StringBuilder("");

        if (model.getExtension(FBCConstants.shortLabel) != null) {

            FluxBalanceAnalysis solver = new FluxBalanceAnalysis(document);
            if (solver.solve()) {
                Map<String, Double> fbcSolution = solver.getSolution();

                System.out.println(fbcSolution);

                BufferedReader reader = new BufferedReader(new FileReader(resultsPath));
                String[] keys = reader.readLine().trim().split(",");

                for (int i = 0; i < keys.length - 1; i++) {
                    output.append(keys[i]).append(",");
                }
                output.append(keys[keys.length - 1]).append("\n");

                for (String key : keys) {
                    output.append(fbcSolution.get(key)).append(",");
                }

                if (output.length() > 0) {
                    output.deleteCharAt(output.length() - 1);
                    output.append("\n");
                }
            }

        } else {
            if (model.getExtension(CompConstants.shortLabel) == null) {
                DESSolver solver = new RosenbrockSolver();
                solver.setStepSize(duration / steps);

                if (solver instanceof AbstractDESSolver) {
                    solver.setIncludeIntermediates(false);
                }

                if (solver instanceof AdaptiveStepsizeIntegrator) {
                    ((AdaptiveStepsizeIntegrator) solver).setAbsTol(TOLERANCE_FACTOR * absolute);
                    ((AdaptiveStepsizeIntegrator) solver).setRelTol(TOLERANCE_FACTOR * relative);
                }

                SBMLinterpreter interpreter = new SBMLinterpreter(model, 0, 0, 1, amountHash);

                // Compute the numerical solution of the problem
                solution = solver.solve(interpreter, interpreter.getInitialValues(), timepoints);
            } else {
                CompSimulator compSimulator = new CompSimulator(sbmlfile);
                double stepSize = (duration / steps);

                solution = compSimulator.solve(stepSize, duration);
            }

            MultiTable left = solution;
            if (solution.isSetTimePoints() && inputData.isSetTimePoints()) {
                left = solution.filter(inputData.getTimePoints());
            }

            // Map of variables present in the test suite results file
            Map<String, Integer> resultColumns = new HashMap<>();
            for (int i = 0; i < inputData.getColumnCount(); i++) {
                resultColumns.put(inputData.getColumnName(i), 1);
            }

            // Boolean array to check which variables are present in the test suite results file
            boolean[] variablesToAdd = new boolean[solution.getColumnCount()];
            if (resultColumns.containsKey(left.getColumnName(0).toLowerCase())) {
                variablesToAdd[0] = true;
            }
            for (int i = 1; i < left.getColumnCount(); i++) {
                if (resultColumns.containsKey(left.getColumnName(i))) {
                    variablesToAdd[i] = true;
                }
            }

            if (variablesToAdd[0]) {
                output.append(left.getColumnName(0).toLowerCase()).append(",");
            }
            for (int i = 1; i < left.getColumnCount() - 1; i++) {
                if (variablesToAdd[i]) {
                    output.append(left.getColumnName(i)).append(",");
                }
            }
            if (variablesToAdd[left.getColumnCount() - 1]) {
                output.append(left.getColumnName(left.getColumnCount() - 1)).append("\n");
            } else {
                if (output.length() > 0) {
                    output.deleteCharAt(output.length() - 1);
                    output.append("\n");
                }
            }

            for (int i = 0; i < left.getRowCount(); i++) {
                for (int j = 0; j < left.getColumnCount() - 1; j++) {
                    if (variablesToAdd[j]) {
                        output.append(left.getValueAt(i, j)).append(",");
                    }
                }
                if (variablesToAdd[left.getColumnCount() - 1]) {
                    output.append(left.getValueAt(i, left.getColumnCount() - 1)).append("\n");
                } else {
                    if (output.length() > 0) {
                        output.deleteCharAt(output.length() - 1);
                        output.append("\n");
                    }
                }
            }
        }

        csvWriter.append(output);
        csvWriter.flush();
        csvWriter.close();

    }

}
