package fern.analysis;

/**
 * Implementing class can be used as search structure for searches in {@link AnalysisBase}.
 * @author Florian Erhard
 *
 */
public interface IntSearchStructure {

	void add(int i);

	boolean isEmpty();

	int get();

}
