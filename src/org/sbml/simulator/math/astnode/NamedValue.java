package org.sbml.simulator.math.astnode;

import org.sbml.jsbml.ASTNode;

public class NamedValue extends ASTNodeObject {

  private FunctionValue function;
  private int index;
  
  public NamedValue(ASTNodeInterpreterWithTime interpreter, ASTNode node, FunctionValue function) {
    super(interpreter, node);
    this.function=function;
    this.index=function.getIndex(node.getName());
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    doubleValue=function.getArgumentValues()[index];
  }
  
  
}
