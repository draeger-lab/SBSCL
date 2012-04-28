package org.simulator.sedml;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.jlibsedml.Libsedml;
import org.jlibsedml.SEDMLDocument;
import org.jlibsedml.XMLException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.SBMLException;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.sbml.jsbml.xml.stax.SBMLReader;
import org.simulator.math.odes.AbstractDESSolver;
import org.simulator.math.odes.DormandPrince54Solver;
import org.simulator.sbml.SBMLinterpreter;

public class SEDMLWriterTest {
	private static final String COMMENT = "Standard time course of ABC1 model";
	File SBMLFile = new File("files/sedmlTest/abc_1.xml");
	SEDMLWriter writer;
	@Before
	public void setUp() throws Exception {
		writer = new SEDMLWriter();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void testSaveExperimentToSEDML() throws IOException, XMLStreamException, SBMLException, ModelOverdeterminedException, XMLException {
		double start=0;
		double end=10;
		double stepsize=0.1;
	    Model model = (new SBMLReader()).readSBML(SBMLFile).getModel();
	    AbstractDESSolver solver = new DormandPrince54Solver();
	    solver.setStepSize(0.1);
	    SBMLinterpreter interpreter = new SBMLinterpreter(model);
	
	    if (solver instanceof AbstractDESSolver) {
	      ((AbstractDESSolver) solver).setIncludeIntermediates(false);
	    }
	    writer.setComment(COMMENT);
	    
	    File tmp = File.createTempFile("sedmlOut", "xml");
	    FileOutputStream fos = new FileOutputStream(tmp);
	    writer.saveExperimentToSEDML(start,end,stepsize,solver,model,
	    		SBMLFile.toURI(),fos );
	    
	    // now test reading in SEDML file
	    SEDMLDocument doc = Libsedml.readDocument(tmp);
	    assertFalse(doc.hasErrors());
	    // time, A, B,C
	    assertEquals(4, doc.getSedMLModel().getDataGenerators().size());
	    // check comment added
	    assertEquals(COMMENT, doc.getSedMLModel().getNotes().get(0).getNotesElement().getText());
	    
	    // save will fail if OutputStream is unavailable
	    fos.close();
	    boolean IoExthrown=false;
	    try{
	    writer.saveExperimentToSEDML(start,end,stepsize,solver,model,
	    		SBMLFile.toURI(),fos );
	    }catch(IOException e){
	    	IoExthrown =true;
	    }
	    if(!IoExthrown){
	    	fail("Should throw IOEXception since OS was closed");
	    }
	    
	}

}
