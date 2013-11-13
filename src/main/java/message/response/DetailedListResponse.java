/**
 * 
 */
package message.response;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import message.Response;
import model.FileInfo;

/**
 * Lists files .
 * <p/>
 * <b>Request</b>:<br/>
 * {@code !details}<br/>
 * <b>Response:</b><br/>
 * {@code No files found.}<br/>
 * or<br/>
 * {@code &lt;filename1&gt;}<br/>
 * {@code &lt;filename2&gt;}<br/>
 * {@code ...}<br/>
 * 
 * @see message.request.DetailedListRequest
 * 
 * @author David
 */
public class DetailedListResponse implements Response {

	private static final long serialVersionUID = -5625308517844254780L;

	private final Set<FileInfo> fileinfo;

	/**
	 * @param fileinfo
	 *            the infos of all files of the server
	 */
	public DetailedListResponse(Set<FileInfo> fileinfo) {
		this.fileinfo =  Collections.unmodifiableSet(new LinkedHashSet<FileInfo>(fileinfo));
	}

	/**
	 * @return the fileinfo
	 */
	public Set<FileInfo> getFileInfo() {
		return fileinfo;
	}

	@Override
	public String toString() {
		if (getFileInfo().isEmpty()) {
			return "No files found.";
		}

		StringBuilder sb = new StringBuilder();
		for (FileInfo f : getFileInfo()) {
			sb.append(f.toString()).append("\n");
		}
		return sb.toString();
	}

}
