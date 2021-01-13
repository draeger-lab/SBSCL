package fern.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

/**
 * Base implementation of the {@link AnnotationManager} interface. Annotations are stored in one big
 * hash with Strings as keys. If you are looking for a high performance implementation of
 * <code>AnnotationManager</code>, don't look here. However, annotations are only supposed to hold
 * data which
 * <ul>
 * 	<li>almost never changes</li>
 * 	<li>is read very infrequently</li>
 * </ul>
 *
 * @author Florian Erhard
 */
public class AnnotationManagerImpl implements AnnotationManager {

  HashMap<String, String> bigHash = null;

  public AnnotationManagerImpl() {
    bigHash = new HashMap<>();
  }


  private String makeSpeciesKey(int i, String typ) {
    String s = String.valueOf(i);
    StringBuffer sb = new StringBuffer(s.length() + typ.length() + 2);
    sb.append(s);
    sb.append("S#");
    sb.append(typ);
    return sb.toString();
  }

  private String makeReactionKey(int i, String typ) {
    String s = String.valueOf(i);
    StringBuffer sb = new StringBuffer(s.length() + typ.length() + 2);
    sb.append(s);
    sb.append("R#");
    sb.append(typ);
    return sb.toString();
  }

  public boolean containsNetworkAnnotation(String typ) {
    return bigHash.containsKey(typ);
  }

  public boolean containsReactionAnnotation(int reaction, String typ) {
    return bigHash.containsKey(makeReactionKey(reaction, typ));
  }

  public boolean containsSpeciesAnnotation(int species, String typ) {
    return bigHash.containsKey(makeSpeciesKey(species, typ));
  }


  public String getNetworkAnnotation(String typ) {
    return bigHash.get(typ);
  }

  public Collection<String> getNetworkAnnotationTypes() {
    ArrayList<String> re = new ArrayList<>();
    for (String key : bigHash.keySet()) {
      int index = key.indexOf('#');
      if (index == -1) {
        re.add(key);
      }
    }
    return re;
  }

  public String getReactionAnnotation(int reaction, String typ) {
    return bigHash.get(makeReactionKey(reaction, typ));
  }

  public Collection<String> getReactionAnnotationTypes(int reaction) {
    ArrayList<String> re = new ArrayList<>();
    for (String key : bigHash.keySet()) {
      int index = key.indexOf('#');
      if (index > 0 && key.charAt(index - 1) == 'R'
          && Integer.parseInt(key.substring(0, index - 1)) == reaction) {
        re.add(key.substring(index + 1));
      }
    }
    return re;
  }

  public String getSpeciesAnnotation(int species, String typ) {
    return bigHash.get(makeSpeciesKey(species, typ));
  }

  public Collection<String> getSpeciesAnnotationTypes(int species) {
    ArrayList<String> re = new ArrayList<>();
    for (String key : bigHash.keySet()) {
      int index = key.indexOf('#');
      if (index > 0 && key.charAt(index - 1) == 'S'
          && Integer.parseInt(key.substring(0, index - 1)) == species) {
        re.add(key.substring(index + 1));
      }
    }
    return re;
  }

  public void setNetworkAnnotation(String typ, String annotation) {
    bigHash.put(typ, annotation);
  }

  public void setReactionAnnotation(int reaction, String typ,
      String annotation) {
    bigHash.put(makeReactionKey(reaction, typ), annotation);
  }

  public void setSpeciesAnnotation(int species, String typ, String annotation) {
    bigHash.put(makeSpeciesKey(species, typ), annotation);
  }

}
