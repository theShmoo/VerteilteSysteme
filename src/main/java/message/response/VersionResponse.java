package message.response;

import message.Response;

/**
 * Retrieves the highest available version number of a particular file on a certain server.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !version &lt;filename&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !version &lt;filename&gt; &lt;version&gt;}<br/>
 *
 * @see message.request.VersionRequest
 */
public class VersionResponse implements Response {
	private static final long serialVersionUID = 3436691185468350784L;

	private final String filename;
	private final int version;

	public VersionResponse(String filename, int version) {
		this.filename = filename;
		this.version = version;
	}

	public String getFilename() {
		return filename;
	}

	public int getVersion() {
		return version;
	}

	@Override
	public String toString() {
		return String.format("!version %s %d", filename, version);
	}
}
