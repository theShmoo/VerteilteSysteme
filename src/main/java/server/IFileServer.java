package server;

import message.Response;
import message.request.DownloadFileRequest;
import message.request.InfoRequest;
import message.request.UploadRequest;
import message.request.VersionRequest;
import message.response.*;

import java.io.IOException;

/**
 * This interface defines the functionality for the file server.
 */
public interface IFileServer {
	/**
	 * Gets the complete list of files available to download on this server.
	 * E.g.:
	 * <pre>
	 * &gt; !list
	 * file1.txt
	 * file2.txt
	 * </pre>
	 *
	 * @return a {@link message.response.ListResponse ListResponse} containing file names (can be empty)<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	Response list() throws IOException;

	/**
	 * Downloads the file with the given name (if possible).
	 * E.g.:
	 * <pre>
	 * &gt; !download &lt;ticket&gt;
	 * !data &lt;content&gt;
	 * </pre>
	 *
	 * @param request the download request
	 * @return a {@link message.response.DownloadFileResponse DownloadFileResponse} containing the content<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 * @see model.DownloadTicket
	 */
	Response download(DownloadFileRequest request) throws IOException;

	/**
	 * Returns the size of the file.
	 * E.g.:
	 * <pre>
	 * &gt; !info file.txt
	 * !info 73
	 * </pre>
	 *
	 * @param request the name of the file to get info about
	 * @return a {@link message.response.InfoResponse InfoResponse} containing file information<br/>
	 * OR</br>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	Response info(InfoRequest request) throws IOException;

	/**
	 * Return the last version of the file available on this server.
	 * E.g.:
	 * <pre>
	 * &gt; !version file.txt
	 * !version 4
	 * </pre>
	 *
	 * @param request the version request
	 * @return a {@link message.response.VersionResponse VersionResponse} containing the latest file version<br/>
	 * OR</br>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	Response version(VersionRequest request) throws IOException;

	/**
	 * Saves the file to the shared directory.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !upload &lt;filename&gt; &lt;content&gt;}<br/>
	 * <b>Response:</b><br/>
	 * {@code !upload &lt;message&gt;}<br/>
	 *
	 * @param request the request containing the file to upload
	 * @return message stating whether the upload was successful
	 * @throws IOException if an I/O error occurs
	 */
	MessageResponse upload(UploadRequest request) throws IOException;
}
