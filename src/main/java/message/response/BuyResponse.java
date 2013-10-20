package message.response;

import message.Response;

/**
 * Buys additional credits for the authenticated user.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !buy &lt;credits&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !credits &lt;total_credits&gt;}<br/>
 *
 * @see message.request.BuyRequest
 */
public class BuyResponse implements Response {
	private static final long serialVersionUID = -7058325034457705550L;

	private final long credits;

	public BuyResponse(long credits) {
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
