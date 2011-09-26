package org.sbml.simulator.math.astnode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.LocalParameter;

public class LocalParameterValue extends ASTNodeObject {
  protected LocalParameter lp;

  /**
   * 
   * @param interpreter
   * @param node
   * @param lp
   */
  public LocalParameterValue(ASTNodeInterpreterWithTime interpreter, ASTNode node,
    LocalParameter lp) {
    super(interpreter, node);
    this.lp=lp;
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    doubleValue=lp.getValue();;
  }
}
