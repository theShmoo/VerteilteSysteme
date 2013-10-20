package message.response;

import message.Response;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

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
 * @see message.request.ListRequest
 */
public class ListResponse implements Response {
	private static final long serialVersionUID = -7319020129445822795L;

	private final Set<String> fileNames;

	public ListResponse(Set<String> fileNames) {
		this.fileNames = Collections.unmodifiableSet(new LinkedHashSet<String>(fileNames));
	}

	public Set<String> getFileNames() {
		return fileNames;
	}

	@Override
	public String toString() {
		if (getFileNames().isEmpty()) {
			return "No files found.";
		}

		StringBuilder sb = new StringBuilder();
		for (String fileName : getFileNames()) {
			sb.append(fileName).append("\n");
		}
		return sb.toString();
	}
}
