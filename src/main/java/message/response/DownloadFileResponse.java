package message.response;

import message.Response;
import model.DownloadTicket;

import java.nio.charset.Charset;

/**
 * Downloads the file for the given {@link DownloadTicket}.
 * <p/>
 * <b>Request (client to server)</b>:<br/>
 * {@code !download &lt;ticket&gt;}<br/>
 * <b>Response (server to client):</b><br/>
 * {@code !data &lt;content&gt;}<br/>
 *
 * @see message.request.DownloadFileRequest
 */
public class DownloadFileResponse implements Response {
	private static final long serialVersionUID = 5457101636243702226L;
	private static final Charset CHARSET = Charset.forName("ISO-8859-1");

	private final DownloadTicket ticket;
	private final byte[] content;

	public DownloadFileResponse(DownloadTicket ticket, byte[] content) {
		this.ticket = ticket;
		this.content = content;
	}

	public DownloadTicket getTicket() {
		return ticket;
	}

	public byte[] getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "!data " + new String(getContent(), CHARSET);
	}
}
