package org.sbml.simulator.math.astnode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.CallableSBase;
import org.sbml.simulator.math.ValueHolder;

public class CompartmentOrParameterValue extends ASTNodeObject {
  protected CallableSBase sb;
  protected String id;
  protected ValueHolder valueHolder;
  
  /**
   * 
   * @param interpreter
   * @param node
   * @param sb
   * @param valueHolder
   */
  public CompartmentOrParameterValue(ASTNodeInterpreterWithTime interpreter, ASTNode node,
    CallableSBase sb, ValueHolder valueHolder) {
    super(interpreter, node);
    this.sb = sb;
    this.id=sb.getId();
    this.valueHolder = valueHolder;
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    doubleValue=valueHolder.getCurrentValueOf(id);
  }
}
