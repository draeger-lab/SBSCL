package org.sbml.simulator.math.astnode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Reaction;

public class ReactionValue extends ASTNodeObject {
  protected Reaction r;
  protected ASTNodeObject kineticLawUserObject;
  
  /**
   * 
   * @param interpreter
   * @param node
   * @param r
   */
  public ReactionValue(ASTNodeInterpreterWithTime interpreter, ASTNode node,
    Reaction r) {
    super(interpreter, node);
    this.r = r;
    if (r.isSetKineticLaw()) {
      this.kineticLawUserObject = (ASTNodeObject) r.getKineticLaw().getMath()
          .getUserObject();
    } else {
      this.kineticLawUserObject = null;
    }
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    doubleValue = Double.NaN;
    if (kineticLawUserObject != null) {
      doubleValue = kineticLawUserObject.compileDouble(time);
    }
  }
}
