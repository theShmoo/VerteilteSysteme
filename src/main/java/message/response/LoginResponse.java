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

	private final byte[] proxyChallenge;
	
	public enum Type {
		SUCCESS("Successfully logged in."),
		WRONG_CREDENTIALS("Wrong username or password."),
		OK("");
		String message;

		Type(String message) {
			this.message = message;
		}
	}

	private final Type type;

	/**
	 * @param proxyChallenge 
	 */
	public LoginResponse(byte[] proxyChallenge) {
		this.proxyChallenge = proxyChallenge;
		this.type = Type.OK;
	}
	
	/**
	 * @param type
	 */
	public LoginResponse(Type type){
		this.type = type;
		this.proxyChallenge = new byte[0];
	}

	/**
	 * @return the Proxy Challenge
	 */
	public byte[] getProxyChallenge() {
		return proxyChallenge;
	}
	
	/**
	 * @return the type
	 */
	public Type getType() {
		return type;
	}

	@Override
	public String toString() {
		if(type == Type.OK){
			return "!ok " + new String(proxyChallenge);
		}
		return "!login " + type.message;
	}
}
