package org.simulator.math.odes;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.math.ode.DerivativeException;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.LocalParameter;
import org.sbml.jsbml.Model;
import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;
import org.sbml.jsbml.Species;
import org.sbml.jsbml.util.compilers.ASTNodeCompiler;
import org.sbml.jsbml.util.compilers.ASTNodeValue;
import org.sbml.jsbml.util.compilers.FormulaCompiler;

public class LSODAIntegrator_F extends AdaptiveStepsizeIntegrator {

    public LSODAIntegrator_F() {
        super();
    }

    public LSODAIntegrator_F (LSODAIntegrator_F integrator) {
        super(integrator);
    }

    @Override
    public AbstractDESSolver clone() {
        return new LSODAIntegrator_F(this);
    }

    @Override
    public int getKiSAOterm() {
        return 0;
    }

    @Override
    public String getName() {
        return "LSODA Integrator)";
    }

    @Override
    protected boolean hasSolverEventProcessing() {
        return false;
    }

    @Override
    public double[] computeChange(DESystem DES, double[] y, double t, double stepSize, double[] change, boolean steadyState) throws DerivativeException{
        Data data = (Data) DES;
        int idx = 0;

        // update mapVariables with new values (y)
        for (String key : data.mapVariables.keySet()) {
            data.mapVariables.put(key, y[idx]);
            idx++;
        }

        idx = 0;
        ASTNodeCompiler compiler = new FormulaCompiler();
        for (Map.Entry<String, ASTNode> entry: data.mapRates.entrySet()) {
            ASTNode ast = entry.getValue();
            ASTNodeValue value = ast.compile(compiler);
            change[idx] = value.toDouble();
            idx++;
        }

        return change;
    };


    
    public static class Data {
        public Map<String, ASTNode> mapRates;
        public Map<String, Double> mapVariables;
        public Model model;

        public Data() {
            mapRates = new HashMap<>();
            mapVariables = new HashMap<>();
        }
    }

    public static class ASTUtils {
        public static ASTNode addASTasReactant(ASTNode ast, ASTNode kineticLaw) {
            ASTNode root;
            if (ast == null) {
                root = new ASTNode(ASTNode.Type.TIMES);
                ASTNode l = new ASTNode(ASTNode.Type.REAL);
                l.setValue(-1.0);
                root.addChild(l);
                root.addChild(kineticLaw);
            } else {
                root = new ASTNode(ASTNode.Type.MINUS);
                root.addChild(ast);
                root.addChild(kineticLaw);
            }
            return root;
        }

        public static ASTNode addASTasProduct(ASTNode ast, ASTNode kineticLaw) {
            ASTNode root;
            if (ast == null) {
                root = kineticLaw;
            } else {
                root = new ASTNode(ASTNode.Type.PLUS);
                root.addChild(ast);
                root.addChild(kineticLaw);
            }
            return root;
        }
    }

    public static class SpeciesUtils {
        public static boolean isSpeciesReactantOf(Species species, Reaction reaction) {
            return reaction.getListOfReactants().stream()
                            .anyMatch(reactant -> reactant.getSpecies().equals(species.getId()));  
        }
        public static boolean isSpeciesProductOf(Species species, Reaction reaction) {
            return reaction.getListOfProducts().stream()
                    .anyMatch(product -> product.getSpecies().equals(species.getId()));
        }
    }

    public static class ParameterUtils {
        public static ASTNode rewriteLocalParameters(ASTNode node, ListOf<LocalParameter> localParameters) {
            if (node.isName()) {
                String name = node.getName();
                for (LocalParameter param : localParameters) {
                    if (name.equals(param.getId())) {
                        ASTNode ret = new ASTNode(ASTNode.Type.REAL);
                        ret.setValue(param.getValue());
                        return ret;
                    }
                }
            }
            ASTNode ret = node.clone();
            for (int i = 0; i < ret.getChildCount(); i++) {
                ASTNode newChild = rewriteLocalParameters(ret.getChild(i), localParameters);
                ret.replaceChild(i, newChild);
            }
            return ret;
        }
    }

    public class ModelLoader {
        public Data loadModel(String filename) throws IOException, XMLStreamException {
            Data data = new Data();
            try {
                SBMLDocument document = new SBMLReader().readSBMLFromFile(filename);
                data.model = document.getModel();
                for (Reaction reaction : data.model.getListOfReactions()) {
                    ASTNode node = reaction.getKineticLaw().getMath();
                    data.mapRates.put(reaction.getId(), ParameterUtils.rewriteLocalParameters(node, reaction.getKineticLaw().getListOfLocalParameters()));
                }

                for (Species species : data.model.getListOfSpecies()) {
                    if (species.getBoundaryCondition() || species.getConstant()) {
                        continue;
                    }

                    if (species.isSetInitialAmount()) {
                        data.mapVariables.put(species.getId(), species.getInitialAmount());
                    } else {
                        data.mapVariables.put(species.getId(), species.getInitialConcentration());
                    }

                    ASTNode root = null;
                    for (Reaction reaction : data.model.getListOfReactions()) {
                        if (SpeciesUtils.isSpeciesReactantOf(species, reaction)) {
                            root = ASTUtils.addASTasReactant(root, data.mapRates.get(reaction.getId()));
                        }
                        if (SpeciesUtils.isSpeciesProductOf(species, reaction)) {
                            root = ASTUtils.addASTasProduct(root, data.mapRates.get(reaction.getId()));
                        }
                    }
                    if (root != null) {
                        data.mapRates.put(species.getId(), root);
                    }
                }
            } catch(IOException ioException) {
                throw ioException;
            } catch(XMLStreamException xmlStreamException) {
                throw xmlStreamException;
            }
            return data;
        }
    }

}