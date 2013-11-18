/**
 * 
 */
package util;

/**
 * 
 * @author David
 */
public class UnexpectedCloseException extends Exception {

	private static final long serialVersionUID = 7145341231603288786L;

	/**
	 * 
	 */
	public UnexpectedCloseException() {
		super();
	}

	/**
	 * Exception constructor with msg
	 * 
	 * @param message
	 *            the Exception message
	 */
	public UnexpectedCloseException(String message) {
		super(message);
	}

}
