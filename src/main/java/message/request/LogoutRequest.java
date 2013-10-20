package message.request;

import message.Request;

/**
 * Performs a logout if necessary and closes open connections between client and proxy.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !logout}<br/>
 * <b>Response:</b><br/>
 * {@code !logout &lt;message&gt;}<br/>
 *
 * @see message.response.MessageResponse
 */
public class LogoutRequest implements Request {
	private static final long serialVersionUID = -1496068214330800650L;

	@Override
	public String toString() {
		return "!logout";
	}
}
