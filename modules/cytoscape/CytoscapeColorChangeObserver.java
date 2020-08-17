package fern.cytoscape;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import javax.swing.JOptionPane;

import cytoscape.CyNetwork;
import cytoscape.Cytoscape;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeAppearanceCalculator;
import fern.cytoscape.ui.ExtendedPane;
import fern.simulation.Simulator;
import fern.simulation.Simulator.FireType;
import fern.simulation.observer.AmountIntervalObserver;
import fern.simulation.observer.Observer;
import fern.tools.gnuplot.GnuPlot;
import giny.model.Node;
import giny.view.NodeView;

public class CytoscapeColorChangeObserver extends Observer {

  final static int numSamplePoints = 100;

  boolean visualize;
  boolean showTrendSteps;
  CytoscapeNetworkWrapper net;
  LinkedList<NodeView> activated = null;
  //	ColorChangingNodeAppeareanceCalculator changingNodeAppeareanceCalculator;
//	NodeAppearanceCalculator defaultNodeAppereanceCalculator;
  private FernVisualStyle style = null;
  AmountIntervalObserver[] trendObserver = null;
  GnuPlot[] gp = null;
  double[] oldTheta;

  public CytoscapeColorChangeObserver(boolean visualize, boolean showTrendSteps, Simulator sim,
      CytoscapeNetworkWrapper net, FernVisualStyle style, String[] trendSpecies, double time) {
    super(sim);
    this.visualize = visualize;
    this.showTrendSteps = showTrendSteps;
    this.net = net;
    this.style = style;

    activated = new LinkedList<NodeView>();
//		this.defaultNodeAppereanceCalculator = defaultNodeAppereanceCalculator;

    createTrendObservers(trendSpecies, time);
  }

  private void createTrendObservers(String[] trendSpecies, double time) {
    LinkedList<AmountIntervalObserver> t = new LinkedList<AmountIntervalObserver>();

    double interval = time / numSamplePoints;
    int s = 0;
    for (int i = 0; i < trendSpecies.length; i++) {
      if (trendSpecies[i].equals(ExtendedPane.sep)) {
        if (s < i) {
          String[] slice = new String[i - s];
          System.arraycopy(trendSpecies, s, slice, 0, slice.length);
          t.add(new AmountIntervalObserver(getSimulator(), interval, slice));
        }
        s = i + 1;
      }
    }
    if (s < trendSpecies.length) {
      String[] slice = new String[trendSpecies.length - s];
      System.arraycopy(trendSpecies, s, slice, 0, slice.length);
      t.add(new AmountIntervalObserver(getSimulator(), interval, slice));
    }

    trendObserver = t.toArray(new AmountIntervalObserver[t.size()]);
		for (int i = 0; i < trendObserver.length; i++) {
			getSimulator().addObserver(trendObserver[i]);
		}

    gp = new GnuPlot[trendObserver.length];
    oldTheta = new double[trendObserver.length];
    for (int i = 0; i < gp.length; i++) {
      gp[i] = new GnuPlot();
      gp[i].setDefaultStyle("with linespoints");
      gp[i].addCommand("set xrange[0:" + time + "]");
    }
  }

  @Override
  public void finished() {
    try {
      for (int i = 0; i < trendObserver.length; i++) {
        gp[i].clearData();
        trendObserver[i].toGnuplot(gp[i]);
        gp[i].setVisible(true);
        gp[i].plot();
      }
    } catch (Exception e) {
			if (e.getMessage().toLowerCase().contains("pipe")) {
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
						"Gnuplot-Error:\ngnuplot is not accessible! You have to add it to your path-Variable!");
			} else {
				JOptionPane.showMessageDialog(Cytoscape.getDesktop(), "Gnuplot-Error:\n" + e.getMessage());
			}
    }
  }


  @Override
  public void started() {
		for (int i = 0; i < oldTheta.length; i++) {
			oldTheta[i] = Double.NEGATIVE_INFINITY;
		}

//		changingNodeAppeareanceCalculator = new ColorChangingNodeAppeareanceCalculator(defaultNodeAppereanceCalculator);
//		Cytoscape.getVisualMappingManager().getVisualStyle().setNodeAppearanceCalculator(changingNodeAppeareanceCalculator);
  }

  @Override
  public void step() {
    if (showTrendSteps) {
      try {
        for (int i = 0; i < trendObserver.length; i++) {
          if (trendObserver[i].getTheta() > oldTheta[i]) {
            oldTheta[i] = trendObserver[i].getTheta();
            gp[i].clearData();
            trendObserver[i].toGnuplotRecent(gp[i]);
            gp[i].setVisible(true);
            gp[i].plot();
          }
        }
      } catch (Exception e) {
      }
    }
    if (visualize) {

			while (!activated.isEmpty()) {
				style.setReactionUnFire(activated.poll().getNode());
			}
//				changingNodeAppeareanceCalculator.unsetColor(activated.poll().getNode());

      double maxAmount = 0;
      for (int i = 0; i < net.getNumSpecies(); i++) {
        double amount = net.getAmountManager().getAmount(i);
        maxAmount = Math.max(maxAmount, amount);
      }

      for (int i = 0; i < net.getNumSpecies(); i++) {
        double amount = net.getAmountManager().getAmount(i);
        Node node = net.getSpeciesView(i).getNode();
//				changingNodeAppeareanceCalculator.setColor(node, ColorSpectrum.getGrayScaleSpectrum((float) (amount/maxAmount)));
        style.setValue(node, amount, maxAmount);
      }
      net.getNetworkViewObject().redrawGraph(true, true);


    }
  }
//	private double lastTime = -1;

  @Override
  public void activateReaction(int mu, double tau, FireType fireType, int times) {
		if (!visualize) {
			return;
		}

//		changingNodeAppeareanceCalculator.setColor(net.getReactionView(mu).getNode(),Color.red);
    style.setReactionFire(net.getReactionView(mu).getNode());
    activated.add(net.getReactionView(mu));
    net.getNetworkViewObject().redrawGraph(true, true);

//		try {
//			if ((int)(lastTime/0.1) < (int)(getSimulator().getTime()/0.1)) { 
//				Component cmp = net.getNetworkViewObject().getComponent();
//				BufferedImage img = new BufferedImage(cmp.getWidth(),cmp.getHeight(),BufferedImage.TYPE_INT_ARGB);
//				Graphics2D g = (Graphics2D) img.getGraphics();
//				g.setColor(Color.BLACK);
//				g.drawString("t="+Math.round(getSimulator().getTime()*10)/10.0, 1, 1);
//				cmp.paintAll(g);
//				
//				ImageIO.write(img,"png",new File(String.format("../Uni/images/cyto/cyto%f.png",getSimulator().getTime())));
//			}
//			lastTime = getSimulator().getTime();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
  }

  protected static class ColorChangingNodeAppeareanceCalculator extends NodeAppearanceCalculator {

    Map<Node, Color> colorMapping;

    public ColorChangingNodeAppeareanceCalculator(NodeAppearanceCalculator parent) {
      super(parent);
      this.colorMapping = new HashMap<Node, Color>();
    }

    @Override
    public void calculateNodeAppearance(NodeAppearance app, Node node, CyNetwork net) {
      // TODO Auto-generated method stub
      super.calculateNodeAppearance(app, node, net);
			if (colorMapping.containsKey(node)) {
				app.setFillColor(colorMapping.get(node));
			}
    }

    public void setColor(Node n, Color c) {
      colorMapping.put(n, c);
    }

    public void unsetColor(Node n) {
      colorMapping.remove(n);
    }
  }

  @Override
  public void theta(double theta) {
    // TODO Auto-generated method stub

  }

}
