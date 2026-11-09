package org.simulator.math;

import org.junit.Test;
import org.mockito.Mockito;
import org.simulator.math.odes.MultiTable;

import static org.junit.Assert.assertTrue;

public class SimulationTraceCompressorTest {

    @Test
    public void testGenerateSummary() {
        // Create a Mock MultiTable
        MultiTable mockTable = Mockito.mock(MultiTable.class);

        // Define the behavior of our mock table (3 time steps, 2 columns)
        Mockito.when(mockTable.getRowCount()).thenReturn(3);
        Mockito.when(mockTable.getColumnCount()).thenReturn(2);
        
        // Mock time points
        Mockito.when(mockTable.getTimePoint(0)).thenReturn(0d);
        Mockito.when(mockTable.getTimePoint(1)).thenReturn(1d);
        Mockito.when(mockTable.getTimePoint(2)).thenReturn(2d);

        // Mock column names
        Mockito.when(mockTable.getColumnName(0)).thenReturn("Time");
        Mockito.when(mockTable.getColumnName(1)).thenReturn("Species_A");
        
        // Give Species A some data: Starts at 10, peaks at 50, ends at 5
        Mockito.when(mockTable.getValueAt(0, 1)).thenReturn(10d);
        Mockito.when(mockTable.getValueAt(1, 1)).thenReturn(50d);
        Mockito.when(mockTable.getValueAt(2, 1)).thenReturn(5d);

        // Run your new compressor!
        String result = SimulationTraceCompressor.generateSummary(mockTable);

        // Print it to the console so you can see your own work
        System.out.println("=== SUMMARY OUTPUT ===");
        System.out.println(result);
        System.out.println("=======================");

        // Verify the output contains the correct compressed insights
        assertTrue("Should contain row count", result.contains("Total Time Steps: 3"));
        assertTrue("Should contain initial value", result.contains("Species_A: Init:10.00"));
        assertTrue("Should contain peak value and exact time", result.contains("Peak:50.00 @ t=1.00"));
        assertTrue("Should contain final steady-state value", result.contains("Final:5.00"));
    }

    @Test
    public void testEmptyTableHandling() {
        MultiTable emptyTable = Mockito.mock(MultiTable.class);
        Mockito.when(emptyTable.getRowCount()).thenReturn(0);

        String result = SimulationTraceCompressor.generateSummary(emptyTable);

        assertTrue("Should handle empty tables gracefully", result.contains("Simulation data is empty."));
    }
}