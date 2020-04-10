/*
 * Created on 03.03.2008
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.cellDesigner.ui;


import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.PrintWriter;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import fern.cellDesigner.CellDesignerNetworkWrapper;
import fern.cellDesigner.FernCellDesignerPlugin;
import fern.network.Network;
import fern.simulation.Simulator;
import fern.simulation.algorithm.AbstractBaseTauLeaping;
import fern.simulation.controller.SimulationController;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.InstantOutputObserver;
import fern.tools.gnuplot.GnuPlot;

public class MainFrame extends JFrame {

	private static final int numSamplePoints = 100; 
	private static final long serialVersionUID = 1L;
	private CellDesignerNetworkWrapper network = null;
	
	private OverviewPane overview = null;
	private ExtendedPane extended = null;
	
	private JButton startStopButton = null;
	private JProgressBar progress = null;
	
	private SimulationAction startSimulation;
	
	private FernCellDesignerPlugin plugin = null;
	
	public MainFrame(FernCellDesignerPlugin plugin) {
		super("FERN plugin");
		
		this.plugin = plugin;
		
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		
		startSimulation = new SimulationAction();
		
		initializeComponents();
		pack();
		reloadNetwork();
	}
	
	
	public void reloadNetwork() {
		
		try {
			
			this.network = new CellDesignerNetworkWrapper(plugin.getSelectedModel());
			startStopButton.setEnabled(getNetwork()!=null && getNetwork().getNumSpecies()>0);
			
			overview.setErrorMessage(getNetwork(), null);
			extended.setSpecies(this.network);
		} catch (Exception e) {
			overview.setErrorMessage(getNetwork(),e.getMessage());
		}
	}

	
	public CellDesignerNetworkWrapper getNetwork() {
		return network;
	}
	
	
	private void initializeComponents() {
		Container pane = getContentPane();
		
		overview = new OverviewPane(this);
		extended = new ExtendedPane(this);
		
		pane.add(overview, BorderLayout.WEST);
		pane.add(extended, BorderLayout.EAST);
		
		startStopButton = new JButton(startSimulation);
		progress = new JProgressBar();
		JPanel south = new JPanel(new BorderLayout());
			south.add(startStopButton,BorderLayout.NORTH);
			south.add(progress,BorderLayout.SOUTH);
		pane.add(south,BorderLayout.SOUTH);
	}
	
	
	public static void main(String[] args) {
		new MainFrame(null).setVisible(true);
	}
	
	
	private class SimulationAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		
		boolean cancelFlag = false;
		
		public SimulationAction() {
			super("Start");
		}
		
		public void cancel() {
			cancelFlag = true;
		}
		
		public void actionPerformed(ActionEvent e) {
			if (startStopButton.getText().equals("Start")){
				startStopButton.setText("Stop");
				cancelFlag = false;
				
				progress.setMaximum((int) (overview.getTime()*100));
				progress.setValue(0);
				overview.setEnabled(false);
				extended.setEnabled(false);
				
				new Thread(new Runnable() {
	
					public void run() {
						try {
							int runs = overview.getNumRuns();;
							Simulator sim = overview.getSimulator();
							AmountIntervalObserver[] trendObserver = createTrendObservers(sim, extended.getTrendSpecies(), overview.getTime());
//							sim.addObserver(new InstantOutputObserver(sim,new PrintWriter("sim.log")));
							if (sim instanceof AbstractBaseTauLeaping) {
								((AbstractBaseTauLeaping)sim).setEpsilon(extended.getEpsilon());
								((AbstractBaseTauLeaping)sim).setNCritical(extended.getNc());
								((AbstractBaseTauLeaping)sim).setUseSimpleFactor(extended.getThreshold());
								((AbstractBaseTauLeaping)sim).setNumSimpleCalls(extended.getNum());
							}
							while (!cancelFlag && runs--!=0) {
								sim.start(new SimulationController() {
									double recentTime = 0;
									public boolean goOn(Simulator sim) {
										boolean goOn = sim.getTime()<overview.getTime() && !cancelFlag;
										progress.setValue((int) (sim.getTime()*100));
										recentTime=sim.getTime();
										return goOn;
									}
								});
							}
							GnuPlot[] gp = new GnuPlot[trendObserver.length];
							for (int i=0; i<gp.length; i++) {
								gp[i] = new GnuPlot();
								gp[i].setDefaultStyle("with linespoints");
								gp[i].addCommand("set xrange[0:"+overview.getTime()+"]");
								trendObserver[i].toGnuplot(gp[i]);
								gp[i].setVisible(true);
								gp[i].plot();
							}
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog( null, e.getMessage());
						} finally {
							overview.setEnabled(true);
							extended.setEnabled(true);
							startStopButton.setText("Start");
							cancelFlag=true;
						}
					}
					
				}).start();
			}
			else { // startStopButton=Stop
				startStopButton.setText("Start");
				cancelFlag=true;
			}
		}
	}
	
	private AmountIntervalObserver[] createTrendObservers(Simulator sim, String[] trendSpecies, double time) {
		LinkedList<AmountIntervalObserver> t = new LinkedList<AmountIntervalObserver>();
		
		double interval = time/numSamplePoints;
		int s = 0;
		for (int i=0; i<trendSpecies.length; i++) {
			if (trendSpecies[i].equals(ExtendedPane.sep)) {
				if (s<i) {
					String[] slice = new String[i-s];
					System.arraycopy(trendSpecies, s, slice, 0, slice.length);
					t.add(new AmountIntervalObserver(sim,interval,slice));
				}
				s=i+1;
			}
		}
		if (s<trendSpecies.length) {
			String[] slice = new String[trendSpecies.length-s];
			System.arraycopy(trendSpecies, s, slice, 0, slice.length);
			t.add(new AmountIntervalObserver(sim,interval,slice));
		}
		
		AmountIntervalObserver[] trendObserver = t.toArray(new AmountIntervalObserver[t.size()]);
		for (int i=0; i<trendObserver.length; i++)
			sim.addObserver(trendObserver[i]);
		return trendObserver;
	}
	
	public void loadNetwork(Network net) {
//		plugin.getAllModels().append(sbase)
//		CyNetwork cynet = Cytoscape.createNetwork(net.getName());
//		for (int i=0; i<net.getNumReactions(); i++)
//			cynet.addNode(Cytoscape.getCyNode(net.getReactionName(i), true));
//		for (int i=0; i<net.getNumSpecies(); i++)
//			cynet.addNode(Cytoscape.getCyNode(net.getSpeciesName(i), true));
//		
//		for (int i=0; i<net.getNumReactions(); i++) {
//			int[] reactants = net.getReactants(i);
//			int[] products = net.getProducts(i);
//			for (int r=0; r<reactants.length; r++)
//				cynet.addEdge(Cytoscape.getCyEdge(
//						net.getSpeciesName(reactants[r]),
//						net.getSpeciesName(reactants[r])+" to "+net.getReactionName(i), 
//						net.getReactionName(i), 
//						"reactant to reaction"));
//			
//			for (int p=0; p<products.length; p++)
//				cynet.addEdge(Cytoscape.getCyEdge(
//						net.getReactionName(i), 
//						net.getReactionName(i)+" to "+net.getSpeciesName(products[p]), 
//						net.getSpeciesName(products[p]),
//						"reaction to product"));
//		}
//		
//		CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
//		for (int i=0; i<net.getNumSpecies(); i++) {
//			nodeAttr.setAttribute(net.getSpeciesName(i), "node type", "species");
//			nodeAttr.setAttribute(net.getSpeciesName(i), "initial amount", (int)net.getInitialAmount(i));
//		}
//		
//		if (!(net.getPropensityCalculator() instanceof AbstractKineticConstantPropensityCalculator))
//			throw new RuntimeException("Up to now only KineticConstantPropensityCalculators are allowed!");
//		
//		AbstractKineticConstantPropensityCalculator prop = (AbstractKineticConstantPropensityCalculator) net.getPropensityCalculator();
//		for (int i=0; i<net.getNumReactions(); i++) {
//			nodeAttr.setAttribute(net.getReactionName(i), "node type", "reaction");
//			nodeAttr.setAttribute(net.getReactionName(i), "reaction coefficient", prop.getConstant(i));
//		}
//		
//		reloadNetwork(new NetworkChecker("node type","reaction","species","reaction coefficient","initial amount"));
		
	}

	

	
}
