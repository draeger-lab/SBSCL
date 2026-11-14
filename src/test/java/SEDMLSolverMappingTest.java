import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.lang.reflect.Method;
import org.jlibsedml.Libsedml;
import org.jlibsedml.SedML;
import org.junit.jupiter.api.Test;

import org.simulator.math.odes.LSODA.LSODAIntegrator;
import org.simulator.sedml.SedMLSBMLSimulatorExecutor;

public class SEDMLSolverMappingTest {

  @Test
  public void kisao0000088SelectsLSODA() throws Exception {     // 5x0 + 88
    File file = new File("src/test/resources/sedml/sed1.xml").getAbsoluteFile();
    SedML sedml = Libsedml.readDocument(file).getSedMLModel();
    SedMLSBMLSimulatorExecutor exe = new SedMLSBMLSimulatorExecutor(sedml, sedml.getOutputs().get(0), file.getParent());

    Method m = SedMLSBMLSimulatorExecutor.class.getDeclaredMethod("getSolverForKisaoID", String.class);
    m.setAccessible(true);
    assertTrue(m.invoke(exe, "KISAO:0000088") instanceof LSODAIntegrator);
  }
}
