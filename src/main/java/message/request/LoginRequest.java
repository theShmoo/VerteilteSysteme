package message.request;

import message.Request;

/**
 * Message 1 from the client for the authentication.
 *
 * @see message.response.LoginResponse
 */
public class LoginRequest implements Request {
	private static final long serialVersionUID = -1596776158259072949L;

	private final byte[] message;

	public LoginRequest(byte[] message) {
		this.message = message;
	}

	public byte[] getMessage() {
		return message;
	}

	@Override
	public String toString() {
		return String.format("%s", getMessage());
	}
}
