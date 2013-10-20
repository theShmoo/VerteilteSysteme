package message.request;

import message.Request;

import java.nio.charset.Charset;

/**
 * Uploads the file with the given name.
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !upload &lt;filename&gt; &lt;content&gt;}<br/>
 * <b>Response:</b><br/>
 * {@code !upload &lt;message&gt;}<br/>
 */
public class UploadRequest implements Request {
	private static final long serialVersionUID = 6951706197428053894L;
	private static final Charset CHARSET = Charset.forName("ISO-8859-1");

	private final String filename;
	private final int version;
	private final byte[] content;

	public UploadRequest(String filename, int version, byte[] content) {
		this.filename = filename;
		this.version = version;
		this.content = content;
	}

	public String getFilename() {
		return filename;
	}

	public int getVersion() {
		return version;
	}

	public byte[] getContent() {
		return content;
	}

	@Override
	public String toString() {
		return String.format("!upload %s %d %s", getFilename(), getVersion(), new String(getContent(), CHARSET));
	}
}
