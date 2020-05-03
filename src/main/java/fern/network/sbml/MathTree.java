package fern.network.sbml;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import fern.network.AmountManager;
import fern.network.sbml.astnode.*;
import fern.simulation.Simulator;
import org.sbml.jsbml.*;
import org.sbml.jsbml.validator.ModelOverdeterminedException;

/**
 * Representation of am evaluation tree. Within a sbml file, MathML branches may occur at
 * different positions. These are represented as MathTrees in FERN.
 *
 * @author Florian Erhard
 */
public class MathTree {

    private Node root;
    private SBMLNetwork net;
    private ASTNode copiedAST;
    private SBMLinterpreter sbmLinterpreter;
    public static final String TEMP_VALUE = "SBML_SIMULATION_TEMP_VALUE";

    /**
     * Creates a MathTree from an libsbml {@link ASTNode}.
     *
     * @param net      sbml network
     * @param ast      ASTNode
     * @param globals  pointer to the global variable mapping
     * @param locals   pointer to the local variable mapping of this entity
     * @param bindings mapping of the variable names to their indices
     */
    public MathTree(SBMLNetwork net, ASTNode ast, Map<String, Double> globals, Map<String, Double> locals, Map<String, Integer> bindings) throws ModelOverdeterminedException {
        this.net = net;
        root = cloneTree(ast, locals, bindings);
        sbmLinterpreter = new SBMLinterpreter(net.getSBMLModel());
        copiedAST = sbmLinterpreter.copyAST(ast, true, null, null);
    }

    /**
     * Gets the species present in this tree.
     *
     * @return indices of the species.
     */
    public List<Integer> getSpecies() {
        List<Integer> re = new LinkedList<Integer>();
        Stack<Node> dfs = new Stack<Node>();
        dfs.add(root);
        while (!dfs.isEmpty()) {
            Node n = dfs.pop();
            if (n instanceof InnerNode)
                for (Node child : ((InnerNode) n).Children)
                    dfs.add(child);
            else if (n instanceof VarLeaf)
                re.add(((VarLeaf) n).Index);
        }
        return re;
    }

    /**
     * Gets the root of this MathTree. Feature versions may implement the Visitor pattern,
     * but for now traversing by using instanceof will do.
     *
     * @return the root of this MathTree
     */
    public Node getRoot() {
        return root;
    }

    /**
     * Evaluate the MathTree.
     *
     * @param amount AmountManager
     * @param sim    Simulator
     * @return value of the expression
     */
    public double calculate(AmountManager amount, Simulator sim) {

        sbmLinterpreter.updateSpeciesConcentration(amount);
        return ((ASTNodeValue) copiedAST.getUserObject(TEMP_VALUE)).compileDouble(sim.getTime(), 0d);

    }

    private Node cloneTree(ASTNode ast, Map<String, Double> locals, Map<String, Integer> bindings) {
        int childnum = (int) ast.getNumChildren();
        ASTNode[] child = new ASTNode[childnum];
        for (int i = 0; i < childnum; i++)
            child[i] = ast.getChild(i);
        ASTNode.Type astNodeType = ast.getType();

        if (childnum == 0) {
            switch (astNodeType) {
                case INTEGER:
                    return new ConstLeaf((double) ast.getInteger());
                case REAL:
                case REAL_E:
                case RATIONAL:
                    return new ConstLeaf(ast.getReal());
                case NAME:
                    if (locals.containsKey(ast.getName()))
                        return new ConstLeaf(locals.get(ast.getName()));
                    else if (bindings.containsKey(ast.getName()))
                        return new VarLeaf(ast.getName(), bindings.get(ast.getName()));
                    else
                        return new GlobalLeaf(ast.getName());
                case CONSTANT_E:
                    return new ConstLeaf(Math.exp(1));
                case CONSTANT_FALSE:
                    return new ConstLeaf(0);
                case CONSTANT_PI:
                    return new ConstLeaf(Math.PI);
                case CONSTANT_TRUE:
                    return new ConstLeaf(1);
//                case 261:
//                    return new GlobalLeaf("TIME");
                default:
                    throw new IllegalArgumentException("Type " + astNodeType + " not supported for MathML-Node without children");
            }
        } else {
            Node[] c = new Node[child.length];
            for (int i = 0; i < c.length; i++)
                c[i] = cloneTree(child[i], locals, bindings);
            return new InnerNode(c, astNodeType);
        }
    }

    public static abstract class Node {
    }

    public static class InnerNode extends Node {
        public Node[] Children;
        public ASTNode.Type AstNodeType;

        public InnerNode(Node[] Children, ASTNode.Type AstNodeType) {
            this.Children = Children;
            this.AstNodeType = AstNodeType;
        }

        public String toString() {
            return inferStringFromSBMLConstant("AST", AstNodeType);
        }
    }

    public static class VarLeaf extends Node {
        public int Index;
        private String name;

        public VarLeaf(String name, int Index) {
            this.Index = Index;
            this.name = name;
        }

        public String toString() {
            return name;
        }
    }

    public static class ConstLeaf extends Node {
        public double Value;

        public ConstLeaf(double Value) {
            this.Value = Value;
        }

        public String toString() {
            return Value + "";
        }
    }

    public static class GlobalLeaf extends Node {
        public String Value;

        public GlobalLeaf(String Value) {
            this.Value = Value;
        }

        public String toString() {
            return Value + "";
        }
    }


    /**
     * Uses reflection to get a string representation of a libsml constant.
     *
     * @param prefix a prefix for the constant name e.g. AST
     * @param c      the libsml constant
     * @return the name of the constant in libsbmlConstants
     */
    public static String inferStringFromSBMLConstant(String prefix, Object c) {
        // not the most efficient way, but that will do
        for (Field f : ASTNode.Type.class.getDeclaredFields())
            try {
                if (f.getName().startsWith(prefix) && f.get(null).equals(c)) return f.getName();
            } catch (Exception e) {
            }
        return c + "";
    }

}
