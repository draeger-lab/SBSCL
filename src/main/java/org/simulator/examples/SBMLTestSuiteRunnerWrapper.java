package org.simulator.examples;

import org.apache.commons.math.ode.DerivativeException;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.io.CSVImporter;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.MultiTable;
import org.simulator.math.odes.RosenbrockSolver;
import org.simulator.sbml.SBMLinterpreter;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Properties;

/**
 * A wrapper class used to verify tests in the SBML Test Runner
 */
public class SBMLTestSuiteRunnerWrapper {

    /**
     * Runs a simulation of a SBML file and writes result to a specified CSV file
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
        String  version = args[4];

        String filePath = dirPath + '/' + currentCase + '/' + currentCase + "-sbml-l" + level + 'v' + version + ".xml";
        String settingsPath = dirPath + '/' + currentCase + '/' + currentCase + "-settings.txt";
        String outputFilePath = outputDirPath + '/' + currentCase + ".csv";
        String resultsPath = dirPath + '/' + currentCase + '/' + currentCase + "-results.csv";

        Properties properties = new Properties();
        properties.load(new BufferedReader(new FileReader(settingsPath)));
        double duration;
        double steps = (!properties.getProperty("steps").equals("")) ? Double.parseDouble(properties.getProperty("steps")) : 0;

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

        AbstractDESSolver solver = new RosenbrockSolver();
        solver.setStepSize(duration / steps);
        SBMLinterpreter interpreter = new SBMLinterpreter(model);

        ((AbstractDESSolver) solver).setIncludeIntermediates(false);

        // Compute the numerical solution of the problem
        MultiTable solution = solver.solve(interpreter, interpreter.getInitialValues(), timepoints);

        MultiTable left = solution;
        if (solution.isSetTimePoints() && inputData.isSetTimePoints()) {
            left = solution.filter(inputData.getTimePoints());
        }

        // Map of variables present in the test suite results file
        HashMap<String, Integer> resultColumns = new HashMap<>();
        for (int i=0;i<inputData.getColumnCount();i++){
            resultColumns.put(inputData.getColumnName(i), 1);
        }

        // Boolean array to check which variables are present in the test suite results file
        boolean[] variablesToAdd = new boolean[solution.getColumnCount()];
        System.out.println(solution.getColumnCount());
        if (resultColumns.containsKey(left.getColumnName(0))) {
            variablesToAdd[0] = true;
        }
        for (int i=1;i<left.getColumnCount();i++){
            if (resultColumns.containsKey(left.getColumnName(i))) {
                variablesToAdd[i] = true;
            }
        }

        // writes results to the output file in CSV format
        File outputFile = new File(outputFilePath);
        outputFile.createNewFile();

        System.out.println(Paths.get(outputFilePath));
        FileWriter csvWriter = new FileWriter(outputFilePath);

        StringBuilder output = new StringBuilder("");
        if (variablesToAdd[0]) {
            output.append(left.getColumnName(0).toLowerCase()).append(",");
        }
        for (int i=1;i<left.getColumnCount()-1;i++){
            if (variablesToAdd[i]){
                output.append(left.getColumnName(i)).append(",");
            }
        }
        if (variablesToAdd[left.getColumnCount()-1]){
            output.append(left.getColumnName(left.getColumnCount()-1)).append("\n");
        }else {
            if (output.length() > 0) {
                output.deleteCharAt(output.length() - 1);
                output.append("\n");
            }
        }

        for (int i = 0 ; i < left.getRowCount(); i++){
            for (int j = 0; j < left.getColumnCount()-1; j++){
                if (variablesToAdd[j]){
                    output.append(left.getValueAt(i, j)).append(",");
                }
            }
            if (variablesToAdd[left.getColumnCount()-1]){
                output.append(left.getValueAt(i, left.getColumnCount()-1)).append("\n");
            }else {
                if (output.length() > 0) {
                    output.deleteCharAt(output.length() - 1);
                    output.append("\n");
                }
            }
        }

        csvWriter.append(output);
        csvWriter.flush();
        csvWriter.close();

    }

}
