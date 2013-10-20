package message.response;

import message.Response;
import model.DownloadTicket;

/**
 * Requests a {@link model.DownloadTicket} in order to download a file from a file server.
 * <p/>
 * <b>Request (client to proxy)</b>:<br/>
 * {@code !download &lt;filename&gt;}<br/>
 * <b>Response (proxy to client):</b><br/>
 * {@code !download &lt;ticket&gt;}<br/>
 *
 * @see message.request.DownloadTicketRequest
 */
public class DownloadTicketResponse implements Response {
	private static final long serialVersionUID = 8526751933940590003L;

	private final DownloadTicket ticket;

	public DownloadTicketResponse(DownloadTicket ticket) {
		this.ticket = ticket;
	}

	public DownloadTicket getTicket() {
		return ticket;
	}

	@Override
	public String toString() {
		return "!download " + ticket;
	}
}
