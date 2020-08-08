package fern.network.sbml;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import fern.network.AmountManager;
import fern.simulation.Simulator;
import org.sbml.jsbml.*;
import org.simulator.sbml.SBMLinterpreter;
import org.simulator.sbml.astnode.ASTNodeValue;

/**
 * Representation of am evaluation tree. Within a sbml file, MathML branches may occur at
 * different positions. These are represented as MathTrees in FERN.
 *
 * @author Florian Erhard
 */
public class MathTree {

    private ASTNode copiedAST;
    private SBMLinterpreter sbmlInterpreter;
    private Map<String, Integer> bindings;
    public static final String TEMP_VALUE = "SBML_SIMULATION_TEMP_VALUE";

    /**
     * Creates a MathTree {@link ASTNode}.
     *
     * @param interpreter sbmlInterpreter instance for calculating the nodes
     * @param ast      ASTNode
     * @param bindings mapping of the variable names to their indices
     */
    public MathTree(SBMLinterpreter interpreter, ASTNode ast, Map<String, Integer> bindings) {
        this.bindings = bindings;
        sbmlInterpreter = interpreter;
        copiedAST = interpreter.copyAST(ast, true, null, null);
    }

    /**
     * Gets the species present in this tree.
     *
     * @return indices of the species.
     */
    public List<Integer> getSpecies() {
        List<Integer> re = new LinkedList<>();
        Stack<ASTNode> dfs = new Stack<>();
        dfs.add(copiedAST);
        while (!dfs.empty()) {
            ASTNode node = dfs.pop();
            if ((node.getNumChildren() == 0) && !node.isOperator() && !node.isNumber()){
                Integer index = bindings.get(node.getName());
                if ((index != null) && !re.contains(index)){
                    re.add(bindings.get(node.getName()));
                }
            } else if (node.getNumChildren() != 0) {
                for (int i = 0; i < node.getNumChildren(); i++){
                    dfs.add(node.getChild(i));
                }
            }
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
        sbmlInterpreter.updateSpeciesConcentration(amount);
        sbmlInterpreter.setCurrentTime(sim.getTime());
        return ((ASTNodeValue) copiedAST.getUserObject(TEMP_VALUE)).compileDouble(sim.getTime(), 0d);
    }

}
