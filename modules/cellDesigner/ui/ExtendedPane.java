/*
 * Created on 03.03.2008
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.cellDesigner.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.AbstractAction;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;

import fern.network.Network;




public class ExtendedPane extends JPanel {

	private static final long serialVersionUID = 1L;
	public static final String sep = "________";
	
	
	MainFrame main ;
	
	JComboBox nodeTypePicker = null;
	JComboBox reactionPicker = null;
	JComboBox speciesPicker = null;
	JComboBox initialAmountPicker = null;
	JComboBox reactioncoefficientPicker = null;
	
	JTextField eps = null;
	JTextField nc = null;
	JTextField thresh = null;
	JTextField num = null;
	
	JList notList = null;
	JList trendList = null;
	
	public ExtendedPane(MainFrame mainFrame) {
		this.main = mainFrame;
		
		initializeComponents();
	}

	

	private void initializeComponents() {
		removeAll();
		
		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3,3,3,3); 
		c.anchor = GridBagConstraints.NORTHWEST;
		

		
		c.gridx=0;
		c.gridy++;
		c.gridwidth=2;
		add(new JLabel("Tau leaping parameter: "),c);
		c.gridx=2;
		add(createTauLeapingHelpButton(),c);
		
		c.gridx=0;
		c.gridy++;
		c.gridwidth=1;
		add(new JLabel("eps:"),c);
		c.gridx=1;
		if (eps==null) eps = new JTextField("0.03",6);
		add(eps,c);
		c.gridx=2;
		add(new JLabel("n_c:"),c);
		c.gridx=3;
		if (nc==null) nc = new JTextField("10",6);
		add(nc,c);
		
		c.gridy++;
		c.gridx=0;
		add(new JLabel("threshold:"),c);
		c.gridx=1;
		if (thresh==null) thresh = new JTextField("10",6);
		add(thresh,c);
		c.gridx=2;
		add(new JLabel("#exact:"),c);
		c.gridx=3;
		if (num==null) num = new JTextField("100",6);
		add(num,c);
		
		c.gridy++;
		c.gridwidth=4;
		add(new JSeparator(),c);
		
		c.gridx=0;
		c.gridy++;
		add(new JLabel("Trends:"),c);
		
		c.anchor = GridBagConstraints.CENTER;
		c.gridy++;
		add(createTrendLists(),c);
	}

	private JPanel createTrendLists() {
		if (notList==null) {
			notList = new JList(new DefaultListModel());
//			((DefaultListModel)notList.getModel()).addElement("A");
//			((DefaultListModel)notList.getModel()).addElement("B");
//			((DefaultListModel)notList.getModel()).addElement("C");
//			((DefaultListModel)notList.getModel()).addElement("D");
//			((DefaultListModel)notList.getModel()).addElement("E");
//			((DefaultListModel)notList.getModel()).addElement("F");
		}
		if (trendList==null) {
			trendList = new JList(new DefaultListModel());
		}
		
		
		JPanel pane = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(3,3,3,3); 
		
		c.gridheight=3;
		ScrollPane scroll1 = new ScrollPane();
		scroll1.setPreferredSize(new Dimension(120,95));
		scroll1.add(notList);
		pane.add(scroll1,c);
		
		c.gridheight=1;
		c.gridx=1;
		c.gridy=0;
		JButton r = new JButton(">"); 
		r.addActionListener(new TrendActionListener());
		pane.add(r,c);
		
		c.gridy=1;
		JButton m = new JButton("-"); 
		m.addActionListener(new TrendActionListener());
		pane.add(m,c);
		
		c.gridy=2;
		JButton l = new JButton("<"); 
		l.addActionListener(new TrendActionListener());
		pane.add(l,c);
		
		c.gridheight=3;
		c.gridx=2;
		c.gridy=0;
		ScrollPane scroll2 = new ScrollPane();
		scroll2.setPreferredSize(new Dimension(120,95));
		scroll2.add(trendList);
		pane.add(scroll2,c);
		
		
		return pane;
	}

	private class TrendActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String a = ((JButton)e.getSource()).getText();
			if (a.equals(">") && notList.getSelectedValues().length>0) {
				int pos = trendList.getSelectedIndex();
				if (pos==-1) pos = trendList.getModel().getSize();
				for (Object o : notList.getSelectedValues()) 
					((DefaultListModel)trendList.getModel()).add(pos++,o);
				for (Object o : notList.getSelectedValues()) 
					((DefaultListModel)notList.getModel()).removeElement(o);
			}
			else if (a.equals("<") && trendList.getSelectedValues().length>0) {
				int pos = notList.getSelectedIndex();
				if (pos==-1) pos = notList.getModel().getSize();
				for (Object o : trendList.getSelectedValues()) 
					if (!o.equals(sep))
						((DefaultListModel)notList.getModel()).add(pos++,o);
				for (Object o : trendList.getSelectedValues()) 
					((DefaultListModel)trendList.getModel()).removeElement(o);
			} else if (a.equals("-")) {
				int pos = trendList.getSelectedIndex();
				if (pos==-1) pos = trendList.getModel().getSize();
				((DefaultListModel)trendList.getModel()).add(pos,sep);
			}
		}
	}

	private JButton createTauLeapingHelpButton() {
		return new JButton(new AbstractAction("?") {
			private static final long serialVersionUID = 1L;
			public void actionPerformed(ActionEvent arg0) {
				StringBuilder sb = new StringBuilder();
				sb.append("Here you can specify parameters for the tau leaping procedures:\n\n");
				sb.append("eps\nerror bounding\n\n");
				sb.append("n_c\nthreshold for critical reactions\n\n");
				sb.append("threshold\nfor temporarily abandoning tau leaping\n\n");
				sb.append("#exact\nhow many exact SSA steps when abandoning tau leaping\n\n");
				sb.append("For more explanation refer to Cao et al., Efficient step\n");
				sb.append("size selection for the tau-leaping simulation method,\n");
				sb.append("Journal of chemical physics 124");
				JOptionPane.showMessageDialog(main, sb.toString());
			}
		});
	}



	

	public void setSpecies(Network net) {
		((DefaultListModel)notList.getModel()).removeAllElements();
		((DefaultListModel)trendList.getModel()).removeAllElements();
		
		for (int i=0; i<net.getNumSpecies(); i++) 
			((DefaultListModel)notList.getModel()).addElement(net.getSpeciesName(i));
	}

	public String[] getTrendSpecies() {
		String[] re = new String[trendList.getModel().getSize()];
		for (int i=0; i<re.length; i++)
			re[i] = (String) trendList.getModel().getElementAt(i);
		return re;
	}

	
	
	public void setEnabledTauLeaping(boolean enabled) {
		eps.setEnabled(enabled);
		thresh.setEnabled(enabled);
		nc.setEnabled(enabled);
		num.setEnabled(enabled);
	}

	@Override
	public void setEnabled(boolean enabled) {
		setEnabledDeep(enabled, this);	
	}

	private void setEnabledDeep(boolean enabled, Container component) {
		for (Component c : component.getComponents()) {
			c.setEnabled(enabled);
			if (c instanceof Container) setEnabledDeep(enabled, (Container) c);
		}
	}

	public double getEpsilon() {
		return Double.parseDouble(eps.getText());
	}

	public int getNc() {
		return Integer.parseInt(nc.getText());
	}

	public double getThreshold() {
		return Double.parseDouble(thresh.getText());
	}

	public int getNum() {
		return Integer.parseInt(num.getText());
	}
	
}

