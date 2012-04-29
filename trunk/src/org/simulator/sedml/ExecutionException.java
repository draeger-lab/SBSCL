package org.simulator.sedml;

/**
 * Runtime exception indicating that SEDML execution has failed.
 * @author radams
 *
 */
public class ExecutionException extends RuntimeException {

	public ExecutionException(String message) {
		super(message);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

}
