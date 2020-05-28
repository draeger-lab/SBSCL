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

        // writes results to the output file in CSV format
        File outputFile = new File(outputFilePath);
        outputFile.createNewFile();

        System.out.println(Paths.get(outputFilePath));
        FileWriter csvWriter = new FileWriter(outputFilePath);

        csvWriter.append(left.getColumnName(0).toLowerCase()).append(",");
        System.out.print(left.getColumnName(0).toLowerCase() + ",");
        for (int i=1;i<left.getColumnCount()-1;i++){
            System.out.print(left.getColumnName(i) + ",");
            csvWriter.append(left.getColumnName(i)).append(",");
        }
        System.out.println(left.getColumnName(left.getColumnCount()-1));
        csvWriter.append(left.getColumnName(left.getColumnCount()-1)).append("\n");

        for (int i = 0 ; i < left.getRowCount(); i++){
            for (int j = 0; j < left.getColumnCount()-1; j++){
                System.out.print(left.getValueAt(i, j) + ",");
                csvWriter.append(Double.toString(left.getValueAt(i, j))).append(",");
            }
            csvWriter.append(Double.toString(left.getValueAt(i, left.getColumnCount()-1))).append("\n");
            System.out.println(left.getValueAt(i, left.getColumnCount()-1));
        }

        csvWriter.flush();
        csvWriter.close();

    }

}
