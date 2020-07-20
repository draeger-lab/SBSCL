package fern.cytoscape.ui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.data.CyAttributes;
import cytoscape.view.CyNetworkView;
import fern.cytoscape.CytoscapeColorChangeObserver;
import fern.cytoscape.CytoscapeNetworkWrapper;
import fern.cytoscape.FernVisualStyle;
import fern.cytoscape.NetworkChecker;
import fern.network.AbstractKineticConstantPropensityCalculator;
import fern.network.Network;
import fern.simulation.Simulator;
import fern.simulation.algorithm.AbstractBaseTauLeaping;
import fern.simulation.controller.SimulationController;

public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private CytoscapeNetworkWrapper network = null;
	
	private OverviewPane overview = null;
	private ExtendedPane extended = null;
	
	private JButton startStopButton = null;
	private JProgressBar progress = null;
	
	private SimulationAction startSimulation;
	private NetworkChecker recentNetworkChecker = null;
	
	private FernVisualStyle style = null;
	
	public MainFrame(final FernVisualStyle style) {
		super("FERN plugin");
		
		this.style = style;
		
		
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				if (progress.getValue()>0)
				if (JOptionPane.YES_OPTION == JOptionPane.showConfirmDialog(Cytoscape.getDesktop(), "Reset colors to normal?", "FERN", JOptionPane.YES_NO_OPTION)) {
					style.resetColors();
				}

				startSimulation.cancel();
			}

		});	
//		defaultNodeAppereanceCalculator = Cytoscape.getVisualMappingManager().getVisualStyle().getNodeAppearanceCalculator();
		startSimulation = new SimulationAction();
		
		initializeComponents();
		setSize(630,480);
		
		reloadNetwork(new NetworkChecker());
	}
	
	public void reloadNetworkByButton() {
		String[] attr = extended.getAttributeKeys();
		
		if (attr[0]!=null && attr[1]!=null && attr[2]!=null && attr[3]!=null && attr[4]!=null)
			reloadNetwork(new NetworkChecker(attr[0],attr[1],attr[2],attr[3],attr[4]));
		else
			reloadNetwork(recentNetworkChecker);
		
	}
	
	public void reloadNetwork(NetworkChecker networkChecker) {
		this.recentNetworkChecker = networkChecker;
		extended.setAttributes(networkChecker);
		
		try {
			networkChecker.check();
			
			CyNetwork network = Cytoscape.getCurrentNetwork();
			CyNetworkView view = Cytoscape.getCurrentNetworkView();
			CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
			
			this.network = new CytoscapeNetworkWrapper(networkChecker,network, view, nodeAttr);
			startStopButton.setEnabled(getNetwork()!=null && getNetwork().getNumSpecies()>0);
			
			overview.setErrorMessage(getNetwork(), null);
			extended.setSpecies(this.network);
			style.setNetworkChecker(networkChecker);
		} catch (Exception e) {
			overview.setErrorMessage(getNetwork(),e.getMessage());
		}
	}

	
	public CytoscapeNetworkWrapper getNetwork() {
		return network;
	}
	
	public NetworkChecker getNetworkChecker() {
		return recentNetworkChecker;
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
							if (sim instanceof AbstractBaseTauLeaping) {
								((AbstractBaseTauLeaping)sim).setEpsilon(extended.getEpsilon());
								((AbstractBaseTauLeaping)sim).setNCritical(extended.getNc());
								((AbstractBaseTauLeaping)sim).setUseSimpleFactor(extended.getThreshold());
								((AbstractBaseTauLeaping)sim).setNumSimpleCalls(extended.getNum());
							}
							sim.addObserver(new CytoscapeColorChangeObserver(overview.isVisualize(),overview.isRealTime(),sim,getNetwork(),style,extended.getTrendSpecies(),overview.getTime()));
							while (!cancelFlag && runs--!=0) {
								sim.start(new SimulationController() {
									double recentTime = 0;
									public boolean goOn(Simulator sim) {
										boolean goOn = sim.getTime()<overview.getTime() && !cancelFlag;
										progress.setValue((int) (sim.getTime()*100));
										if (overview.isRealTime() && goOn)
											try {
												Thread.sleep((long) ((sim.getTime()-recentTime)*1000));
											} catch (InterruptedException e) {}
											
										recentTime=sim.getTime();
										return goOn;
									}
								});
							}
						} catch (Exception e) {
							e.printStackTrace();
							JOptionPane.showMessageDialog( Cytoscape.getDesktop(), e.getMessage());
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
	
	public void loadNetwork(Network net) {
		CyNetwork cynet = Cytoscape.createNetwork(net.getName());
		for (int i=0; i<net.getNumReactions(); i++)
			cynet.addNode(Cytoscape.getCyNode(net.getReactionName(i), true));
		for (int i=0; i<net.getNumSpecies(); i++)
			cynet.addNode(Cytoscape.getCyNode(net.getSpeciesName(i), true));
		
		for (int i=0; i<net.getNumReactions(); i++) {
			int[] reactants = net.getReactants(i);
			int[] products = net.getProducts(i);
			for (int r=0; r<reactants.length; r++)
				cynet.addEdge(Cytoscape.getCyEdge(
						net.getSpeciesName(reactants[r]),
						net.getSpeciesName(reactants[r])+" to "+net.getReactionName(i), 
						net.getReactionName(i), 
						"reactant to reaction"));
			
			for (int p=0; p<products.length; p++)
				cynet.addEdge(Cytoscape.getCyEdge(
						net.getReactionName(i), 
						net.getReactionName(i)+" to "+net.getSpeciesName(products[p]), 
						net.getSpeciesName(products[p]),
						"reaction to product"));
		}
		
		CyAttributes nodeAttr = Cytoscape.getNodeAttributes();
		for (int i=0; i<net.getNumSpecies(); i++) {
			nodeAttr.setAttribute(net.getSpeciesName(i), "node type", "species");
			nodeAttr.setAttribute(net.getSpeciesName(i), "initial amount", (int)net.getInitialAmount(i));
		}
		
		if (!(net.getPropensityCalculator() instanceof AbstractKineticConstantPropensityCalculator))
			throw new RuntimeException("Up to now only KineticConstantPropensityCalculators are allowed!");
		
		AbstractKineticConstantPropensityCalculator prop = (AbstractKineticConstantPropensityCalculator) net.getPropensityCalculator();
		for (int i=0; i<net.getNumReactions(); i++) {
			nodeAttr.setAttribute(net.getReactionName(i), "node type", "reaction");
			nodeAttr.setAttribute(net.getReactionName(i), "reaction coefficient", prop.getConstant(i));
		}
		
		reloadNetwork(new NetworkChecker("node type","reaction","species","reaction coefficient","initial amount"));
		
	}

	

	
}
