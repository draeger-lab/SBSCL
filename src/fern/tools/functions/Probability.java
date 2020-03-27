package fern.tools.functions;

/**
 * 
 * Function delegate for conditional probabilities.
 * 
 * @author Florian Erhard
 *
 */
public interface Probability {
	
	/**
	 * Gets the probability depending on two integers.
	 * 
	 * @param l1	interger 1
	 * @param l2	integer 2
	 * @return		probability
	 */
	public double getProb(int l1, int l2); 
	
	/**
	 * Implementation that yields a constant (and hence independent)
	 * probability.
	 * 
	 * @author Florian Erhard
	 *
	 */
	public static class Constant implements Probability {
		private double p;
		public Constant(double p) {
			this.p = p;
		}
		public double getProb(int l1, int l2) {
			return p;
		}
	}
	
	/**
	 * 
	 * Implementation for the reaction probability for the autocatalytic network creation.
	 * The probability is calculated by the formula 
	 * <p>
	 * <code>factor / max(l1,l2)</code> 
	 * <p>
	 * if 
	 * <code>l1+l2 > oneToLength<code>, else 1.
	 * 
	 * @author Florian Erhard
	 *
	 */
	public static class ReactionProbability implements Probability {

		private double factor;
		private int oneToLength;
		
		public ReactionProbability(double factor, int oneToLength){
			this.factor = factor; this.oneToLength = oneToLength;
		}
		
		public double getProb(int l1, int l2) {
			return l1+l2<=oneToLength ? 1 : factor / Math.max(l1,l2);
		}
		
	}
}
