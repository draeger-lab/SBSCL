package fern.network.fernml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.jdom.Element;

import fern.network.AnnotationManager;

/**
 * <code>AnnotationManager</code> for {@link FernMLNetwork}s. The data is not copied but maintained
 * within the tree (which should not be an efficiency issue except you want to use the annotations
 * to store user data of reactions / species and do this very excessive - so don't do that). This
 * avoids problems when saving the
 * <code>FernMLNetwork</code> again.
 *
 * @author Florian Erhard
 */
public class FernMLAnnotationManager implements AnnotationManager {

  private static final String NAME = "name";
  private static final String KINETIC_CONSTANT_REVERSIBLE = "kineticConstantReversible";
  private static final String LIST_OF_REACTIONS = "listOfReactions";
  private static final String LIST_OF_SPECIES = "listOfSpecies";
  private static final String LIST_OF_ANNOTATIONS = "listOfAnnotations";


  private Element root = null;

  /**
   * Create the <code>AnnotationManager</code> from the root element of the jdom tree.
   *
   * @param rootElement the root element of the jdom tree
   */
  public FernMLAnnotationManager(Element rootElement) {
    this.root = rootElement;
  }


  /**
   * Gets the reaction jdom element for an index. The difficulty is that some reaction might be
   * reversible, some might not.
   *
   * @param reaction index of the reaction
   * @return jdom {@link Element}
   */
  @SuppressWarnings("unchecked")
  private Element findReaction(int reaction) {
    List<Element> reactions = root.getChild(LIST_OF_REACTIONS).getChildren();
    int numReaction = 0;
    for (Element r : reactions) {
      boolean reversible = r.getAttribute(KINETIC_CONSTANT_REVERSIBLE) != null;
      if (numReaction == reaction || (reversible && numReaction + 1 == reaction)) {
        return r;
      }
      numReaction += reversible ? 2 : 1;
    }
    return null;
  }


  private boolean containsAnnotation(List<Element> annotations, String typ) {
    for (Element e : annotations) {
      if (e.getAttributeValue(NAME).equals(typ)) {
        return true;
      }
    }
    return false;
  }

  private String getAnnotation(List<Element> annotations, String typ) {
    for (Element e : annotations) {
      if (e.getAttributeValue(NAME).equals(typ)) {
        return e.getText();
      }
    }
    return null;
  }

  private Collection<String> getAnnotationTypes(List<Element> annotations) {
    ArrayList<String> re = new ArrayList<String>(annotations.size());
    for (Element e : annotations) {
      re.add(e.getAttributeValue(NAME));
    }
    return re;
  }

  private void setAnnotation(List<Element> annotations, String typ, String annotation) {
    for (Element e : annotations) {
      if (e.getAttributeValue(NAME).equals(typ)) {
        e.setText(annotation);
        return;
      }
    }
    Element e = new Element("annotation");
    e.setAttribute(NAME, typ);
    e.setText(annotation);
    annotations.add(e);
  }

  @SuppressWarnings("unchecked")
  public boolean containsNetworkAnnotation(String typ) {
    if (root.getChild(LIST_OF_ANNOTATIONS) == null) {
      return false;
    }
    return containsAnnotation(root.getChild(LIST_OF_ANNOTATIONS).getChildren(), typ);
  }

  @SuppressWarnings("unchecked")
  public String getNetworkAnnotation(String typ) {
    if (root.getChild(LIST_OF_ANNOTATIONS) == null) {
      return null;
    }
    return getAnnotation(root.getChild(LIST_OF_ANNOTATIONS).getChildren(), typ);
  }

  @SuppressWarnings("unchecked")
  public Collection<String> getNetworkAnnotationTypes() {
    if (root.getChild(LIST_OF_ANNOTATIONS) == null) {
      return new LinkedList<String>();
    }
    return getAnnotationTypes(root.getChild(LIST_OF_ANNOTATIONS).getChildren());
  }

  @SuppressWarnings("unchecked")
  public void setNetworkAnnotation(String typ, String annotation) {
    if (root.getChild(LIST_OF_ANNOTATIONS) == null) {
      root.getChildren().add(0, new Element(LIST_OF_ANNOTATIONS));
    }
    setAnnotation(root.getChild(LIST_OF_ANNOTATIONS).getChildren(), typ, annotation);
  }


  @SuppressWarnings("unchecked")
  public boolean containsReactionAnnotation(int reaction, String typ) {
    Element el = findReaction(reaction);
    if (el.getChild(LIST_OF_ANNOTATIONS) == null) {
      return false;
    }
    return containsAnnotation(el.getChild(LIST_OF_ANNOTATIONS).getChildren(), typ);
  }

  @SuppressWarnings("unchecked")
  public String getReactionAnnotation(int reaction, String typ) {
    Element el = findReaction(reaction);
    if (el.getChild(LIST_OF_ANNOTATIONS) == null) {
      return null;
    }
    return getAnnotation(el.getChild(LIST_OF_ANNOTATIONS).getChildren(), typ);
  }

  @SuppressWarnings("unchecked")
  public Collection<String> getReactionAnnotationTypes(int reaction) {
    Element el = findReaction(reaction);
    if (el.getChild(LIST_OF_ANNOTATIONS) == null) {
      return new LinkedList<String>();
    }
    return getAnnotationTypes(el.getChild(LIST_OF_ANNOTATIONS).getChildren());
  }

  @SuppressWarnings("unchecked")
  public void setReactionAnnotation(int reaction, String typ,
      String annotation) {
    Element el = findReaction(reaction);
    if (el.getChild(LIST_OF_ANNOTATIONS) == null) {
      el.getChildren().add(0, new Element(LIST_OF_ANNOTATIONS));
    }
    setAnnotation(el.getChild(LIST_OF_ANNOTATIONS).getChildren(), typ, annotation);
  }


  @SuppressWarnings("unchecked")
  public boolean containsSpeciesAnnotation(int species, String typ) {
    Element el = (Element) root.getChild(LIST_OF_SPECIES).getChildren().get(species);
    if (el.getChild(LIST_OF_ANNOTATIONS) == null) {
      return false;
    }
    return containsAnnotation(el.getChild(LIST_OF_ANNOTATIONS).getChildren(), typ);
  }

  @SuppressWarnings("unchecked")
  public String getSpeciesAnnotation(int species, String typ) {
    Element el = (Element) root.getChild(LIST_OF_SPECIES).getChildren().get(species);
    if (el.getChild(LIST_OF_ANNOTATIONS) == null) {
      return null;
    }
    return getAnnotation(el.getChild(LIST_OF_ANNOTATIONS).getChildren(), typ);
  }

  @SuppressWarnings("unchecked")
  public Collection<String> getSpeciesAnnotationTypes(int species) {
    Element el = (Element) root.getChild(LIST_OF_SPECIES).getChildren().get(species);
    if (el.getChild(LIST_OF_ANNOTATIONS) == null) {
      return new LinkedList<String>();
    }
    return getAnnotationTypes(el.getChild(LIST_OF_ANNOTATIONS).getChildren());
  }

  @SuppressWarnings("unchecked")
  public void setSpeciesAnnotation(int species, String typ, String annotation) {
    Element el = (Element) root.getChild(LIST_OF_SPECIES).getChildren().get(species);
    if (el.getChild(LIST_OF_ANNOTATIONS) == null) {
      el.getChildren().add(0, new Element(LIST_OF_ANNOTATIONS));
    }
    setAnnotation(el.getChild(LIST_OF_ANNOTATIONS).getChildren(), typ, annotation);
  }


}
