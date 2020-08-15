package fern.tools.gnuplot;

import java.awt.Graphics;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

/**
 * The class <code>GnuPlot</code> provides methods for handling gnuplot data.&nbsp;Additionally this
 * class can be used as an interface to gnuplot in order to actually plot data.<br> After data has
 * been added by one of the <code>addData</code> methods, you are able either to retrieve the
 * gnuplot data file (as String - <code>getData</code> - or saved to a file -
 * <code>saveData</code>) or to <code>plot</code> the data by invoking gnuplot.
 * Once the plot is created, it can be retrieved as an Image object by <code>getImage</code>, saved
 * to a png file by <code>saveImage</code> or presented interactively to the screen (by setting the
 * visible property to true).
 * <p>
 * Multiple data can be added as well as another gnuplot object can be merged.
 * <p>
 * If the plot is shown in the {@link JFrame}, it can be saved by rightclicking on the frame or
 * pressing F3 / CTRL-S.
 *
 * @author Florian Erhard
 * @see Axes
 */
public class GnuPlot extends JFrame {

  private static final long serialVersionUID = 1L;

  protected BufferedImage img = null;
  protected List<String> commands = null;
  protected List<Axes> axes = null;
  protected List<Axes> clearedAxes = null;

  protected String defaultStyle = null;


  /**
   * The constructor sets the DefaultCloseOperation of this JFrame to DISPOSE_ON_CLOSE. Additionally
   * the listeners for saving are created.
   */
  public GnuPlot() {
    super("GnuPlot");
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    commands = new ArrayList<String>();
    axes = new LinkedList<Axes>();
    clearedAxes = new LinkedList<Axes>();
    defaultStyle = "";

    addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        showSaveAsDialog();
      }

    });

    addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_F3) {
          showSaveAsDialog();
        } else if (e.getKeyCode() == KeyEvent.VK_S
            && (e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
          showSaveAsDialog();
        }
        if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
          dispose();
        }
      }

    });


  }


  private void showSaveAsDialog() {
    JFileChooser dia = new JFileChooser();
    dia.setFileFilter(new FileFilter() {
      @Override
      public boolean accept(File f) {
        return f.isDirectory() || f.getName().endsWith(".gnuplot") || f.getName().endsWith(".ps")
            || f.getName().endsWith(".eps") || f.getName().endsWith(".png");
      }

      @Override
      public String getDescription() {
        return "Supported Formats (ps,eps,png,gnuplot)";
      }

    });
    List<File> files = null;
    try {
      if (dia.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        String end = dia.getSelectedFile().getName()
            .substring(dia.getSelectedFile().getName().lastIndexOf(".") + 1);
        if (end.equals("gnuplot")) {
          saveData(dia.getSelectedFile());
        } else {
          String cmd;
          if (end.equals("png")) {
            cmd = "set term png";
          } else {
            cmd = "set term postscript color";
          }

          String tmp = this.hashCode() + ".gnuplot";
          //			String tmpPic = this.hashCode()+"."+end;
          files = saveData(new File(tmp));

          String plotCommand = getPlotCommand(files);
          Process p;
          try {
            p = new ProcessBuilder("gnuplot").start();
          } catch (Exception e) {
            throw new IOException("Gnuplot could not be called!");
          }

          BufferedWriter c = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
          for (String line : plotCommand.split("\n")) {
            if (line.startsWith("set term")) {
              line = cmd + "\n" + "set output \"" + dia.getSelectedFile().toString()
                  .replace("\\", "\\\\") + "\"";
            }
            c.append(line + "\n");
            c.flush();
            checkGnuplotError(p);
          }

          c.close();

          p.destroy();

//			if (new File(tmpPic).exists()) {
//				if (dia.getSelectedFile().exists())
//					dia.getSelectedFile().delete();
//				if (!new File(tmpPic).renameTo(dia.getSelectedFile())) {
//					new File(tmpPic).delete();
//					throw new Exception("Could not create file "+dia.getSelectedFile().toString());
//				}
//			}
        }

      }
    } catch (Exception e) {
      JOptionPane.showMessageDialog(this, "Error:\n" + e.getMessage());
    } finally {
      if (files != null) {
        for (File f : files) {
          f.delete();
        }
      }
    }
  }


  /**
   * Adds commands to the list of commands which are given to gnuplot by invoking <code>plot</code>
   *
   * @param command the gnuplot command (e.g. set xrange [0:100])
   */
  public void addCommand(String... command) {
    for (String c : command) {
      commands.add(c);
    }
  }

  /**
   * Returns the list of commands, e.g. if you want to remove some command from it.
   *
   * @return the list of gnuplot commands
   */
  public Collection<String> getCommands() {
    return commands;
  }

  /**
   * Clears the loaded data.
   */
  public void clearData() {
    clearedAxes.addAll(axes);
    axes.clear();
  }

  /**
   * Adds data in order to plot it. The data has to be given as a collection of Numbers, Doubles,
   * Integer,... or as double[], int[], ... The type is inferred by using the java reflection api.
   *
   * @param col        the collections array with the data
   * @param dataLabels label for each value column
   * @param styles     additional styles for each value column
   * @return axes object containing the data
   */
  @SuppressWarnings("unchecked")
  public Axes addData(Collection col, String[] dataLabels, String[] styles) {
    Axes axes = new CollectionAxes(col, dataLabels, styles);
    return addData(axes);
  }


  /**
   * Adds data in order to plot it. The data has to be given as an axes object.
   *
   * @param axes an axes object with data to plot
   * @return axes object containing the data
   */
  public Axes addData(Axes axes) {
    if (this.axes.size() > 0 && this.axes.get(0).getDimensionType() != axes.getDimensionType()) {
      throw new IllegalArgumentException("No mixture of 2 and 3 dimensional axes allowed!");
    }
    this.axes.add(axes);
    return axes;
  }

  /**
   * Adds data in order to plot it. The data has to be given as a matrix of double, int, ... The
   * first index of the matrix gives the row, the second the column. The type is inferred by using
   * the java reflection api. In lineTypes and dataLabels you can give line style and labels for
   * each column.
   *
   * @param arr        the data matrix to plot
   * @param styles     additional styles for each value column
   * @param dataLabels label for each value column
   * @return axes object containing the data
   */
  public Axes addData(Object arr, String[] dataLabels, String[] styles) {
    Axes axes = new ArrayMatrixAxes(arr, dataLabels, styles);
    return addData(axes);
  }

  /**
   * Adds data in order to plot it. The data has to be given as a matrix of double, int, ... If
   * transposed=true, the first index of the matrix gives the row, the second the column. The type
   * is inferred by using the java reflection api. In lineTypes and dataLabels you can give line
   * style and labels for each column.
   *
   * @param arr        the data matrix to plot
   * @param transposed if the given matrix is transposed
   * @param styles     additional styles for each value column
   * @param dataLabels label for each value column
   * @return axes object containing the data
   */
  public Axes addData(Object[] arr, boolean transposed, String[] dataLabels, String[] styles) {
    Axes axes = transposed ? new TransposedArrayMatrixAxes(arr, dataLabels, styles)
        : new ArrayMatrixAxes(arr, dataLabels, styles);
    return addData(axes);
  }

  /**
   * Merges another gnuplot object with this one by add its axes and copy its default style to the
   * axes (if they have no style).
   *
   * @param gnuplot the gnuplot object to merge with
   */
  public void merge(GnuPlot gnuplot) {
    for (Axes a : gnuplot.axes) {
      addData(a);
    }
    for (Axes a : gnuplot.axes) {
      a.applyDefaultStyle(gnuplot.getDefaultStyle());
    }
  }

  /**
   * Returns the Axes object containing the data, labels and styles.
   *
   * @return Axes object
   */
  public List<Axes> getAxes() {
    return axes;
  }


  /**
   * Returns the plot command for a given filename
   *
   * @param files a list (parallel to axes) of files containing the gnuplot data
   * @return plot command to use in gnuplot
   */
  public String getPlotCommand(List<File> files) {
    String plotCommand, usingPrefix;
    int labelColumns;

    if (axes.size() > 0 && axes.get(0).getDimensionType() == Axes.ThreeD) {
      plotCommand = "splot";
      usingPrefix = "1:2";
      labelColumns = 2;
    } else {
      plotCommand = "plot";
      usingPrefix = "1";
      labelColumns = 1;
    }

    StringBuilder sb = new StringBuilder();
    for (String cmd : commands) {
      sb.append(cmd + "\n");
    }

    sb.append("set term png\n");
    sb.append(plotCommand);

    Iterator<File> fIt = files.iterator();
    Iterator<Axes> aIt = axes.iterator();
    while (fIt.hasNext() || aIt.hasNext()) {
      if (!fIt.hasNext() || !aIt.hasNext()) {
        throw new RuntimeException("Fileslist and Axeslist don't have the same size!");
      }
      File f = fIt.next();
      Axes a = aIt.next();
      for (int i = labelColumns; i < a.getNumColumns(); i++) {
        sb.append(" \"" + f.toString() + "\" u " + usingPrefix + ":" + (i + 1));
        if (a.getLabel(i).length() > 0) {
          sb.append(" title \"" + a.getLabel(i) + "\"");
        } else {
          sb.append(" notitle");
        }
        if (a.getStyle(i).length() > 0) {
          sb.append(" " + a.getStyle(i));
        } else if (defaultStyle.length() > 0) {
          sb.append(" " + defaultStyle);
        }
        if (i + 1 < a.getNumColumns()) {
          sb.append(", ");
        }
      }
      if (fIt.hasNext() || aIt.hasNext()) {
        sb.append(", ");
      }
    }

    sb.append("\n");
    return sb.toString();

  }

  /**
   * Return the plotted data as image. The plot method has to be invoked first, otherwise null is
   * returned.
   *
   * @return the plotted data
   */
  public BufferedImage getImage() {
    return img;
  }

  /**
   * Saves the plotted data to a png file. The plot method has to be invoked first.
   *
   * @param file the file to save the png file
   * @throws IOException
   */
  public void saveImage(File file) throws IOException {
    ImageIO.write(img, "PNG", file);
  }

  /**
   * Returns the data in gnuplot data file format as list containing the data for each added axes.
   *
   * @return gnuplot data
   */
  @SuppressWarnings("unchecked")
  public List<String> getData() {
    List<String> re = new LinkedList<String>();
    for (Axes a : axes) {
      StringBuilder sb = new StringBuilder();
      for (String row : a) {
        sb.append(row);
        sb.append("\n");
      }
      re.add(sb.toString());
    }
    return re;
  }

  /**
   * Saves the gnuplot data to one ore more files. If only one data axes has been loaded, the file
   * is named as given. If there are more than one, the number is attached to the given filename.
   *
   * @param file where to save to file
   * @return list of the saved files
   * @throws IOException
   */
  public List<File> saveData(File file) throws IOException {
    List<File> re = new LinkedList<File>();

    if (axes.size() == 1) {
      FileWriter fw = new FileWriter(file);
      fw.append(getData().iterator().next());
      fw.flush();
      fw.close();
      re.add(file);
    } else {
      int index = 1;
      for (String content : getData()) {
        String fn = file.toString();
        if (fn.lastIndexOf(".") > fn.lastIndexOf(File.pathSeparator)) {
          fn = fn.substring(0, fn.lastIndexOf(".")) + (index++) + fn.substring(fn.lastIndexOf("."));
        } else {
          fn = fn + (index++);
        }

        File file2 = new File(fn);
        FileWriter fw = new FileWriter(file2);
        fw.append(content);
        fw.flush();
        fw.close();
        re.add(file2);
      }
    }
    return re;
  }

  /**
   * Returns whether or not gnuplot is accessible
   *
   * @return whether or not gnuplot is accessible
   */
  public boolean isAccessible() {
    Process p;
    try {
      p = new ProcessBuilder("gnuplot", "--version").start();
      String re = new BufferedReader(new InputStreamReader(p.getInputStream())).readLine();
      if (re == null || re.length() == 0) {
        return false;
      }
      return true;
    } catch (Exception e) {
      return false;
    }
  }


  /**
   * Calls gnuplot to plot the data. <code>loadData</code> has to be invoked first. A temporary file
   * is created in the working directory to hold the data (for compatibility with cygwin, this file
   * is not created by <code>File.createTempFile</code>) and deleted after plotting. If the JFrame
   * is visible, it will be repainted. The commands are given to gnuplot via its stdin, the plotted
   * image is retrieved via its stdout.
   * <p>
   * The name(s) of the temporary files are <code>hashCode()i.gnuplot</code>. If there is only one
   * file created, <code>i</code> is omitted.
   *
   * @throws IOException
   */
  public void plot() throws IOException {
    if (!isAccessible()) {
      throw new IOException("gnuplot is not accessible! Add it to your path variable!");
    }

    String tmp = this.hashCode() + ".gnuplot";
    List<File> files = saveData(new File(tmp));

    String plotCommand = getPlotCommand(files);
    Process p;
    try {
      p = new ProcessBuilder("gnuplot").start();
    } catch (Exception e) {
      throw new IOException("Gnuplot could not be called!");
    }

    BufferedWriter c = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
    for (String line : plotCommand.split("\n")) {
      c.append(line + "\n");
      c.flush();
      checkGnuplotError(p);
    }

    c.close();

    img = ImageIO.read(new BufferedInputStream(p.getInputStream()));
    if (img == null) {
      checkGnuplotError(p);
    }

    p.destroy();

    for (File f : files) {
      f.delete();
    }

    if (this.isVisible()) {
      setSize(img.getWidth(null), img.getHeight(null) + getInsets().top);
      repaint();
    }
  }

  private void checkGnuplotError(Process p) throws IOException {
    try {
      Thread.sleep(100);
    } catch (InterruptedException e1) {
    }
    if (p.getErrorStream().available() > 0) {
      StringBuilder e = new StringBuilder();
      BufferedReader br = new BufferedReader(new InputStreamReader(p.getErrorStream()));
      String line;
      while (br.ready()) {
        line = br.readLine();
        e.append(line + "\n");
      }
      throw new IOException(e.toString());
    }
  }

  @Override
  public void paint(Graphics g) {
    g.drawImage(img, 0, getInsets().top, null);
  }

  /**
   * The defaultStyle is used, when no style is defined for an column in an axes object.
   *
   * @return the defaultStyle
   */
  public String getDefaultStyle() {
    return defaultStyle;
  }

  /**
   * The defaultStyle is used, when no style is defined for an column in an axes object.
   *
   * @param defaultStyle the defaultStyle to set
   */
  public void setDefaultStyle(String defaultStyle) {
    this.defaultStyle = defaultStyle;
  }


}

