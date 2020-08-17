package fern.cytoscape.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import fern.cytoscape.ColorCalculator;
import fern.cytoscape.FernVisualStyle;
import fern.cytoscape.ColorCalculator.Scale;

public class ColorPicker extends JDialog {

  private static final long serialVersionUID = 1L;
  private ColorField reactionColor = null;
  private ColorField amountBottomColor = null;
  private ColorField amountTopColor = null;
  private PreviewField preview = null;
  private JComboBox scaleType = null;
  private JTextField scaleMax = null;
  private ColorCalculator colcalc;
  private PreviewChangeAction action = null;

  public ColorPicker(Frame frame) {
    super(frame, "Colors", ModalityType.APPLICATION_MODAL);
    try {
      this.colcalc = (ColorCalculator) FernVisualStyle.colorCalculator.clone();
    } catch (CloneNotSupportedException e) {
    }
    action = new PreviewChangeAction();

    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    initializeComponents();
    pack();
  }

  private void initializeComponents() {

    Container pane = getContentPane();
    pane.removeAll();

    preview = new PreviewField(colcalc);
    reactionColor = new ColorField(colcalc.getReactionColor());
    amountBottomColor = new ColorField(colcalc.getAmountBottomColor());
    amountTopColor = new ColorField(colcalc.getAmountTopColor());
    scaleType = new JComboBox(new ColorCalculator.Scale[]{Scale.Linear, Scale.Logarithmic});
    scaleType.setSelectedItem(colcalc.getScale());
    scaleMax = new JTextField(colcalc.getScaleMax() < 0 ? "" : colcalc.getScaleMax() + "", 8);

    amountBottomColor.addMouseListener(action);
    amountTopColor.addMouseListener(action);
    scaleType.addActionListener(action);
    scaleMax.addMouseListener(action);
    reactionColor.addMouseListener(action);

    pane.setLayout(new GridBagLayout());
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(3, 3, 3, 3);
    c.anchor = GridBagConstraints.WEST;

    c.gridx = 0;
    c.gridy = 0;
    pane.add(new JLabel("Reaction:"), c);

    c.gridy++;
    pane.add(new JLabel("Amount scale;"), c);
    c.gridy++;
    pane.add(new JLabel("Maximal value:"), c);
    c.gridy++;
    c.gridwidth = 2;
    JLabel exp = new JLabel("(leave emtpy if you want to scale up to the actual maximum)");
    exp.setFont(new Font("", 0, 9));
    pane.add(exp, c);
    c.gridwidth = 1;
    c.gridy++;
    pane.add(amountBottomColor, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridx = 1;
    c.gridy = 0;
    pane.add(reactionColor, c);
    c.gridy++;
    pane.add(scaleType, c);
    c.gridy++;
    pane.add(scaleMax, c);
    c.anchor = GridBagConstraints.EAST;
    c.gridy++;
    c.gridy++;
    pane.add(amountTopColor, c);

    c.anchor = GridBagConstraints.CENTER;
    c.gridwidth = 2;
    c.gridx = 0;
    c.gridy++;
    pane.add(preview, c);

    c.gridwidth = 1;
    c.gridy++;
    c.anchor = GridBagConstraints.EAST;
    pane.add(new JButton(new AbstractAction("Ok") {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        double s = -1;
        try {
          s = Double.parseDouble(scaleMax.getText());
        } catch (Exception ed) {
        }
        colcalc.setScaleMax(s);

        FernVisualStyle.colorCalculator = colcalc;
        dispose();
      }
    }), c);

    c.gridx++;
    c.anchor = GridBagConstraints.WEST;
    pane.add(new JButton(new AbstractAction("Cancel") {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        dispose();
      }
    }), c);

  }

  private class PreviewChangeAction extends MouseAdapter implements ActionListener {

    @Override
    public void mouseClicked(MouseEvent e) {
      update();
    }

    public void actionPerformed(ActionEvent arg0) {
      update();
    }

    private void update() {
      colcalc.setAmountBottomColor(amountBottomColor.getBackground());
      colcalc.setAmountTopColor(amountTopColor.getBackground());
      colcalc.setReactionColor(reactionColor.getBackground());
      colcalc.setScale((Scale) scaleType.getSelectedItem());

      preview.repaint();
    }
  }

  private static class PreviewField extends JTextField {

    private static final long serialVersionUID = 1L;

    ColorCalculator colcalc;

    public PreviewField(ColorCalculator colcalc) {
      super(25);
      this.colcalc = colcalc;

    }

    @Override
    public void paint(Graphics g) {
      double max = colcalc.getScaleMax();
      colcalc.setScaleMax(getWidth());
      Graphics2D g2 = (Graphics2D) g;
      for (int i = 0; i < this.getWidth(); i++) {
        g2.setColor(colcalc.getColor(i, getWidth()));
        g2.drawLine(i, 0, i, getHeight());
      }
      colcalc.setScaleMax(max);
    }
  }

  private static class ColorField extends JTextField implements MouseListener {

    private static final long serialVersionUID = 1L;

    public ColorField(Color c) {
      super(5);
      setBackground(c);
      setEditable(false);
      addMouseListener(this);
    }

    public void mouseClicked(MouseEvent e) {
      Color c = JColorChooser.showDialog(this, "Pick color", getBackground());
			if (c != null) {
				setBackground(c);
			}
    }

    public void mouseEntered(MouseEvent e) {
      // TODO Auto-generated method stub

    }

    public void mouseExited(MouseEvent e) {
      // TODO Auto-generated method stub

    }

    public void mousePressed(MouseEvent e) {
      // TODO Auto-generated method stub

    }

    public void mouseReleased(MouseEvent e) {
      // TODO Auto-generated method stub

    }


  }

}
