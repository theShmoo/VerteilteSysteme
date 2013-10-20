package message.request;

import message.Request;

/**
 * Retrieves the size of a particular file on a certain server.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !info &lt;filename&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !info &lt;filename&gt; &lt;file_size&gt;}<br/>
 *
 * @see message.response.InfoResponse
 */
public class InfoRequest implements Request {
	private static final long serialVersionUID = -6085488358609044428L;

	private final String filename;

	public InfoRequest(String filename) {
		this.filename = filename;
	}

	public String getFilename() {
		return filename;
	}

	@Override
	public String toString() {
		return "!info " + getFilename();
	}
}
