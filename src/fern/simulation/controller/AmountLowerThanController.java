package fern.simulation.controller;

import fern.simulation.Simulator;

public class AmountLowerThanController implements SimulationController {

	private long amount;
	private String speciesName;
	private int speciesIndex = -1;
	
	public AmountLowerThanController(long amount, String speciesName) {
		this.amount = amount;
		this.speciesName = speciesName;
	}

	public AmountLowerThanController(long amount, int speciesIndex) {
		this.amount = amount;
		this.speciesIndex = speciesIndex;
	}


	public boolean goOn(Simulator sim) {
		if (speciesIndex==-1)
			speciesIndex = sim.getNet().getSpeciesByName(speciesName);
		
		return sim.getAmount(speciesIndex)<amount;
	}

}
