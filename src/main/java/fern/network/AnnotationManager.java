/*
 * Created on 12.03.2007
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.network;

import java.util.Collection;

/**
 * AnnotationManager objects are the places, where static (or quasi-static) properties of a network
 * are stored. At this time, all the annotations and the initial amounts of the species belong to
 * the static properties. Annotations are only supposed to hold data which
 * <ul>
 * 	<li>almost never changes</li>
 * 	<li>is read very infrequently</li>
 * </ul>
 *
 * @author Florian Erhard
 */
public interface AnnotationManager {


  /**
   * Returns true iff the species contains an annotation of the specified type.
   *
   * @param species index of the species
   * @param typ     field name of the annotation
   * @return true iff such an annotation is present
   */
  public boolean containsSpeciesAnnotation(int species, String typ);

  /**
   * Gets the names of the species annotations.
   *
   * @param species index of the species
   * @return names of the fields
   */
  public Collection<String> getSpeciesAnnotationTypes(int species);

  /**
   * Gets the species annotation of the specified field.
   *
   * @param species index of the species
   * @param typ     name of the field
   * @return species annotation
   */
  public String getSpeciesAnnotation(int species, String typ);

  /**
   * Sets the species annotation of the specified field.
   *
   * @param species    index of the species
   * @param typ        name of the field
   * @param annotation species annotation
   */
  public void setSpeciesAnnotation(int species, String typ, String annotation);

  /**
   * Returns true iff the reaction contains an annotation of the specified type.
   *
   * @param reaction index of the reaction
   * @param typ      field name of the annotation
   * @return true iff such an annotation is present
   */
  public boolean containsReactionAnnotation(int reaction, String typ);

  /**
   * Gets the names of the species reaction.
   *
   * @param reaction index of the reaction
   * @return names of the fields
   */
  public Collection<String> getReactionAnnotationTypes(int reaction);

  /**
   * Gets the reaction annotation of the specified field.
   *
   * @param reaction index of the reaction
   * @param typ      name of the field
   * @return reaction annotation
   */
  public String getReactionAnnotation(int reaction, String typ);

  /**
   * Sets the reaction annotation of the specified field.
   *
   * @param reaction   index of the reaction
   * @param typ        name of the field
   * @param annotation reaction annotation
   */
  public void setReactionAnnotation(int reaction, String typ, String annotation);

  /**
   * Returns true iff the network contains an annotation of the specified type.
   *
   * @param typ field name of the annotation
   * @return true iff such an annotation is present
   */
  public boolean containsNetworkAnnotation(String typ);

  /**
   * Gets the names of the network reaction.
   *
   * @return names of the fields
   */
  public Collection<String> getNetworkAnnotationTypes();

  /**
   * Gets the network annotation of the specified field.
   *
   * @param typ name of the field
   * @return network annotation
   */
  public String getNetworkAnnotation(String typ);

  /**
   * Sets the network annotation of the specified field.
   *
   * @param typ        name of the field
   * @param annotation network annotation
   */
  public void setNetworkAnnotation(String typ, String annotation);
}
