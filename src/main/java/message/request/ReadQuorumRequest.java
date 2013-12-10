/**
 * 
 */
package message.request;

import message.Request;

/**
 * 
 * @author Astrid
 */
public class ReadQuorumRequest  implements Request {
	private static final long serialVersionUID = 1L;

	@Override
	public String toString() {
		return "!readQuorum";
	}
}