package fern.analysis;

import fern.network.Network;

/**
 * An instance of <code>NodeCheckerByAnnotation</code> can be used to control a search in
 * <code>AnalysisBase</code> by a <code>NetworkSearchAction</code>. Then the reactions / species
 * in the network are only visited, if they have the specified annotation.
 *
 * @author Florian Erhard
 */
public class NodeCheckerByAnnotation implements NodeChecker {

  private String field;
  private String value;

  /**
   * Creates the NodeChecker with the specified annotation field and value. If value is
   * <code>null</code> then the reaction / species is valid if an annotation named
   * <code>field</code> exists.
   *
   * @param field the annotation name
   * @param value the annotation value
   */
  public NodeCheckerByAnnotation(String field, String value) {
    this.field = field;
    this.value = value;
  }

  public boolean checkReactionNode(Network network, int reaction) {
    String rval = network.getAnnotationManager().getReactionAnnotation(reaction, field);
		if (rval == null) {
			return false;
		} else if (value == null) {
			return true;
		} else {
			return rval.equals(value);
		}
  }

  public boolean checkSpeciesNode(Network network, int species) {
    String rval = network.getAnnotationManager().getSpeciesAnnotation(species, field);
		if (rval == null) {
			return false;
		} else if (value == null) {
			return true;
		} else {
			return rval.equals(value);
		}
  }

}
