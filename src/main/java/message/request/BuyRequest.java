package message.request;

import message.Request;

/**
 * Buys additional credits for the authenticated user.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !buy &lt;credits&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !credits &lt;total_credits&gt;}<br/>
 * 
 * @see message.response.BuyResponse
 */
public class BuyRequest implements Request {
	private static final long serialVersionUID = 8589241767079930421L;

	private final long credits;

	public BuyRequest(long credits) {
		this.credits = credits;
	}

	public long getCredits() {
		return credits;
	}

	@Override
	public String toString() {
		return "!buy " + getCredits();
	}
}
