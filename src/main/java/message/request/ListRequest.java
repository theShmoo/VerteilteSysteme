package message.request;

import message.Request;

/**
 * Lists all files available on all file servers.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !list}<br/>
 * <b>Response:</b><br/>
 * {@code No files found.}<br/>
 * or<br/>
 * {@code &lt;filename1&gt;}<br/>
 * {@code &lt;filename2&gt;}<br/>
 * {@code ...}<br/>
 *
 * @see message.response.ListResponse
 */
public class ListRequest implements Request {
	private static final long serialVersionUID = -3772629665574053670L;

	@Override
	public String toString() {
		return "!list";
	}
}
