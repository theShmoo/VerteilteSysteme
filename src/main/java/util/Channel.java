package util;

/**
 * 
 * @author David
 */
public interface Channel {

	/**
	 * Waits for a stream and returns the received Object
	 * 
	 * @return the received Object
	 * 
	 * @throws UnexpectedCloseException
	 *             if the stream is closed from the partners side this throws an Exception
	 */
	Object receive() throws UnexpectedCloseException;

	/**
	 * Send an object via the stream
	 * 
	 * @param response
	 *            the object to send
	 */
	void send(Object response);

	/**
	 * Closes all resources that includes the input/output Stream and the socket
	 */
	void close();
}
