package fern.network.sbml;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import fern.network.AmountManager;
import fern.network.Network;
import fern.simulation.Simulator;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.util.Maths;

/**
 * Representation of am evaluation tree. Within a sbml file, MathML branches may occur at
 * different positions. These are represented as MathTrees in FERN.
 *
 *
 * @author Florian Erhard
 *
 */
public class MathTree {

	private Node root;
	private Network net;

	/**
	 * Creates a MathTree from an libsbml {@link ASTNode}.
	 *
	 * @param net	sbml network
	 * @param ast	ASTNode
	 * @param globals	pointer to the global variable mapping
	 * @param locals	pointer to the local variable mapping of this entity
	 * @param bindings  mapping of the variable names to their indices
	 */
	public MathTree(Network net, ASTNode ast, Map<String,Double> globals, Map<String,Double> locals, Map<String, Integer> bindings)  {
		this.net = net;
		root = cloneTree(ast, locals, bindings);
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
				for (Node child : ((InnerNode)n).Children)
					dfs.add(child);
			else if (n instanceof VarLeaf)
				re.add(((VarLeaf)n).Index);
		}
		return re;
	}

	/**
	 * Gets the root of this MathTree. Feature versions may implement the Visitor pattern,
	 * but for now traversing by using instanceof will do.
	 * @return the root of this MathTree
	 */
	public Node getRoot() {
		return root;
	}

	/**
	 * Evaluate the MathTree.
	 *
	 * @param amount AmountManager
	 * @param sim		Simulator
	 * @return			value of the expression
	 */
	public double calculate(AmountManager amount, Simulator sim) {
		return calculateNode(root, amount, sim);
	}

	private double calculateNode(Node n, AmountManager amount, Simulator sim) {
		if (n instanceof ConstLeaf) return ((ConstLeaf)n).Value;
		else if (n instanceof VarLeaf) return amount.getAmount(((VarLeaf)n).Index);
		else if (n instanceof GlobalLeaf) {
			if (((GlobalLeaf)n).Value.equals("TIME")) return sim.getTime();
			if (!((SBMLPropensityCalculator)net.getPropensityCalculator()).getGlobalParameters().containsKey(((GlobalLeaf)n).Value))
				throw new RuntimeException("Global parameter "+((GlobalLeaf)n).Value+" not specified!");
			return ((SBMLPropensityCalculator)net.getPropensityCalculator()).getGlobalParameters().get(((GlobalLeaf)n).Value);
		}
		else {
			double[] c = new double[((InnerNode)n).Children.length];
			for (int i=0; i<c.length; i++)
				c[i] = calculateNode(((InnerNode)n).Children[i], amount,sim);

			switch (((InnerNode)n).AstNodeType) {
				case PLUS:
					double sum = 0;
					for (double v : c) sum += v;
					return sum;
				case MINUS:
					double sub = 0;
					if (c.length > 0){
						sub = -1 * c[0];
					}
					for(int i = 1;i < c.length;i++){
						sub -= c[i];
					}
					return sub;
				case TIMES:
					double mul = 1;
					for(double v : c) mul *= v;
					return mul;
				case DIVIDE:
					return c[0] / c[1];

				case POWER:
					return Math.pow(c[0], c[1]);
				case FUNCTION_ABS:
					return Math.abs(c[0]);
				case FUNCTION_ARCCOS:
					return Math.acos(c[0]);
				case FUNCTION_ARCCOT:
					/* arccot x =  arctan (1 / x) */
					return Math.atan(1. / c[0]);
				case FUNCTION_ARCCOTH:
					return ((1. / 2.) *
							Math.log((c[0] + 1.) /
									(c[1] - 1.)));
				case FUNCTION_ARCCSC:
					/* arccsc(x) = Arctan(1 / sqrt((x - 1)(x + 1))) */
					return Math.atan(1. / Math.sqrt((c[0] - 1.0) * (c[0] + 1.0)));
				case FUNCTION_ARCCSCH:
					/* arccsch(x) = ln((1 + sqrt(1 + x^2)) / x) */
					return Math.log((1. + Math.pow(1 + Math.pow(c[0], 2), 2)) / c[0]);
				case FUNCTION_ARCSEC:
					/* arcsec(x) = arctan(sqrt((x - 1)(x + 1))) */
					return Math.atan(Math.sqrt((c[0] - 1.0) * (c[0] + 1.0)));
				case FUNCTION_ARCSECH:
					/* arcsech(x) = ln((1 + sqrt(1 - x^2)) / x) */
					return Math.log((1. + Math.pow(1 - Math.pow(c[0], 2), 0.5)) / c[0]);
				case FUNCTION_ARCSIN:
					return Math.asin(c[0]);
				case FUNCTION_ARCSINH:
					return Math.log(c[0] + Math.sqrt(c[0] * c[0] + 1.0));
				case FUNCTION_ARCTAN:
					return Math.atan(c[0]);
				case FUNCTION_ARCTANH:
					return 0.5 * Math.log((c[0] + 1.0) / (c[0] - 1.0));
				case FUNCTION_CEILING:
					return Math.ceil(c[0]);
				case FUNCTION_COS:
					return Math.cos(c[0]);
				case FUNCTION_COSH:
					return Math.cosh(c[0]);
				case FUNCTION_COT:
					/* cot x = 1 / tan x */
					return 1. / Math.tan(c[0]);
				case FUNCTION_COTH:
					/* coth x = cosh x / sinh x */
					return Math.cosh(c[0]) / Math.sinh(c[0]);
				case FUNCTION_CSC:
					/* csc x = 1 / sin x */
					return (1. / Math.sin(c[0]));
				case FUNCTION_CSCH:
					/* csch x = 1 / cosh x  */
					return (1. / Math.cosh(c[0]));
				case FUNCTION_EXP:
					return Math.exp(c[0]);
				case FUNCTION_FLOOR:
					return Math.floor(c[0]);
				case FUNCTION_LN:
					return Math.log(c[0]);
				case FUNCTION_LOG:
					return Math.log10(c[0]);
				case FUNCTION_POWER:
					return Math.pow(c[0], c[1]);
				case FUNCTION_ROOT:
					return Math.pow(c[1], 1. / c[0]);
				case FUNCTION_SEC:
					/* sec x = 1 / cos x */
					return 1. / Math.cos(c[0]);
				case FUNCTION_SECH:
					/* sech x = 1 / sinh x */
					return 1. / Math.sinh(c[0]);
				case FUNCTION_SIN:
					return Math.sin(c[0]);
				case FUNCTION_SINH:
					return Math.sinh(c[0]);
				case FUNCTION_TAN:
					return Math.tan(c[0]);
				case FUNCTION_TANH:
					return Math.tanh(c[0]);
				case FUNCTION_FACTORIAL:
					return Maths.factorial(Math.toIntExact(Math.round(c[0])));
				case FUNCTION_PIECEWISE:
				case LAMBDA:
					return 0;
				case FUNCTION_QUOTIENT:
					return (int) (c[0] / c[1]);
				case FUNCTION_REM:
					return (int) (c[0] % c[1]);
				case FUNCTION_MAX:
					double max = c[0];
					for (int i = 1; i < c.length; i++) {
						max = Math.max(max, c[i]);
					}
					return max;
				case FUNCTION_MIN:
					double min = c[0];
					for (int i = 1; i < c.length; i++) {
						min = Math.min(min, c[i]);
					}
					return min;
				case RELATIONAL_EQ:
					return c[0] == c[1] ? 1 : 0;
				case RELATIONAL_GEQ:
					return c[0] >= c[1] ? 1 : 0;
				case RELATIONAL_GT:
					return c[0] > c[1] ? 1 : 0;
				case RELATIONAL_LEQ:
					return c[0] <= c[1] ? 1 : 0;
				case RELATIONAL_LT:
					return c[0] < c[1] ? 1 : 0;
				case RELATIONAL_NEQ:
					return c[0] != c[1] ? 1 : 0;

			default:
				throw new IllegalArgumentException("Type "+((InnerNode)n).AstNodeType+" not supported");
			}
		}
	}

	private Node cloneTree(ASTNode ast, Map<String,Double> locals, Map<String, Integer> bindings)  {
		int childnum = (int) ast.getNumChildren();
		ASTNode[] child = new ASTNode[childnum];
		for(int i = 0; i < childnum; i++)
			child[i] = ast.getChild(i);
		ASTNode.Type astNodeType = ast.getType();

		if (childnum==0) {
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
				throw new IllegalArgumentException("Type "+astNodeType+" not supported for MathML-Node without children");
			}
		} else {
			Node[] c = new Node[child.length];
			for (int i=0; i<c.length; i++)
				c[i] = cloneTree(child[i],locals,bindings);
			return new InnerNode(c,astNodeType);
		}
	}

	public static abstract class Node {}
	public static class InnerNode extends Node {
		public Node[] Children;
		public ASTNode.Type AstNodeType;
		public InnerNode(Node[] Children, ASTNode.Type AstNodeType) { this.Children = Children; this.AstNodeType = AstNodeType; }
		public String toString() { return inferStringFromSBMLConstant("AST",AstNodeType); }
	}
	public static class VarLeaf extends Node {
		public int Index;
		private String name;
		public VarLeaf(String name,int Index) { this.Index=Index; this.name = name;}
		public String toString() {return name;}
	}
	public static class ConstLeaf extends Node {
		public double Value;
		public ConstLeaf(double Value) { this.Value=Value; }
		public String toString() { return Value+""; }
	}
	public static class GlobalLeaf extends Node {
		public String Value;
		public GlobalLeaf(String Value) { this.Value=Value; }
		public String toString() { return Value+""; }
	}


	/**
	 * Uses reflection to get a string representation of a libsml constant.
	 * @param prefix a prefix for the constant name e.g. AST
	 * @param c the libsml constant
	 * @return the name of the constant in libsbmlConstants
	 */
	public static String inferStringFromSBMLConstant(String prefix, Object c) {
		// not the most efficient way, but that will do
		for (Field f : ASTNode.Type.class.getDeclaredFields())
			try {
				if (f.getName().startsWith(prefix) && f.get(null).equals(c)) return f.getName();
			} catch (Exception e) {}
		return c+"";
	}

}
