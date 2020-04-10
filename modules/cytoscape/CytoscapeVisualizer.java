package fern.cytoscape;

import java.awt.event.ActionEvent;

import javax.swing.JOptionPane;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.plugin.CytoscapePlugin;
import cytoscape.util.CytoscapeAction;
import cytoscape.view.CyNetworkView;
import fern.cytoscape.ui.MainFrame;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GibsonBruckSimulator;
import fern.simulation.controller.DefaultController;

public class CytoscapeVisualizer extends CytoscapePlugin {

	FernVisualStyle style = new FernVisualStyle();
	
	public CytoscapeVisualizer() {
		//create a new action to respond to menu activation
		CytoscapeAction action = new ShowMainFrameAction();
		//set the preferred menu
		action.setPreferredMenu("Plugins");
		//and add it to the menus
		Cytoscape.getDesktop().getCyMenus().addAction(action);
		
		Cytoscape.getVisualMappingManager().getCalculatorCatalog().addVisualStyle(style);
		Cytoscape.getDesktop().getVizMapUI().getStyleSelector().resetStyles();
		
	}

	public String describe() {
		return "";
	}
	
	public class ShowMainFrameAction extends CytoscapeAction {
		private static final long serialVersionUID = 1L;

		public ShowMainFrameAction() { super("Stochastic Simulation"); }
		
		@Override
		public void actionPerformed(ActionEvent arg0) {
			
			if (!Cytoscape.getVisualMappingManager().getCalculatorCatalog().getVisualStyleNames().contains(style.getName())) {
				Cytoscape.getVisualMappingManager().getCalculatorCatalog().addVisualStyle(style);
				Cytoscape.getDesktop().getVizMapUI().getStyleSelector().resetStyles();
			}
			Cytoscape.getVisualMappingManager().setVisualStyle(style.getName());
			Cytoscape.getCurrentNetworkView().redrawGraph(true, true);
			
			MainFrame frame = new MainFrame(style);
			frame.setVisible(true);
			
		}
		
	}

	public class SimulationAction extends CytoscapeAction {

		private static final long serialVersionUID = 1L;

		public SimulationAction() {super("Stochastic Simulation");}

		public void actionPerformed(ActionEvent ae) {
			
			Thread t = new Thread(new Runnable() {

				public void run() {
					try {
						CyNetwork network = Cytoscape.getCurrentNetwork();
						CyNetworkView view = Cytoscape.getCurrentNetworkView();
						CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
						
						NetworkChecker checker = new NetworkChecker();
						
						CytoscapeNetworkWrapper net = new CytoscapeNetworkWrapper(checker,network, view, nodeAttr);
						
						Simulator sim = new GibsonBruckSimulator(net);
//						sim.addObserver(new CytoscapeColorChangeObserver(sim,net,null));
						sim.start(new DefaultController(10));
						System.out.println("done");
					} catch (Exception e) {
						JOptionPane.showMessageDialog( Cytoscape.getDesktop(), e.getMessage());
					}
				}
				
			});
			
			t.start();
			
		}
	}

	
}
