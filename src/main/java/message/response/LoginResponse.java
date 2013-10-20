package message.response;

import message.Response;

/**
 * Authenticates the client with the provided username and password.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !login &lt;username&gt; &lt;password&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !login success}<br/>
 * or<br/>
 * {@code !login wrong_credentials}
 *
 * @see message.request.LoginRequest
 */
public class LoginResponse implements Response {
	private static final long serialVersionUID = 3134831924072300109L;

	public enum Type {
		SUCCESS("Successfully logged in."),
		WRONG_CREDENTIALS("Wrong username or password.");

		String message;

		Type(String message) {
			this.message = message;
		}
	}

	private final Type type;

	public LoginResponse(Type type) {
		this.type = type;
	}

	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		return "!login " + getType().name().toLowerCase();
	}
}
