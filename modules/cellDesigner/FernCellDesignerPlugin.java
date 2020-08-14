/*
 * Created on 03.03.2008
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.cellDesigner;

import java.awt.event.ActionEvent;

import jp.sbi.celldesigner.plugin.CellDesignerPlugin;
import jp.sbi.celldesigner.plugin.PluginAction;
import jp.sbi.celldesigner.plugin.PluginMenu;
import jp.sbi.celldesigner.plugin.PluginMenuItem;
import jp.sbi.celldesigner.plugin.PluginSBase;
import fern.cellDesigner.ui.MainFrame;

public class FernCellDesignerPlugin extends CellDesignerPlugin {

  public FernCellDesignerPlugin() {
    PluginMenu menu = new PluginMenu("FERN");
    PluginAction action = new FernPluginAction();
    PluginMenuItem item = new PluginMenuItem("Simulation", action);
    menu.add(item);
    addCellDesignerPluginMenu(menu);
  }


  private class FernPluginAction extends PluginAction {

    public void myActionPerformed(ActionEvent arg0) {
      new MainFrame(FernCellDesignerPlugin.this).setVisible(true);
    }
  }


  public void SBaseAdded(PluginSBase arg0) {

  }


  public void SBaseChanged(PluginSBase arg0) {

  }


  public void SBaseDeleted(PluginSBase arg0) {

  }


  public void addPluginMenu() {

  }


  public void modelClosed(PluginSBase arg0) {

  }


  public void modelOpened(PluginSBase arg0) {
  }


  public void modelSelectChanged(PluginSBase model) {

  }

}
