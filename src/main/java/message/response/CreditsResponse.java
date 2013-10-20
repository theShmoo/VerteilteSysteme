package message.response;

import message.Response;

/**
 * Retrieves the current amount of credits of the authenticated user.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !credits}<br/>
 * <b>Response:</b><br/>
 * {@code !credits &lt;total_credits&gt;}<br/>
 *
 * @see message.request.CreditsRequest
 */
public class CreditsResponse implements Response {
	private static final long serialVersionUID = -4994758755942921733L;

	private final long credits;

	public CreditsResponse(long credits) {
		this.credits = credits;
	}

	public long getCredits() {
		return credits;
	}

	@Override
	public String toString() {
		return "!credits " + getCredits();
	}
}
