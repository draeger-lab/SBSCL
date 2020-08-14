/*
 * Created on 03.08.2006
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package fern.simulation.algorithm;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;


/**
 * Implements an indexed priority queue. It is a data structure to manage the absolute taus of the
 * Gibson-Bruck algorithm in an efficient way. It is composed of a binary heap (the taus are the
 * keys) and an index for fast access to a node of the heap. The fast access is necessary to update
 * unused taus in the queue.
 * <p>
 * For reference see M.A.Gibson and J.Bruck, Efficient Exact Stochastic Simulation of Chemical
 * Systems with Many Species and Many Channels, J.Phys.Chem.A., Vol 104, no 9, 2000
 *
 * @author Florian Erhard
 * @see GibsonBruckSimulator
 */
public class IndexedPriorityQueue {

  private double[] t;
  private int[] oldIndex;

  private Node heapRoot = null;
  private Node[] index = null;


  /**
   * Creates a <code>IndexedPriorityQueue</code> from the given taus
   *
   * @param t taus
   */
  public IndexedPriorityQueue(double[] t) {
    this.t = t;
    oldIndex = new int[t.length];
    for (int i = 0; i < oldIndex.length; i++) {
      oldIndex[i] = i;
    }

    qsort(0, t.length - 1);

    // create balanced binary heap and index
    index = new Node[t.length];
    heapRoot = new Node(oldIndex[0], t[0]);
    Queue<Node> queue = new LinkedList<Node>();
    queue.add(heapRoot);
    index[heapRoot.index] = heapRoot;
    for (int i = 1; i < t.length; i += 2) {
      Node n = queue.poll();
      Node left = new Node(oldIndex[i], t[i]);
      n.left = left;
      left.parent = n;
      queue.add(left);
      index[left.index] = left;
      if (i + 1 < t.length) {
        Node right = new Node(oldIndex[i + 1], t[i + 1]);
        n.right = right;
        right.parent = n;
        queue.add(right);
        index[right.index] = right;
      }
    }
  }

  @Override
  public String toString() {
    return Arrays.toString(index);
  }

  /**
   * Gets the number of nodes in the binary heap.
   *
   * @return number of nodes in the binary heap.
   */
  public int size() {
    int s = 0;
    Queue<Node> q = new LinkedList<Node>();
    q.add(heapRoot);
    while (!q.isEmpty()) {
      Node n = q.poll();
      s++;
      if (n.left != null) {
        q.add(n.left);
      }
      if (n.right != null) {
        q.add(n.right);
      }
    }
    return s;
  }

  /**
   * Gets the index of the minimal tau in the queue.
   *
   * @return index of the minimal tau
   */
  public int getMin() {
    return heapRoot.index;
  }

  /**
   * Gets the minimal tau in the queue.
   *
   * @return minimal tau
   */
  public double getMinKey() {
    return heapRoot.key;
  }

  /**
   * Updates the tau of a reaction to a given value
   *
   * @param i     index of the reaction
   * @param value new tau
   */
  public void update(int i, double value) {
    if (Double.isNaN(value)) {
      value = Double.POSITIVE_INFINITY;
    }

    Node n = index[i];
    n.key = value;
    update_aux(n);
  }

  /**
   * Gets the tau of a reaction by lookup in the index
   *
   * @param i index of the reaction
   * @return tau of the reaction
   */
  public double getKey(int i) {
    return index[i].key;
  }

  private void update_aux(Node n) {
    if (n.parent != null && n.key < n.parent.key) {
      swap(n, n.parent);
      update_aux(n.parent);
    } else if (n.left != null && n.right != null && n.key > Math.min(n.left.key, n.right.key)) {
      Node child = n.left.key < n.right.key ? n.left : n.right;
      swap(n, child);
      update_aux(child);
    } else if (n.left != null && n.key > n.left.key) {
      swap(n, n.left);
      update_aux(n.left);
    }
  }

  private void swap(Node n, Node m) {
    double tmp = n.key;
    n.key = m.key;
    m.key = tmp;
    int tmp2 = n.index;
    n.index = m.index;
    m.index = tmp2;
    Node tmp3 = index[n.index];
    index[n.index] = index[m.index];
    index[m.index] = tmp3;
  }

  private class Node {

    Node left = null;
    Node right = null;
    Node parent = null;
    int index;
    double key;

    public Node(int index, double key) {
      this.index = index;
      this.key = key;
    }

    @Override
    public String toString() {
      return key + "";
    }
  }

  public static final Random RND = new Random();

  private void swap(int i, int j) {
    double tmp = t[i];
    t[i] = t[j];
    t[j] = tmp;
    int tmp2 = oldIndex[i];
    oldIndex[i] = oldIndex[j];
    oldIndex[j] = tmp2;
  }

  private int partition(int begin, int end) {
    int index = begin + RND.nextInt(end - begin + 1);
    double pivot = t[index];
    swap(index, end);
    for (int i = index = begin; i < end; ++i) {
      if (t[i] < pivot) {
        swap(index++, i);
      }
    }
    swap(index, end);
    return (index);
  }

  private void qsort(int begin, int end) {
    if (end > begin) {
      int index = partition(begin, end);
      qsort(begin, index - 1);
      qsort(index + 1, end);
    }
  }


}
