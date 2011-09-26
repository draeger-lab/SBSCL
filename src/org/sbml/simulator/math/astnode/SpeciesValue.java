package org.sbml.simulator.math.astnode;

import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.Species;
import org.sbml.simulator.math.ValueHolder;

public class SpeciesValue extends ASTNodeObject {
  protected Species s;
  protected String id;
  protected ValueHolder valueHolder;
  protected boolean isSetInitialAmount;
  protected boolean hasOnlySubstanceUnits;
  protected boolean isSetInitialConcentration;
  
  /**
   * 
   * @param interpreter
   * @param node
   * @param s
   * @param valueHolder
   */
  public SpeciesValue(ASTNodeInterpreterWithTime interpreter, ASTNode node,
    Species s, ValueHolder valueHolder) {
    super(interpreter, node);
    this.s = s;
    this.id=s.getId();
    this.valueHolder = valueHolder;
    this.isSetInitialAmount=s.isSetInitialAmount();
    this.isSetInitialConcentration=s.isSetInitialConcentration();
    this.hasOnlySubstanceUnits=s.getHasOnlySubstanceUnits();
  }
  
  /*
   * (non-Javadoc)
   * @see org.sbml.simulator.math.astnode.ASTNodeObject#computeDoubleValue()
   */
  protected void computeDoubleValue() {
    double compartmentValue = valueHolder.getCurrentCompartmentValueOf(id);
    if (compartmentValue == 0d) {
      doubleValue = valueHolder.getCurrentSpeciesValue(id);
    }

    else if (isSetInitialAmount && hasOnlySubstanceUnits) {
      doubleValue = valueHolder.getCurrentSpeciesValue(id) / compartmentValue;
      
    }

    else if (isSetInitialConcentration && hasOnlySubstanceUnits) {  
      doubleValue = valueHolder.getCurrentSpeciesValue(id) * compartmentValue;
    } else {
      doubleValue = valueHolder.getCurrentSpeciesValue(id);
      
    }
  }
  
}
