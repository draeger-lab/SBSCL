package org.simulator.math.odes;
import java.util.Map;
import org.sbml.jsbml.ASTNode;
import org.sbml.jsbml.ListOf;
import org.sbml.jsbml.Model;

import org.sbml.jsbml.Reaction;
import org.sbml.jsbml.Species;

import org.sbml.jsbml.Parameter;

import org.sbml.jsbml.SBMLDocument;
import org.sbml.jsbml.SBMLReader;

public class LSODAIntegrator {
    public static class Data {
        public Map<String, ASTNode> mapRates;
        public Map<String, Double> mapVariables;
        public Model model;

        public Data() {
            mapRates = new HashMap<>();
            mapVariables = new HashMap<>();
        }
    }

    public class ASTUtils {
        public ASTNode addASTasReactant(ASTNode ast, ASTNode kineticLaw) {
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

        public ASTNode addASTasProduct(ASTNode ast, ASTNode kineticLaw) {
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

    public class SpeciesUtils {
        public boolean isSpeciesReactantOf(Species species, Reaction reaction) {
            return reaction.getListOfReactants().stream()
                            .anyMatch(reactant -> reactant.getSpecies().equals(species.getId()));  
        }
    }

    public class ParameterUtils {
        public ASTNode rewriteLocalParameters(ASTNode node, ListOf<Parameter> localParameters) {
            if (node.isName()) {
                String name = node.getName();
                for (Parameter param : localParameters) {
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
        public Data loadModel(String filename) {
            Data data = new Data();
            SBMLDocument document = new SBMLReader().readSBMLFromFile(filename);
            data.model = document.getModel();

            for (Reaction reaction : data.model.getListOfReactions()) {
                ASTNode node = reaction.getKineticLaw().getMath();
                data.mapRates.put(reaction.getId(), ParameterUtils.rewriteLocalParameters(node, reaction.getKineticLaw().getListOfParameters()));
            }

            for (Species species : data.model.getListOfSpecies())  {
                if (species.getBoundaryCondition || species.getConstant()) {
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
                return data;
            }
        }
    }

}
































