package fern.simulation.algorithm;

import fern.network.Network;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TauLeapingSimulatorTest {

    @Test
    public void testSimulatorInitialization() {
        // Create a mock Network to safely satisfy the Simulator's constructor
        Network mockNetwork = Mockito.mock(Network.class);
        
        // Initialize the simulator with our dummy network
        TauLeapingSimulator simulator = new TauLeapingSimulator(mockNetwork);
        
        // Verify the object is created successfully 
        assertNotNull(simulator, "TauLeapingSimulator should instantiate successfully");
        
        // Verify the overridden getName() method works
        assertEquals("Explicit Tau-Leaping", simulator.getName());
    }
}