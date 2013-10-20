package proxy;

import message.Response;
import message.request.BuyRequest;
import message.request.DownloadTicketRequest;
import message.request.LoginRequest;
import message.request.UploadRequest;
import message.response.*;

import java.io.IOException;

/**
 * This interface defines the functionality for the proxy.
 */
public interface IProxy {
	/**
	 * Authenticates the client with the provided username and password.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !login &lt;username&gt; &lt;password&gt;}<br/>
	 * <b>Response:</b><br/>
	 * {@code !login success}<br/>
	 * or<br/>
	 * {@code !login wrong_credentials}
	 *
	 * @param request the login request
	 * @return status whether the authentication was successful or not
	 * @throws IOException if an I/O error occurs
	 */
	LoginResponse login(LoginRequest request) throws IOException;

	/**
	 * Retrieves the current amount of credits of the authenticated user.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !credits}<br/>
	 * <b>Response:</b><br/>
	 * {@code !credits &lt;total_credits&gt;}<br/>
	 *
	 * @return a {@link message.response.CreditsResponse CreditsResponse} containing the amount of credits<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	Response credits() throws IOException;

	/**
	 * Buys additional credits for the authenticated user.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !buy &lt;amount&gt;}<br/>
	 * <b>Response:</b><br/>
	 * {@code !credits &lt;total_credits&gt;}<br/>
	 *
	 * @param credits the amount of credits to buy
	 * @return a {@link message.response.BuyResponse BuyResponse} containing the current amount of credits<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	Response buy(BuyRequest credits) throws IOException;

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
	 * @return a {@link message.response.ListResponse ListResponse} containing file names (can be empty)<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 */
	Response list() throws IOException;

	/**
	 * Downloads the file with the given name (if possible).
	 * <p/>
	 * <b>Request (client to proxy)</b>:<br/>
	 * {@code !download &lt;filename&gt;}<br/>
	 * <b>Response (proxy to client):</b><br/>
	 * {@code !download &lt;ticket&gt;}<br/>
	 * <b>Request (client to server)</b>:<br/>
	 * {@code !download &lt;ticket&gt;}<br/>
	 * <b>Response (server to client):</b><br/>
	 * {@code !data &lt;content&gt;}<br/>
	 *
	 * @param request the download ticket request
	 * @return a {@link message.response.DownloadTicketResponse DownloadTicketResponse} containing the ticket<br/>
	 * OR<br/>
	 * a {@link MessageResponse} if an error occurred
	 * @throws IOException if an I/O error occurs
	 * @see model.DownloadTicket
	 */
	Response download(DownloadTicketRequest request) throws IOException;

	/**
	 * Uploads the file with the given name.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !upload &lt;filename&gt; &lt;content&gt;}<br/>
	 * <b>Response:</b><br/>
	 * {@code !upload &lt;message&gt;}<br/>
	 *
	 * @param request the file upload request
	 * @return message stating whether the upload was successful
	 * @throws IOException if an I/O error occurs
	 */
	MessageResponse upload(UploadRequest request) throws IOException;

	/**
	 * Performs a logout if necessary and closes open connections between client and proxy.
	 * <p/>
	 * <b>Request</b>:<br/>
	 * {@code !logout}<br/>
	 * <b>Response:</b><br/>
	 * {@code !logout &lt;message&gt;}<br/>
	 *
	 * @return message stating whether the logout was successful
	 * @throws IOException if an I/O error occurs
	 */
	MessageResponse logout() throws IOException;
}
