/**
 * 
 */
package util;

/**
 * 
 * @author David
 */
public class MyConnectionError extends RuntimeException {

	private static final long serialVersionUID = -585803859777982129L;
	

	/**
	 * Default Constructor
	 */
	public MyConnectionError() {
		super();
	}
	
	/**
	 * Default Constructor with message
	 * 
	 * @param message the message
	 */
	public MyConnectionError(String message) {
		super(message);
	}

}
