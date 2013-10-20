package message.response;

import message.Response;

/**
 * Retrieves the size of a particular file on a certain server.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !info &lt;filename&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !info &lt;filename&gt; &lt;file_size&gt;}<br/>
 *
 * @see message.request.InfoRequest
 */
public class InfoResponse implements Response {
	private static final long serialVersionUID = 2775359461473083371L;

	private final String filename;
	private final long size;

	public InfoResponse(String filename, long size) {
		this.filename = filename;
		this.size = size;
	}

	public String getFilename() {
		return filename;
	}

	public long getSize() {
		return size;
	}

	@Override
	public String toString() {
		return String.format("!info %s %d", filename, size);
	}
}
