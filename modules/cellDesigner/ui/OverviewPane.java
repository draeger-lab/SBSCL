/*
 * Created on 03.03.2008
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.cellDesigner.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import fern.network.Network;
import fern.network.NetworkLoader;
import fern.network.fernml.FernMLNetwork;
import fern.simulation.Simulator;
import fern.simulation.algorithm.GibsonBruckSimulator;
import fern.simulation.algorithm.GillespieEnhanced;
import fern.simulation.algorithm.GillespieSimple;
import fern.simulation.algorithm.HybridMaximalTimeStep;
import fern.simulation.algorithm.TauLeapingAbsoluteBoundSimulator;
import fern.simulation.algorithm.TauLeapingRelativeBoundSimulator;
import fern.simulation.algorithm.TauLeapingSpeciesPopulationBoundSimulator;
import fern.tools.NetworkTools;

public class OverviewPane extends JPanel {

  private static final long serialVersionUID = 1L;

  private MainFrame main;
  private JLabel networkStatus = null;
  private JTextArea message = null;
  private JComboBox simulator = null;
  private JTextField time = null;
  private JTextField runs = null;

  public OverviewPane(MainFrame mainFrame) {
    this.main = mainFrame;

    initializeComponents();
  }


  private void initializeComponents() {
    removeAll();

    setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(3, 3, 3, 3);

    c.gridx = 0;
    c.gridy = 0;
    add(new JLabel("Network:"), c);

    c.gridx = 1;
		if (networkStatus == null) {
			networkStatus = new JLabel();
		}
    add(networkStatus, c);

    c.gridwidth = 2;
    c.gridx = 0;
    c.gridy++;
    JPanel buttons = new JPanel();
    buttons.add(getReloadButton());
//		buttons.add(getLoadButton());
    buttons.add(getSaveButton());
    add(buttons, c);

    c.gridx = 0;
    c.gridy++;
    ScrollPane scroll = new ScrollPane();
    scroll.setSize(300, 150);
    add(scroll, c);

		if (message == null) {
			message = new JTextArea();
		}
    message.setEditable(false);
    scroll.add(message);

    c.gridx = 0;
    c.gridy++;
    add(new JSeparator(JSeparator.HORIZONTAL), c);

    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.WEST;
    add(new JLabel("Simulator:"), c);

    c.gridx = 0;
    c.gridy++;
    c.anchor = GridBagConstraints.CENTER;
		if (simulator == null) {
			simulator = new JComboBox(new String[]{
					"GillespieSimple",
					"GillespieEnhanced",
					"GibsonBruckSimulator",
					"TauLeapingAbsoluteBoundSimulator",
					"TauLeapingRelativeBoundSimulator",
					"TauLeapingSpeciesPopulationBoundSimulator",
					"Hybrid Maximal Time Step Method"
			});
		}

    add(simulator, c);

    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.WEST;
    add(new JLabel("Time:"), c);

    c.gridx = 1;
		if (time == null) {
			time = new JTextField("10", 10);
		}
    add(time, c);

    c.gridx = 0;
    c.gridy++;
    c.gridwidth = 1;
    c.anchor = GridBagConstraints.WEST;
    add(new JLabel("Runs:"), c);

    c.gridx = 1;
		if (runs == null) {
			runs = new JTextField("1", 10);
		}
    add(runs, c);

    repaint();

  }


  private Component getSaveButton() {
    return new JButton(new AbstractAction("Save") {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        JFileChooser dia = new JFileChooser();
        dia.addChoosableFileFilter(new FileFilter() {
          @Override
          public boolean accept(File f) {
            if (f.isDirectory()) {
              return true;
            }
            return f.getName().endsWith(".xml");
          }

          @Override
          public String getDescription() {
            return "FernML-Files (*.xml)";
          }
        });
        if (dia.showSaveDialog(main) != JFileChooser.CANCEL_OPTION) {
          try {
            new FernMLNetwork(main.getNetwork()).saveToFile(dia.getSelectedFile());
          } catch (IOException e1) {
          }
        }
      }

    });
  }

//	private Component getLoadButton() {
//		return new JButton(new AbstractAction("Load") {
//			private static final long serialVersionUID = 1L;
//			public void actionPerformed(ActionEvent e) {
//				JFileChooser dia = new JFileChooser();
//				NetworkLoader.addTypesToFileChooser(dia);
//				
//				if (dia.showSaveDialog(main)!=JFileChooser.CANCEL_OPTION) {
//					try {
//						Network net = NetworkLoader.readNetwork(dia.getSelectedFile());
//						main.loadNetwork(net);
//					} catch (Exception e1) {}
//				}
//			}
//			
//		});
//	}


  private Component getReloadButton() {
    return new JButton(new AbstractAction("Reload") {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        main.reloadNetwork();
      }

    });
  }

  public void setErrorMessage(Network net, String message) {
    if (message == null) {
      StringWriter sw = new StringWriter();
      try {
        NetworkTools.dumpNetwork(net, sw);
      } catch (IOException e) {
      }
      this.message.setText(sw.toString());
    } else {
      this.message.setText("Error:\n\n" + message);
    }

    if (net != null && net.getNumSpecies() > 0) {
      networkStatus.setText("ok");
      networkStatus.setForeground(new Color(0, 127, 0));
    } else if (net != null && net.getNumSpecies() == 0) {
      networkStatus.setText("empty");
      networkStatus.setForeground(Color.red);
    } else {
      networkStatus.setText("not loaded");
      networkStatus.setForeground(Color.red);
    }

  }

  public double getTime() {
    double re = 0;
    try {
      re = Double.parseDouble(time.getText());
    } catch (Exception e) {
    }
    return re;
  }

  public int getNumRuns() {
    return Integer.parseInt(runs.getText());
  }


  public Simulator getSimulator() throws Exception {
    switch (simulator.getSelectedIndex()) {
      case 0:
        return new GillespieSimple(main.getNetwork());
      case 1:
        return new GillespieEnhanced(main.getNetwork());
      case 2:
        return new GibsonBruckSimulator(main.getNetwork());
      case 3:
        return new TauLeapingAbsoluteBoundSimulator(main.getNetwork());
      case 4:
        return new TauLeapingRelativeBoundSimulator(main.getNetwork());
      case 5:
        return new TauLeapingSpeciesPopulationBoundSimulator(main.getNetwork());
      case 6:
        return new HybridMaximalTimeStep(main.getNetwork());
    }
    return (Simulator) ClassLoader.getSystemClassLoader()
        .loadClass("fern.simulation.algorithm." + (String) simulator.getSelectedItem())
        .getConstructor(ClassLoader.getSystemClassLoader().loadClass("fern.network.Network"))
        .newInstance(main.getNetwork());
  }

  @Override
  public void setEnabled(boolean enabled) {
    setEnabledDeep(enabled, this);
  }

  private void setEnabledDeep(boolean enabled, Container component) {
    for (Component c : component.getComponents()) {
      c.setEnabled(enabled);
			if (c instanceof Container) {
				setEnabledDeep(enabled, (Container) c);
			}
    }
  }


}
