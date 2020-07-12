package fern.network.sbml;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import fern.network.AmountManager;
import fern.simulation.Simulator;
import org.sbml.jsbml.*;
import org.sbml.jsbml.validator.ModelOverdeterminedException;
import org.simulator.sbml.SBMLinterpreter;
import org.simulator.sbml.astnode.ASTNodeValue;

/**
 * Representation of am evaluation tree. Within a sbml file, MathML branches may occur at
 * different positions. These are represented as MathTrees in FERN.
 *
 * @author Florian Erhard
 */
public class MathTree {

    private SBMLNetwork net;
    private ASTNode copiedAST;
    private SBMLinterpreter sbmLinterpreter;
    private Map<String, Integer> bindings;
    public static final String TEMP_VALUE = "SBML_SIMULATION_TEMP_VALUE";

    /**
     * Creates a MathTree {@link ASTNode}.
     *
     * @param net      sbml network
     * @param ast      ASTNode
     * @param globals  pointer to the global variable mapping
     * @param locals   pointer to the local variable mapping of this entity
     * @param bindings mapping of the variable names to their indices
     */
    public MathTree(SBMLNetwork net, ASTNode ast, Map<String, Double> globals, Map<String, Double> locals, Map<String, Integer> bindings) throws ModelOverdeterminedException {
        this.net = net;
        this.bindings = bindings;
        sbmLinterpreter = new SBMLinterpreter(net.getSBMLModel());
        copiedAST = sbmLinterpreter.copyAST(ast, true, null, null);
    }

    /**
     * Gets the species present in this tree.
     *
     * @return indices of the species.
     */
    public List<Integer> getSpecies() {
        List<Integer> re = new LinkedList<>();
        for (int i = copiedAST.getChildCount() - 1; i > 0; i--){
            re.add(bindings.get(copiedAST.getChild(i).getName()));
        }
        return re;
    }

    /**
     * Gets the ASTNode of this MathTree.
     *
     * @return the copiedAST of this MathTree
     */
    public ASTNode getCopiedAST() {
        return copiedAST;
    }

    /**
     * Evaluate the MathTree.
     *
     * @param amount AmountManager
     * @param sim    Simulator
     * @return value of the expression
     */
    public double calculate(AmountManager amount, Simulator sim) {

//        sbmLinterpreter.updateSpeciesConcentration(amount);
        return ((ASTNodeValue) copiedAST.getUserObject(TEMP_VALUE)).compileDouble(sim.getTime(), 0d);

    }

}
