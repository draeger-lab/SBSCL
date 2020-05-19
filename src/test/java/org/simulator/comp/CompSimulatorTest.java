package org.simulator.comp;

import org.apache.commons.math.ode.DerivativeException;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.TestUtils;
import org.simulator.math.odes.MultiTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLStreamException;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;

@RunWith(value = Parameterized.class)
public class CompSimulatorTest {
    private static final Logger logger = LoggerFactory.getLogger(CompSimulatorTest.class);
    private String resource;

    public CompSimulatorTest(String resource) {
        this.resource = resource;
    }

    @Parameterized.Parameters(name= "{index}: {0}")
    public static Iterable<Object[]> data(){
        HashSet<String> skip = new HashSet<>();
        skip.add("test6.xml");
        skip.add("test7.xml");
        skip.add("test8.xml");
        skip.add("test9.xml");
        skip.add("test10.xml");
        String filter = null;

        // find all comp models
        String compPath = TestUtils.getPathForTestResource("/comp/");
        System.out.println("Comp models path: " + compPath);
        return TestUtils.findResources(compPath, ".xml", filter, skip, false);
    }

    @Test
    public void testComp() throws IOException, XMLStreamException, DerivativeException, ModelOverdeterminedException {
        // String compPath = TestUtils.getPathForTestResource("/comp/test1.xml");
        String compPath = resource;
        CompSimulator compSimulator = new CompSimulator(new File(compPath));

        assertNotNull(compSimulator);
        assertNotNull(compSimulator.getDoc());
        assertNotNull(compSimulator.getDocFlat());

        double stepSize = 1.0;
        double timeEnd = 100.0;
        MultiTable sol = compSimulator.solve(stepSize, timeEnd);

        assertNotNull(sol);
    }
}
