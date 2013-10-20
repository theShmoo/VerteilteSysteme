package message.request;

import message.Request;

/**
 * Retrieves the current amount of credits of the authenticated user.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !credits}<br/>
 * <b>Response:</b><br/>
 * {@code !credits &lt;total_credits&gt;}<br/>
 *
 * @see message.response.CreditsResponse
 */
public class CreditsRequest implements Request {
	private static final long serialVersionUID = -8173360074261745303L;

	@Override
	public String toString() {
		return "!credits";
	}
}
